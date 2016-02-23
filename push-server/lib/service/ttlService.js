module.exports = TTLService;

var debug = require('debug')('TTLService');

function TTLService(redis) {
    if (!(this instanceof TTLService)) return new TTLService(redis);
    this.redis = redis;
}

TTLService.prototype.onConnect = function (socket) {
    var outerThis = this;
    socket.packetListeners.push(function (parsed, packet) {
        if (socket.version > 0 && (parsed[0] === "noti" || parsed[0] === "push")) {
            var timestamp = Date.now();
            parsed[1]['timestamp'] = timestamp;
            var timeToLive = parsed[1]['timeToLive'];
            if (timeToLive > 0) {
                parsed[1]['reply'] = true;
                outerThis.addPacket(socket.pushId, parsed, timeToLive);
            }
            packet[0] = "2" + JSON.stringify(parsed);
        }
    });
}

TTLService.prototype.onReply = function (socket) {
    var key = "ttlPacket#" + socket.pushId;
    this.redis.del(key);
    debug("onReply %s", key);
}


TTLService.prototype.onPushId = function (socket) {
    var redis = this.redis;
    var key = "ttlPacket#" + socket.pushId;
    redis.lrange(key, 0, -1, function (err, packets) {
        if (packets.length > 0) {
            debug("onPushId key %s , %d ", key, packets.length);
            packets.forEach(function (raw) {
                try {
                    debug("ttl packet %s", raw);
                    var packet = JSON.parse(raw);
                    socket.emit(packet[0], packet[1]);
                } catch (err) {
                    debug("ttl packet parse error %s", err);
                }
            });
            redis.del(key);
        }
    });
}


TTLService.prototype.addPacket = function (pushId, packet, timeToLive) {
    if (timeToLive > 0) {
        var redis = this.redis;
        var key = "ttlPacket#" + pushId;
        packet[1]['timestampValid'] = Date.now() + timeToLive;
        redis.pttl(key, function (err, oldTtl) {
            debug("addPacket key %s , %d , %d", key, oldTtl, timeToLive);
            redis.rpush(key, JSON.stringify(packet));
            if (timeToLive > oldTtl) {
                redis.pexpire(key, timeToLive);
            }
        });
    }
};