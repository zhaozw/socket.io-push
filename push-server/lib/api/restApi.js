module.exports = RestApi;

function RestApi(io, stats, notificationService, port, uidStore, ttlService, redis, apiThreshold) {

    var restify = require('restify');

    var server = restify.createServer({
        name: 'myapp',
        version: '1.0.0'
    });


    var debug = require('debug')('RestApi');

    server.on('uncaughtException', function (req, res, route, err) {
        try {
            console.log("RestApi uncaughtException " + err.stack + " \n params: \n" + JSON.stringify(req.params));
            res.send({code: "error", message: "exception " + err.stack});
        } catch (err) {
            console.log("RestApi uncaughtException catch " + err.stack);
        }
    });

    server.use(restify.acceptParser(server.acceptable));
    server.use(restify.queryParser());
    server.use(restify.bodyParser());

    var staticConfig = restify.serveStatic({
        directory: __dirname + '/../../static',
        default: 'index.html'
    });

    server.get(/^\/push\/?.*/, staticConfig);

    server.get(/^\/notification\/?.*/, staticConfig);

    server.get(/^\/uid\/?.*/, staticConfig);

    server.get(/^\/handleStatsBase\/?.*/, staticConfig);

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
        var uid = req.params.uid;
        debug('push %j', req.params);
        var pushData = {topic: topic, data: data};

        var timeToLive = parseInt(req.params.timeToLive);

        if (pushAll == 'true') {
            apiThreshold.checkPushDrop(topic, function (call) {
                if (call) {
                    ttlService.addPacketAndEmit(topic, 'push', timeToLive, pushData, io, false);
                    res.send({code: "success"});
                } else {
                    res.send({code: "error", message: "call threshold exceeded"});
                }

            });
            return next();
        } else {
            if (pushId) {
                if (typeof pushId === 'string') {
                    ttlService.addPacketAndEmit(pushId, 'push', timeToLive, pushData, io, true);
                    res.send({code: "success"});
                    return next();
                } else {
                    pushId.forEach(function (id) {
                        ttlService.addPacketAndEmit(id, 'push', timeToLive, pushData, io, true);
                    });
                    res.send({code: "success"});
                    return next();
                }
            } else {
                if (uid) {
                    if (typeof uid === 'string') {
                        uidStore.getPushIdByUid(uid, function (pushIds) {
                            pushIds.forEach(function (id) {
                                ttlService.addPacketAndEmit(id, 'push', timeToLive, pushData, io, true);
                            });
                            res.send({code: "success"});
                            return next();
                        });
                    } else {
                        uid.forEach(function (id, i) {
                            uidStore.getPushIdByUid(id, function (pushIds) {
                                pushIds.forEach(function (result) {
                                    ttlService.addPacketAndEmit(result, 'push', timeToLive, pushData, io, true);
                                });
                            });
                        });
                        res.send({code: "success"});
                        return next();
                    }
                }
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
        var timeToLive = parseInt(req.params.timeToLive);

        debug('notification %j', req.params);

        if (pushAll === 'true') {
            notificationService.sendAll(notification, timeToLive, io);
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
                notificationService.sendByPushIds(pushIds, timeToLive, notification, io);
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
                    uids.forEach(function (uid, i) {
                        uidStore.getPushIdByUid(uid, function (pushIds) {
                            notificationService.sendByPushIds(pushIds, timeToLive, notification, io);
                        });
                    });
                    res.send({code: "success"});
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

    var handleQueryDataKeys = function (req, res, next) {
        stats.getQueryDataKeys(function (result) {
            debug("getQueryDataKeys result: " + result);
            res.send({"result": result});
        });
        return next();
    }

    server.get('/api/stats/base', handleStatsBase);
    server.get('/api/stats/chart', handleChartStats);
    server.get('/api/push', handlePush);
    server.post('/api/push', handlePush);
    server.get('/api/notification', handleNotification);
    server.post('/api/notification', handleNotification);
    server.get('/api/addPushIdToUid', handleAddPushIdToUid);
    server.post('/api/addPushIdToUid', handleAddPushIdToUid);
    server.get('api/state/getQueryDataKeys', handleQueryDataKeys)

    server.get('/api/topicOnline', function (req, res, next) {
        var topic = req.params.topic;
        var key = "stats#topicOnline#" + topic;
        redis.del(key, function () {
            redis.publish("adminCommand", JSON.stringify({command: "topicOnline", topic: topic}));
            setTimeout(function () {
                redis.get(key, function (err, result) {
                    result = result || "0";
                    res.send({topic: topic, online: result.toString()});
                });
            }, 3000);
        });
        return next();
    });

    server.get('/api/status', function (req, res, next) {
        res.send(redis.status());
        return next();
    });

    server.get('/api/redis/del', function (req, res, next) {
        redis.del(req.params.key);
        res.send({code: "success", key: req.params.key});
        return next();
    });

    server.get('/api/redis/get', function (req, res, next) {
        redis.get(req.params.key, function (err, result) {
            res.send({key: req.params.key, result: result});
        });
        return next();
    });

    server.get('/api/redis/hash', function (req, res, next) {
        redis.hash(req.params.key, function (result) {
            res.send(result);
        });
        return next();
    });


    server.get('/api/admin/command', function (req, res, next) {
        redis.publish("adminCommand", req.params.command);
        res.send({code: "success"});
        return next();
    });

    server.get('/api/redis/hgetall', function (req, res, next) {
        redis.hgetall(req.params.key, function (err, result) {
            res.send({key: req.params.key, count: result.length, result: result});
        });
        return next();
    });

    server.get('/api/redis/hkeys', function (req, res, next) {
        redis.hkeys(req.params.key, function (err, result) {
            var strs = [];
            result.forEach(function (token) {
                strs.push(token.toString('ascii'));
            });
            res.send({key: req.params.key, count: strs.length, result: strs});
        });
        return next();
    });

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


