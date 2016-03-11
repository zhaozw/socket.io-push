module.exports = ProxyServer;

function ProxyServer(io, stats, packetService, notificationService, uidStore, ttlService) {
    var http = require('http');
    var debug = require('debug')('ProxyServer');
    var msgpack = require('msgpack5')();

    io.on('connection', function (socket) {

        socket.version = 0;

        socket.on('disconnect', function () {
            stats.removeSession();
            stats.removePlatformSession(socket.platform);
            if (socket.pushId) {
                debug("publishDisconnect %s", socket.pushId);
                packetService.publishDisconnect(socket);
            }
        });

        var oldPacket = socket.packet;
        socket.packetListeners = [];
        socket.packet = function (packet, preEncoded) {
            try {
                if (preEncoded && preEncoded.preEncoded) {
                    var packetBody = packet[0];
                    if (packetBody.length > 0) {
                        var json = packetBody.substring(1, packetBody.length);
                        var parsed = JSON.parse(json);
                        for (var i = 0; i < socket.packetListeners.length; i++) {
                            socket.packetListeners[i](parsed, packet);
                        }

                        if(parsed[0] === "push" && socket.version > 1) {
                            parsed[1]["data"] = new Buffer(parsed[1]["data"], 'base64');
                            var json =  JSON.stringify(parsed[1]);

                            var jsonPacket = JSON.parse(json);
                            jsonPacket.data = parsed[1]["data"];
                            console.log("pushJson:  " + JSON.stringify(jsonPacket));
                            var encodeJson =  msgpack.encode(jsonPacket);
                            var bufferPacket = new Buffer(encodeJson, 'base64');

                            var data = ["push"];
                            data.push(bufferPacket);
                            var myPacket = { type: 5, data: data };
                            console.log("push myPacket: " + JSON.stringify(myPacket));

                            packet = myPacket;
                            preEncoded.preEncoded = false;
                        }
                    }
                }
            } catch (err) {
            }
            oldPacket.call(socket, packet, preEncoded);
        };

        socket.on('pushId', function (data) {
            if (data.id && data.id.length >= 10) {
                debug("on pushId %s", JSON.stringify(data));
                if(data.platform){
                    socket.platform = data.platform.toLowerCase();
                }
                stats.addPlatformSession(socket.platform);
                var topics = data.topics;
                if (data.version) {
                    socket.version = data.version;
                }
                if (topics && topics.length > 0) {
                    topics.forEach(function (topic) {
                        socket.join(topic);
                        debug('join topic ' + topic);
                    });
                }
                uidStore.getUidByPushId(data.id, function (uid) {
                    var reply = {id: data.id};
                    if (uid) {
                        reply.uid = uid;
                        socket.uid = uid;
                    }
                    socket.pushId = data.id;
                    packetService.publishConnect(socket);
                    socket.join(data.id);
                    socket.emit('pushId', reply);
                    debug('join room socket.id %s ,pushId %s', socket.id, socket.pushId);
                    ttlService.onPushId(socket);
                })
            }
        });

        socket.on('subscribeTopic', function (data) {
            debug("on subscribeTopic %s", JSON.stringify(data));
            var topic = data.topic;
            socket.join(topic);
        });


        socket.on('unsubscribeTopic', function (data) {
            debug("on unsubscribeTopic %s", JSON.stringify(data));
            var topic = data.topic;
            socket.leave(topic);
        });

        socket.on('apnToken', function (data) {
            debug("on apnToken %s", JSON.stringify(data));
            var pushId = data.pushId;
            var apnToken = data.apnToken;
            notificationService.setApnToken(pushId, apnToken, data.bundleId);
        });

        socket.on('packetProxy', function (data) {
            data.pushId = socket.pushId;
            if (socket.uid) {
                data.uid = socket.uid;
            }
            packetService.publishPacket(data);
        });

        socket.on('notificationReply', function (data) {
            stats.onNotificationReply(data.timestamp);
            ttlService.onReply(socket);
        });

        socket.on('pushReply', function () {
            ttlService.onReply(socket);
        });

        stats.addSession(socket);
        ttlService.onConnect(socket);
    });
}