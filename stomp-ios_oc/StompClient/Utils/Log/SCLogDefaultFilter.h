//
//  SCLogDefaultFilter.h
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SCLogFilterProtocol.h"
#import "SCLogTypes.h"

@interface SCLogDefaultFilter : NSObject <SCLogFilterProtocol>

+ (id<SCLogFilterProtocol>)filter:(SCLogLevel)level;

@end
