module.exports = SimpleRedisHashCluster;

var commands = require('redis-commands');
var redis = require('redis');
var util = require("../util/util.js");
var debug = require('debug')('SimpleRedisHashCluster');

function SimpleRedisHashCluster(addrs) {
    if (!(this instanceof SimpleRedisHashCluster)) return new SimpleRedisHashCluster(addrs);
    this.clients = [];
    this.messageCallbacks = [];
    var outerThis = this;
    addrs.forEach(function (addr) {
        var client = redis.createClient({host: addr.host, port: addr.port});
        client.on("message", function (channel, message) {
            console.log("client1 channel " + channel + ": " + message);
            outerThis.messageCallbacks.forEach(function (callback) {
                callback(channel, message);
            });
        });
        outerThis.clients.push(client);
    });
    var defaultPubAddr = util.getByHash(addrs, "packetProxy#default");
    console.log("packetProxy#default " + defaultPubAddr.host + ":" + defaultPubAddr.port);
}

commands.list.forEach(function (command) {

    SimpleRedisHashCluster.prototype[command.toUpperCase()] = SimpleRedisHashCluster.prototype[command] = function (key, arg, callback) {
        if (Array.isArray(key)) {
            debug("multiple key not supported ");
            console.log("multiple key not supported ");
            throw "multiple key not supported";
        }
        var client;
        if (this.clients.length == 1) {
            client = this.clients[0];
        } else {
            client = util.getByHash(this.clients, key);
        }

        if (Array.isArray(arg)) {
            arg = [key].concat(arg);
            return client.send_command(command, arg, callback);
        }
        // Speed up the common case
        var len = arguments.length;
        if (len === 2) {
            return client.send_command(command, [key, arg]);
        }
        if (len === 3) {
            return client.send_command(command, [key, arg, callback]);
        }
        return client.send_command(command, toArray(arguments));
    }

});

SimpleRedisHashCluster.prototype.on = function (message, callback) {
    if (message === "message") {
        this.messageCallbacks.push[callback];
    } else {
        var err = "on " + message + " not supported";
        debug(err);
        console.log(err);
        throw err;
    }
}

function toArray(args) {
    var len = args.length,
        arr = new Array(len), i;

    for (i = 0; i < len; i += 1) {
        arr[i] = args[i];
    }

    return arr;
}