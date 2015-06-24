//
//  MINSObject+Notification.m
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "MINSObject+Notification.h"

@implementation NSObject (Notification)

- (void)addObserver:(SEL)aSelector name:(NSString *)aName object:(id)anObject
{
    [self.notifyCenter addObserver:self selector:aSelector name:aName object:anObject];
}

- (void)postNotification:(NSNotification *)notification
{
    [self.notifyCenter postNotification:notification];
}

- (void)postNotificationName:(NSString *)aName
{
    [self.notifyCenter postNotificationName:aName object:self];
}

- (void)postNotificationName:(NSString *)aName userInfo:(NSDictionary *)aUserInfo
{
    [self.notifyCenter postNotificationName:aName object:self userInfo:aUserInfo];
}

- (void)removeObserver
{
    [self.notifyCenter removeObserver:self];
}

- (void)removeObserver:(NSString *)aName object:(id)anObject
{
    [self.notifyCenter removeObserver:self name:aName object:anObject];
}

- (NSNotificationCenter *)notifyCenter
{
    return [NSNotificationCenter defaultCenter];
}

@end
