module.exports = ProxyServer;

function ProxyServer(io, stats, packetService, notificationService, uidStore) {
    if (!(this instanceof ProxyServer)) return new ProxyServer(io, stats, packetService, notificationService, uidStore);
    var http = require('http');
    var debug = require('debug')('ProxyServer');

    io.on('connection', function (socket) {

        socket.on('disconnect', function () {
            stats.removeSession();
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
                    }
                }
            } catch (err) {
            }
            oldPacket.call(socket, packet, preEncoded);
        };

        socket.on('pushId', function (data) {
            if (data.id && data.id.length >= 10) {
                debug("on pushId %s", JSON.stringify(data));
                var topics = data.topics;
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
            notificationService.setApnToken(pushId, apnToken);
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
        });

        stats.addSession(socket);

    });
}