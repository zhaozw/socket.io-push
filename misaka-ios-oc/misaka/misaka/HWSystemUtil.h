//
//  HWSystemUtil.h
//  huwai
//
//  Created by crazylhf on 15/5/1.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>

#define HWIOS8_OR_LATER	( [[HWSystemUtil systemVersion] compare:@"8.0"] != NSOrderedAscending )
#define HWIOS7_OR_LATER	( [[HWSystemUtil systemVersion] compare:@"7.0"] != NSOrderedAscending )
#define HWIOS6_OR_LATER	( [[HWSystemUtil systemVersion] compare:@"6.0"] != NSOrderedAscending )

@interface HWSystemUtil : NSObject

+ (NSString *)systemVersion;

+ (NSString *)processName;

+ (uint32_t)currentPID;

+ (uint64_t)currentTID;

@end
