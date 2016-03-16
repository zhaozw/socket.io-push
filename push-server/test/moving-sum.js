var MS = require('../lib/stats/moving-sum.js');

var chai = require('chai')
    , spies = require('chai-spies');
chai.use(spies);
var expect = chai.expect;


describe('test moving-sum', function () {


    it('base test', function (done) {
        var ms = new MS();
        ms.push(Date.now());
        ms.push(Date.now());
        ms.push(Date.now());
        ms.push(Date.now());
        ms.push(Date.now());
        expect(ms.sum([100000])).to.deep.equal([5]);
        done();
    });


    it('less test', function (done) {
        var ms = new MS();
        ms.push(Date.now() - 1000);
        ms.push(Date.now());
        ms.push(Date.now());
        ms.push(Date.now());
        ms.push(Date.now());
        expect(ms.sum([500])).to.deep.equal([4]);
        done();
    });

    it('double test', function (done) {
        var ms = new MS();
        ms.push(Date.now() - 10000);
        ms.push(Date.now() - 5000);
        ms.push(Date.now() - 4000);
        ms.push(Date.now() - 3000);
        ms.push(Date.now() - 2000);
        ms.push(Date.now());
        expect(ms.sum([4500, 6000])).to.deep.equal([4, 5]);
        expect(ms.stamps.length).to.equal(5);
        expect(ms.sum([1500, 3500])).to.deep.equal([1, 3]);
        expect(ms.stamps.length).to.equal(3);
        done();
    });

    it('reverse test', function (done) {
        var ms = new MS();
        ms.push(Date.now() - 10000);
        ms.push(Date.now() - 5000);
        ms.push(Date.now() - 4000);
        ms.push(Date.now() - 3000);
        ms.push(Date.now() - 2000);
        ms.push(Date.now());
        expect(ms.sum([6000, 4500])).to.deep.equal([5, 4]);
        expect(ms.stamps.length).to.equal(5);
        expect(ms.sum([3500, 1500])).to.deep.equal([3, 1]);
        expect(ms.stamps.length).to.equal(3);
        done();
    });


});
