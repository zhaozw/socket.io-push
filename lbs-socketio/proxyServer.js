module.exports = ProxyServer;


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
     });

     socket.on('pushId', function (data) {
         if(data.id && data.id.length >= 10){
           debug("on pushId %s" ,JSON.stringify(data));
           var topics = data.topics;
           if(topics && topics.length > 0) {
             topics.forEach(function(topic) {
                  socket.join(topic);
                  console.log('join topic ' + topic);
             });
           }
           socket.join(data.id);
           socket.emit('pushId', { id:data.id });
           console.log('join room ' + data.id);
 	    }
     });

      socket.on('subscribeTopic', function (data) {
          console.log("on subscribeTopic " + JSON.stringify(data));
          var topic = data.topic;
          socket.join(topic);
      });

      socket.on('apnToken', function (data) {
          debug("on apnToken %s" , JSON.stringify(data));
          var pushId = data.pushId;
          var apnToken = data.apnToken;
          redis.setApnToken(pushId,apnToken);
      });

      socket.on('packetProxy', function (data) {
               var stringData =  JSON.stringify(data);
               debug('body %s', stringData);
               var body = new Buffer(data.body.toString(),"base64").toString('utf-8');

               var post_data = body;

               redis.publishPacket(stringData);

           });

     socket.on('httpProxy', function (data) {
         console.log('body' + JSON.stringify(data));
         var body = new Buffer(data.body.toString(),"base64").toString('utf-8');
         console.log('httpProxy ' + data.sequenceId + ' path ' + data.path + ' body : ' + body);
         console.log('headers ' + data.headers['X-Authorization']);
         var post_data = body;

         var options = {
           host: data.host,
           port: data.port,
           path: data.path,
           method: data.method,
           headers: data.headers
         };

         var req = http.request(options, function(res) {
               res.setEncoding('utf8');
               console.log('request res ' +  res.statusCode);
               console.log('HEADERS: ' + JSON.stringify(res.headers));
               var body = "";
               res.on('data', function (chunk) {
                   console.log('Response: ' + chunk);
                   body += chunk;
               });
               res.on('end', function() {
                   console.log('res on end: ' + body);
                   socket.emit('httpProxy', {
                         sequenceId: data.sequenceId,
                         statusCode: res.statusCode,
                         response: new Buffer(body).toString('base64'),
                         headers: res.headers
                     });
               });
         });

         req.on('error', function(e) {
                 console.log('error request: ' + e.message);
                 socket.emit('httpProxy', {
                         sequenceId: data.sequenceId,
                         errorMessage: e.message,
                 error: true
                     });
         });

         req.setTimeout(5000,function(){
             console.log('timeout');
         });
         req.write(post_data);
         req.end();
     });

     socket.on('pushReply', function () {
         receiveCounter++;
     });

 });
}




