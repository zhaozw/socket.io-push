//
//  HWNetworkMonitor.m
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//
#import "HWNSObject+Notification.h"
#import <SystemConfiguration/SystemConfiguration.h>
#import <netinet/in.h>

#import "HWNetworkMonitor.h"

HWDEFINE_NOTIFCATION(KHWNetworkStatusChangedNotify);
HWDEFINE_NOTIFICATIONKEY(KHWNetworkStatusInfoKey);

@interface HWNetworkMonitor()

- (void)onNetworkStatusChanged;

@end

static void ReachabilityCallback(SCNetworkReachabilityRef target, SCNetworkReachabilityFlags flags, void* info)
{
    [[HWNetworkMonitor sharedInstance] onNetworkStatusChanged];
}

@implementation HWNetworkMonitor
{
    SCNetworkReachabilityRef _reachability;
}

- (void)enable
{
    if (!_reachability) {
        do {
            if (!(_reachability = self.SCNetworkReachability)) {
                HWGLLoge(@"Failed to create network reachability");
                break;
            }
            
            SCNetworkReachabilityContext context = {0, (__bridge void *)(self), NULL, NULL, NULL};
            if (!SCNetworkReachabilitySetCallback(_reachability, ReachabilityCallback, &context)) {
                HWGLLoge(@"Failed to set network reachability callback function");
                break;
            }
            
            if (!SCNetworkReachabilityScheduleWithRunLoop(_reachability, CFRunLoopGetCurrent(), kCFRunLoopDefaultMode)) {
                HWGLLoge(@"Failed to set network reachability callback run loop");
                break;
            }
        } while (false);
    }
}

- (void)disable
{
    if (_reachability) {
        SCNetworkReachabilityUnscheduleFromRunLoop(_reachability, CFRunLoopGetCurrent(), kCFRunLoopDefaultMode);
        CFRelease(_reachability);
    }
    _reachability = NULL;
}

- (HWNetworkStatus)networkStatus
{
    HWNetworkStatus state = HWNetwork_NotReachable;
    
    SCNetworkReachabilityFlags flags = 0;
//    SCNetworkReachabilityRef reachability = self.SCNetworkReachability;
    
    if (!SCNetworkReachabilityGetFlags(_reachability, &flags)) {
        HWGLLoge(@"Failed to get network reachability flags");
    } else {
        state = [self StatusFromReachabilityFlags:flags];
    }
//    CFRelease(reachability);
    
    return state;
}

- (void)onNetworkStatusChanged
{
    NSDictionary * userInfo = @{ KHWNetworkStatusInfoKey : @(self.networkStatus) };
    [self postNotificationName:KHWNetworkStatusChangedNotify userInfo:userInfo];
}

#pragma mark - utils

- (HWNetworkStatus)StatusFromReachabilityFlags:(SCNetworkReachabilityFlags)flags
{
    if ((flags & kSCNetworkReachabilityFlagsReachable) == 0) {
        return HWNetwork_NotReachable;
    }
    
    HWNetworkStatus retVal = HWNetwork_NotReachable;
    
    if ((flags & kSCNetworkReachabilityFlagsConnectionRequired) == 0) {
        retVal = HWNetwork_Wifi;
    }
    
    
    if ((((flags & kSCNetworkReachabilityFlagsConnectionOnDemand ) != 0) ||
         (flags & kSCNetworkReachabilityFlagsConnectionOnTraffic) != 0)) {
        if ((flags & kSCNetworkReachabilityFlagsInterventionRequired) == 0) {
            retVal = HWNetwork_Wifi;
        }
    }
    
    if((flags & kSCNetworkReachabilityFlagsIsWWAN) == kSCNetworkReachabilityFlagsIsWWAN) {
        retVal = HWNetwork_2G;
        if ((flags & kSCNetworkReachabilityFlagsTransientConnection) == kSCNetworkReachabilityFlagsTransientConnection) {
            retVal = HWNetwork_3G;
            if((flags & kSCNetworkReachabilityFlagsConnectionRequired) == kSCNetworkReachabilityFlagsConnectionRequired) {
                retVal = HWNetwork_2G;
            }
        }
    }
    return retVal;
}

- (SCNetworkReachabilityRef)SCNetworkReachability
{
    struct sockaddr_in zeroAddress;
    bzero(&zeroAddress, sizeof(zeroAddress));
    zeroAddress.sin_len = sizeof(zeroAddress);
    zeroAddress.sin_family = AF_INET;
    return SCNetworkReachabilityCreateWithAddress(kCFAllocatorDefault, (const struct sockaddr *)&zeroAddress);
}

#pragma mark -

- (id)init
{
    if (self = [super init]) {
        _reachability = NULL;
    }
    return self;
}

- (void)dealloc
{
    [self disable];
}

HWIMPLEMENT_SINGLETON(HWNetworkMonitor)

@end
