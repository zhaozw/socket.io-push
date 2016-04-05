module.exports = Stats;

var Logger = require('../log/index.js')('Stats');
var randomstring = require("randomstring");

function Stats(redis, port) {
    this.redis = redis;
    this.sessionCount = {ios: 0, android: 0, total: 0};
    this.redisIncrBuffer = require('./redisIncrBuffer.js')(redis);
    this.packetDrop = 0;
    this.packetDropThreshold = 0;
    this.ms = new (require('./moving-sum.js'))();
    var ipPath = process.cwd() + "/ip";
    var fs = require('fs');
    var ip;
    if (fs.existsSync(ipPath)) {
        ip = fs.readFileSync(ipPath, "utf8").trim() + ":" + port;
    }
    Logger.info("ip file %s %s", ipPath, ip);
    this.id = ip || randomstring.generate(32);
    var stats = this;
    setInterval(function () {
        var packetAverage = stats.ms.sum([10 * 1000]);
        stats.packetAverage1 = packetAverage[0];
        redis.hset("stats#sessionCount", stats.id, JSON.stringify({
            timestamp: Date.now(),
            sessionCount: stats.sessionCount,
            packetAverage1: stats.packetAverage1,
            packetDrop: stats.packetDrop,
            packetDropThreshold: stats.packetDropThreshold
        }));
    }, 10000);
    redis.del("stats#sessionCount");
}

Stats.prototype.shouldDrop = function () {
    if (this.packetDropThreshold != 0 && this.packetAverage1 && this.packetAverage1 > this.packetDropThreshold) {
        Logger.info('threshold exceeded dropping packet %d > %d', this.packetAverage1, this.packetDropThreshold);
        this.packetDrop++;
        return true;
    } else {
        return false;
    }
}

Stats.prototype.addPlatformSession = function (platform, count) {
    if (!count) {
        count = 1;
    }
    if (platform == 'ios') {
        this.sessionCount.ios += count;
    } else if (platform == 'android') {
        this.sessionCount.android += count;
    }
}

Stats.prototype.removePlatformSession = function (platform, count) {
    if (!count) {
        count = 1;
    }
    if (platform == 'ios') {
        this.sessionCount.ios -= count;
    } else if (platform == 'android') {
        this.sessionCount.android -= count;
    }
}


Stats.prototype.onPacket = function () {
    var timestamp = Date.now();
    this.packetAverage1++;
    this.ms.push(timestamp);
    this.incr("stats#toClientPacket#totalCount", timestamp);
}

Stats.prototype.addApnTotal = function (count) {
    var timestamp = Date.now();
    this.incrby("stats#apnPush#successCount", timestamp, count);
}

Stats.prototype.addApnSuccess = function (count) {
    var timestamp = Date.now();
    this.incrby("stats#apnPush#totalCount", timestamp, count);
}

Stats.prototype.addApnError = function (count, errorCode) {
    var timestamp = Date.now();
    this.incrby("stats#apnPushError" + errorCode + "#totalCount", timestamp, count);
}

Stats.prototype.addSession = function (socket, count) {
    if (!count) {
        count = 1;
    }
    this.sessionCount.total += count;

    var stats = this;

    socket.on('stats', function (data) {
        Logger.info("on stats %s", JSON.stringify(data.requestStats));
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
    this.sessionCount.total -= count;
};

var mSecPerHour = 60 * 60 * 1000

function hourStrip(timestamp) {
    return Math.ceil(timestamp / mSecPerHour) * mSecPerHour;
}

Stats.prototype.incr = function (key, timestamp) {
    var hourKey = hourStrip(timestamp);
    key = key + "#" + hourKey;
    this.redisIncrBuffer.incrby(key, 1);
    Logger.info("incr %s %s", key, hourKey);
};

Stats.prototype.incrby = function (key, timestamp, by) {
    if (by > 0) {
        var hourKey = hourStrip(timestamp);
        key = key + "#" + hourKey;
        this.redisIncrBuffer.incrby(key, by);
        Logger.info("incrby %s %s by %d ", key, hourKey, by);
    }
};

Stats.prototype.onNotificationReply = function (timestamp) {
    var latency = Date.now() - timestamp;
    Logger.info('onNotificationReply %s', latency);
    if (latency < 10000) {
        this.incr("stats#notification#successCount", timestamp);
        this.incrby("stats#notification#totalLatency", timestamp, latency);
        Logger.info("onNotificationReply %d", latency);
    }
};

Stats.prototype.getSessionCount = function (callback) {
    this.redis.hgetall('stats#sessionCount', function (err, results) {
        var totalCount = 0;
        var androidCount = 0;
        var iosCount = 0;
        var currentTimestamp = Date.now();
        var processCount = [];
        var packetAverage1 = 0;
        var packetDrop = 0;
        var packetDropThreshold = 0;
        for (var id in results) {
            var data = JSON.parse(results[id]);
            if ((currentTimestamp - data.timestamp) < 60 * 1000) {
                totalCount += data.sessionCount.total;
                iosCount += data.sessionCount.ios;
                androidCount += data.sessionCount.android;
                packetAverage1 += data.packetAverage1 || 0;
                packetDrop += data.packetDrop || 0;
                packetDropThreshold += data.packetDropThreshold || 0;
                processCount.push({
                    id: id,
                    count: data.sessionCount,
                    packetAverage1: data.packetAverage1,
                    packetDrop: data.packetDrop,
                    packetDropThreshold: data.packetDropThreshold
                });
            }
        }

        callback({
            sessionCount: totalCount,
            android: androidCount,
            ios: iosCount,
            packetAverage1: packetAverage1,
            packetDrop: packetDrop,
            processCount: processCount.sort(function (a, b) {
                if (a.id < b.id) return -1;
                if (a.id > b.id) return 1;
                return 0;
            })
        })
    });
};

Stats.prototype.getQueryDataKeys = function (callback) {
    this.redis.hkeys("queryDataKeys", function (err, replies) {
        var strs = [];
        replies.forEach(function (buffer) {
            strs.push(buffer.toString());
        });
        callback(strs.sort(sortString));

    });
}

var sortString = function (a, b) {
    a = a.toLowerCase();
    b = b.toLowerCase();
    if (a < b) return 1;
    if (a > b) return -1;
    return 0;
}

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
        keys.push("stats#" + key + "#errorCount#" + timestamp);
        timestamp += mSecPerHour;
    }

    var results = [];
    var redis = this.redis;

    var recursive = function (err, replies) {
        if (replies != -1) {
            results.push(replies);
        }
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

            for (var i = 0; i < results.length / 4; i++) {

                var total = parseInt(results[i * 4 + 0]) || 0;
                var success = parseInt(results[i * 4 + 1]) || 0;
                var latency = parseInt(results[i * 4 + 2]) || 0;
                var error = parseInt(results[i * 4 + 3]) || 0;
                if (success == 0) {
                    success = total - error;
                }
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

    recursive(null, -1);

}

