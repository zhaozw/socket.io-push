//
//  MIWebSocketDelegate.h
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MIStompDefs.h"

/**
 *  @brief MIWebSocket回调接口.
 */
@protocol MIWebSocketDelegate <NSObject>

/// WebSocket已经连接.
- (void)onConnected;

- (void)onMessage:(NSString *)message;

- (void)onConnectFailed:(NSError *)error;

- (void)onDisconnected:(NSString *)reason code:(MIWSStatusCode)code;

/// 收到心跳包.
- (void)onHeartbeat;

@end
