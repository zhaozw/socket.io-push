//
//  HWWebSocketProtocol.h
//  huwai
//
//  Created by crazylhf on 15/4/17.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 *  @brief HWWebSocket主调接口.
 */
@protocol HWWebSocketProtocol <NSObject>

/// 打开连接.
- (void)connect;

/// 关闭连接.
- (void)close;

/// 当前是否已经连接.
- (BOOL)isConnected;

/// 当前是否已经断开连接.
- (BOOL)isDisconnected;

/// 基于当前链路, 发送数据data.
- (BOOL)send:(NSString *)data;

@end
