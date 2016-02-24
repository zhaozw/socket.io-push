var config = require("./config");
console.log("config " + JSON.stringify(config));
var instance = process.env.LBS_INSTANCE || "1";
console.log("starting instance #" + instance);
var ioPort = config["io_" + instance].port;
var apiPort = config["api_" + instance].port;
console.log("start server on port " + ioPort);
var io = require('socket.io')(ioPort, {pingTimeout: config.pingTimeout, pingInterval: config.pingInterval});
var socketIoRedis = require('socket.io-redis')({host: config.redis.host, port: config.redis.port});
io.adapter(socketIoRedis);
var redis = require("redis")
var pubClient = redis.createClient({host: config.redis.host, port: config.redis.port});
var subClient = redis.createClient({host: config.redis.host, port: config.redis.port});
var statClient = redis.createClient({host: config.redis.host, port: config.redis.port});
var packetService = require('./lib/service/packetService.js')( pubClient, subClient);
var stats = require('./lib/stats/stats.js')(statClient);
var uidStore = require('./lib/redis/uidStore.js')(statClient);
var ttlService = require('./lib/service/ttlService.js')(statClient);
var notificationService = require('./lib/service/notificationService.js')(config.apns, pubClient, ttlService);

require('./lib/server/proxyServer.js')(io, stats, packetService,notificationService, uidStore, ttlService);

// push
var restApi = require('./lib/api/restApi.js')(io, stats, notificationService,apiPort, uidStore, ttlService);