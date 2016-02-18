module.exports = {
    batchGet: function (client, keys, callback) {
        if (keys.length > 0) {
            var cmds = [];
            for (var key of keys) {
                cmds.push("get");
                cmds.push(key);
            }
            client.batch(cmds).exec(function (err, replies) {
                callback(replies);
            });
        }
    },
    batchHget: function (client, keys, callback) {
        if (keys.length > 0) {
            var cmds = [];
            for (var key of keys) {
                cmds.push("hget");
                cmds.push(key);
            }
            client.batch(cmds).exec(function (err, replies) {
                callback(replies);
            });
        }
    }
};