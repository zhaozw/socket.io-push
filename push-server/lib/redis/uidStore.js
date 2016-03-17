module.exports = UidStore;
var debug = require('debug')('UidStore');

function UidStore(redis, subClient) {
    if (!(this instanceof UidStore)) return new UidStore(redis, subClient);
    this.redis = redis;
}

UidStore.prototype.addUid = function(pushId, uid, timeToLive) {
    debug("addUid pushId %s %s", uid, pushId);
    var key = "pushIdToUid#" + pushId;
    var ourThis = this;
    this.getUidByPushId(pushId, function(oldUid){
        if(oldUid) {
            debug("remove %s from old uid %s", pushId, oldUid);
            ourThis.redis.hdel("uidToPushId#" + oldUid, pushId);
        }
        ourThis.redis.set(key, uid);
        if(timeToLive){
            ourThis.redis.expire(key, timeToLive);
        }
        ourThis.redis.hset("uidToPushId#" + uid, pushId , Date.now());
    });
};

UidStore.prototype.removePushId = function (pushId) {
    debug("removePushId pushId %s %s", uid, pushId);
    var key = "pushIdToUid#" + pushId;
    var ourThis = this;
    this.redis.get(key, function (err, oldUid) {
        if (oldUid) {
            debug("remove %s from old uid %s", pushId, oldUid);
            ourThis.redis.hdel("uidToPushId#" + uid, pushId);
            ourThis.redis.del(key);
        }
    });
};

UidStore.prototype.getUidByPushId = function (pushId, callback) {
    this.redis.get("pushIdToUid#" + pushId, function (err, uid) {
        // reply is null when the key is missing
        debug("getUidByPushId %s %s", pushId, uid);
        callback(uid);
    });
};

UidStore.prototype.getPushIdByUid = function (uid, callback) {
    this.redis.hkeys("uidToPushId#" + uid, function (err, replies) {
        callback(replies);
    });
};
