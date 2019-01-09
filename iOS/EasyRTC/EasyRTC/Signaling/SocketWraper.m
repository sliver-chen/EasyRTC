//
//  Signaling.m
//  EasyRTC
//
//  Created by cx on 2018/12/11.
//  Copyright © 2018 cx. All rights reserved.
//

#import "SocketWraper.h"
#import "JsonObject.h"
#import "EasyLog.h"

@import SocketIO;

#define TAG "SocketWraper"

@interface SocketWraper()

@property (nonatomic, strong) SocketManager *socketManger;

@property (nonatomic, strong) SocketIOClient *socketIOClient;

@property (nonatomic, strong) NSMutableArray *listeners;

@end

@implementation SocketWraper

+ (instancetype)shareSocketWraper
{
    static SocketWraper *socketWraper = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        socketWraper = [[SocketWraper alloc] init];
    });
    return socketWraper;
}

- (void)setURL:(NSURL *)url
{
    self.listeners = [[NSMutableArray alloc] init];
    self.socketManger = [[SocketManager alloc] initWithSocketURL:url config:@{@"log": @NO, @"compress": @YES}];
    self.socketIOClient = self.socketManger.defaultSocket;
    [self setSocketEvent];
    [self.socketIOClient connect];
}

- (void)setSocketEvent
{
    [self.socketIOClient on:@"connect" callback:^(NSArray *data, SocketAckEmitter *ack) {
        EasyLog(TAG, "SocketIO connect");
        
        NSString *deviceName = [[UIDevice currentDevice] name];
        [self.socketIOClient emit:@"get_id" with:@[@{@"name" : deviceName, @"type" : @"iOS_client"}]];
    }];
    
    [self.socketIOClient on:@"disconnect" callback:^(NSArray *data, SocketAckEmitter *ack) {
        EasyLog(TAG, "SocketIO disconnect");
    }];
    
    [self.socketIOClient on:@"user_list" callback:^(NSArray *data, SocketAckEmitter *ack) {
        [self processUserList:data];
        [self verify:@"user_list"];
        EasyLog(TAG, "signal user_list update");
    }];
    
    [self.socketIOClient on:@"set_id" callback:^(NSArray *data, SocketAckEmitter *ack) {
        self.uid = [JsonObject getStringFromJson:data type:@"value"];
        [self verify:@"set_id"];
        EasyLog(TAG, "signal set_id get sockt id %@", self.uid);
    }];
    
    [self.socketIOClient on:@"event" callback:^(NSArray *data, SocketAckEmitter *ack) {
        [self processEvent:data];
        [self verify:@"event"];
    }];
    
    [self.socketIOClient on:@"candidate" callback:^(NSArray *data, SocketAckEmitter *ack) {
        NSString *labelStr = [JsonObject getStringFromJson:data type:@"label"];
        NSString *mid = [JsonObject getStringFromJson:data type:@"mid"];
        NSString *candidate = [JsonObject getStringFromJson:data type:@"candidate"];
        for (int i = 0; i < self.listeners.count; i++) {
            [self.listeners[i] onRemoteCandidate:[labelStr intValue] mid:mid candidate:candidate];
        }
        [self verify:@"candidate"];
    }];
}

- (void)addListener:(id<SocketDelegate>)delegate
{
    [self.listeners addObject:delegate];
}

- (void)removeListener:(id<SocketDelegate>)delegate
{
    [self.listeners removeObject:delegate];
}

- (void)emit:(NSString *)type value:(NSString *)value
{
    NSArray *data = @[@{@"source" : self.uid, @"target" : self.target, @"type" : type, @"value" : value}];
    [self.socketIOClient emit:@"event" with:data];
}

- (void)emit:(NSString *)type dict:(NSDictionary *)dict
{
    NSArray *data = @[@{@"source" : self.uid, @"target" : self.target, @"type" : type, @"value" : dict}];
    [self.socketIOClient emit:@"event" with:data];
}

- (void)emit:(NSString *)type data:(NSArray *)data
{
    [self.socketIOClient emit:type with:data];
}

- (void)emitHeartBeat
{
    if (self.uid) {
        NSArray *data = @[@{@"source" : self.uid, @"target" : @"EasyRTC Server", @"type" : @"heart", @"value" : @"yes"}];
        [self.socketIOClient emit:@"heart" with:data];
    }
}

- (void)requestUserList
{
    NSArray *data = @[@{@"value" : @"yes"}];
    [self.socketIOClient emit:@"request_user_list" with:data];
}

- (void)verify:(NSString *)type
{
    NSArray *data = @[@{@"source" : self.uid, @"target" : @"EasyRTC Server", @"type" : type}];
    [self.socketIOClient emit:@"ack" with:data];
}

#pragma mark signal data
- (void)processUserList:(NSArray *)data
{
    NSDictionary *dict = [data firstObject];
    NSString *scnt = dict[@"cnt"];
    int cnt = (int)[scnt integerValue];
    
    NSMutableArray *agentArray = [[NSMutableArray alloc] init];
    for (int i = 0; i < cnt; i++) {
        NSString *itemName = [NSString stringWithFormat:@"%d", i];
        NSDictionary *item = dict[itemName];
        [agentArray addObject:item];
    }
    
    for (int i = 0; i < self.listeners.count; i++) {
        [self.listeners[i] onUserAgentsUpdate:[agentArray mutableCopy]];
    }
}

- (void)processEvent:(NSArray *)data
{
    NSString *source = [JsonObject getStringFromJson:data type:@"source"];
    NSString *target = [JsonObject getStringFromJson:data type:@"target"];
    NSString *type = [JsonObject getStringFromJson:data type:@"type"];
    NSString *value = [JsonObject getStringFromJson:data type:@"value"];
    
    for (int i = 0; i < self.listeners.count; i++) {
        [self.listeners[i] onRemoteEventMsg:source target:target type:type value:value];
    }
}

@end
