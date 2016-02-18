module.exports = {
    batch: function (client,cmd, prefix, keys, callback) {
        if (keys.length > 0) {
            var cmds = [];
            console.log(123);
            keys.forEach(function (key) {
                cmds.push([cmd,prefix + key]);
            });
            console.log(cmds);
            client.batch(cmds).exec(function (err, replies) {
                callback(replies);
            });
        }
    },
};