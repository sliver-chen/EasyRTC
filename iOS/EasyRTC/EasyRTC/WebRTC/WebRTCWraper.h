//
//  WebRTCWraper.h
//  EasyRTC
//
//  Created by cx on 2018/12/11.
//  Copyright © 2018 cx. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RTCStatsDelegate.h"
#import "RTCPeerConnectionDelegate.h"
#import "RTCSessionDescriptionDelegate.h"
#import "SocketWraper.h"

@protocol WebRTCWraperDelegate <NSObject>

- (void)onLocalStream:(RTCMediaStream *)stream;

- (void)onRemoteStream:(RTCMediaStream *)stream;

- (void)onRemoveStream:(RTCMediaStream *)stream;

- (void)onCreateOfferOrAnswer:(NSString *)type sdp:(NSString *)sdp;

- (void)onIceCandidate:(int)label mid:(NSString *)mid candidate:(NSString *)candidate;

@end

@interface WebRTCWraper : NSObject <RTCPeerConnectionDelegate,RTCSessionDescriptionDelegate, RTCStatsDelegate>

@property (nonatomic, weak)id<WebRTCWraperDelegate> wraperDelegate;

- (instancetype)initWithDelegate:(id<WebRTCWraperDelegate>) delegate;

- (void)createOffer;

- (void)createAnswer;

- (void)setRemoteSdp:(NSString *)type sdp:(NSString *)sdp;

- (void)setIceCandidate:(int)label mid:(NSString *)mid candidate:(NSString *)candidate;

- (void)exitSession;

@end
