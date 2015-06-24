//
//  MIStompConnectCbk.h
//  huwai
//
//  Created by crazylhf on 15/4/19.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MIStompDefs.h"

/**
 *  @brief stomp connect callback.
 */
@interface MIStompConnectCbk : NSObject

- (id)initWithConnectCbkId:(NSString *)connectCbkId
              connectedCbk:(stompclient_connected_block_t)connected
           disConnectedCbk:(stompclient_disconnected_block_t)disconnected
          connectFailedCbk:(stompclient_connect_failed_block_t)connectFailed;

- (NSString *)connectCallbackId;

- (stompclient_connected_block_t)connectedCallback;

- (stompclient_disconnected_block_t)disconnectedCallback;

- (stompclient_connect_failed_block_t)connectFailedCallback;

@end
