module.exports = RestApi;

function RestApi(io,stats,redis,port){

 var restify = require('restify');
 var randomstring = require("randomstring");
 var server = restify.createServer({
   name: 'myapp',
   version: '1.0.0'
 });
 server.use(restify.acceptParser(server.acceptable));
 server.use(restify.queryParser());
 server.use(restify.bodyParser());

 var handlePush = function (req, res, next) {
   var topic = req.params.topic;
   if(!topic){
     res.send({code:"error",message:'topic is required'});
     return next();
   }
    var data = req.params.data;
   if(!data){
     res.send({code:"error",message:'data is required'});
     return next();
   }
   var pushId = req.params.pushId;
   var pushAll = req.params.pushAll;
   console.log('push ' + JSON.stringify(req.params));
   var pushData = {topic: topic, data :data};
   if(pushAll === 'true') {
     io.to(topic).emit('push', pushData);
     res.send({code:"success"});
       return next();
   } else if(!pushId){
     res.send({code:"error",message:'pushId is required'});
     return next();
   } else {
     if(typeof pushId === 'string') {
         io.to(pushId).emit('push', pushData);
                res.send({code:"success"});
                return next();
     } else {
         pushId.forEach(function(id){
                 io.to(id).emit('push', pushData);
         });
         res.send({code:"success"});
         return next();
     }
   }
 };

 var handleNotification = function (req, res, next) {
     var notification = JSON.parse(req.params.notification);
    if(!notification){
      res.send({code:"error",message:'notification is required'});
      return next();
    }
    notification.id = randomstring.generate(32);
    var pushId = req.params.pushId;
    var pushAll = req.params.pushAll;
    console.log('notification ' + JSON.stringify(req.params));

    if(pushAll === 'true') {
      io.to(topic).emit('notification', notification);
      res.send({code:"success"});
        return next();
    } else if(!pushId){
      res.send({code:"error",message:'pushId is required'});
      return next();
    } else {
      if(typeof pushId === 'string') {
          sendNotification(pushId,notification);
          res.send({code:"success"});
          return next();
      } else {
          pushId.forEach(function(pushId){
            sendNotification(pushId,notification);
          });
          res.send({code:"success"});
          return next();
      }
    }
  };

 function sendNotification(pushId,notification){
     io.to(pushId).emit('notification', notification);
     redis.sendNotification(pushId, notification);
 }

 var handleStats = function (req, res, next) {
    res.send({sessionCount:stats.sessionCount});
    return next();
 };

 server.get('/api/stats', handleStats);
 server.get('/api/push', handlePush);
 server.post('/api/push', handlePush);
 server.get('/api/notification', handleNotification);
 server.post('/api/notification', handleNotification);

 server.get('/api/stats', function(req,res,nex) {
       res.send({connectCounter:connectCounter,sentCounter :sentCounter , receiveCounter:receiveCounter, percent:receiveCounter/sentCounter });
       return next();
 });

 server.listen(port, function () {
   console.log('%s listening at %s', server.name, server.url);
 });
}

