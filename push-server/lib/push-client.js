module.exports = PushClient;
var randomstring = require("randomstring");
var EventEmitter = require('events').EventEmitter;

function PushClient(url, opt) {
    if (!(this instanceof PushClient)) return new PushClient(url, opt);
    this.socket = require('socket.io-client')(url, opt);
    this.pushId = randomstring.generate(24);
    this.event = new EventEmitter();
    this.socket.on('connect', function () {
        this.socket.emit('pushId', {id: this.pushId, version: 1, platform: "android", topics: ['message', "noti"]});
    }.bind(this));

    this.socket.on('push', function (data) {
        console.log("push success data: " + new Buffer(data.data, 'base64').toString());
        this.event.emit(data.topic, data.topic, JSON.parse(new Buffer(data.data, 'base64').toString()));
    }.bind(this));

    this.socket.on('noti', function (data) {
        console.log("      notification success data: " + JSON.stringify(data));
        this.event.emit("notification", data);
    }.bind(this))
}

PushClient.prototype.subscriptTopic = function (topic) {
    this.socket.emit('subscriptTopic', {topic: topic});
};


PushClient.prototype.unsubscribeTopic = function (topic) {
    this.socket.emit("unsubscribeTopic", {topic: topic});
}



