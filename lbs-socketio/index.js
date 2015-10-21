var http = require('http');
var randomstring = require("randomstring");
var server = http.createServer(handler);
var io = require('socket.io')(8093);
//var port = process.env.PORT || 8080;
//var request = require('request');
//var request = require('sync-request');
//var unirest = require('unirest');

var connectCounter = 0;
var redis = require('socket.io-redis');
io.adapter(redis({ host: 'localhost', port: 6379 }));

function handler (req, response) {
  response.writeHead(200, {"Content-Type": "text/html"});
  response.write("<!DOCTYPE html>");
  response.write("<html>");
  response.write("<head>");
  response.write("<title>Hello World Page</title>");
  response.write("</head>");
  response.write("<body>");
  response.write("connections " + connectCounter);
  response.write("</body>");
  response.write("</html>");
  response.end();
}


server.listen(8083, function () {
    console.log('Server listening at port %d', 8082);
});



io.set('heartbeat interval', 30000);
io.set('heartbeat timeout', 10000);

io.on('connection', function (socket) {

    console.log("connected " + connectCounter++);

    socket.on('pushId', function (data) {
	console.log('pushId ' + data.id);
        if(data.id && data.id.length == 32){
          socket.join(data.id);
          socket.emit('pushId', {id:data.id});
	}
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
//        unirest.post(data.url)
//.send({ "data": 123, "foo": "bar" })
//.end(function (response) {
//   socket.emit('httpProxy', {
//                sequenceId: data.sequenceId,
//                response: "{data='test555'}",
//                responseCode: 1,
//                responseMessage: "success fake"
//            });
//  console.log(" res " +  response.body);
//});
//        var res = request("POST",data.url ,{ body :"data=1234"});
//        console.log('res ' + res.getBody());

//         var json = JSON.parse(data);
  //      console.log('request ' + json);
 /**       console.log('data.url ');
        var param;
        if (json.nyy) {
            param = {url: json.url, form: {data: json.data, appId: json.appId}};
        } else {
            param = {url: json.url, body: json.data};
        }

        var handler = function (error, response, body) {
            console.log(body) // 打印google首页

            var code;
            var message;
            if (response && response.statusCode == 511){
               var result = JSON.parse(body);
               code = result.code;
               message = result.message;
            } else if (error || response.statusCode >= 400) {
                code = -1000;
                message = "服务器繁忙!";
            } else {
                code = 1;
                message = "success"
            }

            if(response){
              var write = response.headers['misaka-write'];
              if(write){
                  var addHeaders = JSON.parse(write);
                  for (var key in addHeaders) { socket.headers[key] = addHeaders[key]; }
                  console.log('write header ' + socket.headers.socketId + ' uid ' + socket.headers.uid + ' write ' + write);
              }
            }
    **/
//            socket.emit('httpProxy', {
//                sequenceId: data.sequenceId,
//                response: "{data='test555'}",
//                responseCode: 1,
//                responseMessage: "success fake"
//            });

  //      }

 /**       request(
            {
                method: 'POST'
                , uri: json.url
                , gzip: true
                , headers: socket.headers
                , body: json.data
            }, handler);
*/
    });


    socket.on('disconnect', function () {
        console.log("disconnect " + connectCounter--);
    });
});

function b64_to_utf8( str ) {
    return decodeURIComponent(escape(window.atob( str )));
}
