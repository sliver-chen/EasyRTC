//
//  WebRTCWraper.m
//  EasyRTC
//
//  Created by cx on 2018/12/11.
//  Copyright © 2018 cx. All rights reserved.
//

#import "WebRTCWraper.h"
#import <AVFoundation/AVFoundation.h>
#import "RTCPair.h"
#import "RTCICEServer.h"
#import "RTCICECandidate.h"
#import "RTCMediaStream.h"
#import "RTCVideoCapturer.h"
#import "RTCPeerConnection.h"
#import "RTCMediaConstraints.h"
#import "RTCSessionDescription.h"
#import "RTCPeerConnectionFactory.h"
#import "EasyLog.h"

#pragma clang diagnostic ignored "-Wdeprecated-declarations"

#define TAG "WebRTCWraper"

static NSString *const kARDDefaultSTUNServerUrl = @"stun:stun.l.google.com:19302";
static NSString *const kARDDefaultSTUNServerUrl2 = @"turn:129.28.101.171:3478";
static NSString *const kARDDefaultSTUNServerUrl3 = @"turn:39.105.125.160:3478";

@interface WebRTCWraper ()

@property (nonatomic, strong) RTCPeerConnection *peer;

@property (nonatomic, strong) RTCPeerConnectionFactory *peerFactory;

@property (nonatomic, strong) NSMutableArray *iceServers;

@property (nonatomic, strong) RTCMediaStream *localMedia;

@property bool ifNeedAddStream;

@end

@implementation WebRTCWraper

- (instancetype)initWithDelegate:(id<WebRTCWraperDelegate>) delegate ifNeedAddStream:(bool)ifNeedAddStream
{
    self = [super init];
    if (self) {
        self.ifNeedAddStream = ifNeedAddStream;
        self.wraperDelegate = delegate;
        self.peerFactory = [[RTCPeerConnectionFactory alloc] init];
        self.iceServers = [[NSMutableArray alloc] init];
        [self.iceServers addObject:[self createIceServer:kARDDefaultSTUNServerUrl username:@"" password:@""]];
        [self.iceServers addObject:[self createIceServer:kARDDefaultSTUNServerUrl2 username:@"cx" password:@"1234"]];
        [self.iceServers addObject:[self createIceServer:kARDDefaultSTUNServerUrl3 username:@"helloword" password:@"helloword"]];
        [self createLocalMedia];
        self.peer = [self.peerFactory peerConnectionWithICEServers:self.iceServers constraints:[self defaultPeerConnectionConstraints] delegate:self];
        if (ifNeedAddStream) {
            [self.peer addStream:self.localMedia];
        }
        [self.peer getStatsWithDelegate:self mediaStreamTrack:nil statsOutputLevel:RTCStatsOutputLevelDebug];
    }
    return self;
}

- (void)createOffer
{
    EasyLog(TAG, "createOffer");
    [self.peer createOfferWithDelegate:self constraints:[self defaultOfferConstraints]];
}

- (void)createAnswer
{
    EasyLog(TAG, "createAnswer");
    [self.peer createAnswerWithDelegate:self constraints:[self defaultAnswerConstraints]];
}

- (void)setRemoteSdp:(NSString *)type sdp:(NSString *)sdp
{
    EasyLog(TAG, "on setRemoteSdp type %@", type);
    RTCSessionDescription *sessionDescription = [[RTCSessionDescription alloc] initWithType:type sdp:sdp];
    [self.peer setRemoteDescriptionWithDelegate:self sessionDescription:sessionDescription];
}

- (void)setIceCandidate:(int)label mid:(NSString *)mid candidate:(NSString *)candidate
{
    RTCICECandidate *iceCandidate = [[RTCICECandidate alloc] initWithMid:mid index:label sdp:candidate];
    if (self.peer.remoteDescription) {
        [self.peer addICECandidate:iceCandidate];
        EasyLog(TAG, "on setRemoteCandidate");
    }
}

- (void)exitSession
{
    [self.peer close];
}

#pragma mark webrtc delegate
//生成peer connection时调用
- (void)peerConnection:(RTCPeerConnection *)peerConnection didCreateSessionDescription:(RTCSessionDescription *)sdp error:(NSError *)error
{
    EasyLog(TAG, "on create sdp error %@", error);
    if (error)
        abort();
    [peerConnection setLocalDescriptionWithDelegate:self sessionDescription:sdp];
    [self.wraperDelegate onCreateOfferOrAnswer:sdp.type sdp:sdp.description];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection didSetSessionDescriptionWithError:(NSError *)error
{
    if (error) {
        EasyLog(TAG, "on set sdp error %@", error);
    }
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection addedStream:(RTCMediaStream *)stream
{
    EasyLog(TAG, "on add remote stream");
    [self.wraperDelegate onRemoteStream:stream];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection removedStream:(RTCMediaStream *)stream
{
    EasyLog(TAG, "on remove remote stream");
    [self.wraperDelegate onRemoveStream:stream];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection gotICECandidate:(RTCICECandidate *)candidate
{
    EasyLog(TAG, "on ice candidate");
    [self.wraperDelegate onIceCandidate:(int)candidate.sdpMLineIndex
                                    mid:candidate.sdpMid
                              candidate:candidate.sdp];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection iceGatheringChanged:(RTCICEGatheringState)newState
{
    if (newState == RTCICEGatheringNew) {
        EasyLog(TAG, "on ice status new");
    } else if (newState == RTCICEGatheringGathering) {
        EasyLog(TAG, "on ice status gathering");
    } else if (newState == RTCICEGatheringComplete) {
        EasyLog(TAG, "on ice status complete");
    }
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection iceConnectionChanged:(RTCICEConnectionState)newState
{
    EasyLog(TAG, "on ice connection change");
}

- (void)peerConnectionOnRenegotiationNeeded:(RTCPeerConnection *)peerConnection
{
    EasyLog(TAG, "on renegotiantion need");
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection didOpenDataChannel:(RTCDataChannel *)dataChannel
{
    EasyLog(TAG, "on data channel open");
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection signalingStateChanged:(RTCSignalingState)stateChanged
{ 
    EasyLog(TAG, "on signaling state change");
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection didGetStats:(NSArray *)stats
{
    EasyLog(TAG, "on didGetStats %@", stats);
}

#pragma mark create internal resource
- (RTCICEServer *)createIceServer:(NSString *)url username:(NSString *)username password:(NSString *)password
{
    NSURL *serverUrl = [NSURL URLWithString:url];
    return [[RTCICEServer alloc] initWithURI:serverUrl
                                    username:username
                                    password:password];
}

- (RTCMediaConstraints *)createLocalMediaconstraints
{
    NSArray *optionalConstraints = @[[[RTCPair alloc] initWithKey:@"DtlsSrtpKeyAgreement" value:@"true"]];
    RTCMediaConstraints* constraints =
    [[RTCMediaConstraints alloc]
     initWithMandatoryConstraints:nil
     optionalConstraints:optionalConstraints];
    return constraints;
}

- (void)createLocalMedia
{
    self.localMedia = [self createLocalMediaStream];
    if (self.ifNeedAddStream) {
        [self.wraperDelegate onLocalStream:self.localMedia];
    }
}

- (RTCMediaStream *)createLocalMediaStream {
    RTCMediaStream *localStream = [self.peerFactory mediaStreamWithLabel:@"ARDAMS"];
    
    RTCVideoTrack *localVideoTrack = [self createLocalVideoTrack];
    if (localVideoTrack) {
        [localStream addVideoTrack:localVideoTrack];
    }
    
    [localStream addAudioTrack:[self.peerFactory audioTrackWithID:@"ARDAMSa0"]];
    [[AVAudioSession sharedInstance] overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:nil];
    
    return localStream;
}

- (RTCVideoTrack *)createLocalVideoTrack {
    
    RTCVideoTrack *localVideoTrack = nil;
    
    NSString *cameraID = nil;
    for (AVCaptureDevice *captureDevice in
         [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo]) {
        if (captureDevice.position == AVCaptureDevicePositionFront) {
            cameraID = [captureDevice localizedName];
            break;
        }
    }
    NSAssert(cameraID, @"Unable to get the front camera id");
    
    RTCVideoCapturer *capturer = [RTCVideoCapturer capturerWithDeviceName:cameraID];
    RTCMediaConstraints *mediaConstraints = [self defaultMediaStreamConstraints];
    RTCVideoSource *videoSource = [self.peerFactory videoSourceWithCapturer:capturer constraints:mediaConstraints];
    localVideoTrack = [self.peerFactory videoTrackWithID:@"ARDAMSv0" source:videoSource];

    return localVideoTrack;
}

- (RTCMediaConstraints *)defaultMediaStreamConstraints {
    RTCMediaConstraints* constraints =
    [[RTCMediaConstraints alloc]
     initWithMandatoryConstraints:nil
     optionalConstraints:nil];
    return constraints;
}

- (RTCMediaConstraints *)defaultOfferConstraints {
    return [self defaultAnswerConstraints];
}

- (RTCMediaConstraints *)defaultAnswerConstraints {
    NSArray *mandatoryConstraints = @[
                                      [[RTCPair alloc] initWithKey:@"OfferToReceiveAudio" value:@"true"],
                                      [[RTCPair alloc] initWithKey:@"OfferToReceiveVideo" value:@"true"]
                                      ];
    RTCMediaConstraints* constraints =
    [[RTCMediaConstraints alloc]
     initWithMandatoryConstraints:mandatoryConstraints
     optionalConstraints:nil];
    return constraints;
}

- (RTCMediaConstraints *)defaultPeerConnectionConstraints {
    NSArray *optionalConstraints = @[
                                     [[RTCPair alloc] initWithKey:@"DtlsSrtpKeyAgreement" value:@"true"]
                                     ];
    RTCMediaConstraints* constraints =
    [[RTCMediaConstraints alloc]
     initWithMandatoryConstraints:nil
     optionalConstraints:optionalConstraints];
    return constraints;
}

@end
