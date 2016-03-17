module.exports = PushServer;

function PushServer(config) {
    console.log("config " + JSON.stringify(config));
    var instance = config.instance || 1;
    console.log("starting instance #" + instance);
    var ioPort = config.io_port + instance - 1;
    var apiPort = config.api_port + instance - 1;


    var simpleRedisHashCluster = require('./redis/simpleRedisHashCluster.js');

    new simpleRedisHashCluster(config.redis, function (cluster) {

        var io = require('socket.io')(ioPort, {pingTimeout: config.pingTimeout, pingInterval: config.pingInterval});
        console.log("start server on port " + ioPort);
        var socketIoRedis = require('./redis/redisAdapter.js')({pubClient: cluster, subClient: cluster});
        io.adapter(socketIoRedis);
        var packetService = require('./service/packetService.js')(cluster, cluster);
        var Stats = require('./stats/stats.js');
        var stats = new Stats(cluster, ioPort);
        var uidStore = require('./redis/uidStore.js')(cluster);
        var TtlService = require('./service/ttlService.js');
        var ttlService = new TtlService(cluster);
        var notificationService = require('./service/notificationService.js')(config.apns, cluster, ttlService);

        require('./server/proxyServer.js')(io, stats, packetService, notificationService, uidStore, ttlService);

        var restApi = require('./api/restApi.js')(io, stats, notificationService, apiPort, uidStore, ttlService, cluster);
    });
}


