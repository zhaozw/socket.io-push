module.exports = RedisIncrBuffer;

var debug = require('debug')('Stats');

function RedisIncrBuffer(redis) {
    if (!(this instanceof RedisIncrBuffer)) return new RedisIncrBuffer(redis);
    this.redis = redis;
    this.map = {};
    this.timestamp = Date.now();
    this.commitThreshold = 20 * 1000;
}

RedisIncrBuffer.prototype.incrby = function (key, by) {
    var currentIncr = this.map[key] || 0;
    this.map[key] = currentIncr + by;
    this.checkCommit();
};

RedisIncrBuffer.prototype.checkCommit = function () {
    var timestamp = Date.now();
    if ((timestamp - this.timestamp) > this.commitThreshold) {
        debug("stats threshold committing");
        for (var key in this.map) {
            this.redis.incrby(key, this.map[key]);
            var index = key.indexOf("#totalCount");
            if(index != -1){
                var str = key.substring(6, index);
                debug("checkCommit:  " + str);  //stats#request#/addDot#totalCount#1457344800000
                this.redis.hset("queryDataKeys", str, Date.now())
            }
        }
        this.map = {};
        this.timestamp = timestamp;
    }
};