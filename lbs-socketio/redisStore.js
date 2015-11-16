module.exports = RedisStore;

var debug = require('debug')('RedisStore');

var apn = require('apn');

var options = { production:false };

var apnConnection = new apn.Connection(options);

function RedisStore(redis){
    if (!(this instanceof RedisStore)) return new RedisStore(redis);
    this.redis = redis;
}

RedisStore.prototype.publishPacket = function(data) {
    this.redis.publish("packetProxy" , data);
};

RedisStore.prototype.setApnToken = function(pushId,apnToken) {
    if(pushId && apnToken){
       this.redis.set("apnToken#" + pushId, apnToken);
       //  this.redis.expire("apnToken#" + pushId, 3600 * 24 * 7);
       this.redis.hset("apnTokens", apnToken , "");
    }
};

RedisStore.prototype.sendNotification = function(pushId, notification) {
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



