module.exports = PacketService;

var Logger = require('../log/index.js')('PacketService');

var randomstring = require("randomstring");
var pathToServer = {};

String.prototype.hashCode = function () {
    var hash = 0;
    if (this.length == 0) return hash;
    for (i = 0; i < this.length; i++) {
        var char = this.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}

function PacketService(redis, subClient) {
    if (!(this instanceof PacketService)) return new PacketService(redis, subClient);
    this.redis = redis;
    this.stopped = false;
    subClient.on("message", function (channel, message) {
        Logger.info("subscribe message " + channel + ": " + message)
        if (channel == "packetServer") {
            var handlerInfo = JSON.parse(message);
            updatePathServer(handlerInfo);
        }
    });
    subClient.subscribe("packetServer");
}

function updatePathServer(handlerInfo) {
    var timestamp = new Date().getTime();
    var serverId = handlerInfo.serverId;
    for (path of handlerInfo.paths) {
        var servers = pathToServer[path];
        if (!servers) {
            servers = [];
        }
        var updatedServers = [];
        var found = false;
        for (server of servers) {
            if (server.serverId === serverId) {
                server.timestamp = timestamp;
                updatedServers.push(server);
                found = true;
            } else if (timestamp - server.timestamp > 10000) {
                Logger.info("server is dead %s", server.serverId);
            } else {
                updatedServers.push(server);
            }
        }
        if (!found) {
            Logger.info("new server is added %s", serverId);
            updatedServers.push({serverId: serverId, timestamp: timestamp});
        }
        pathToServer[path] = updatedServers;
    }
}

function hashIndex(pushId, count) {
    return pushId.hashCode() % count;
}

PacketService.prototype.publishPacket = function (data) {
    if (this.stopped) {
        return;
    }
    var path = data.path;
    var pushId = data.pushId;
    if (path && pushId) {
        if (!data.sequenceId) {
            data.sequenceId = randomstring.generate(16);
        }
        var servers = pathToServer[path];
        var strData = JSON.stringify(data);
        if (servers) {
            var serverCount = servers.length;
            var idx = hashIndex(pushId, serverCount);
            if (servers[idx]) {
                var serverId = servers[idx]["serverId"];
                this.redis.publish("packetProxy#" + serverId, strData);
                Logger.info("publishPacket %s %s", serverId, strData);
                return;
            }
        }
        this.redis.publish("packetProxy#default", strData);
    }
};

PacketService.prototype.publishDisconnect = function (socket) {
    if (this.stopped) {
        return;
    }
    Logger.info("publishDisconnect pushId %s", socket.pushId);
    var outerThis = this;
    this.redis.get("pushIdSocketId#" + socket.pushId, function (err, lastSocketId) {
        // reply is null when the key is missing
        Logger.info("pushIdSocketId redis %s %s %s", socket.id, lastSocketId, socket.pushId);
        if (lastSocketId == socket.id) {
            Logger.info("publishDisconnect current socket disconnect %s", socket.id);
            outerThis.redis.del("pushIdSocketId#" + socket.pushId);
            var data = {pushId: socket.pushId, path: "/socketDisconnect"};
            if (socket.uid) {
                data.uid = socket.uid;
            }
            outerThis.publishPacket(data);
        }
    });
};

PacketService.prototype.publishConnect = function (socket) {
    if (this.stopped) {
        return;
    }
    Logger.info("publishConnect pushId %s", socket.pushId);
    var outerThis = this;
    this.redis.get("pushIdSocketId#" + socket.pushId, function (err, lastSocketId) {
        // reply is null when the key is missing
        Logger.info("publishConnect query redis %s", lastSocketId);
        if (lastSocketId) {
            Logger.info("reconnect do not publish", lastSocketId);
        } else {
            Logger.info("first connect publish", lastSocketId);
            var data = {pushId: socket.pushId, path: "/socketConnect"};
            if (socket.uid) {
                data.uid = socket.uid;
            }
            outerThis.publishPacket(data);
        }
        outerThis.redis.set("pushIdSocketId#" + socket.pushId, socket.id);
        outerThis.redis.expire("pushIdSocketId#" + socket.pushId, 3600 * 24 * 7);
    });
};