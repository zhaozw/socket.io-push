module.exports = UidStore;
var debug = require('debug')('UidStore');
var util = require('./util.js');
var apn = require('apn');

var socketIdToUid = {};

function UidStore(redis,subClient){
    if (!(this instanceof UidStore)) return new UidStore(redis,subClient);
    this.redis = redis;
}


UidStore.prototype.addUid = function(pushId, uid, timeToLive) {
    debug("addUid pushId %s %s",uid,pushId);
    var key = "pushIdToUid#" + pushId;
    this.redis.set(key, uid);
    if(timeToLive){
        this.redis.expire(key, timeToLive);
    }
    this.redis.hset("uidToPushId#" + uid, pushId , Date.now());
};

UidStore.prototype.batchGetPushId = function(uids, callback) {
    util.batchGet(this.redis, uids, callback);
};

UidStore.prototype.getUidByPushId = function(pushId, callback) {
    this.redis.get("pushIdToUid#" + pushId,  function(err, uid) {
        // reply is null when the key is missing
        debug("getUidByPushId %s %s", pushId, uid);
        callback(uid);
    });
};