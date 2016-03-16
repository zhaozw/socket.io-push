module.exports = MovingSum;


function MovingSum() {
    this.stamps = [];
}

MovingSum.prototype.push = function (timestamp) {
    this.stamps.push(timestamp);
}

MovingSum.prototype.sum = function (timespans) {
    var starts = [];
    var current = Date.now();
    timespans.forEach(function (span) {
        starts.push(current - span);
    });
    var sum = [];
    var spliceIndex = 0;
    var totalLength = this.stamps.length;
    this.stamps.forEach(function (stamp, stampIndex) {
        starts.forEach(function (start, sumIndex) {
            if (stamp >= start && !sum[sumIndex]) {
                sum[sumIndex] = totalLength - stampIndex;
                if (spliceIndex == 0) {
                    spliceIndex = stampIndex;
                }
            }
        });
    });
    if (spliceIndex > 0) {
        this.stamps = this.stamps.slice(spliceIndex, totalLength);
    }
    return sum;
}