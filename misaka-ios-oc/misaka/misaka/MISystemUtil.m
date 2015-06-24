//
//  MISystemUtil.m
//  huwai
//
//  Created by crazylhf on 15/5/1.
//  Copyright (c) 2015年 zq. All rights reserved.
//
#import <UIKit/UIKit.h>
#import "MISystemUtil.h"
#import <pthread.h>

@implementation MISystemUtil

+ (NSString *)systemVersion
{
    return [[UIDevice currentDevice] systemVersion];
}

+ (NSString *)processName
{
    return [[NSProcessInfo processInfo] processName];
}

+ (uint32_t)currentPID
{
    return [[NSProcessInfo processInfo] processIdentifier];
}

+ (uint64_t)currentTID
{
    uint64_t tid;
    pthread_threadid_np(NULL, &tid);
    return tid;
}

@end
