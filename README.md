SocketIO-Push
=======================

##功能
* 提供小米/极光PUSH类似功能
* 长连接协议推送(透传)
* 消息栏通知(Android跨进程长连接实现,IOS apns实现)
* 基于长连接的,request/response负载均衡代理(开发中)

##文档
* [服务器](push-server)
* [Android SDK](android-push-sdk)
* [IOS SDK](https://github.com/xuduo/socket.io-push-iossdk) 另一个git repo

##更新日志

####2013-1-25 
    服务器统计优化,每10秒写redis

####2013-1-13 
    添加统计数据(安卓)
    统计方法:
    数据展示为最近7天,图表精度1个小时
    total = 服务端对连接的客户端发送通知栏消息总数
    success = 客户端收到通知栏消息后,发包给服务器,10秒内记成功
    latency = 服务器发通知的时候,加上服务器时间戳,客户端回包加上这个时间戳,计算延迟,10秒外计算为失败
    average latency = sum(latency) / success
    success rate = success/total
    