//
//  MIWebSocketFactory.h
//  huwai
//
//  Created by crazylhf on 15/4/17.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MIWebSocketProtocol.h"
#import "MIWebSocketDelegate.h"
#import "MIWebSocketConfig.h"


/**
 *  @brief WebSocket工厂类, 创建local WebSocket或者remote WebSocket对象.
 */
@interface MIWebSocketFactory : NSObject

/**
 *  @brief 创建WebSocket对象.
 *  @param url          WebSocket的连接地址
 *  @param wsMode       WebSocket的类型(local | remote)
 *  @param wsDelegate   WebSocket的回调
 */
+ (id<MIWebSocketProtocol>)hwWebSocket:(NSString *)url
                                wsMode:(MIWSConfigMode)wsMode
                            wsDelegate:(id<MIWebSocketDelegate>)wsDelegate;

@end
