//
//  HWWebSocketLocal.h
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HWWebSocketDelegate.h"
#import "HWWebSocketProtocol.h"

/**
 *  @brief local WebSocket.
 */
@interface HWWebSocketLocal : NSObject <HWWebSocketProtocol>

- (id)initWithUrl:(NSString *)url wsDelegate:(id<HWWebSocketDelegate>)wsDelegate;

@end
