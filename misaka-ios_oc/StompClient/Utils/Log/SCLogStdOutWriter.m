//
//  SCLogStdOutWriter.m
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import "SCLogStdOutWriter.h"
#import "SCLogDefaultFilter.h"
#import "SCLogDefaultFormatter.h"

@implementation SCLogStdOutWriter

- (void)registerOption:(id)option {}

- (void)log:(SCLogContent *)logContent
{
    id<SCLogFilterProtocol> filter = [SCLogDefaultFilter filter:logContent.level];
    if (filter.isPermited)
    {
        id<SCLogFormatterProtocol> formatter = [SCLogDefaultFormatter formatter:logContent];
        fprintf(stdout, "%s\n", formatter.formatLogContent.UTF8String);
    }
}

@end
