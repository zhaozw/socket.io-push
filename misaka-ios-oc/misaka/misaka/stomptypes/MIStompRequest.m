//
//  MIStompRequest.m
//  huwai
//
//  Created by crazylhf on 15/4/19.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "MIStompRequest.h"

@implementation MIStompRequest
{
    uint64_t _reqTimestamp;
    
    MIStompReply * _stompReply;
    
    NSString * _reqAppId;
    NSString * _reqDestination;
    id<MIWSObjectProtocol> _reqBody;
}

- (id)init
{
    if (self = [super init]) {
        MIGLAssertW(NO, @"MIStompRequest init is not support");
    }
    return self;
}

- (id)initWithAppId:(NSString *)reqAppId destination:(NSString *)reqDestination body:(id<MIWSObjectProtocol>)reqBody stompReply:(MIStompReply *)stompReply
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

- (MIStompReply *)stompReply { return _stompReply; }

- (BOOL)timeoutForReconnect
{
    return (STOMP_CURRENT_MICROSECONDS - _reqTimestamp) > (4 * MIMicroSecondPerSecond);
}

- (BOOL)timeoutForRequest
{
    return (STOMP_CURRENT_MICROSECONDS - _reqTimestamp) > (10 * MIMicroSecondPerSecond);
}

- (NSString *)reqAppId { return _reqAppId; }

- (NSString *)reqDestination { return _reqDestination; }

- (id<MIWSObjectProtocol>)reqBody { return _reqBody; }

@end
