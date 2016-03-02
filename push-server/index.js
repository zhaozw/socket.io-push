var program = require('commander');

program
    .version('0.0.3')
    .usage('[options] <server>')
    .option('-d --debug', 'debug output')
    .option('-c, --count <n>', 'process count to start', parseInt)
    .option('-i, --instance <n>', 'used by start script', parseInt)
    .parse(process.argv);

if (!program.instance) {
    program.instance = 1;
}
var config = require(process.cwd() + "/config");
console.log("config " + JSON.stringify(config));
var instance = program.instance;

console.log("starting instance #" + instance);
var ioPort = config.io_port + instance - 1;
var apiPort = config.api_port + instance - 1;
console.log("start server on port " + ioPort);
var io = require('socket.io')(ioPort, {pingTimeout: config.pingTimeout, pingInterval: config.pingInterval});

var simpleRedisHashCluster = require('./lib/redis/simpleRedisHashCluster.js');

if (!config.redisSlave){
    config.redisSlave = config.redis;
}

var pubClient = simpleRedisHashCluster(config.redis, config.redisSlave);

var socketIoRedis = require('socket.io-redis')({pubClient: pubClient, subClient: pubClient});
io.adapter(socketIoRedis);

var packetService = require('./lib/service/packetService.js')(pubClient, pubClient);
var stats = require('./lib/stats/stats.js')(pubClient, ioPort);
var uidStore = require('./lib/redis/uidStore.js')(pubClient);
var ttlService = require('./lib/service/ttlService.js')(pubClient);
var notificationService = require('./lib/service/notificationService.js')(config.apns, pubClient, ttlService);

require('./lib/server/proxyServer.js')(io, stats, packetService, notificationService, uidStore, ttlService);

// push
var restApi = require('./lib/api/restApi.js')(io, stats, notificationService, apiPort, uidStore, ttlService, pubClient);