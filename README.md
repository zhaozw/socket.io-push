# 移动长连接

----
## 概要

> | **项**   | **详细**               |
> | ---      | ---                    |
> | 联系人   | 许铎，江成彦,李登科,林华锋         |
> | 状态     | 开发中 |

类似于YY体系的service平台,专门为简化移动开发打造的平台.

主要包括

1. 移动sdk

2. 应用服务器sdk

3. LBS长连接服务器

  - 转发客户端的http请求到应用服务器
  - 提供http推送接口给应用服务器调用

4. 登录验证服务

  - 每个应用独立的帐号系统,登录注册过程对应用服务器透明
  - 对应用服务器透明,用户登录以后,LBS转发的请求会带上uid参数

<br/>
<img class="img-responsive" src="posts/misaka/structure.png" alt= "struct" />
<br/>

## 基本功能

### 1. 请求代理 (例:发送消息给另一个用户uid:12345)

客户端代码(android)
```
 Message message = new Message();
 message.setContent("hello!");
 message.setToUid(12345);
 
 AppModel.INSTANCE.getStomp().request("demo-server", "/sendMessage", message, new ReplyHandler<Message>(Message.class) {

                    @Override
                    public void onSuccess(Message result) {
                        Log.i(TAG, "onSuccess " + message.getContent());
                    }

                    @Override
                    public void onError(int code, String message) {
                        Toast.makeText(DemoActivity.this, "request error!code:" + code + " ,message:" + message, Toast.LENGTH_LONG).show();
                    }
                });
```

应用服务器代码

```
@RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public
    @ResponseBody
    Message sendMessage(@RequestBody User user, @RequestHeader(required = false, defaultValue = "0") Long uid) throws IOException {
        logger.info("data {}, appId {}", data, appId);
        Message message = mapper.readValue(data, Message.class);
        messageService.sendMessage(message);
        return message;
    }
```

### 2. uid单播

客户端代码(android)

```
 stomp.subscribeUserPush("demo-server", "/message", new StompClient.SubscribeHandler<Message>(Message.class) {
            @Override
            public void onSuccess(Message result) {
                NotificationCenter.INSTANCE.getObserver(ImCallback.Message.class).onMessageReceived(result);
            }
        });
```

应用服务器代码

```
broadcastService.pushToUser(message.getToUid(), "demo-server", "/message", message);
```

### 3. 广播 (例:订阅用户在线列表更新)

客户端代码(android)

```
stomp.subscribeBroadcast("demo-server", "/userList",

    new SubscribeHandler<List<User>>(new TypeToken<List<User>>() {}.getType()) {

        @Override
        public void onSuccess(List<User> result) {
            Logger.info(this, "onUserList, size: %d", result == null ? 0 : result.size());
            if (result != null) {
                  users = result;
                  NotificationCenter.INSTANCE.getObserver(Room.class).onUserList();
            }
        }
    }
);
```

应用服务器代码

```
broadcastService.broadcast("demo-server", "/userList", allUsers());
```