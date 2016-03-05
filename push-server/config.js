var config = {};

config.pingTimeout = 25000;
config.pingInterval = 25000;

config.apns = [
    {
        production: false,
        bundleId: "com.xuduo.pushtest",
        cert: process.cwd() + "/cert/com.xuduo.pushtest/cert.pem",
        key: process.cwd() + "/cert/com.xuduo.pushtest/key.pem"
    },
    {
        production: false,
        bundleId: "com.xuduo.pushtest2",
        cert: process.cwd() + "/cert/com.xuduo.pushtest2/cert.pem",
        key: process.cwd() + "/cert/com.xuduo.pushtest2/key.pem"
    }
];

config.redis = [
    {
        host: "127.0.0.1",
        port: 6379
    }
];

config.redisSlave = [
    {
        host: "127.0.0.1",
        port: 6380
    }
];


config.io_port = 10001;
config.api_port = 11001;


module.exports = config;
