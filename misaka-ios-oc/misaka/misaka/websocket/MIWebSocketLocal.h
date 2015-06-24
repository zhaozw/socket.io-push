//
//  MIWebSocketLocal.h
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MIWebSocketDelegate.h"
#import "MIWebSocketProtocol.h"

/**
 *  @brief local WebSocket.
 */
@interface MIWebSocketLocal : NSObject <MIWebSocketProtocol>

- (id)initWithUrl:(NSString *)url wsDelegate:(id<MIWebSocketDelegate>)wsDelegate;

@end
