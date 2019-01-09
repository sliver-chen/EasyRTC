//
//  JsonObject.h
//  EasyRTC
//
//  Created by cx on 2018/12/13.
//  Copyright © 2018 cx. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface JsonObject : NSObject

+ (NSString *)getStringFromJson:(NSArray *)data type:(NSString *)type;

@end
