module.exports = RestApi;

function RestApi(io, stats, notificationService, port, uidStore, ttlService) {

    var restify = require('restify');

    var server = restify.createServer({
        name: 'myapp',
        version: '1.0.0',
    });

    var debug = require('debug')('RestApi');

    server.use(restify.acceptParser(server.acceptable));
    server.use(restify.queryParser());
    server.use(restify.bodyParser());

    var staticConfig = restify.serveStatic({
        directory: __dirname + '/../../static',
        default: 'index.html'
    });

    server.get(/^\/push\/?.*/, staticConfig);

    server.get(/^\/uid\/?.*/, staticConfig);

    server.get(/^\/stats\/?.*/, staticConfig);

    server.get(/^\/js\/?.*/, staticConfig);

    server.get("/", staticConfig);

    var handlePush = function (req, res, next) {
        var topic = req.params.topic;
        if (!topic) {
            res.send({code: "error", message: 'topic is required'});
            return next();
        }
        var data = req.params.data;
        if (!data) {
            res.send({code: "error", message: 'data is required'});
            return next();
        }
        var pushId = req.params.pushId;
        var pushAll = req.params.pushAll;
        debug('push ' + JSON.stringify(req.params));
        var pushData = {topic: topic, data: data};

        fillPacket(pushData, req);

        if (pushAll === 'true') {
            io.to(topic).emit('push', pushData);
            res.send({code: "success"});
            return next();
        } else if (!pushId) {
            res.send({code: "error", message: 'pushId is required'});
            return next();
        } else {
            if (typeof pushId === 'string') {
                io.to(pushId).emit('push', pushData);
                ttlService.addPacket(pushId, 'push', pushData);
                res.send({code: "success"});
                return next();
            } else {
                pushId.forEach(function (id) {
                    io.to(id).emit('push', pushData);
                    ttlService.addPacket(id, 'push', pushData);
                });
                res.send({code: "success"});
                return next();
            }
        }
    };


    var handleNotification = function (req, res, next) {
        var notification = JSON.parse(req.params.notification);
        if (!notification) {
            res.send({code: "error", message: 'notification is required'});
            return next();
        }

        var pushId = req.params.pushId;
        var uid = req.params.uid;
        var pushAll = req.params.pushAll;
        fillPacket(notification, req);

        debug('notification ' + JSON.stringify(req.params));


        if (pushAll === 'true') {
            notificationService.sendAll(notification, io);
            res.send({code: "success"});
            return next();
        } else {
            if (pushId) {
                var pushIds;
                if (typeof pushId === 'string') {
                    pushIds = [pushId];
                } else {
                    pushIds = pushId;
                }
                notificationService.sendByPushIds(pushIds, notification, io);
                res.send({code: "success"});
                return next();
            } else {
                if (uid) {
                    var uids;
                    if (typeof uid === 'string') {
                        uids = [uid];
                    } else {
                        uids = uid;
                    }
                    var pushIds = [];
                    //util.batch(pubClient, "hkeys", "uidToPushId#", uids, function (replies) {
                    //    replies.forEach(function (result, i) {
                    //         pushIds = pushIds.concat(result);
                    //    });
                    //    redis.sendNotification(pushIds, notification, io);
                    //    res.send({code: "success"});
                    //});
                }
            }
        }
    };

    var handleStatsBase = function (req, res, next) {
        stats.getSessionCount(function (count) {
            res.send(count);
        });
        return next();
    };

    var handleChartStats = function (req, res, next) {
        var key = req.params.key;
        stats.find(key, function (result) {
            res.send(result);
        });
        return next();
    };

    var handleAddPushIdToUid = function (req, res, next) {
        var uid = req.params.uid;
        var pushId = req.params.pushId;
        uidStore.addUid(pushId, uid, 3600 * 1000)
        res.send({code: "success"});
        return next();
    };

    server.get('/api/stats/base', handleStatsBase);
    server.get('/api/stats/chart', handleChartStats);
    server.get('/api/push', handlePush);
    server.post('/api/push', handlePush);
    server.get('/api/notification', handleNotification);
    server.post('/api/notification', handleNotification);
    server.get('/api/addPushIdToUid', handleAddPushIdToUid);
    server.post('/api/addPushIdToUid', handleAddPushIdToUid);

    server.get('/api/nginx', function (req, res, next) {
        stats.getSessionCount(function (count) {
            res.writeHead(200, {
                'Content-Type': 'text/plain'
            });
            count.processCount.forEach(function (process) {
                res.write("server " + process.id + ";\n");
            });
            res.end();
        });
        return next();
    });

    server.get('/api/ip', function (req, res, next) {
        var ip = req.connection.remoteAddress;
        ip = ip.substr(ip.lastIndexOf(':') + 1, ip.length);
        res.writeHead(200, {
            'Content-Length': Buffer.byteLength(ip),
            'Content-Type': 'text/plain'
        });
        res.write(ip);
        res.end();
        return next();
    });

    server.get('/api/stats', function (req, res, next) {
        res.send({
            sentCounter: sentCounter,
            receiveCounter: receiveCounter,
            percent: receiveCounter / sentCounter
        });
        return next();
    });

    server.listen(port, function () {
        console.log('%s listening at %s', server.name, server.url);
    });

}

var randomstring = require("randomstring");

function fillPacket(packet, req) {
    packet.id = randomstring.generate(32);
    var timeToLive = parseInt(req.params.timeToLive);
    if (timeToLive > 0) {
        packet.timeToLive = timeToLive;
    }
}

