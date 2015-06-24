//
//  HWStompMessage.h
//  huwai
//
//  Created by crazylhf on 15/4/17.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HWStompMessage : NSObject

- (id)initWithCommand:(NSString *)command
              headers:(NSDictionary *)headers body:(NSString *)body;

- (id)initWithStompMsgString:(NSString *)stompMsgString;

- (NSString *)toStompMsgString;

- (NSDictionary *)stompHeaders;
- (NSString *)stompCommand;
- (NSString *)stompBody;

@end
