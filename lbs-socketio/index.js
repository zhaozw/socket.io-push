var port = process.env.LBS_PORT || 9101;
port = parseInt(port)

console.log("start server on port " + port);
var io = require('socket.io')(port);
var socketIoRedis = require('socket.io-redis');
io.adapter(socketIoRedis({ host: 'localhost', port: 6379 }));

var redis = require("redis")
var redisClient = redis.createClient({ host: 'localhost', port: 6379 });
var redisStore = require('./redisStore.js')(redisClient);
var stats = require('./stats.js')();



var proxyServer = require('./proxyServer.js')(io,stats, redisStore);

// push
var restApi = require('./restApi.js')(io, stats,redisStore, port + 1);
