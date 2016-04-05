var loggerSingleton;

var winston = require('winston-levelonly');
var fs = require('fs');

var Logger = function Logger(index, dir) {
    console.log("new singleton");
    var dir =  'log';
    var workerId =  1;

    this.getLogger = function (tag, index, logDir) {
        var fileTag = tag;
        if(index){
            workerId = index;
        }
        if(logDir){
            dir = logDir;
            if (!fs.existsSync(dir)) {
                fs.mkdirSync(dir);
            }
            return;
        }
        var opts = {
            name: 'error',
            json: false,
            level: 'error',
            datePattern: 'yyyy-MM-dd_error.log',
            filename: dir + "/" + "log",
            timestamp: function () {
                return new Date().toLocaleString();
            },
            formatter: function (options) {
                return options.timestamp() + " " + options.level.toUpperCase() + ' ' + ' ' + 'instance:' + workerId + ' '
                    + fileTag + ' ' + (undefined !== options.message ? options.message : '');
            }
        };
        var logger = new (winston.Logger)({
            transports: [
                new (winston.transports.Console)({
                    level: 'debug',
                    levelOnly: false,//if true, will only log the specified level, if false will log from the specified level and above
                    timestamp: function () {
                        return new Date().toLocaleString();
                    },
                    formatter: function (options) {
                        return options.timestamp() + " " + options.level.toUpperCase() + ' ' + ' ' + 'instance:' + workerId + ' '
                            + fileTag + ' ' + (undefined !== options.message ? options.message : '');
                    }
                })
            ]
        });
        logger.add(winston.transports.DailyRotateFile, opts);

        opts.name = 'info';
        opts.level = 'info';
        opts.filename = dir + "/" + "log";
        opts.datePattern = 'yyyy-MM-dd_info.log';
        logger.add(winston.transports.DailyRotateFile, opts);
        return logger;
    };
}

Logger.getInstance = function () {
    if (!loggerSingleton) {
        loggerSingleton = new Logger();
    }
    return loggerSingleton;
};

module.exports = Logger.getInstance().getLogger;