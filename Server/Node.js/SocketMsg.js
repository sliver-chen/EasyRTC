module.exports = function(socketio, agents) {

    socketio.on('connection', function(client) {
        console.log(('client:' + client.id + 'connet to server').red);

        client.on('get_id', get);

        client.on('request_user_list', request_user_list);

        client.on('event', turn);

        client.on('candidate', candidate);

        client.on('disconnect', leave);

        client.on('ack', ack);

        client.on('heart', heart);

        function get(user) {
            console.log(('register:' + client.id + ' name : ' + user.name + ' type:' + user.type).red);
            agents.addUserAgent(client.id, user.name, user.type);
            //agents.echoUserAgents();

            var msg = {
                "source" : "EasyRTC Server",
                "target" : client.id,
                "type" : "id",
                "value" : client.id
            };
            client.emit('set_id', msg);
            update();
        }
    
        function request_user_list() {
            var msg = {
                "source" : "EasyRTC Server",
                "target" : client.id,
                "type" : "agent",
                "cnt" : agents.getUserAgents().length,
            };

            for (var i = 0; i < agents.getUserAgents().length; i++) {
                msg[i] = {
                    "id" : agents.getUserAgents()[i].id,
                    "name" : agents.getUserAgents()[i].name,
                    "type" : agents.getUserAgents()[i].type,
                };
            }
            client.emit('user_list', msg);
        }

        function leave() {
            console.log(('client:' + client.id + ' disconnect').red);
            agents.removeUserAgent(client.id);
            update();
        }

        function update() {
            var clients = agents.getUserAgents();
            for (var i = 0; i < clients.length; i++) {
                var target = socketio.sockets.connected[clients[i].id];

                var msg = {
                    "source" : "EasyRTC Server",
                    "target" : clients[i].id,
                    "type" : "agent",
                    "cnt" : agents.getUserAgents().length,
                };
    
                for (var j = 0; j < agents.getUserAgents().length; j++) {
                    msg[j] = {
                        "id" : agents.getUserAgents()[j].id,
                        "name" : agents.getUserAgents()[j].name,
                        "type" : agents.getUserAgents()[j].type,
                    };
                }
                target.emit('user_list', msg);
            }
        }

        function turn(msg) {
            var target = socketio.sockets.connected[msg.target];
            console.log(('receive event ' + msg.type + ' from ' + msg.source + ' to ' + msg.target).yellow);
            target.emit('event', msg);
            if (msg.type == 'offer' || msg.type == 'answer') {
                console.log('value:' + msg.value);
            }   
        }

        function candidate(msg) {
            var target = socketio.sockets.connected[msg.target];
            console.log(('receive candidate from ' + msg.source + ' to ' + msg.target).green);
            target.emit('candidate', msg);
        }

        function ack(msg) {
            console.log(('receive ack ' + msg.type + ' from ' + msg.source).green);
        }

        function heart(msg) {
            //console.log(('receive heart beat msg from ' + msg.source));
        }
    });

    return {
        
    }
};
