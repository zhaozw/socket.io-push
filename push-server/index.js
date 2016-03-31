var program = require('commander');

program
    .version('0.0.3')
    .usage('[options] <server>')
    .option('-d --debug', 'debug output')
    .option('-c, --count <n>', 'process count to start', parseInt)
    .option('-i, --instance <n>', 'used by start script', parseInt)
    .parse(process.argv);

if (!program.instance) {
    program.instance = 1;
}
var config = require(process.cwd() + "/config");
config.instance = program.instance;

var Logger = require('./lib/log/index.js')('log');

var cluster = require('cluster');
if (cluster.isMaster) {
    for (var i = 0; i<program.count; i++){
        cluster.fork();
    }
    cluster.on('exit', function(worker, code, signal) {
        console.log('Starting a new worker');
        cluster.fork();
    });

    Object.keys(cluster.workers).forEach(function(id) {
        cluster.workers[id].on('message', function(msg){
            Logger.logger.log(msg.level, msg.message, msg.pid, id);
        });
    });
    return;
}
require('./lib/push-server.js')(config);

