//
//  HWWebSocketDelegate.h
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HWStompDefs.h"

/**
 *  @brief HWWebSocket回调接口.
 */
@protocol HWWebSocketDelegate <NSObject>

/// WebSocket已经连接.
- (void)onConnected;

- (void)onMessage:(NSString *)message;

- (void)onConnectFailed:(NSError *)error;

- (void)onDisconnected:(NSString *)reason code:(HWWSStatusCode)code;

/// 收到心跳包.
- (void)onHeartbeat;

@end
