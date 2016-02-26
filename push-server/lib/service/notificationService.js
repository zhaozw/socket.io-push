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
    var ca = [fs.readFileSync(__dirname + "/../../cert/entrust_2048_ca.cer")];

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
        this.redis.get("apnTokenToPushId#" + apnToken, function (err, oldPushId) {
            if (oldPushId !== pushId) {
                outerThis.redis.set("pushIdToApnData#" + pushId, apnData);
                outerThis.redis.set("apnTokenToPushId#" + apnToken, pushId);
                if (oldPushId) {
                    outerThis.redis.del("pushIdToApnData#" + oldPushId);
                    debug("remove old pushId to apnToken %s %s", oldPushId, apnData);
                }
                debug("set pushIdToApnData %s %s", pushId, apnData);
            }

            outerThis.redis.hset("apnTokens#" + bundleId, apnToken, Date.now());
            outerThis.redis.expire("pushIdToApnData#" + pushId, 3600 * 24 * 7);
            outerThis.redis.expire("apnTokenToPushId#" + apnToken, 3600 * 24 * 7);

        });
    }
};

NotificationService.prototype.sendByPushIds = function (pushIds, notification, io) {
    var outerThis = this;
    pushIds.forEach(function (pushId) {
        outerThis.redis.get("pushIdToApnData#" + pushId, function (err, reply) {
            if (reply) {
                var apnData = JSON.parse(reply);
                var bundleId = apnData.bundleId;
                var apnConnection = outerThis.apnConnections[bundleId];
                if (apnConnection) {
                    var note = toApnNotification(notification, notification.timeToLive);
                    apnConnection.pushNotification(note, apnData.apnToken);
                    debug("send to notification to ios %s %s", pushId, apnData.apnToken);
                }
            } else {
                debug("send to notification to android %s", pushId);
                io.to(pushId).emit('noti', notification);
                outerThis.ttlService.addPacket(pushId, 'noti', notification);
            }

        });
    });

};

NotificationService.prototype.sendAll = function (notification, io) {
    io.to("noti").emit('noti', notification);
    var bundleIds = this.bundleIds;
    var apnConnections = this.apnConnections;
    pushIds.forEach(function (pushId) {
        outerThis.redis.get("pushIdToApnData#" + pushId, function (err, reply) {
            if (reply) {
                var apnData = JSON.parse(reply);
                var bundleId = apnData.bundleId;
                var apnConnection = outerThis.apnConnections[bundleId];
                if (apnConnection) {
                    var note = toApnNotification(notification, notification.timeToLive);
                    apnConnection.pushNotification(note, apnData.apnToken);
                    debug("send to notification to ios %s %s", pushId, apnData.apnToken);
                }
            } else {
                debug("send to notification to android %s", pushId);
                io.to(pushId).emit('noti', notification);
                outerThis.ttlService.addPacket(pushId, 'noti', notification);
            }

        });
    });
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