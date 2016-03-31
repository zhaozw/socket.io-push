module.exports = PushServer;

function PushServer(config) {
    console.log("config " + JSON.stringify(config));
    var instance = config.instance || 1;
    console.log("starting instance #" + instance);
    var ioPort = config.io_port + instance - 1;
    var apiPort = config.api_port + instance - 1;


    var simpleRedisHashCluster = require('./redis/simpleRedisHashCluster.js');

    new simpleRedisHashCluster(config.redis, function (cluster) {

        var io = require('socket.io')(ioPort, {
            pingTimeout: config.pingTimeout,
            pingInterval: config.pingInterval,
            transports: ['websocket']
        });
        console.log("start server on port " + ioPort);
        var Stats = require('./stats/stats.js');
        var stats = new Stats(cluster, ioPort);
        var socketIoRedis = require('./redis/redisAdapter.js')({pubClient: cluster, subClient: cluster}, null, stats);
        //var socketIoRedis = require('socket.io-redis')({pubClient: cluster, subClient: cluster}, null, stats);
        io.adapter(socketIoRedis);
        var packetService = require('./service/packetService.js')(cluster, cluster);

        var uidStore = require('./redis/uidStore.js')(cluster);
        var TtlService = require('./service/ttlService.js');
        var ttlService = new TtlService(cluster);
        var notificationService = require('./service/notificationService.js')(config.apns, cluster, ttlService);
        var ProxyServer = require('./server/proxyServer.js');
        var proxyServer = new ProxyServer(io, stats, packetService, notificationService, uidStore, ttlService);
        var ApiThreshold = require('./api/apiThreshold.js');
        var apiThreshold = new ApiThreshold(cluster);
        var AdminCommand = require('./server/adminCommand.js');
        var adminCommand = new AdminCommand(cluster, stats, packetService, proxyServer, apiThreshold);

        if (apiPort) {
            var apnService = require('./service/apnService.js')(config.apns, config.apnsSliceServers, cluster, stats);
            notificationService.apnService = apnService;
            var restApi = require('./api/restApi.js')(io, stats, notificationService, apiPort, uidStore, ttlService, cluster, apiThreshold, apnService);
        }
    });
}


