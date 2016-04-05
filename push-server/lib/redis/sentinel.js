module.exports = Sentinel;

var redis = require('redis');
var Logger = require('../log/index.js')('Sentinel');

function Sentinel(sentinelAddrs, masterNames, ipMap, completeCallback, masterChangeCallback) {
    var masters = [];
    this.masters = masters;
    this.completeCallback = completeCallback;
    var outerThis = this;
    sentinelAddrs.forEach(function (addr) {
        var client = redis.createClient({
            host: addr.host,
            port: addr.port,
            return_buffers: true,
            retry_max_delay: 3000,
            max_attempts: 0,
            connect_timeout: 10000000000000000
        });
        client.on("error", function (err) {
            Logger.log('error', "sentinel connect Error %s:%s %s", addr.host, addr.port, err);
        });

        masterNames.forEach(function (masterName, i) {
            masters[i] = {
                name: masterName
            };
            client.send_command("SENTINEL", ['get-master-addr-by-name', masterName], function (err, replies) {
                if (replies && outerThis.completeCallback) {
                    Logger.log('info', "get-master-addr-by-name %s %j", masterName, replies);
                    var allQueried = true;
                    masters.forEach(function (master) {
                        if (master.name == masterName) {
                            master.host = getIp(replies[0].toString());
                            master.port = parseInt(replies[1].toString());
                        } else if (!master.host) {
                            allQueried = false;
                        }
                    });
                    if (allQueried) {
                        Logger.log('info', "masters all queried %j", masters);
                        outerThis.completeCallback();
                        outerThis.completeCallback = null;
                    }
                }
            })
        });

        var subClient = redis.createClient({
            host: addr.host,
            port: addr.port,
            return_buffers: true,
            retry_max_delay: 3000,
            max_attempts: 0,
            connect_timeout: 10000000000000000
        });
        subClient.on("error", function (err) {
            Logger.log('error', "sentinel subscribe Error %s:%s %s", addr.host, addr.port, err);
        });

        subClient.on("message", function (channel, message) {
            if (channel == "+switch-master") {
                var lines = message.toString().split(" ");
                var name = lines[0];
                var host = lines[3].toString();
                var port = parseInt(lines[4].toString());
                Logger.log('info', "+switch-master %j %j \n", lines, masters, name, host, port);
                masters.forEach(function (master, i) {
                    if (master && master.name == name) {
                        master.host = getIp(host);
                        master.port = port;
                        Logger.log('info', "switch master callback %j", master);
                        masterChangeCallback(master, i);
                        return;
                    }
                });
            }
        });
        subClient.subscribe("+switch-master");

    });

    function getIp(fromSentinel) {
        if (ipMap && ipMap[fromSentinel]) {
            Logger.log('info', 'getIp %s -> %s', fromSentinel, ipMap[fromSentinel]);
            return ipMap[fromSentinel];
        } else {
            return fromSentinel;
        }
    }

}