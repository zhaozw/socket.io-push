var config = require("./config");
console.log("config " + JSON.stringify(config));
var instance = process.env.LBS_INSTANCE || "1";
console.log("starting instance #" + instance);
var ioPort = config["io_" + instance].port;
var apiPort = config["api_" + instance].port;
console.log("start server on port " + ioPort);
var io = require('socket.io')(ioPort, {pingTimeout: config.pingTimeout, pingInterval: config.pingInterval});

var simpleRedisHashCluster = require('./lib/redis/simpleRedisHashCluster.js');

var pubClient = simpleRedisHashCluster(config.redis);
var subClient = simpleRedisHashCluster(config.redis);

var socketIoRedis = require('socket.io-redis')({pubClient: pubClient, subClient: subClient});
io.adapter(socketIoRedis);

var packetService = require('./lib/service/packetService.js')(pubClient, subClient);
var stats = require('./lib/stats/stats.js')(pubClient);
var uidStore = require('./lib/redis/uidStore.js')(pubClient);
var ttlService = require('./lib/service/ttlService.js')(pubClient);
var notificationService = require('./lib/service/notificationService.js')(config.apns, pubClient, ttlService);

require('./lib/server/proxyServer.js')(io, stats, packetService, notificationService, uidStore, ttlService);

// push
var restApi = require('./lib/api/restApi.js')(io, stats, notificationService, apiPort, uidStore, ttlService);