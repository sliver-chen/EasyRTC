//
//  CallViewController.h
//  EasyRTC
//
//  Created by cx on 2018/12/16.
//  Copyright © 2018 cx. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "RTCEAGLVideoView.h"
#import "RTCVideoRenderer.h"
#import "RTCVideoTrack.h"
#import "RTCMediaStream.h"
#import "RTCOpenGLVideoRenderer.h"
#import "SocketWraper.h"
#import "WebRTCWraper.h"

@interface CallViewController : UIViewController <SocketDelegate, WebRTCWraperDelegate, RTCEAGLVideoViewDelegate>

@property (strong, nonatomic)NSString *actionType;

@property bool ifNeedAddStream;

@end
