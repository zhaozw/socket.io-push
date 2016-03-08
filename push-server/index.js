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

var simpleRedisHashCluster = require('./lib/redis/simpleRedisHashCluster.js');

new simpleRedisHashCluster(config.redis, function (cluster) {

    var io = require('socket.io')(ioPort, {pingTimeout: config.pingTimeout, pingInterval: config.pingInterval});
    var socketIoRedis = require('socket.io-redis')({cluster: cluster, subClient: cluster});
    io.adapter(socketIoRedis);
    console.log("cluster " + cluster);
    var packetService = require('./lib/service/packetService.js')(cluster, cluster);
    var Stats = require('./lib/stats/stats.js');
    var stats = new Stats(cluster, ioPort);
    var uidStore = require('./lib/redis/uidStore.js')(cluster);
    var ttlService = require('./lib/service/ttlService.js')(cluster);
    var notificationService = require('./lib/service/notificationService.js')(config.apns, cluster, ttlService);

    require('./lib/server/proxyServer.js')(io, stats, packetService, notificationService, uidStore, ttlService);

// push
    var restApi = require('./lib/api/restApi.js')(io, stats, notificationService, apiPort, uidStore, ttlService, cluster);
});

