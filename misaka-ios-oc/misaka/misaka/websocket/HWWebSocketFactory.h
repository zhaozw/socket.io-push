//
//  HWWebSocketFactory.h
//  huwai
//
//  Created by crazylhf on 15/4/17.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HWWebSocketProtocol.h"
#import "HWWebSocketDelegate.h"
#import "HWWebSocketConfig.h"


/**
 *  @brief WebSocket工厂类, 创建local WebSocket或者remote WebSocket对象.
 */
@interface HWWebSocketFactory : NSObject

/**
 *  @brief 创建WebSocket对象.
 *  @param url          WebSocket的连接地址
 *  @param wsMode       WebSocket的类型(local | remote)
 *  @param wsDelegate   WebSocket的回调
 */
+ (id<HWWebSocketProtocol>)hwWebSocket:(NSString *)url
                                wsMode:(HWWSConfigMode)wsMode
                            wsDelegate:(id<HWWebSocketDelegate>)wsDelegate;

@end
