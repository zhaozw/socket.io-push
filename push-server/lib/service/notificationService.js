module.exports = NotificationService;

var debug = require('debug')('NotificationService');
var randomstring = require("randomstring");
var util = require('../util/util.js');
var apn = require('apn');

var apnConnection;


function NotificationService(apnConfig, redis) {
    if (!(this instanceof NotificationService)) return new NotificationService(apnConfig,redis);
    this.redis = redis;
    apnConfig.maxConnections = 10;
    apnConfig.errorCallback = function (errorCode, notification, device) {
        var id = device.token.toString('hex');
        debug("apn errorCallback %d %s", errorCode, id);
        if (errorCode == 8) {
            redis.hdel("apnTokens", id);
        }
    }
    apnConnection = new apn.Connection(apnConfig);
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
    pushIds.forEach(function (pushId) {
        io.to(pushId).emit('noti', notification);
    });

    util.batch(this.redis, "get", "pushIdToApnToken#", pushIds, function (replies) {
        debug("util.batchGet %s", replies);
        replies.clean();
        if (replies.length > 0) {
            var note = toApnNotification(notification);
            apnConnection.pushNotification(note, replies);
        }
    });
};

Array.prototype.clean = function () {
    for (var i = 0; i < this.length; i++) {
        if (!this[i]) {
            this.splice(i, 1);
            i--;
        }
    }
    return this;
};

NotificationService.prototype.sendAll = function (notification, io) {
    io.to("noti").emit('noti', notification);
    this.redis.hkeys("apnTokens", function (err, replies) {
        debug(replies.length + " replies:");
        var note = toApnNotification(notification);
        if (replies.length > 0) {
            apnConnection.pushNotification(note, replies);
        }
    });
};

function toApnNotification(notification) {
    var note = new apn.Notification();
    note.badge = notification.apn.badge;
    if (notification.apn.sound) {
        note.sound = notification.apn.sound;
    } else {
        note.sound = "default";
    }
    note.alert = notification.apn.alert;
    note.expiry = Math.floor(Date.now() / 1000) + 600;
    if (notification.apn.payload) {
        note.payload = notification.apn.payload;
    } else {
        note.payload = {};
    }
    return note;
}