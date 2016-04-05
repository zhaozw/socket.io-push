module.exports = NotificationService;

var Logger = require('../log/index.js')('NotificationService');
var util = require('../util/util.js');
var apn = require('apn');
var apnTokenTTL = 3600 * 24 * 7;


function NotificationService(apnConfigs, redis, ttlService) {
    if (!(this instanceof NotificationService)) return new NotificationService(apnConfigs, redis, ttlService);
    this.redis = redis;
    this.ttlService = ttlService;
    var outerThis = this;
    apnConfigs.forEach(function (apnConfig) {
        if (!outerThis.defaultBundleId) {
            outerThis.defaultBundleId = apnConfig.bundleId;
        }
    });

    Logger.info("defaultBundleId %s", this.defaultBundleId);
}

NotificationService.prototype.setApnToken = function (pushId, apnToken, bundleId) {
    if (pushId && apnToken) {
        if (!bundleId) {
            bundleId = this.defaultBundleId;
        }
        try {
            var buffer = new Buffer(apnToken, 'hex');
        } catch (err) {
            Logger.info("invalid apnToken format %s", apnToken);
            return;
        }
        var apnData = JSON.stringify({bundleId: bundleId, apnToken: apnToken});
        var outerThis = this;
        this.redis.get("apnTokenToPushId#" + apnToken, function (err, oldPushId) {
            Logger.info("oldPushId %s", oldPushId);
            if (oldPushId && oldPushId != pushId) {
                outerThis.redis.del("pushIdToApnData#" + oldPushId);
                Logger.info("remove old pushId to apnToken %s %s", oldPushId, apnData);
            }
            outerThis.redis.set("apnTokenToPushId#" + apnToken, pushId);
            outerThis.redis.set("pushIdToApnData#" + pushId, apnData);
            outerThis.redis.hset("apnTokens#" + bundleId, apnToken, Date.now());
            outerThis.redis.expire("pushIdToApnData#" + pushId, apnTokenTTL);
            outerThis.redis.expire("apnTokenToPushId#" + apnToken, apnTokenTTL);
        });
    }
};

NotificationService.prototype.sendByPushIds = function (pushIds, timeToLive, notification, io) {
    var outerThis = this;
    pushIds.forEach(function (pushId) {
        outerThis.redis.get("pushIdToApnData#" + pushId, function (err, reply) {
            Logger.info("pushIdToApnData %s %s", pushId, JSON.stringify(reply));
            if (reply) {
                var apnData = JSON.parse(reply);
                outerThis.apnService.sendOne(apnData, notification, timeToLive);
            } else {
                Logger.info("send notification to android %s", pushId);
                outerThis.ttlService.addPacketAndEmit(pushId, 'noti', timeToLive, notification, io, true);
            }
        });
    });

};

NotificationService.prototype.sendAll = function (notification, timeToLive, io) {
    this.ttlService.addPacketAndEmit("noti", 'noti', timeToLive, notification, io, false);
    this.apnService.sendAll(notification, timeToLive);
};
