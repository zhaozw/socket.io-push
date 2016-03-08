module.exports = SimpleRedisHashCluster;

var commands = require('redis-commands');
var redis = require('redis');
var util = require("../util/util.js");
var debug = require('debug')('SimpleRedisHashCluster');

function SimpleRedisHashCluster(config, completeCallback) {
    this.masters = [];
    this.slaves = [];
    this.messageCallbacks = [];
    var outerThis = this;
    var masterAddrs = config.masters;
    var slaveAddrs = config.slaves;
    if (!slaveAddrs) {
        slaveAddrs = masterAddrs;
    }
    slaveAddrs.forEach(function (addr) {
        var client = redis.createClient({
            host: addr.host,
            port: addr.port,
            return_buffers: true,
            retry_max_delay: 3000,
            max_attempts: 0,
            connect_timeout: 10000000000000000
        });
        client.on("error", function (err) {
            console.log("redis slave connect Error %s:%s %s", addr.host, addr.port, err);
        });
        client.on("message", function (channel, message) {
            debug("on slave message %s %s %s", channel, message, client.port);
            outerThis.messageCallbacks.forEach(function (callback) {
                callback(channel, message);
            });
        });
        outerThis.slaves.push(client);
    });

    var defaultPubAddr = util.getByHash(masterAddrs, "packetProxy#default");
    console.log("packetProxy#default " + defaultPubAddr.host + ":" + defaultPubAddr.port);

    if (config.sentinels) {
        debug('use sentinels %j', config.sentinels);
        var sentinel = require('./sentinel.js')(config.sentinels, config.sentinelMasters, function () {
            sentinel.masters.forEach(function (addr) {
                var client = redis.createClient({
                    host: addr.host,
                    port: addr.port,
                    return_buffers: true,
                    retry_max_delay: 3000,
                    max_attempts: 0,
                    connect_timeout: 10000000000000000
                });
                client.on("error", function (err) {
                    console.log("redis master connect Error %s", err);
                });
                outerThis.masters.push(client);
            });
            completeCallback(outerThis);
        }, function (newMaster, i) {
            var master = outerThis.masters[i];
            debug('current master %j', master.connection_options);
            if (master.connection_options.port != newMaster.port || master.connection_options.host != newMaster.host) {
                debug("switch master %j", newMaster);
                master.connection_options.port = newMaster.port;
                master.connection_options.host = newMaster.host;
            }
        });
    } else {
        debug('use masters %j', masterAddrs);
        masterAddrs.forEach(function (addr) {
            var client = redis.createClient({
                host: addr.host,
                port: addr.port,
                return_buffers: true,
                retry_max_delay: 3000,
                max_attempts: 0,
                connect_timeout: 10000000000000000
            });
            client.on("error", function (err) {
                console.log("redis master %s", err);
            });
            outerThis.masters.push(client);
        });
        completeCallback(outerThis);
    }
}

commands.list.forEach(function (command) {

    SimpleRedisHashCluster.prototype[command.toUpperCase()] = SimpleRedisHashCluster.prototype[command] = function (key, arg, callback) {
        if (Array.isArray(key)) {
            console.log("multiple key not supported ");
            throw "multiple key not supported";
        }
        var client;
        if (this.masters.length == 1) {
            client = this.masters[0];
        } else {
            client = util.getByHash(this.masters, key);
        }

        handleCommand(command, arguments, key, arg, callback, client);
    }

});

['subscribe'].forEach(function (command) {

    SimpleRedisHashCluster.prototype[command.toUpperCase()] = SimpleRedisHashCluster.prototype[command] = function (key, arg, callback) {
        if (Array.isArray(key)) {
            console.log("multiple key not supported ");
            throw "multiple key not supported";
        }
        var client;
        if (this.slaves.length == 1) {
            client = this.slaves[0];
        } else {
            client = util.getByHash(this.slaves, key);
        }

        handleCommand(command, arguments, key, arg, callback, client);
    }

});

function handleCommand(command, callArguments, key, arg, callback, client) {

    if (Array.isArray(arg)) {
        arg = [key].concat(arg);
        return client.send_command(command, arg, callback);
    }
    // Speed up the common case
    var len = callArguments.length;
    if (len === 2) {
        return client.send_command(command, [key, arg]);
    }
    if (len === 3) {
        return client.send_command(command, [key, arg, callback]);
    }
    return client.send_command(command, toArray(callArguments));
}

SimpleRedisHashCluster.prototype.on = function (message, callback) {
    if (message === "message") {
        debug("add messageCallbacks %s", callback);
        this.messageCallbacks.push(callback);
    } else {
        var err = "on " + message + " not supported";
        console.log(err);
        throw err;
    }
}


SimpleRedisHashCluster.prototype.status = function () {
    var masterError = 0;
    this.masters.forEach(function (master) {
        !master.ready && masterError++;
    });
    var slaveError = 0;
    this.slaves.forEach(function (slave) {
        !slave.ready && slaveError++;
    });
    return {masterError: masterError, slaveError: slaveError};
}


function toArray(args) {
    var len = args.length,
        arr = new Array(len), i;

    for (i = 0; i < len; i += 1) {
        arr[i] = args[i];
    }

    return arr;
}