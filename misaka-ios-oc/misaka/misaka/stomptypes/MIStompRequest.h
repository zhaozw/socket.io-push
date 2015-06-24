//
//  MIStompRequest.h
//  huwai
//
//  Created by crazylhf on 15/4/19.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MIGlobalMacros.h"
#import "MIWSObjectProtocol.h"
#import "MIStompReply.h"

@interface MIStompRequest : NSObject

- (id)initWithAppId:(NSString *)reqAppId destination:(NSString *)reqDestination body:(id<MIWSObjectProtocol>)reqBody stompReply:(MIStompReply *)stompReply;

- (MIStompReply *)stompReply;

- (BOOL)timeoutForReconnect;
- (BOOL)timeoutForRequest;

- (NSString *)reqAppId;
- (NSString *)reqDestination;
- (id<MIWSObjectProtocol>)reqBody;

@end