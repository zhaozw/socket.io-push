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

require('./lib/push-server.js')(config);

