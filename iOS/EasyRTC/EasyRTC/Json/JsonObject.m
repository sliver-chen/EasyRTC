//
//  JsonObject.m
//  EasyRTC
//
//  Created by cx on 2018/12/13.
//  Copyright © 2018 cx. All rights reserved.
//

#import "JsonObject.h"

#define TAG "JsonObject"

@implementation JsonObject

+ (NSString *)getStringFromJson:(NSArray *)data type:(NSString *)type
{
    return [data firstObject][type];
}

@end
