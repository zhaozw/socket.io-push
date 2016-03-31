module.exports = Logger;

var winston = require('winston-levelonly');
var fs = require('fs');

function formatStr(param){
    if(param < 10) {
        param = '0' + param;
    }
    return param;
}

function format(times) {
    var date = new Date(times);
    var Y = date.getFullYear() + '-';
    var M = formatStr(date.getMonth() + 1) + '-';//January is 0!
    var D = formatStr(date.getDate()) + ' ' ;
    var h = formatStr(date.getHours()) + ":";
    var m = formatStr(date.getMinutes()) + ":" ;
    var s = formatStr(date.getSeconds());
    return Y + M + D + h + m + s;
}

function formatDate(times){
    var date = new Date(times);
    var Y = date.getFullYear() + '-';
    var M = formatStr(date.getMonth() + 1) + '-';
    var D = formatStr(date.getDate()) ;
    return Y + M + D;
}


function Logger(dir) {
    if (!(this instanceof Logger)) return new Logger(dir);
    this.logger = this.newInstance(dir);
    var oldLog = this.logger.log;
    var ourterThis = this;
    this.logger.log = function (level) {
        if(ourterThis.fileName != formatDate(Date.now())){
            ourterThis.changeLogFile(dir);
        }
        //add property index 1
        var args = Array.prototype.slice.call(arguments);
        //args.splice(1, 0, "instance: " + instance);
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
            return format(Date.now());
        }
    }
    opts.level = level;

    this.fileName = formatDate(Date.now());

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
                levelOnly: false//if true, will only log the specified level, if false will log from the specified level and above
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