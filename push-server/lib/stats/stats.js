module.exports = Stats;

var debug = require('debug')('Stats');
var randomstring = require("randomstring");

function Stats(redis, port) {
    if (!(this instanceof Stats)) return new Stats(redis, port);
    this.redis = redis;
    this.sessionCount = 0;
    this.redisIncrBuffer = require('./redisIncrBuffer.js')(redis);
    var ipPath = process.cwd() + "/ip";
    var fs = require('fs');
    var ip;
    if (fs.existsSync(ipPath)) {
        ip = fs.readFileSync(ipPath,"utf8").trim() + ":" + port;
    }
    debug("ip file %s %s", ipPath, ip);
    this.id = ip || randomstring.generate(32);
    var stats = this;
    setInterval(function () {
        redis.hset("stats#sessionCount", stats.id, JSON.stringify({
            timestamp: Date.now(),
            sessionCount: stats.sessionCount
        }));
    }, 10000);
    redis.del("stats#sessionCount");
}

Stats.prototype.addSession = function (socket, count) {
    if (!count) {
        count = 1;
    }
    this.sessionCount += count;

    var stats = this;

    socket.packetListeners.push(function (parsed, packet) {
        var timestamp = Date.now();
        stats.incr("stats#toClientPacket#totalCount", timestamp);
        if (parsed[0] === "noti") {
            parsed[1]['timestamp'] = timestamp
            packet[0] = "2" + JSON.stringify(parsed);
            stats.incr("stats#notification#totalCount", timestamp);
            debug("adding notification timestamp %s", packet[0]);
        }
    });

    socket.on('stats', function (data) {
        debug("on stats %s", JSON.stringify(data.requestStats));
        var timestamp = Date.now();
        var totalCount = 0;
        if (data.requestStats && data.requestStats.length) {
            for (var i = 0; i < data.requestStats.length; i++) {
                var requestStat = data.requestStats[i];
                stats.incrby("stats#request#" + requestStat.path + "#totalCount", timestamp, requestStat.totalCount);
                stats.incrby("stats#request#" + requestStat.path + "#successCount", timestamp, requestStat.successCount);
                stats.incrby("stats#request#" + requestStat.path + "#totalLatency", timestamp, requestStat.totalLatency);
            }
        }
    });
};

Stats.prototype.removeSession = function (count) {
    if (!count) {
        count = 1;
    }
    this.sessionCount -= count;
};

var mSecPerHour = 60 * 60 * 1000

function hourStrip(timestamp) {
    return Math.ceil(timestamp / mSecPerHour) * mSecPerHour;
}

Stats.prototype.incr = function (key, timestamp) {
    var hourKey = hourStrip(timestamp);
    key = key + "#" + hourKey;
    this.redisIncrBuffer.incrby(key, 1);
    debug("incr %s %s", key, hourKey);
};

Stats.prototype.incrby = function (key, timestamp, by) {
    var hourKey = hourStrip(timestamp);
    key = key + "#" + hourKey;
    this.redisIncrBuffer.incrby(key, by);
    debug("incrby %s %s by %d ", key, hourKey, by);
};

Stats.prototype.onNotificationReply = function (timestamp) {
    var latency = Date.now() - timestamp;
    if (latency < 10000) {
        this.incr("stats#notification#successCount", timestamp);
        this.incrby("stats#notification#totalLatency", timestamp, latency);
        debug("onNotificationReply %d", latency);
    }
};

Stats.prototype.getSessionCount = function (callback) {
    this.redis.hgetall('stats#sessionCount', function (err, results) {
        debug("stats#sessionCount %s", results.length);
        var totalCount = 0;
        var currentTimestamp = Date.now();
        var processCount = [];
        for (var id in results) {
            var data = JSON.parse(results[id]);
            if ((currentTimestamp - data.timestamp) < 20 * 1000) {
                totalCount += data.sessionCount;
                processCount.push({id: id, count: data.sessionCount});
            }
        }

        callback({
            sessionCount: totalCount, processCount: processCount.sort(function (a, b) {
                if (a.id < b.id) return -1;
                if (a.id > b.id) return 1;
                return 0;
            })
        })
    });
};

Stats.prototype.find = function (key, callback) {
    var totalHour = 7 * 24;
    var timestamp = hourStrip(Date.now() - (totalHour - 1) * mSecPerHour);
    var keys = [];
    var totalCount = 0;
    var totalLatency = 0;
    var totalSuccess = 0;
    var timestamps = [];
    for (var i = 0; i < totalHour; i++) {
        timestamps.push(timestamp);
        keys.push("stats#" + key + "#totalCount#" + timestamp);
        keys.push("stats#" + key + "#successCount#" + timestamp);
        keys.push("stats#" + key + "#totalLatency#" + timestamp);
        timestamp += mSecPerHour;
    }

    var results = [];
    var redis = this.redis;
    redis.get(timestamps.shift(), function (err, replies) {
        results.push(replies);

        redis.get("stats#" + key + "#successCount#" + timestamp, function (err, replies) {
            results.push(replies);

        });
    });


    var recursive = function (err, replies) {
        results.push(replies);
        if (keys.length > 0) {
            redis.get(keys.shift(), recursive);
        } else {
            var totalChart = [];
            var latencyChart = [];
            var successRateChart = [];
            var countPerSecondChart = [];

            var totalDay = 0;
            var successDay = 0;
            var latencyDay = 0;
            var successRateChartDay = [];
            var latencyChartDay = [];

            for (var i = 0; i < results.length / 3; i++) {

                var total = parseInt(results[i * 3 + 0]) || 0;
                var success = parseInt(results[i * 3 + 1]) || 0;
                var latency = parseInt(results[i * 3 + 2]) || 0;

                totalCount += total;
                totalDay += total;
                totalSuccess += success;
                successDay += success;
                totalLatency += latency;
                latencyDay += latency;
                totalChart.push(total);
                latencyChart.push(Math.round(latency / success) || 0);
                successRateChart.push(((100 * success / total) || 0).toFixed(2));
                countPerSecondChart.push(total / mSecPerHour * 1000);

                if ((i + 1) % (24) == 0) {
                    successRateChartDay.push(((100 * successDay / totalDay) || 0).toFixed(2));
                    latencyChartDay.push(Math.round(latencyDay / successDay) || 0);
                    totalDay = 0;
                    successDay = 0;
                    latencyDay = 0;
                }

            }
            var avgLatency = Math.round(totalLatency / totalSuccess) || 0;
            var successRate = totalSuccess / totalCount;
            var countPerSecond = totalCount / totalHour / mSecPerHour * 1000;

            var chartData = {
                timestamps: timestamps,
                total: totalChart,
                latency: latencyChart,
                successRate: successRateChart,
                countPerSecond: countPerSecondChart,
                successRateDay: successRateChartDay,
                latencyDay: latencyChartDay
            };

            callback({
                "totalCount": totalCount,
                "totalSuccess": totalSuccess,
                "avgLatency": avgLatency,
                "successRate": successRate,
                "countPerSecond": countPerSecond,
                "chartData": chartData
            });
        }
    };

    recursive();

}

