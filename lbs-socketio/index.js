var stats = require('./stats')();

var port = process.env.LBS_PORT || 9101;
port = parseInt(port)

console.log("start server on port " + port);
var io = require('socket.io')(port);
var redis = require('socket.io-redis');
var http = require('http');
var stats = require('./stats')();
console.log('stats ' + stats);

io.adapter(redis({ host: 'localhost', port: 6379 }));
io.set('heartbeat interval', 30000);
io.set('heartbeat timeout', 10000);

io.on('connection', function (socket) {


//    stats.addSession();
//
//    console.log(stats.sessionCount);

    socket.on('disconnect', function () {
        //stats.removeSession();
    });

    socket.on('pushId', function (data) {
        if(data.id && data.id.length >= 10){
          console.log("on pushId " + JSON.stringify(data));
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

// push
var restify = require('restify');

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


server.get('/api/push', handlePush);
server.post('/api/push', handlePush);

server.get('/api/stats', function(req,res,nex) {
      res.send({connectCounter:connectCounter,sentCounter :sentCounter , receiveCounter:receiveCounter, percent:receiveCounter/sentCounter });
      return next();
});

if(process.env.TIMER === '1')
{
    console.log('start st test timer')
    setInterval(function(){
        io.to("/topic/sttest").emit("push",{topic: "/topic/sttest", data :"aGVsbG93IHdvcmxk",reply:true});
        sentCounter = sentCounter + connectCounter;
    },30000);
}


server.listen(port + 1, function () {
  console.log('%s listening at %s', server.name, server.url);
});
