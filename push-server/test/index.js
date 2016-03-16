var request = require('superagent');
var config = require('../config.js');
var pushService = require('../lib/push-server.js')(config);
var apiUrl = 'http://localhost:' + config.api_port;
var pushClient = require('../lib/push-client.js')('http://localhost:' + config.io_port, {transports: ['websocket', 'polling']});

var chai = require('chai')
    , spies = require('chai-spies');

chai.use(spies);
var expect = chai.expect;

describe('长连接Socket IO的测试', function () {

    it('Socket Io  connect', function (done) {
        pushClient.socket.on('pushId', function (data) {
            expect(data.id).to.be.equal(pushClient.pushId);
            done();
        });
    });

    it('Socket IO Push', function (done) {
        var b = new Buffer('{message:"ok"}');
        var data = b.toString('base64');

        var messageCallback = function(data){
            expect(data.topic).to.be.equal('message');
            expect(data.data).to.be.equal('{message:"ok"}');
        }
        var spy = chai.spy(messageCallback);
        pushClient.event.on('message',spy);
        request
            .post(apiUrl + '/api/push')
            .send({
                pushId: '',
                pushAll: 'true',
                topic: 'message',
                data:data
            })
            .set('Accept', 'application/json')
            .end(function (err, res) {
                expect(res.text).to.be.equal('{"code":"success"}');
                expect(spy).to.have.been.called();
                done();
            });
    });

    it('Socket IO Notification', function (done) {
        var title = 'hello',
            message = 'hello world';
        var data = {
            "android": {"title": title, "message": message},
            "apn": {"alert": message, "badge": 5, "sound": "default", "payload": {}}
        }
        var str = JSON.stringify(data);

        var notificationCallback = function(data){
            expect(data.android.title).to.be.equal(title);
            expect(data.android.message).to.be.equal(message);
        }
        var spy = chai.spy(notificationCallback);
        pushClient.event.on('notification',spy);

        //leave topic
        pushClient.unsubscribeTopic("message");

        request
            .post('http://localhost:11001/api/notification')
            .send({
                pushId: '',
                pushAll: 'true',
                uid:'',
                notification:str
            })
            .set('Accept', 'application/json')
            .end(function (err, res) {
                expect(res.text).to.be.equal('{"code":"success"}');
                expect(spy).to.have.been.called();
                done();
            });
    });


    it('Socket IO Push leave Topic', function (done) {
        var b = new Buffer('{message:"ok"}');
        var data = b.toString('base64');

        var messageCallback = function(data){
            expect(data.topic).to.be.equal('message');
            expect(data.data).to.be.equal('{message:"ok"}');
        }
        var spy = chai.spy(messageCallback);
        pushClient.event.on('message',spy);
        request
            .post(apiUrl + '/api/push')
            .send({
                pushId: '',
                pushAll: 'true',
                topic: 'message',
                data:data
            })
            .set('Accept', 'application/json')
            .end(function (err, res) {
                expect(res.text).to.be.equal('{"code":"success"}');
                expect(spy).to.have.been.called();
                done();
            });
    });

});
