module.exports = ApiThreshold;
var Logger = require('../log/index.js')('ApiThreshold');

function ApiThreshold(redis) {
    this.watchedTopics = [];
    this.redis = redis;
}

ApiThreshold.prototype.checkPushDrop = function (topic, callback) {
    var call = true;
    var threshold = this.watchedTopics[topic];
    if (threshold) {
        var redis = this.redis;
        redis.lindex("apiThreshold#callTimestamp#" + topic, -1, function (err, result) {
            if (result && result > (Date.now() - 10 * 1000)) {
                Logger.log("info", "too many call dropping %s", topic);
                call = false;
            }
            doPush(redis, topic, call, threshold, callback);
        });
    } else {
        doPush(this.redis, topic, call, threshold, callback);
    }
}

ApiThreshold.prototype.setThreshold = function (topic, threshold) {
    if (threshold == 0) {
        delete this.watchedTopics[topic];
        Logger.log("info", "remove ApiThreshold %s %s", topic, threshold);
    } else {
        var fakeValues = [];
        var fakeTime = Date.now() - 20 * 1000;
        for (var i = 0; i < threshold; i++) {
            fakeValues.push(fakeTime);
        }
        var key = "apiThreshold#callTimestamp#" + topic;
        this.redis.lpush(key, fakeValues);
        this.redis.ltrim(key, 0, threshold - 1);
        this.watchedTopics[topic] = threshold;
        Logger.log("info", "set ApiThreshold %s %s", topic, threshold);
    }
}


function doPush(redis, topic, call, threshold, callback) {
    if (call && threshold) {
        Logger.log("info", 7);
        var key = "apiThreshold#callTimestamp#" + topic;
        redis.lpush(key, Date.now());
        redis.ltrim(key, 0, threshold - 1);
    }
    callback(call);
}