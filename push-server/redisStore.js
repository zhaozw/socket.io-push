module.exports = RedisStore;
var config = require("./config");
var debug = require('debug')('RedisStore');
var randomstring = require("randomstring");

var apn = require('apn');

var options = config.apn;

var apnConnection = new apn.Connection(options);

var pathToServer = {};


function RedisStore(redis,subClient,directKey){
    if (!(this instanceof RedisStore)) return new RedisStore(redis,subClient,directKey);
    this.redis = redis;
    this.directKey = directKey;
    debug("RedisStore directKey %s",directKey);
    subClient.on("message", function (channel, message) {
        //debug("subscribe message " + channel + ": " + message);
        if(channel === "packetServer" ) {
            var handlerInfo = JSON.parse(message);
            updatePathServer(handlerInfo);
        }
    });
    subClient.subscribe("packetServer");
}

function updatePathServer(handlerInfo){
    var timestamp = new Date().getTime();
    var serverId = handlerInfo.serverId;
    for( path of handlerInfo.paths ){
        var servers = pathToServer[path];
        if(!servers){
            servers = [];
        }
        var updatedServers = [];
        var found = false;
        for(server of servers){
            if(server.serverId === serverId){
                server.timestamp = timestamp;
                updatedServers.push(server);
                found = true;
            } else if(timestamp - server.timestamp > 10000){
                debug("server is dead %s", server.serverId);
            } else {
                updatedServers.push(server);
            }
        }
        if(!found){
            debug("new server is added %s", serverId);
            updatedServers.push({serverId:serverId,timestamp:timestamp});
        }
        pathToServer[path] = updatedServers;
    }
//    debug("updatePathServer %s",JSON.stringify(pathToServer));
}

function hashIndex(pushId,count) {
    var hash = 0;
    if (pushId.length == 0) return hash;
    for (i = 0; i < pushId.length; i++) {
        char = pushId.charCodeAt(i);
        hash = ((hash<<5)-hash)+char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash % count;
}

RedisStore.prototype.publishPacket = function(data) {
    var path = data.path;
    var pushId = data.pushId;
    if(path && pushId) {
        if(!data.sequenceId) {
            data.sequenceId = randomstring.generate(16);
        }
        var servers = pathToServer[path];
        var strData = JSON.stringify(data);
        if(servers){
            var serverCount = servers.length;
            var idx = hashIndex(pushId,serverCount);
            if(pathToServer[path][idx]){
                var serverId = pathToServer[path][idx]["serverId"];
                this.redis.publish("packetProxy#" + serverId , strData);
                debug("publishPacket %s %s", serverId,strData);
                return;
            }
        }
        this.redis.publish("packetProxy#default", strData);
    }
};

RedisStore.prototype.publishDisconnect = function(pushId) {
    debug("publish pushId %s",pushId);
    var data = { pushId:pushId, path:"/socketDisconnect"};
    this.publishPacket(data);
};

RedisStore.prototype.setApnToken = function(pushId,apnToken) {
    if(pushId && apnToken){
       this.redis.set("apnToken#" + pushId, apnToken);
       this.redis.expire("apnToken#" + pushId, 3600 * 24 * 7);
       this.redis.hset("apnTokens", apnToken , "");
    }
};

RedisStore.prototype.sendNotification = function(pushId, notification,io) {
    this.redis.get("apnToken#" + pushId,  function(err, token) {
        // reply is null when the key is missing
        debug("apnToken redis %s", token);
        if(token) {
            var note = toApnNotification(notification);
            apnConnection.pushNotification(note, token);
        } else {
            io.to(pushId).emit('notification', notification);
        }
    });
};



RedisStore.prototype.sendNotificationToAll = function(notification,io) {
    io.to("android").emit('notification', notification);
    this.redis.hkeys("apnTokens", function (err, replies) {
      debug(replies.length + " replies:");
      var note = toApnNotification(notification);
      if(replies.length > 0){
        apnConnection.pushNotification(note, replies);
      }
    });
};

function toApnNotification(notification){
            var note = new apn.Notification();
            note.badge = notification.apn.badge;
            if(notification.apn.sound) {
               note.sound = notification.apn.sound;
            } else {
               note.sound = "default";
            }
            note.alert = notification.apn.alert;
            if(notification.apn.payload) {
                note.payload = notification.apn.payload;
            } else {
                note.payload = {};
            }
            return note;
}



