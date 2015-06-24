//
//  MIStompConnectCbk.m
//  huwai
//
//  Created by crazylhf on 15/4/19.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "MIStompConnectCbk.h"
#import "MIGlobalMacros.h"

@implementation MIStompConnectCbk
{
    NSString * _connectCallbackId;
    
    stompclient_connected_block_t      _connectedCallback;
    stompclient_disconnected_block_t   _disconnectedCallback;
    stompclient_connect_failed_block_t _connectFailedCallback;
}

- (id)init
{
    if (self = [super init]) {
        MIGLAssertW(NO, @"MIStompConnectCbk init is not support");
    }
    return self;
}

- (id)initWithConnectCbkId:(NSString *)connectCbkId
              connectedCbk:(stompclient_connected_block_t)connected
           disConnectedCbk:(stompclient_disconnected_block_t)disconnected
          connectFailedCbk:(stompclient_connect_failed_block_t)connectFailed
{
    if (self = [super init]) {
        _connectCallbackId  = connectCbkId;
        
        _connectedCallback     = connected;
        _disconnectedCallback  = disconnected;
        _connectFailedCallback = connectFailed;
    }
    return self;
}

- (NSString *)connectCallbackId { return _connectCallbackId; }

- (stompclient_connected_block_t)connectedCallback { return _connectedCallback; }

- (stompclient_disconnected_block_t)disconnectedCallback { return _disconnectedCallback; }

- (stompclient_connect_failed_block_t)connectFailedCallback { return _connectFailedCallback; }

@end
