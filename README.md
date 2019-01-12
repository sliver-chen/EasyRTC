# EasyRTC  
EasyRTC is a one-to-one webrtc session application. 
it refer to following project in github 
1.ProjectRTC https://github.com/pchab/ProjectRTC 
2.AndroidRTC https://github.com/pchab/AndroidRTC 
3.iOSRTC https://github.com/digixtechnology/iOSRTC 
 
why did i refactor the webrtc application instead of using above projects? 
if you have ever use AndroidRTC,you will find that the way of use it is not east to understand. 
we usually click on an avater of online friend when invite others to start a media session. 
so i decide to make it easyliy to make a call session by add clickable ui item to start a session. 
in other hand,i want to add camera client which just send media to visitors,it can be used to 
monitor pets at home by start a media session. 

## plugins  
EasyRTC consist of following plugins. 
1. Client 
Client can connect to other client with a media session,include Mobile Client and Camera Client. 

1.1 Mobile 
Mobile Client can proactivitely initiated media session. 

1.2 Camera 
Camera Client only can passively wait for invite from Mobile Client. 

2. Server 
Server play the role of forward signal between clients 

## install 
1. Mobile Client 
iOS/Android Mobile is support now,the source code is located in 'iOS' and 'Android' under top level directory. 

2. Camera Client 
in my vision,camera client should be like arm-linux ip camera. 
but due to conditional restrictions,camera client is android camera Client,the source code is located in Camera/Android directory. 

3. Server 
``` 
npm install
node Server.js
```

