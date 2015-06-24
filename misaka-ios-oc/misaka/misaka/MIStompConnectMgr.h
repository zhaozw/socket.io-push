//
//  MIStompConnectMgr.h
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MIMisakaConnectDelegate.h"
#import "MIWebSocketConfig.h"

/**
 *  @brief 处理基于stomp协议的连接逻辑及协议数据parse.
 */
@interface MIStompConnectMgr : NSObject

- (id)initWithRequestUrl:(NSString *)requestUrl
                  wsMode:(MIWSConfigMode)wsMode
           stompDelegate:(id<MIMisakaConnectDelegate>)stompDelegate;

/// 触发重新连接.
- (void)reconnect;

- (BOOL)isConnected;

/// 订阅由reqPath指定的目标地址.
- (void)subscribe:(NSString *)reqPath;

- (uint64_t)request:(NSString *)reqPath body:(NSString *)body headers:(NSDictionary *)headers;

@end
