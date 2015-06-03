![上传图片](http://image.game.yy.com/o/cloudapp/25555152/170x170/201505-e677512b_c1d2_42b4_a9bb_03667c60783e.png)# 移动长连接

----
## 概要

> | **项**   | **详细**               |
> | ---      | ---                    |
> | 联系人   | 许铎，江成彦,李登科,林华锋,连兴伦       |
> | 状态     | 开发中 |

针对使用http短链接协议开发的app,加入LBS长连接代理层

###作用

1. 业务服务器和app, 使用短链接接口, 即可长连接功能

2. 性能提升,自动测速连接最快的LBS服务器

3. SDK集成,协议序列化和反序列化

4. 可选接入注册,登录,验证服务,每个app帐号独立,无需重复开发

5. 可选海度性能上报, 每个请求uri分类统计, 耗时和成功率统计


###主要包括

1. 移动sdk

2. 应用服务器sdk

3. LBS长连接代理服务器

  - 转发客户端的http请求到应用服务器
  - 提供http推送接口给应用服务器调用

4. 登录验证服务

  - 每个应用独立的帐号系统,登录注册过程对应用服务器透明
  - 对应用服务器透明,用户登录以后,LBS转发的请求会带上uid参数



## 基本功能



### maven 依赖

```
<dependency>
            <groupId>com.yy</groupId>
            <artifactId>misaka-android</artifactId>
            <version>1.0.21</version>
            <type>aar</type>
</dependency>
```
### 初始化

```
        misaka = new MisakaClient(context, lbsHost, new Config().dataAsBody(true)); // dataAsBody true表示整个对象序列化为json,post到业务服务器,否则使用nyy协议
        misaka.addRoute("demo-server", demoHost);      // appId,业务服务器地址
        misaka.addRoute("login", "http://uaas.yy.com"); // 使用登录服务
        misaka.addConnectionCallback(new Callback() {
            @Override
            public void onConnected() {
                im.onConnected();
            }
        });
```


### 1. 请求代理 (例:发送消息给另一个用户uid:12345)

客户端代码(android)
```
 Message message = new Message();
 message.setContent("hello!");
 message.setToUid(12345);
 
 misaka.request("demo-server", "/sendMessage", message, new ReplyHandler<Message>(Message.class) {

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
  misaka..subscribeUserPush("demo-server", "/message", new StompClient.SubscribeHandler<Message>(Message.class) {
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
 misaka..subscribeBroadcast("demo-server", "/userList",

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

### 4. 使用性能统计

```
     HiidoProfiling profiling = new HiidoProfiling("app_id");
     misaka.setProfiling(profiling);
```

### 5. 使用登录功能

```
//初始化
Login login = new Login(context, misaka, "app_id");

//登录
LoginRequest user = new LoginRequest();
final String username = usernameEdit.getText().toString();
final String password = passwordEdit.getText().toString();
user.setPhone(username);
user.setPassword(password);

login.login(user, new ReplyHandler<LoginRequest>(LoginRequest.class) {
     @Override
     public void onSuccess(LoginRequest result) {
         Log.i(TAG, "onSuccess " + result);      
     }
                 
     @Override
     public void onError(int code, String message) {
         Toast.makeText(LoginActivity.this, "request error!code:" + code + " ,message:" + message, Toast.LENGTH_LONG).show();
      }
});

//获取注册验证码
login.getRegisterSmsCode(phone, "86", new ReplyHandler<RegisterRequest>(RegisterRequest.class) {
   @Override
   public void onSuccess(RegisterRequest result) {
       Toast.makeText(RegisterActivity.this, "get register code success ", Toast.LENGTH_LONG).show();
   }

    @Override
    public void onError(int code, String message) {
        Toast.makeText(RegisterActivity.this, "get register code error " + message, Toast.LENGTH_LONG).show();
    }
});

//设置密码
login.register(phone, password, code, new ReplyHandler<RegisterRequest>(RegisterRequest.class) {
    @Override
    public void onSuccess(RegisterRequest result) {
        Toast.makeText(RegisterActivity.this, "setPassword success ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(int code, String message) {
        Toast.makeText(RegisterActivity.this, "setPassword error " + message, Toast.LENGTH_LONG).show();
    }
});

```

### 5. 使用LBS测速功能

config中设置hostResolver
```

config.hostResolver(new YYHostResolver(context));

```

## 项目运行和编译

项目使用gradle进行构建(除了IOS)部分
1. demo-android   安卓demo
2. demo-ios ios   demo
3. misaka-android   android sdk
4. misaka-ios_oc  ios oc sdk
5. lbs-server   lbs服务器
6. demo-server   服务器demo

### 1.运行lbs-server和demo服务器
demo-server Application.java 配置链接的lbs服务器地址
```
@Bean
    public BroadcastService broadcastService() {
        BroadcastService broadcastService = new BroadcastService();
        broadcastService.setHost("http://dev.yypm.com:8080");
        return broadcastService;
    }
```

./gradlew demo-server:bootRun
./gradlew lbs-server:bootRun
lbs-sever也可以使用我们的测试服务器
内网 dev.yypm.com:8080
demo-server可以使用我们的测试服务器
内网 mlbs.yypm.com:8091

### 2.运行demo-android

AppModel.java配置

```
    private String lbsHost = "http://" + "dev.yypm.com:8080"; //lbs服务器地址
    private String demoHost = "http://" + "dev.yypm.com:8091"; //demo服务器地址
    
```
./gradlew demo-android:build 
命令打包apk或者ide导入gradle项目直接运行

