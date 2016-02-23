module.exports = {
    batch: function (client, cmd, prefix, keys, callback) {
        if (keys.length > 0) {
            var cmds = [];
            keys.forEach(function (key) {
                cmds.push([cmd, prefix + key]);
            });
            client.batch(cmds).exec(function (err, replies) {
                callback(replies);
            });
        }
    },
};