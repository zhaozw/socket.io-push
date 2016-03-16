module.exports = Stats;

var debug = require('debug')('Stats');
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
    debug("ip file %s %s", ipPath, ip);
    this.id = ip || randomstring.generate(32);
    var stats = this;
    setInterval(function () {
        var packetAverage = stats.ms.sum([60 * 1000, 5 * 60 * 1000]);
        stats.packetAverage1Minute = packetAverage[0];
        stats.packetAverage5Minute = packetAverage[1];
        redis.hset("stats#sessionCount", stats.id, JSON.stringify({
            timestamp: Date.now(),
            sessionCount: stats.sessionCount,
            packetAverage1Minute: stats.packetAverage1Minute,
            packetAverage5Minute: stats.packetAverage5Minute,
            packetDrop: stats.packetDrop,
            packetDropThreshold: stats.packetDropThreshold
        }));
    }, 5000);
    redis.del("stats#sessionCount");

    redis.on("message", function (channel, message) {
        if (channel == "adminCommand") {
            debug('adminCommand %s', message);
            var command = JSON.parse(message);
            if (command.command = 'packetDropThreshold') {
                debug('setting packetDropThreshold %d', stats.packetDropThreshold);
                stats.packetDropThreshold = command.packetDropThreshold;
            }
        }
    });
    redis.subscribe("adminCommand");
}

Stats.prototype.shouldDrop = function () {
    if (this.packetDropThreshold != 0 && this.packetAverage1Minute > this.packetDropThreshold) {
        this.packetDrop++;
        debug('threshold exceeded dropping packet %d > %d', this.packetAverage1Minute, this.packetDropThreshold);
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


Stats.prototype.onPacket = function (packetData) {
    var timestamp = Date.now();
    this.ms.push(timestamp);

    this.incr("stats#toClientPacket#totalCount", timestamp);
    if (packetData[0] == "noti") {
        packetData[1].timestamp = timestamp;
        this.incr("stats#notification#totalCount", timestamp);
        debug("adding notification timestamp %j", packetData);
        return true;
    } else {
        return false;
    }
}

Stats.prototype.addSession = function (socket, count) {
    if (!count) {
        count = 1;
    }
    this.sessionCount.total += count;

    var stats = this;

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
    debug('onNotificationReply %s', latency);
    if (latency < 10000) {
        this.incr("stats#notification#successCount", timestamp);
        this.incrby("stats#notification#totalLatency", timestamp, latency);
        debug("onNotificationReply %d", latency);
    }
};

Stats.prototype.getSessionCount = function (callback) {
    this.redis.hgetall('stats#sessionCount', function (err, results) {
        var totalCount = 0;
        var androidCount = 0;
        var iosCount = 0;
        var currentTimestamp = Date.now();
        var processCount = [];
        var packetAverage1Minute = 0;
        var packetAverage5Minute = 0;
        var packetDrop = 0;
        var packetDropThreshold = 0;
        for (var id in results) {
            var data = JSON.parse(results[id]);
            if ((currentTimestamp - data.timestamp) < 20 * 1000) {
                totalCount += data.sessionCount.total;
                iosCount += data.sessionCount.ios;
                androidCount += data.sessionCount.android;
                packetAverage1Minute += data.packetAverage1Minute || 0;
                packetAverage5Minute += data.packetAverage5Minute || 0;
                packetDrop += data.packetDrop || 0;
                packetDropThreshold += data.packetDropThreshold || 0;
                processCount.push({
                    id: id,
                    count: data.sessionCount,
                    packetAverage1Minute: packetAverage1Minute,
                    packetAverage5Minute: packetAverage5Minute,
                    packetDrop: packetDrop,
                    packetDropThreshold: packetDropThreshold
                });
            }
        }

        callback({
            sessionCount: totalCount,
            android: androidCount,
            ios: iosCount,
            packetAverage1Minute: packetAverage1Minute,
            packetAverage5Minute: packetAverage5Minute,
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

