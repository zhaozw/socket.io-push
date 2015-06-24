//
//  HWStompRequest.h
//  huwai
//
//  Created by crazylhf on 15/4/19.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HWGlobalMacros.h"
#import "HWWSObjectProtocol.h"
#import "HWStompReply.h"

@interface HWStompRequest : NSObject

- (id)initWithAppId:(NSString *)reqAppId destination:(NSString *)reqDestination body:(id<HWWSObjectProtocol>)reqBody stompReply:(HWStompReply *)stompReply;

- (HWStompReply *)stompReply;

- (BOOL)timeoutForReconnect;
- (BOOL)timeoutForRequest;

- (NSString *)reqAppId;
- (NSString *)reqDestination;
- (id<HWWSObjectProtocol>)reqBody;

@end