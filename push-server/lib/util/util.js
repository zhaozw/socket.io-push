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
    getByHash: function (array, key) {
        var hash = 0;
        if (key.length == 0) return hash;
        for (var i = 0; i < key.length; i++) {
            var char = key.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        hash = Math.abs(hash);
        return array[hash % array.length];
    }
};