//
//  Signaling.h
//  EasyRTC
//
//  Created by cx on 2018/12/11.
//  Copyright © 2018 cx. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol SocketDelegate <NSObject>

- (void)onUserAgentsUpdate:(NSArray *)data;

- (void)onRemoteEventMsg:(NSString *)source target:(NSString *)target type:(NSString *)type value:(NSString *)value;

- (void)onRemoteCandidate:(int)label mid:(NSString *)mid candidate:(NSString *)candidate;

@end

@interface SocketWraper : NSObject

@property (nonatomic, strong) NSString *uid;

@property (nonatomic, strong) NSString *target;

+ (instancetype)shareSocketWraper;

- (void)setURL:(NSURL *)url;

- (void)emit:(NSString *)type value:(NSString *)value;

- (void)emit:(NSString *)type dict:(NSDictionary *)dict;

- (void)emit:(NSString *)type data:(NSArray *)data;

- (void)emitHeartBeat;

- (void)requestUserList;

- (void)addListener:(id<SocketDelegate>) delegate;

- (void)removeListener:(id<SocketDelegate>)delegate;

@end
