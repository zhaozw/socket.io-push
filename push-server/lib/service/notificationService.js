module.exports = NotificationService;

var debug = require('debug')('NotificationService');
var util = require('../util/util.js');
var apn = require('apn');


function NotificationService(apnConfig, redis, ttlService) {
    if (!(this instanceof NotificationService)) return new NotificationService(apnConfig, redis, ttlService);
    this.redis = redis;
    this.ttlService = ttlService;
    apnConfig.maxConnections = 10;
    apnConfig.cert = "cert/cert.pem";
    apnConfig.key = "cert/key.pem";
    apnConfig.ca = "cert/entrust_2048_ca.cer";
    apnConfig.errorCallback = function (errorCode, notification, device) {
        var id = device.token.toString('hex');
        debug("apn errorCallback %d %s", errorCode, id);
        if (errorCode == 8) {
            redis.hdel("apnTokens", id);
        }
    }
    this.apnConnection = new apn.Connection(apnConfig);
}

NotificationService.prototype.setApnToken = function (pushId, apnToken) {
    if (pushId && apnToken) {
        var outerThis = this;
        this.redis.get("apnTokenToPushId#" + apnToken, function (err, oldPushId) {
            if (oldPushId) {
                debug("removing duplicate pushIdToApnToken %s", oldPushId);
                outerThis.redis.del("pushIdToApnToken#" + oldPushId);
            }
            outerThis.redis.set("pushIdToApnToken#" + pushId, apnToken);
            outerThis.redis.expire("pushIdToApnToken#" + pushId, 3600 * 24 * 7);
            outerThis.redis.set("apnTokenToPushId#" + apnToken, pushId);
            outerThis.redis.expire("apnTokenToPushId#" + pushId, 3600 * 24 * 7);
            outerThis.redis.hset("apnTokens", apnToken, 1);
            debug("set pushIdToApnToken %s %s", pushId, apnToken);
        });
    }
};

NotificationService.prototype.sendByPushIds = function (pushIds, notification, io) {
    var ttlService = this.ttlService;
    var apnConnection = this.apnConnection;

    util.batch(this.redis, "get", "pushIdToApnToken#", pushIds, function (replies) {
        var apnTokens = [];
        for (var i = 0; i < pushIds.length; i++) {
            var pushId = pushIds[i];
            var token = replies[i];
            if (token) {
                debug("send to notification to ios %s %s", pushId, token);
                apnTokens.push(token);
            } else {
                debug("send to notification to android %s %s", pushId, token);
                io.to(pushId).emit('noti', notification);
                ttlService.addPacket(pushId, 'noti', notification);
            }
        }
        if (apnTokens.length > 0) {
            debug("send to apn %s", apnTokens);
            var note = toApnNotification(notification, notification.timeToLive);
            apnConnection.pushNotification(note, apnTokens);
        }
    });
};

NotificationService.prototype.sendAll = function (notification, io) {
    io.to("noti").emit('noti', notification);
    var apnConnection = this.apnConnection;
    this.redis.hkeys("apnTokens", function (err, replies) {
        debug(replies.length + " replies:");
        var note = toApnNotification(notification);
        if (replies.length > 0) {
            apnConnection.pushNotification(note, replies);
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
    debug(" note length %d", note.length);
    return note;
}