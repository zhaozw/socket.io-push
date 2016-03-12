module.exports = TTLService;

var debug = require('debug')('TTLService');

function TTLService(redis) {
    if (!(this instanceof TTLService)) return new TTLService(redis);
    this.redis = redis;
}

TTLService.prototype.onConnect = function (socket) {
    var outerThis = this;
    socket.packetListeners.push(function (parsed, packet) {
        if (socket.version > 0 && (parsed[0] === "push")) {
            outerThis.addPacket(socket.pushId, parsed[0], parsed[1]);
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
        if (packets && packets.length > 0) {
            debug("onPushId key %s , %d ", key, packets.length);
            var timestamp = Date.now();
            var pushedIds = [];
            packets.forEach(function (raw) {
                try {
                    var packet = JSON.parse(raw);
                    if (!(pushedIds.indexOf(packet[1].id) > -1) && packet[1]['timestampValid'] > timestamp) {
                        debug("ttl packet %s", raw);
                        socket.emit(packet[0], packet[1]);
                        pushedIds.push(packet[1].id);
                    }
                } catch (err) {
                    debug("ttl packet parse error %s", err);
                }
            });
            redis.del(key);
        }
    });
}

TTLService.prototype.addPacket = function (pushId, topic, data) {
    var timeToLive = data.timeToLive;
    if (timeToLive > 0) {
        var redis = this.redis;
        var key = "ttlPacket#" + pushId;
        data.timestampValid = Date.now() + timeToLive;
        redis.pttl(key, function (err, oldTtl) {
            debug("addPacket key %s , %d , %d", key, oldTtl, timeToLive);
            redis.rpush(key, JSON.stringify(data));
            if (timeToLive > oldTtl) {
                redis.pexpire(key, timeToLive);
            }
        });
    }
};

var maxTllPacketPerTopic = -50;

TTLService.prototype.addPacket = function (topic, event, timeToLive, data) {
    if (timeToLive > 0) {
        var redis = this.redis;
        data.timestampValid = Date.now() + timeToLive;
        data.event = event;
        var listKey = "ttl#packet#" + topic;

        var packet = [topic, data];
        redis.pttl(listKey, function (err, oldTtl) {
            debug("addPacket key %s , %d , %d", key, oldTtl, timeToLive);
            redis.rpush(listKey, JSON.stringify(packet));
            redis.ltrim(listKey, maxTllPacketPerTopic, -1);
            if (timeToLive > oldTtl) {
                redis.pexpire(key, timeToLive);
            }
        });
    }
};

TTLService.prototype.getPackets = function (topic, lastId, socket) {
    if (timeToLive > 0) {
        var redis = this.redis;
        data.timestampValid = Date.now() + timeToLive;
        var listKey = "ttl#packet#" + topic;
        redis.lrange(listKey, 0, -1, function (err, list) {
            if (list) {
                var lastFound = false;
                list.forEach(function (packet) {
                    var jsonPacket = JSON.parse(packet);
                    var now = Date.now();
                    if (jsonPacket.id == lastId) {
                        lastFound = true;
                    } else if (lastFound == true && jsonPacket.timestampValid < now) {
                        var event = jsonPacket.event;
                        delete jsonPacket.event;
                        delete jsonPacket.timestampValid;
                        socket.emit(event, jsonPacket);
                    }
                });
            }
        });
    }
};

function emitPacket(socket,packet){

}