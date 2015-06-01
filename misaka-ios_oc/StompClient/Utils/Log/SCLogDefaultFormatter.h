//
//  SCLogDefaultFormatter.h
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SCLogFormatterProtocol.h"

@class SCLogContent;

@interface SCLogDefaultFormatter : NSObject <SCLogFormatterProtocol>

+ (id<SCLogFormatterProtocol>)formatter:(SCLogContent *)logContent;

@end
