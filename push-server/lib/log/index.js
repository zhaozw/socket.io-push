module.exports = Logger;

var winston = require('winston-levelonly');
var fs = require('fs');
var loggerSingleton;

function formatStr(param){
    if(param < 10) {
        param = '0' + param;
    }
    return param;
}

function format(times, isDate) {
    var date = new Date(times);
    var Y = date.getFullYear() ;
    var M = formatStr(date.getMonth() + 1);//January is 0!
    var D = formatStr(date.getDate());
    var h = formatStr(date.getHours());
    var m = formatStr(date.getMinutes());
    var s = formatStr(date.getSeconds());
    if(isDate){
        return Y + '-'+ M + '-' + D;
    }else {
        return Y + '-'+ M + '-' + D + ' ' + h + ':' + m + ':' + s;
    }
}

function Logger(dir) {
/*    if(!(loggerSingleton instanceof Logger)){
        loggerSingleton = new Logger(dir);
        return loggerSingleton;
    }*/
    if (!(this instanceof Logger)) return new Logger(dir);
    this.logger = this.newInstance(dir);
    var oldLog = this.logger.log;
    var ourterThis = this;

    this.logger.log = function (level) {
        if(ourterThis.fileName != format(Date.now(), true)){
            ourterThis.changeLogFile(dir);
        }
        var args = Array.prototype.slice.call(arguments);
        args[1] = 'process pid:%d '+ args[1];
        console.log(JSON.stringify(args));
        oldLog.apply(ourterThis.logger, args);
    }
}

Logger.prototype.setOpts = function (dir, level) {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir);
    }
    var opts = {
        json: false,
        timestamp: function () {
            return format(Date.now(), false);
        }
    }
    opts.level = level;

    this.fileName = format(Date.now(), true);

    if (level === 'info') {
        opts.name = "info";
        opts.filename = dir + "/" + this.fileName + "_info.log"
    } else if (level === 'error') {
        opts.filename = dir + "/" + this.fileName + "_error.log"
        opts.name = "error";
    }
    return opts;
};

Logger.prototype.newInstance = function (dir) {
    var errorOpts = this.setOpts(dir, 'error');
    var infoOpts = this.setOpts( dir, 'info');

    var logger = new (winston.Logger)({
        transports: [
            new (winston.transports.Console)({
                level:'debug',
                levelOnly: true//if true, will only log the specified level, if false will log from the specified level and above
            }),
            new (winston.transports.File)(errorOpts),
            new (winston.transports.File)(infoOpts)
        ]
    });
    return logger;
};

Logger.prototype.changeLogFile = function(dir){
    this.logger.remove("info");
    this.logger.remove("error");
    var errorOpts = this.setOpts(dir, 'error');
    var infoOpts = this.setOpts(dir, 'info');
    this.logger.add(winston.transports.File, errorOpts);
    this.logger.add(winston.transports.File, infoOpts);
};
