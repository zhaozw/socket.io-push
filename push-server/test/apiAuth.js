var request = require('superagent');
var config = require('../config.js');
var apiUrl = 'http://localhost:' + config.api_port;
var chai = require('chai');
var expect = chai.expect;

var apiCheck = function (path, req) {
    debug("req %j", req);
    return true;
}

var pushServer = require('../lib/push-server.js')(config);

describe('api auth', function () {

    it('check should pass', function (done) {
        request
            .post(apiUrl + '/api/push')
            .send({
                pushId: '',
                pushAll: 'true',
                topic: 'message',
                data: 'test'
            })
            .set('Accept', 'application/json')
            .end(function (err, res) {
                expect(res.text).to.be.equal('{"code":"success"}');
                done();
            });
    });

    it('check should not pass', function (done) {
        request
            .post(apiUrl + '/api/push')
            .send({
                pushId: '',
                pushAll: 'true',
                topic: 'message',
                data: 'test'
            })
            .set('Accept', 'application/json')
            .end(function (err, res) {
                expect(res.text).to.be.equal('{"code":"error"}');
                done();
            });
    });



});
