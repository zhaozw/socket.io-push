stomp-app
=========

stomp client for android, server using spring

Library used

https://github.com/koush/AndroidAsync 

https://code.google.com/p/google-gson/ 

https://github.com/SwiftyJSON/SwiftyJSON 

https://github.com/daltoniam/Starscream 

Usage

change http://172.19.99.97:8080/stomp in DemoActivity.java to your running pc ip for the DemoClient to connect to

Client-side  
 
requestInfo-response

    stomp.requestInfo("/login", user, new StompJsonClient.ReplyHandler() {

      @Override
      public void onSuccess(Object result) {
         Toast.makeText(DemoActivity.this, result.toString(), Toast.LENGTH_LONG).show();
      }

     @Override
     public void onError(int code, String message) {
         Toast.makeText(DemoActivity.this, "requestInfo error!code:" + code + " ,message:" + message, Toast.LENGTH_LONG).show();
     }
    });
                
subscribe 

    stomp.subscribe("/topic/time", new StompJsonClient.SubscribeHandler<SubscribeMessage>(SubscribeMessage.class) {
        @Override
        public void onSuccess(SubscribeMessage result) {
           Log.i(TAG, "subscribe broadcast " + destination + " " + result.getMessage());
           ((TextView) findViewById(R.id.tv_subscribe_message)).setText(result.getMessage());
            }
    });
                
Server-side

reply

    @MessageMapping("/login")
    public void login(Message<Object> message, Principal principal, User user) throws Exception {
        logger.info("login username {} password {}", user.getUsername(), user.getPassword());
        if ("tom".equals(user.getUsername()) && "123456".equals(user.getPassword())) {
            StompPrincipal sp = (StompPrincipal) principal;
            sp.setUserId("tom");
            messagingTemplate.replyToUserSuccess(message);
        } else {
            throw new ServiceException(-1, "username  password error!");
        }
    }
    
 broadcast
 
    @Scheduled(fixedDelay = 1000)
    public void broadcastTime() {
        SubscribeMessage message = new SubscribeMessage();
        message.setMessage("server time is " + new Date().toString());
        messagingTemplate.broadcast("/topic/time", message);
    }
