module.exports = ApnService;

var debug = require('debug')('ApnService');
var util = require('../util/util.js');
var apn = require('apn');
var apnTokenTTL = 3600 * 24 * 7;


function ApnService(apnConfigs, redis) {
    if (!(this instanceof ApnService)) return new ApnService(apnConfigs, redis);
    this.redis = redis;
    this.apnConnections = {};
    var outerThis = this;
    var fs = require('fs');
    var ca = [fs.readFileSync(__dirname + "/../../cert/entrust_2048_ca.cer")];

    apnConfigs.forEach(function (apnConfig, index) {
        apnConfig.maxConnections = 10;
        apnConfig.ca = ca;
        apnConfig.errorCallback = function (errorCode, notification, device) {
            if (device && device.token) {
                var id = device.token.toString('hex');
                debug("apn errorCallback %d %s", errorCode, id);
                if (errorCode == 8) {
                    redis.hdel("apnTokens#" + apnConfig.bundleId, id);
                }
            } else {
                debug("apn errorCallback no token %s", errorCode);
            }
        }
        var connection = apn.Connection(apnConfig);
        connection.index = index;
        outerThis.apnConnections[apnConfig.bundleId] = connection;

        apnConfig.batchFeedback = true;
        apnConfig.interval = 300;

        var feedback = new apn.Feedback(apnConfig);
        feedback.on("feedback", function (devices) {
            devices.forEach(function (item) {
                debug("apn feedback %s %j", apnConfig.bundleId, item);
            });
        });
        debug("apnConnections init for %s", apnConfig.bundleId);
    });

    this.bundleIds = Object.keys(this.apnConnections);
    this.defaultBundleId = this.bundleIds[0];
    debug("defaultBundleId %s", this.defaultBundleId);

}

ApnService.prototype.sendOne = function (apnData, notification, timeToLive) {
    var bundleId = apnData.bundleId;
    var apnConnection = this.apnConnections[bundleId];
    if (apnConnection) {
        var note = toApnNotification(notification, timeToLive);
        apnConnection.pushNotification(note, apnData.apnToken);
        debug("send to notification to ios %s %s", apnData.bundleId, apnData.apnToken);
    }
};

ApnService.prototype.sendAll = function (notification, timeToLive) {
    var apnConnections = this.apnConnections;
    var timestamp = Date.now();
    var redis = this.redis;
    var note = toApnNotification(notification, timeToLive);
    this.bundleIds.forEach(function (bundleId) {
        redis.hgetall("apnTokens#" + bundleId, function (err, replies) {
            if (replies) {
                var apnConnection = apnConnections[bundleId];
                for (var token in replies) {
                    if (timestamp - replies[token] > apnTokenTTL * 1000) {
                        debug("delete outdated apnToken %s", token);
                        redis.hdel("apnTokens#" + bundleId, token);
                    } else {
                        apnConnection.pushNotification(note, token);
                    }
                }
            }
        });
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