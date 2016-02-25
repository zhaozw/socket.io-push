module.exports = NotificationService;

var debug = require('debug')('NotificationService');
var util = require('../util/util.js');
var apn = require('apn');


function NotificationService(apnConfigs, redis, ttlService) {
    if (!(this instanceof NotificationService)) return new NotificationService(apnConfigs, redis, ttlService);
    this.redis = redis;
    this.ttlService = ttlService;
    this.apnConnections = {};
    var outerThis = this;
    var fs = require('fs');
    var ca = [fs.readFileSync("cert/entrust_2048_ca.cer")];

    apnConfigs.forEach(function (apnConfig, index) {
        apnConfig.maxConnections = 10;
        apnConfig.ca = ca;
        apnConfig.errorCallback = function (errorCode, notification, device) {
            var id = device.token.toString('hex');
            debug("apn errorCallback %d %s", errorCode, id);
            if (errorCode == 8) {
                redis.hdel("apnTokens", id);
            }
        }
        var connection = apn.Connection(apnConfig);
        connection.index = index;
        outerThis.apnConnections[apnConfig.bundleId] = connection;
        debug("apnConnections init for %s", apnConfig.bundleId);
    });

    this.bundleIds = Object.keys(this.apnConnections);
    this.defaultBundleId = this.bundleIds[0];
    debug("defaultBundleId %s", this.defaultBundleId);

}

NotificationService.prototype.setApnToken = function (pushId, apnToken, bundleId) {
    if (pushId && apnToken) {
        if (!bundleId) {
            bundleId = this.defaultBundleId;
        }
        var apnData = JSON.stringify({bundleId: bundleId, apnToken: apnToken});
        var outerThis = this;
        var timestamp = Date.now();
        this.redis.get("apnTokenToPushId#" + apnToken, function (err, oldApnData) {
            if (oldApnData !== apnData) {
                outerThis.redis.set("pushIdToApnData#" + pushId, apnData);
                outerThis.redis.set("apnTokenToPushId#" + apnToken, pushId);
                if (oldApnData && oldApnData.pushId){
                    outerThis.redis.del("pushIdToApnData#" + oldApnData.pushId);
                }
                debug("set pushIdToApnData %s %s", pushId, apnData);
            }

            outerThis.redis.hset("apnTokens#" + bundleId, apnToken, timestamp);
            outerThis.redis.expire("pushIdToApnData#" + pushId, 3600 * 24 * 7);
            outerThis.redis.expire("apnTokenToPushId#" + pushId, 3600 * 24 * 7);

        });
    }
};

NotificationService.prototype.sendByPushIds = function (pushIds, notification, io) {
    var ttlService = this.ttlService;
    var apnConnections = this.apnConnections;

    util.batch(this.redis, "get", "pushIdToApnData#", pushIds, function (replies) {
        var apnTokens = [];
        for (var i = 0; i < pushIds.length; i++) {
            var pushId = pushIds[i];
            if (replies[i]) {
                var apnData = JSON.parse(replies[i]);
                var bundleId = apnData.bundleId;
                if (apnConnections[bundleId]) {
                    var bundledToken = apnTokens[apnConnections[bundleId].index];
                    if (!bundledToken) {
                        bundledToken = [];
                        apnTokens[apnConnections[bundleId].index] = bundledToken;
                    }
                    bundledToken.push(apnData.apnToken);
                    debug("send to notification to ios %s %s", pushId, apnData.apnToken);
                }
            } else {
                debug("send to notification to android %s", pushId);
                io.to(pushId).emit('noti', notification);
                ttlService.addPacket(pushId, 'noti', notification);
            }
        }
        if (apnTokens.length > 0) {
            var note = toApnNotification(notification, notification.timeToLive);
            for (var bundleId in apnConnections) {
                var apnConnection = apnConnections[bundleId];
                debug("send notification by bundle id %s %s", bundleId, apnTokens[apnConnection.index]);
                if (apnTokens[apnConnection.index]) {
                    apnConnection.pushNotification(note, apnTokens[apnConnection.index]);
                }
            }
        }
    });
};

NotificationService.prototype.sendAll = function (notification, io) {
    io.to("noti").emit('noti', notification);
    var bundleIds = this.bundleIds;
    var apnConnections = this.apnConnections;
    util.batch(this.redis, "hkeys", "apnTokens#", bundleIds, function (replies) {
        var note = toApnNotification(notification);
        for (var i = 0; i < bundleIds.length; i++) {
            var apnConnection = apnConnections[bundleIds[i]];
            if (replies[i].length > 0) {
                debug("bundleId %s replies %d", bundleIds[i], replies[i].length);
                apnConnection.pushNotification(note, replies[i]);
            }
        }
    });

};

function toApnNotification(notification, timeToLive) {
    var note = new apn.Notification();
    note.badge = notification.apn.badge;
    if (notification.apn.sound) {
        note.sound = notification.apn.sound;
    } else {
        note.sound = "default";
    }
    note.alert = notification.apn.alert;
    var secondsToLive;
    if (timeToLive > 0) {
        secondsToLive = timeToLive / 1000;
    } else {
        secondsToLive = 600;
    }
    note.expiry = Math.floor(Date.now() / 1000) + secondsToLive;
    if (notification.apn.payload) {
        note.payload = notification.apn.payload;
    } else {
        note.payload = {};
    }
    return note;
}