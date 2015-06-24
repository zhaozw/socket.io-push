//
//  HWStompConnectDelegate.h
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HWStompMessage.h"
#import "HWStompDefs.h"

@protocol HWStompConnectDelegate <NSObject>

- (void)onStompConnected;

- (void)onStompConnectFailed:(NSError *)error;

- (void)onStompMessage:(HWStompMessage *)message;

- (void)onStompDisconnected:(NSString *)reason statusCode:(HWWSStatusCode)statusCode;

@end
