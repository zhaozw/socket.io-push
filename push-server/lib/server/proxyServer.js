module.exports = ProxyServer;
var debug = require('debug')('ProxyServer');
var http = require('http');
var msgpack = require('msgpack-lite');
var parser = require('socket.io-parser');
var encoder = new parser.Encoder();
var decoder = new parser.Decoder();
var json = require('json3');

function ProxyServer(io, stats, packetService, notificationService, uidStore, ttlService) {

    decoder.on("decoded", function (xxx) {
        debug('decoded %j ', xxx);
    });

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
        socket.packet = function (packet, preEncoded) {
            try {
                if (preEncoded && preEncoded.preEncoded) {
                    var parsed = decodeString(packet[0]);
                    debug('parsed %j %s %s %s', parsed, parsed.data[0], parsed.type, socket.version);
                    var needEncode = stats.onPacket(parsed.data);
                    if (parsed.type == 2 && parsed.data[0] == "push" && socket.version > 1) {
                        parsed.data[1].data = new Buffer(parsed.data[1].data, 'base64');
                        parsed.type = parser.BINARY_EVENT;
                        parsed.data[1] = msgpack.encode(parsed.data[1]);
                        needEncode = true;
                    }
                    if (needEncode) {
                        encoder.encode(parsed, function (encoded) {
                            debug('convert to binary packet %s', encoded);
                            oldPacket.call(socket, encoded, preEncoded);
                        });
                    } else {
                        oldPacket.call(socket, packet, preEncoded);
                    }
                }
            } catch (err) {
                debug('packet error %s', err.stack);
                oldPacket.call(socket, packet, preEncoded);
            }
        };

        socket.on('pushId', function (data) {
            if (data.id && data.id.length >= 10) {
                debug("on pushId %s", JSON.stringify(data));
                if (data.platform) {
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
                var lastPacketIds = data.lastPacketIds;
                if (lastPacketIds) {
                    for (var topic in lastPacketIds) {
                        ttlService.getPackets(topic, lastPacketIds[topic], socket);
                    }
                }
                if (data.lastUnicastId) {
                    ttlService.getPackets(data.id, data.lastUnicastId, socket);
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
            ttlService.getPackets(topic, data.lastPacketId, socket);
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
        });

        stats.addSession(socket);
    });
}

exports.types = [
    'CONNECT',
    'DISCONNECT',
    'EVENT',
    'ACK',
    'ERROR',
    'BINARY_EVENT',
    'BINARY_ACK'
];

/**
 * Decode a packet String (JSON data)
 *
 * @param {String} str
 * @return {Object} packet
 * @api private
 */

function decodeString(str) {
    var p = {};
    var i = 0;
    // look up type
    p.type = Number(str.charAt(0));
    if (null == parser.types[p.type]) return error();

    // look up attachments if type binary
    if (parser.BINARY_EVENT == p.type || parser.BINARY_ACK == p.type) {
        var buf = '';
        while (str.charAt(++i) != '-') {
            buf += str.charAt(i);
            if (i == str.length) break;
        }
        if (buf != Number(buf) || str.charAt(i) != '-') {
            throw new Error('Illegal attachments');
        }
        p.attachments = Number(buf);
    }
    // look up namespace (if any)
    if ('/' == str.charAt(i + 1)) {
        p.nsp = '';
        while (++i) {
            var c = str.charAt(i);
            if (',' == c) break;
            p.nsp += c;
            if (i == str.length) break;
        }
    } else {
        p.nsp = '/';
    }
    // look up id
    var next = str.charAt(i + 1);
    if ('' !== next && Number(next) == next) {
        p.id = '';
        while (++i) {
            var c = str.charAt(i);
            if (null == c || Number(c) != c) {
                --i;
                break;
            }
            p.id += str.charAt(i);
            if (i == str.length) break;
        }
        p.id = Number(p.id);
    }
    // look up json data
    if (str.charAt(++i)) {
        try {
            p.data = json.parse(str.substr(i));
        } catch (e) {
            debug('parse json error %s', e);
            return error();
        }
    }

    debug('decoded %s as %j', str, p);
    return p;
}

function error(data) {
    return {
        type: exports.ERROR,
        data: 'parser error'
    };
}