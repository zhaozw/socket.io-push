module.exports = Stats;

function Stats(){
 if (!(this instanceof Stats)) return new Stats();
 this.sessionCount = 0;
}

Stats.prototype.addSession = function(count) {
    if(!count) {
       count = 1;
    }
    this.sessionCount += count;
};