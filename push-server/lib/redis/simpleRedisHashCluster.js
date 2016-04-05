module.exports = SimpleRedisHashCluster;

var commands = require('redis-commands');
var redis = require('redis');
var util = require("../util/util.js");
var Logger = require('../log/index.js')('SimpleRedisHashCluster');

function SimpleRedisHashCluster(config, completeCallback) {
    this.masters = [];
    this.subSlaves = [];
    this.readSlaves = [];
    this.messageCallbacks = [];
    var outerThis = this;
    var masterAddrs = config.masters;
    var slaveAddrs = config.slaves;
    if (!slaveAddrs) {
        slaveAddrs = masterAddrs;
    }
    slaveAddrs.forEach(function (addr) {
        var subClient = redis.createClient({
            host: addr.host,
            port: addr.port,
            return_buffers: true,
            retry_max_delay: 3000,
            max_attempts: 0,
            connect_timeout: 10000000000000000
        });
        subClient.on("error", function (err) {
            Logger.error("redis slave connect Error %s:%s %s", addr.host, addr.port, err);
        });
        subClient.on("message", function (channel, message) {
            outerThis.messageCallbacks.forEach(function (callback) {
                try {
                    callback(channel, message);
                } catch (err) {
                }
            });
        });
        outerThis.subSlaves.push(subClient)
        var readClient = redis.createClient({
            host: addr.host,
            port: addr.port,
            return_buffers: true,
            retry_max_delay: 3000,
            max_attempts: 0,
            connect_timeout: 10000000000000000
        });
        readClient.on("error", function (err) {
            Logger.error("redis slave connect Error %s:%s %s", addr.host, addr.port, err);
        });
        outerThis.readSlaves.push(readClient);
    });

    if (config.sentinels) {
        Logger.log('info', 'use sentinels %j', config.sentinels)
        var Sentinel = require('./sentinel.js');
        var sentinel = new Sentinel(config.sentinels, config.sentinelMasters, config.ipMap, function () {
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
                    Logger.error("redis master connect Error %s", err);
                });
                outerThis.masters.push(client);
            });
            var defaultPubAddr = util.getByHash(sentinel.masters, "packetProxy#default");
            Logger.log('debug', "packetProxy#default " + defaultPubAddr.host + ":" + defaultPubAddr.port);
            completeCallback(outerThis);
        }, function (newMaster, i) {
            var master = outerThis.masters[i];
            Logger.log('info', 'current master %j', master.connection_options);
            if (master.connection_options.port != newMaster.port || master.connection_options.host != newMaster.host) {
                Logger.log('info', "switch master %j", newMaster);
                master.connection_options.port = newMaster.port;
                master.connection_options.host = newMaster.host;
            }
        });
    } else {
        Logger.info('use masters %s', JSON.stringify(masterAddrs));
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
                Logger.error("redis master %s", err);
            });
            outerThis.masters.push(client);
        });
        var defaultPubAddr = util.getByHash(masterAddrs, "packetProxy#default");
        Logger.log('debug', "packetProxy#default " + defaultPubAddr.host + ":" + defaultPubAddr.port);
        completeCallback(outerThis);
    }
}
commands.list.forEach(function (command) {

    SimpleRedisHashCluster.prototype[command.toUpperCase()] = SimpleRedisHashCluster.prototype[command] = function (key, arg, callback) {
        if (Array.isArray(key)) {
            Logger.log('debug', "multiple key not supported ");
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

['subscribe', 'unsubscribe'].forEach(function (command) {

    SimpleRedisHashCluster.prototype[command.toUpperCase()] = SimpleRedisHashCluster.prototype[command] = function (key, arg, callback) {
        if (Array.isArray(key)) {
            Logger.log('debug', "multiple key not supported ");
            throw "multiple key not supported";
        }
        var client;
        if (this.subSlaves.length == 1) {
            client = this.subSlaves[0];
        } else {
            client = util.getByHash(this.subSlaves, key);
        }
        handleCommand(command, arguments, key, arg, callback, client);
    }

});

['get', 'hkeys', 'hgetall', 'pttl', 'lrange'].forEach(function (command) {

    SimpleRedisHashCluster.prototype[command.toUpperCase()] = SimpleRedisHashCluster.prototype[command] = function (key, arg, callback) {
        if (Array.isArray(key)) {
            Logger.log('debug', "multiple key not supported ");
            throw "multiple key not supported";
        }
        var client;
        if (this.readSlaves.length == 1) {
            client = this.readSlaves[0];
        } else {
            client = util.getByHash(this.readSlaves, key);
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

SimpleRedisHashCluster.prototype.hash = function (key, callback) {
    var client = util.getByHash(this.readSlaves, key);
    callback({host: client.connection_options.host, port: client.connection_options.port});
}


SimpleRedisHashCluster.prototype.on = function (message, callback) {
    if (message === "message") {
        this.messageCallbacks.push(callback);
    } else {
        var err = "on " + message + " not supported";
        Logger.error(error);
        throw err;
    }
}


SimpleRedisHashCluster.prototype.status = function () {
    var masterError = 0;
    this.masters.forEach(function (master) {
        !master.ready && masterError++;
    });
    var slaveError = 0;
    this.subSlaves.forEach(function (slave) {
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