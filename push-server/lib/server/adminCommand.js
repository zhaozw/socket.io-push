module.exports = AdminCommand;
var Logger = require('../log/index.js')('AdminCommand');

function AdminCommand(redis, stats, packetSevice, proxyServer, apiThrehold) {

    redis.on("message", function (channel, message) {
        if (channel == "adminCommand") {
            var command = JSON.parse(message);
            Logger.log('debug', 'adminCommand %j', command);
            if (command.command == 'packetDropThreshold') {
                Logger.log('debug', 'setting packetDropThreshold %d', stats.packetDropThreshold);
                stats.packetDropThreshold = command.packetDropThreshold;
            } else if (command.command == 'stopPacketService') {
                packetSevice.stopped = true;
            } else if (command.command == 'startPacketService') {
                packetSevice.stopped = false;
            } else if (command.command == 'topicOnline') {
                var online = proxyServer.getTopicOnline(command.topic);
                if (online > 0) {
                    redis.incrby("stats#topicOnline#" + command.topic, online);
                }
            } else if (command.command == 'topicThreshold') {
                apiThrehold.setThreshold(command.topic, command.threshold);
            }
        }
    });
    redis.subscribe("adminCommand");

}