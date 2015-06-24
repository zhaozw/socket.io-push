//
//  HWStompRequest.m
//  huwai
//
//  Created by crazylhf on 15/4/19.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "HWStompRequest.h"

@implementation HWStompRequest
{
    uint64_t _reqTimestamp;
    
    HWStompReply * _stompReply;
    
    NSString * _reqAppId;
    NSString * _reqDestination;
    id<HWWSObjectProtocol> _reqBody;
}

- (id)init
{
    if (self = [super init]) {
        HWGLAssertW(NO, @"HWStompRequest init is not support");
    }
    return self;
}

- (id)initWithAppId:(NSString *)reqAppId destination:(NSString *)reqDestination body:(id<HWWSObjectProtocol>)reqBody stompReply:(HWStompReply *)stompReply
{
    if (self = [super init]) {
        _stompReply     = stompReply;
        
        _reqBody        = reqBody;
        _reqAppId       = reqAppId;
        _reqDestination = reqDestination;
        
        _reqTimestamp   = STOMP_CURRENT_MICROSECONDS;
    }
    return self;
}

- (HWStompReply *)stompReply { return _stompReply; }

- (BOOL)timeoutForReconnect
{
    return (STOMP_CURRENT_MICROSECONDS - _reqTimestamp) > (4 * HWMicroSecondPerSecond);
}

- (BOOL)timeoutForRequest
{
    return (STOMP_CURRENT_MICROSECONDS - _reqTimestamp) > (10 * HWMicroSecondPerSecond);
}

- (NSString *)reqAppId { return _reqAppId; }

- (NSString *)reqDestination { return _reqDestination; }

- (id<HWWSObjectProtocol>)reqBody { return _reqBody; }

@end
