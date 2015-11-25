##install & run

* install nodejs

```
sudo apt-get update
curl -sL https://deb.nodesource.com/setup | sudo bash -
sudo apt-get install -y nodejs
#sudo apt-get install -y npm
sudo npm install -g n
sudo n stable
```

* install node modules

```
./npmInstall.sh
```

* run

```
node .
```

##HTTP API

* 应用内透传 Push, 根据客户端生成的pushId,推送给一个或多个客户端

```
//推送给abc,def两个客户端.透传数据为字符串hello world (base64 aGVsbG8gd29ybGQ),到topic=/topic/test

http://183.61.6.33/api/push?pushId=abc&pushId=def&data=aGVsbG8gd29ybGQ&topic=/topic/test

data -> base64编码的二进制数据

topic -> 客户端订阅的topic, (subscribe,subscribeBroadcast皆可收到)

pushId -> 客户端生成的随机ID

```

* 应用内透传 Push, 根据客户端生成的pushId,推送给所有订阅的客户端

```
//推送给abc,def两个客户端.透传数据为字符串hello world (base64 aGVsbG8gd29ybGQ),到topic=/topic/test

http://183.61.6.33/api/push?pushAll=true&data=aGVsbG8gd29ybGQ&topic=/topic/test

data -> base64编码的二进制数据

topic -> 客户端订阅的topic, (subscribeBroadcast的才能收到)

pushAll -> true

```

* 状态栏通知api 单个推送

http://183.61.6.33/api/notification?pushId=abc&notification=%7B%20%22android%22%3A%7B%22title%22%3A%22title%22%2C%22message%22%3A%22message%22%7D%2C%22apn%22%3A%7B%22alert%22%3A%22message%22%20%2C%20%22badge%22%3A5%2C%20%22sound%22%3A%22default%22%2C%20%22payload%22%3A1234%7D%7D

pushId -> 客户端生成的随机ID

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


* 状态栏通知api 全网推送

http://183.61.6.33/api/notification?pushId=true&notification=%7B%20%22android%22%3A%7B%22title%22%3A%22title%22%2C%22message%22%3A%22message%22%7D%2C%22apn%22%3A%7B%22alert%22%3A%22message%22%20%2C%20%22badge%22%3A5%2C%20%22sound%22%3A%22default%22%2C%20%22payload%22%3A1234%7D%7D

pushAll -> true

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