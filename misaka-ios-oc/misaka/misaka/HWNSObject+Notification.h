//
//  HWNSObject+Notification.h
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSObject (Notification)

- (void)addObserver:(SEL)aSelector name:(NSString *)aName object:(id)anObject;

- (void)postNotification:(NSNotification *)notification;
- (void)postNotificationName:(NSString *)aName;
- (void)postNotificationName:(NSString *)aName userInfo:(NSDictionary *)aUserInfo;

- (void)removeObserver;
- (void)removeObserver:(NSString *)aName object:(id)anObject;

@end
