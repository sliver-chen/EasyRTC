var socketio = require('socket.io').listen(1234)
var agents = require('./UserAgent.js')();
var socketmsg = require('./SocketMsg.js')(socketio, agents);
var colors = require('colors');

console.log('EasyRTC Server listening on port 1234'.red);