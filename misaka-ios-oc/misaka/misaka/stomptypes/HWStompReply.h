//
//  HWStompReply.h
//  huwai
//
//  Created by crazylhf on 15/4/19.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HWStompDefs.h"

@interface HWStompReply : NSObject

- (id)initWithReplyfailed:(stompclient_failed_block_t)replyFailed
             replySucceed:(stompclient_success_block_t)replySucceed
              resultClass:(Class)resultClass;

- (Class)resutlClass;

- (stompclient_failed_block_t)replyFailBlock;

- (stompclient_success_block_t)replySuccessBlock;

@end
