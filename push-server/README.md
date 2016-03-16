Push-Server
=======================
对外服务

##install & run

* 安装/更新
sudo npm install -g socket.io-push

* 新建工作目录

mkdir push-server    
cd push-server

* 新建config.js

```
var config = {};

config.pingTimeout = 25000;
config.pingInterval = 25000;
config.apns = [];

config.redis = {
    masters: [
        {
            host: "127.0.0.1",
            port: 6379
        }
    ]
};

config.io_port = 10001;
config.api_port = 11001;

module.exports = config;
```

#运行
push-server -v -f    
-v verbose   
-f foreground   
-d debug     
-c 起的进程数

#后台地址
http://yourip:10001/

#websocket地址
http://yourip:11001/

##Nginx reverse proxy

nginx.conf

```
upstream ws_backend {
    ip_hash;
    server 127.0.0.1:11001;
    server 127.0.0.1:11002;
    server 127.0.0.1:11003;
}

upstream ws_api {
    ip_hash;
    server 127.0.0.1:12001;
    server 127.0.0.1:12002;
    server 127.0.0.1:12003;
}

server
{
    listen 80;

    location / {
        proxy_pass http://ws_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Host $host;
        proxy_set_header Connection "upgrade";
    }
    
    location /api {
        proxy_pass ws_api;
    }
}
```

##HTTP API

string[]类型,表示http协议中list类型参数，如 get?uid=123&uid=456 ,表示一个uid数组 [123, 456]. get?uid=123 表示单个uid数组 [123]

### /api/push 应用内透传

//推送给abc,def两个客户端.透传数据为字符串hello world (base64 aGVsbG8gd29ybGQ),到topic=/topic/test

http://yourip:11001/api/push?pushAll=true&data=aGVsbG8gd29ybGQ&topic=/topic/test

pushAll -> string, true表示推送全网,其它或者留空表示单个推送

pushId -> string[], 客户端生成的随机ID,单个或者数组

uid -> string[] 通过addPushIdToUid接口绑定的uid

--- 以上参数3选一,指定推送对象

data -> string, base64编码的二进制数据

topic -> string, 客户端订阅的topic, (subscribeBroadcast的才能收到)


### /api/notification 状态栏通知api

http://yourip:11001/api/notification?pushId=true&notification=%7B%20%22android%22%3A%7B%22title%22%3A%22title%22%2C%22message%22%3A%22message%22%7D%2C%22apn%22%3A%7B%22alert%22%3A%22message%22%20%2C%20%22badge%22%3A5%2C%20%22sound%22%3A%22default%22%2C%20%22payload%22%3A1234%7D%7D

pushAll -> string, true表示推送全网,其它或者留空表示单个推送

pushId -> string[], 客户端生成的随机ID,单个或者数组

uid -> string[], 通过addPushIdToUid接口绑定的uid

--- 以上参数3选一,指定推送对象

notification -> 通知消息内容 需要url encode

```
{
  "android" : {"title":"title","message":"message" , "payload" : {"abc":123} },
  "apn":  {"alert":"message" , "badge":5, "sound":"default", "payload":{"abc":123} }
}
```

notification是一个json map,内容说明如下

android - 推送给安卓手机的通知内容

apn - 通过apns推送给ios手机的通知内容

title & message - 安卓通知栏的消息标题和内容

alert(ios) - (apn对应的alert字段)消息内容

badge(ios) - (apn对应的badge字段) 可选

sound(ios) - (apn对应的sound字段) 可选

payload - 发送给应用非显示用的透传信息, 需要是一个json map


### /api/addPushIdToUid 绑定UID和pushId

http://yourip:11001/api/addPushIdToUid?pushId=abc&uid=123

pushId -> string,客户端生成的随机ID

uid -> string,服务器需要绑定的UID
