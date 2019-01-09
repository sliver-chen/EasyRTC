module.exports = function() {
    var UserAgents = [];

    var UserAgent = function(id, name, type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    return {
        addUserAgent : function(id, name, type) {
            var agent = new UserAgent(id, name, type);
            UserAgents.push(agent);
        },

        removeUserAgent : function(id) {
            var idx = 0;
            while (idx < UserAgents.length && UserAgents[idx].id != id) {
                idx++;
            }
            UserAgents.splice(idx, 1)
        },

        getUserAgents : function() {
            return UserAgents;
        },

        echoUserAgents : function() {
            if (UserAgents.length == 0) {
                console.log(('user agent list:empty').yellow);
                return;
            }
            console.log('user agent list:');
            for (var i = 0; i < UserAgents.length; i++) {
                console.log(('agent idx:' + i + ' id:' + UserAgents[i].id + ' name:' + UserAgents[i].name + ' type:' + UserAgents[i].type).yellow);
            }
        }
    }
};