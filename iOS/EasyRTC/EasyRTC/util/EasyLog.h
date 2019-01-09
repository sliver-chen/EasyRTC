//
//  EasyLog.h
//  EasyRTC
//
//  Created by cx on 2018/12/19.
//  Copyright © 2018 cx. All rights reserved.
//

#import <Foundation/Foundation.h>

#define EasyLog(tag,fmt,...) NSLog(@"%s : " fmt, tag, ##__VA_ARGS__)
