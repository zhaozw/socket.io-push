var port = process.env.LBS_PORT || 9101;
port = parseInt(port)

console.log("start server on port " + port);
var io = require('socket.io')(port);
var redis = require('socket.io-redis');
var stats = require('./stats')();

io.adapter(redis({ host: 'localhost', port: 6379 }));

var proxyServer = require('./proxyServer.js')(io,stats);

// push
var restApi = require('./restApi.js')(io, stats, port + 1);
