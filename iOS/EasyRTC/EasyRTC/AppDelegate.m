//
//  AppDelegate.m
//  EasyRTC
//
//  Created by cx on 2018/12/10.
//  Copyright © 2018 cx. All rights reserved.
//

#import "AppDelegate.h"
#import "SocketWraper.h"
#import "RTCPeerConnectionFactory.h"

@interface AppDelegate ()

@property (nonatomic, strong)dispatch_source_t timer;

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [self initWebRTCWraper];
    
    NSURL *url = [[NSURL alloc] initWithString:@"http://129.28.101.171:1234"];
    [[SocketWraper shareSocketWraper] setURL:url];

    [self initSocketHeartBeat];
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    [self deinitWebRTCWraper];
}

- (void)initSocketWraper {
    NSURL *url = [[NSURL alloc] initWithString:@"http://172.20.64.86:1234"];
    [[SocketWraper shareSocketWraper] setURL:url];
}

- (void)initWebRTCWraper {
    [RTCPeerConnectionFactory initializeSSL];
}

- (void)deinitWebRTCWraper {
    [RTCPeerConnectionFactory deinitializeSSL];
}

- (void)initSocketHeartBeat {
    self.timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, dispatch_get_main_queue());
    dispatch_source_set_timer(self.timer, dispatch_time(DISPATCH_TIME_NOW, 0), 2.0 * NSEC_PER_SEC, 0.0 * NSEC_PER_SEC);
    __weak typeof(self) WeakSelf = self;
    dispatch_source_set_event_handler(WeakSelf.timer, ^{
        [[SocketWraper shareSocketWraper] emitHeartBeat];
    });
    dispatch_resume(self.timer);
}

@end
