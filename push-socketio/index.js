var io = require('socket.io-emitter')({ host: '127.0.0.1', port: 6379 });


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
  var pushId = req.params.pushId;
  var data = req.params.data;
  console.log('push ' + JSON.stringify(req.params));
  io.to(pushId).emit('push',{topic: topic, data :data});
  res.send({success:true});
  return next();
};

server.get('/api/push', handlePush);
server.post('/api/push', handlePush);

server.listen(9080, function () {
  console.log('%s listening at %s', server.name, server.url);
});
