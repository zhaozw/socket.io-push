var config = {};

config.pingTimeOut = 70000;
config.pingInterval = 25000;

config.apns = [
    {
        production: false,
        bundleId: "com.xuduo.pushtest",
        cert: "cert/com.xuduo.pushtest/cert.pem",
        key: "cert/com.xuduo.pushtest/key.pem"
    },
    {
        production: false,
        bundleId: "com.xuduo.pushtest2",
        cert: "cert/com.xuduo.pushtest2/cert.pem",
        key: "cert/com.xuduo.pushtest2/key.pem"
    }
];

config.redis = [
    {
        host: "127.0.0.1",
        port: 6379
    }
];

//config.redis = [
//    {
//        host: "127.0.0.1",
//        port: 6379
//    },{
//        host: "127.0.0.1",
//        port: 6380
//    },{
//        host: "127.0.0.1",
//        port: 6381
//    },{
//        host: "127.0.0.1",
//        port: 6382
//    },{
//        host: "127.0.0.1",
//        port: 6383
//    },{
//        host: "127.0.0.1",
//        port: 6384
//    }
//];

config.io_1 = {
    port: 9101
};

config.api_1 = {
    port: 9102
};

config.io_2 = {
    port: 9201
};

config.api_2 = {
    port: 9202
};

config.io_3 = {
    port: 9301
};

config.api_3 = {
    port: 9302
};

config.io_4 = {
    port: 9401
};

config.api_4 = {
    port: 9402
};

config.io_5 = {
    port: 9501
};

config.api_5 = {
    port: 9502
};

config.io_6 = {
    port: 9601
};

config.api_6 = {
    port: 9602
};

config.io_7 = {
    port: 9701
};

config.api_7 = {
    port: 9702
};

config.io_8 = {
    port: 9801
};

config.api_8 = {
    port: 9802
};

config.io_9 = {
    port: 9901
};

config.api_9 = {
    port: 9902
};

config.io_10 = {
    port: 10001
};

config.api_10 = {
    port: 10002
};

config.io_11 = {
    port: 10101
};

config.api_11 = {
    port: 10102
};

config.io_12 = {
    port: 10201
};

config.api_12 = {
    port: 10202
};

config.io_13 = {
    port: 10301
};

config.api_13 = {
    port: 10302
};

config.io_14 = {
    port: 10401
};

config.api_14 = {
    port: 10402
};

config.io_15 = {
    port: 10501
};

config.api_15 = {
    port: 10502
};

config.io_16 = {
    port: 10601
};

config.api_16 = {
    port: 10602
};

module.exports = config;
