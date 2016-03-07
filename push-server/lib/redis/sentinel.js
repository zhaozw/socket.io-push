module.exports = Sentinel;

var redis = require('redis');
var debug = require('debug')('Sentinel');

function Sentinel(sentinelAddrs, masterNames, completeCallback, masterChangeCallback) {
    if (!(this instanceof Sentinel)) return new Sentinel(sentinelAddrs, masterNames, completeCallback, masterChangeCallback);
    var masters = [];
    this.masters = masters;
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
            console.log("sentinel connect Error %s:%s %s", addr.host, addr.port, err);
        });

        masterNames.forEach(function (masterName, i) {
            masters[i] = {
                name: masterName
            };
            client.send_command("SENTINEL", ['get-master-addr-by-name', masterName], function (err, replies) {
                var allQueried = true;
                masters.forEach(function (master) {
                    if (master.name == masterName) {
                        master.host = replies[0].toString();
                        master.port = parseInt(replies[1].toString());
                    } else if (!masters.host) {
                        allQueried = false;
                    }
                });
                if (allQueried) {
                    debug("masters all queried %j", masters)
                    completeCallback();
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
            console.log("sentinel subscribe Error %s:%s %s", addr.host, addr.port, err);
        });

        subClient.on("message", function (channel, message) {
            if (channel == "+switch-master") {
                var lines = message.toString().split(" ");
                var name = lines[0];
                var host = lines[3].toString();
                var port = parseInt(lines[4].toString());
                debug("+switch-master %j %j \n", lines, masters, name, host, port);
                masters.forEach(function (master, i) {
                    if (master && master.name == name) {
                        master.host = host;
                        master.port = port;
                        debug("switch master callback %j", master)
                        masterChangeCallback(master, i);
                        return;
                    }
                });
            }
        });
        subClient.subscribe("+switch-master");

    });

}