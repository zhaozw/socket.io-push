//
//  MISystemUtil.h
//  huwai
//
//  Created by crazylhf on 15/5/1.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>

#define MIIOS8_OR_LATER	( [[MISystemUtil systemVersion] compare:@"8.0"] != NSOrderedAscending )
#define MIIOS7_OR_LATER	( [[MISystemUtil systemVersion] compare:@"7.0"] != NSOrderedAscending )
#define MIIOS6_OR_LATER	( [[MISystemUtil systemVersion] compare:@"6.0"] != NSOrderedAscending )

@interface MISystemUtil : NSObject

+ (NSString *)systemVersion;

+ (NSString *)processName;

+ (uint32_t)currentPID;

+ (uint64_t)currentTID;

@end
