module.exports = ProxyServer;

var socketIdToPushId = {};

function ProxyServer(io,stats,redis){
 if (!(this instanceof ProxyServer)) return new ProxyServer(io,stats,redis);
 var http = require('http');
 var debug = require('debug')('ProxyServer');

 io.set('heartbeat interval', 30000);
 io.set('heartbeat timeout', 10000);

 io.on('connection', function (socket) {

     stats.addSession();

     socket.on('disconnect', function () {
         stats.removeSession();
         var pushId = socketIdToPushId[socket.id];
         if(pushId){
             delete socketIdToPushId[socket.id];
             redis.publishDisconnect(pushId,socket.id);
         }
     });

     socket.on('pushId', function (data) {
         if(data.id && data.id.length >= 10){
           debug("on pushId %s" ,JSON.stringify(data));
           var topics = data.topics;
           if(topics && topics.length > 0) {
             topics.forEach(function(topic) {
                  socket.join(topic);
                  debug('join topic ' + topic);
             });
           }
           socketIdToPushId[socket.id] = data.id;
           redis.publishConnect(data.id,socket.id);
           socket.join(data.id);
           socket.emit('pushId', { id:data.id });
           debug('join room socket.id %s ,pushId %s' ,socket.id, socketIdToPushId[socket.id]);
 	    }
     });

      socket.on('subscribeTopic', function (data) {
          debug("on subscribeTopic %s",JSON.stringify(data));
          var topic = data.topic;
          socket.join(topic);
      });


      socket.on('unsubscribeTopic', function (data) {
          debug("on unsubscribeTopic %s",JSON.stringify(data));
          var topic = data.topic;
          socket.leave(topic);
      });

      socket.on('apnToken', function (data) {
          debug("on apnToken %s" , JSON.stringify(data));
          var pushId = data.pushId;
          var apnToken = data.apnToken;
          redis.setApnToken(pushId,apnToken);
      });

      socket.on('packetProxy', function (data) {
          data.pushId = socketIdToPushId[socket.id];
          redis.publishPacket(data);
      });

     socket.on('httpProxy', function (data) {
         debug('body' + JSON.stringify(data));
//         var body = new Buffer(data.body.toString(),"base64").toString('utf-8');
//         debug('httpProxy ' + data.sequenceId + ' path ' + data.path + ' body : ' + body);
//         debug('headers ' + data.headers['X-Authorization']);
//         var post_data = body;
//
//         var options = {
//           host: data.host,
//           port: data.port,
//           path: data.path,
//           method: data.method,
//           headers: data.headers
//         };
//
//         var req = http.request(options, function(res) {
//               res.setEncoding('utf8');
//               var body = "";
//               res.on('data', function (chunk) {
//                   body += chunk;
//               });
//               res.on('end', function() {
//                   socket.emit('httpProxy', {
//                         sequenceId: data.sequenceId,
//                         statusCode: res.statusCode,
//                         response: new Buffer(body).toString('base64'),
//                         headers: res.headers
//                     });
//               });
//         });
//
//         req.on('error', function(e) {
//                 socket.emit('httpProxy', {
//                         sequenceId: data.sequenceId,
//                         errorMessage: e.message,
//                 error: true
//                     });
//         });
//
//         req.setTimeout(5000,function(){
//         });
//         req.write(post_data);
//         req.end();
     });

 });
}




