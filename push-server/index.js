var config = require("./config");
console.log("config " + JSON.stringify(config));
var instance = process.env.LBS_INSTANCE || "1";
console.log("starting instance #" + instance);
var ioPort = config["io_" + instance].port;
var apiPort = config["api_" + instance].port;
console.log("start server on port " + ioPort);
var io = require('socket.io')(ioPort, {pingTimeout: config.pingTimeout, pingInterval: config.pingInterval});

var RedisClustr = require('redis-clustr');
var redis = new RedisClustr({
    servers: config.redis
});
var socketIoRedis = require('socket.io-redis')({pubClient: redis, subClient: redis});
io.adapter(socketIoRedis);
var packetService = require('./lib/service/packetService.js')(redis, redis);
var stats = require('./lib/stats/stats.js')(redis);
var uidStore = require('./lib/redis/uidStore.js')(redis);
var ttlService = require('./lib/service/ttlService.js')(redis);
var notificationService = require('./lib/service/notificationService.js')(config.apns, redis, ttlService);

require('./lib/server/proxyServer.js')(io, stats, packetService, notificationService, uidStore, ttlService);

// push
var restApi = require('./lib/api/restApi.js')(io, stats, notificationService, apiPort, uidStore, ttlService);