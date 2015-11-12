module.exports = RedisStore;

var apn = require('apn');

var options = { production:false };

var apnConnection = new apn.Connection(options);

function RedisStore(redis){
 if (!(this instanceof RedisStore)) return new RedisStore(redis);
 this.redis = redis;
}

RedisStore.prototype.setApnToken = function(pushId,apnToken) {
    if(pushId && apnToken){
       this.redis.set("apnToken#" + pushId, apnToken);
       this.redis.expire("apnToken#" + pushId, 3600 * 24 * 7);
    }
};

RedisStore.prototype.sendNotification = function(pushId, notification) {
    this.redis.get("apnToken#" + pushId,  function(err, token) {
        // reply is null when the key is missing
        console.log("apnToken redis " +  token);
        if(token) {
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


//note.expiry = Math.floor(Date.now() / 1000) + 3600; // Expires 1 hour from now.
//note.badge = 3;
//note.sound = "ping.aiff";
//note.alert = "\uD83D\uDCE7 \u2709 You have a new message";
//note.payload = {'messageFrom': 'Caroline'};
            apnConnection.pushNotification(note, new apn.Device(token));
        }
    });
};