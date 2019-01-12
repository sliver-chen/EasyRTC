//
//  CallViewController.m
//  EasyRTC
//
//  Created by cx on 2018/12/16.
//  Copyright © 2018 cx. All rights reserved.
//

#import "CallViewController.h"
#import "EasyLog.h"

#define TAG "CallViewController"

@interface CallViewController ()

@property (weak, nonatomic) IBOutlet RTCEAGLVideoView *RemoteView;

@property (weak, nonatomic) IBOutlet RTCEAGLVideoView *LocalView;

@property (weak, nonatomic) IBOutlet UIButton *CancelButton;

@property (strong, nonatomic) WebRTCWraper *rtcWraper;

@property (strong, nonatomic) RTCVideoTrack *localVideoTrack;

@property (strong, nonatomic) RTCVideoTrack *remoteVideoTrack;

@end

@implementation CallViewController

- (void)viewDidLoad
{
    [super viewDidLoad];

    EasyLog(TAG, "CallViewController did load");
    [[SocketWraper shareSocketWraper] addListener:self];
    self.rtcWraper = [[WebRTCWraper alloc] initWithDelegate:self ifNeedAddStream:self.ifNeedAddStream];
    
    self.RemoteView.transform = CGAffineTransformMakeScale(-1.0, 1.0);
    [self.RemoteView setDelegate:self];
    [self.RemoteView renderFrame:nil];
    
    self.LocalView.transform = CGAffineTransformMakeScale(-1.0, 1.0);
    [self.LocalView setDelegate:self];
    [self.LocalView renderFrame:nil];
    
    if ([self.actionType isEqualToString:@"offer"]) {
        [self.rtcWraper createOffer];
    }
    
    [self.view bringSubviewToFront:self.LocalView];
    if (!self.ifNeedAddStream) {
        self.LocalView.hidden = YES;
    }
}

- (void)viewDidDisappear:(BOOL)animated
{
    EasyLog(TAG, "CallViewController did disapper");
    [[SocketWraper shareSocketWraper] removeListener:self];
    
    [super viewDidDisappear:animated];
}

- (IBAction)CalcenButtonClicked:(id)sender
{
    [self sendSessionExitEvent];
    [self.rtcWraper exitSession];
    [self performSegueWithIdentifier:@"CancelSegue" sender:nil];
}

- (void)sendSessionExitEvent
{
    NSString *source = [SocketWraper shareSocketWraper].uid;
    NSString *target = [SocketWraper shareSocketWraper].target;
    NSArray *data = @[@{@"source" : source, @"target" : target, @"type" : @"exit", @"value" : @"yes"}];
    [[SocketWraper shareSocketWraper] emit:@"event" data:data];
}

- (void)processSignalMsg:(NSString *)source target:(NSString *)target type:(NSString *)type value:(NSString *)value
{
    if ([target isEqualToString:[SocketWraper shareSocketWraper].uid]) {
        if ([type isEqualToString:@"offer"]) {
            [self.rtcWraper setRemoteSdp:type sdp:value];
            [self.rtcWraper createAnswer];
        }
        
        if ([type isEqualToString:@"answer"]) {
            [self.rtcWraper setRemoteSdp:type sdp:value];
        }
        
        if ([type isEqualToString:@"exit"]) {
            [self.rtcWraper exitSession];
            [self performSegueWithIdentifier:@"CancelSegue" sender:nil];
        }
    } else {
        EasyLog(TAG, "get error tag");
    }
}

#pragma mark SignalDelegate
- (void)onRemoteEventMsg:(NSString *)source target:(NSString *)target type:(NSString *)type value:(NSString *)value
{
    [self processSignalMsg:source target:target type:type value:value];
}

- (void)onRemoteCandidate:(int)label mid:(NSString *)mid candidate:(NSString *)candidate
{
    EasyLog(TAG, "onRemoteCandidate");
    [self.rtcWraper setIceCandidate:label mid:mid candidate:candidate];
}

#pragma mark WebRTCWraperDelegate
- (void)onLocalStream:(RTCMediaStream *)stream
{
    EasyLog(TAG, "onLocalStream");
    self.localVideoTrack = stream.videoTracks[0];
    [self.localVideoTrack addRenderer:self.LocalView];
}

- (void)onRemoteStream:(RTCMediaStream *)stream
{
    EasyLog(TAG, "onRemoteStream");
    self.remoteVideoTrack = stream.videoTracks[0];
    [self.remoteVideoTrack addRenderer:self.RemoteView];
}

- (void)onRemoveStream:(RTCMediaStream *)stream
{
    EasyLog(TAG, "onRemoveStream");
}

- (void)onCreateOfferOrAnswer:(NSString *)type sdp:(NSString *)sdp
{
    EasyLog(TAG, "onCreateOfferOrAnswer");
    [[SocketWraper shareSocketWraper] emit:type value:sdp];
}

- (void)onIceCandidate:(int)label mid:(NSString *)mid candidate:(NSString *)candidate
{
    EasyLog(TAG, "onIceCandidate");
    NSString *source = [SocketWraper shareSocketWraper].uid;
    NSString *target = [SocketWraper shareSocketWraper].target;
    NSString *labelStr = [NSString stringWithFormat:@"%d", label];

    NSArray *data = @[@{@"source" : source, @"target" : target, @"label" : labelStr, @"mid" : mid, @"candidate" : candidate}];
    [[SocketWraper shareSocketWraper] emit:@"candidate" data:data];
}

- (void)videoView:(RTCEAGLVideoView *)videoView didChangeVideoSize:(CGSize)size
{
    EasyLog(TAG, "onVideoView");
}

@end
