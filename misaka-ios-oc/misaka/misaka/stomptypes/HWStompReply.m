//
//  HWStompReply.m
//  huwai
//
//  Created by crazylhf on 15/4/19.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "HWStompReply.h"
#import "HWGlobalMacros.h"

@implementation HWStompReply
{
    Class                       _resultClass;
    stompclient_failed_block_t  _failBlock;
    stompclient_success_block_t _successBlock;
}

- (id)init
{
    if (self = [super init]) {
        HWGLAssertW(NO, @"HWStompReply init is not support");
    }
    return self;
}

- (id)initWithReplyfailed:(stompclient_failed_block_t)replyFailed
             replySucceed:(stompclient_success_block_t)replySucceed
              resultClass:(Class)resultClass
{
    if (self = [super init]) {
        _resultClass  = resultClass;
        _failBlock    = replyFailed;
        _successBlock = replySucceed;
    }
    return self;
}

- (Class)resutlClass
{
    return (_resultClass ? _resultClass : [NSObject class]);
}

- (stompclient_failed_block_t)replyFailBlock
{
    return _failBlock;
}

- (stompclient_success_block_t)replySuccessBlock
{
    return _successBlock;
}

@end
