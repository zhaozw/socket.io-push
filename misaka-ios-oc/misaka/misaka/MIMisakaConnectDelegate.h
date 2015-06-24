//
//  MIMisakaConnectDelegate.h
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MIStompMessage.h"
#import "MIStompDefs.h"

@protocol MIMisakaConnectDelegate <NSObject>

- (void)onConnected;

- (void)onConnectFailed:(NSError *)error;

- (void)onMessage:(MIStompMessage *)message;

- (void)onDisconnected:(NSString *)reason statusCode:(MIWSStatusCode)statusCode;

@end
