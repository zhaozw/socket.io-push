//
//  HWRandomUtil.m
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "HWRandomUtil.h"

@implementation HWRandomUtil

+ (NSString *)randomAlphaNumeric:(uint)count
{
    NSMutableString * randomStr = [NSMutableString stringWithCapacity:count];
    while (count--) {
        [randomStr appendFormat:@"%c", self.aRandomAlphaNumeric];
    }
    return randomStr;
}

+ (char)aRandomAlphaNumeric
{
    switch (arc4random() % 3) {
        case 0:  return 'A' + (arc4random() % 26);
        case 1:  return 'a' + (arc4random() % 26);
        default: return '0' + (arc4random() % 10);
    }
}

@end
