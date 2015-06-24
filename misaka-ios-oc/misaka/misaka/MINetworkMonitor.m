//
//  MINetworkMonitor.m
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//
#import "MINSObject+Notification.h"
#import <SystemConfiguration/SystemConfiguration.h>
#import <netinet/in.h>

#import "MINetworkMonitor.h"

MIDEFINE_NOTIFCATION(KMINetworkStatusChangedNotify);
MIDEFINE_NOTIFICATIONKEY(KMINetworkStatusInfoKey);

@interface MINetworkMonitor()

- (void)onNetworkStatusChanged;

@end

static void ReachabilityCallback(SCNetworkReachabilityRef target, SCNetworkReachabilityFlags flags, void* info)
{
    [[MINetworkMonitor sharedInstance] onNetworkStatusChanged];
}

@implementation MINetworkMonitor
{
    SCNetworkReachabilityRef _reachability;
}

- (void)enable
{
    if (!_reachability) {
        do {
            if (!(_reachability = self.SCNetworkReachability)) {
                MIGLLoge(@"Failed to create network reachability");
                break;
            }
            
            SCNetworkReachabilityContext context = {0, (__bridge void *)(self), NULL, NULL, NULL};
            if (!SCNetworkReachabilitySetCallback(_reachability, ReachabilityCallback, &context)) {
                MIGLLoge(@"Failed to set network reachability callback function");
                break;
            }
            
            if (!SCNetworkReachabilityScheduleWithRunLoop(_reachability, CFRunLoopGetCurrent(), kCFRunLoopDefaultMode)) {
                MIGLLoge(@"Failed to set network reachability callback run loop");
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

- (MINetworkStatus)networkStatus
{
    MINetworkStatus state = MINetwork_NotReachable;
    
    SCNetworkReachabilityFlags flags = 0;
//    SCNetworkReachabilityRef reachability = self.SCNetworkReachability;
    
    if (!SCNetworkReachabilityGetFlags(_reachability, &flags)) {
        MIGLLoge(@"Failed to get network reachability flags");
    } else {
        state = [self StatusFromReachabilityFlags:flags];
    }
//    CFRelease(reachability);
    
    return state;
}

- (void)onNetworkStatusChanged
{
    NSDictionary * userInfo = @{ KMINetworkStatusInfoKey : @(self.networkStatus) };
    [self postNotificationName:KMINetworkStatusChangedNotify userInfo:userInfo];
}

#pragma mark - utils

- (MINetworkStatus)StatusFromReachabilityFlags:(SCNetworkReachabilityFlags)flags
{
    if ((flags & kSCNetworkReachabilityFlagsReachable) == 0) {
        return MINetwork_NotReachable;
    }
    
    MINetworkStatus retVal = MINetwork_NotReachable;
    
    if ((flags & kSCNetworkReachabilityFlagsConnectionRequired) == 0) {
        retVal = MINetwork_Wifi;
    }
    
    
    if ((((flags & kSCNetworkReachabilityFlagsConnectionOnDemand ) != 0) ||
         (flags & kSCNetworkReachabilityFlagsConnectionOnTraffic) != 0)) {
        if ((flags & kSCNetworkReachabilityFlagsInterventionRequired) == 0) {
            retVal = MINetwork_Wifi;
        }
    }
    
    if((flags & kSCNetworkReachabilityFlagsIsWWAN) == kSCNetworkReachabilityFlagsIsWWAN) {
        retVal = MINetwork_2G;
        if ((flags & kSCNetworkReachabilityFlagsTransientConnection) == kSCNetworkReachabilityFlagsTransientConnection) {
            retVal = MINetwork_3G;
            if((flags & kSCNetworkReachabilityFlagsConnectionRequired) == kSCNetworkReachabilityFlagsConnectionRequired) {
                retVal = MINetwork_2G;
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

MIIMPLEMENT_SINGLETON(MINetworkMonitor)

@end
