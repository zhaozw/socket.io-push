module.exports = UidStore;
var debug = require('debug')('UidStore');

var apn = require('apn');

var socketIdToUid = {};

function UidStore(redis,subClient){
    if (!(this instanceof UidStore)) return new UidStore(redis,subClient);
    this.redis = redis;
}


UidStore.prototype.addUid = function(pushId, uid, timeToLive) {
    debug("publishDisconnect pushId %s",pushId);
    var key = "pushIdToUid#" + pushId;
    this.redis.set(key, uid);
    if(timeToLive){
        this.redis.expire(key, timeToLive);
    }
    this.redis.hset("uidToPushId#" + uid, pushId , Date.now());
};

UidStore.prototype.getPushId = function(uids, callback) {
    debug("publishDisconnect pushId %s",pushId);
    var key = "pushIdToUid#" + pushId;
    this.redis.set(key, uid);
    if(timeToLive){
        this.redis.expire(key, timeToLive);
    }
    this.redis.hset("uidToPushId#" + uid, pushId , Date.now());
};