//
//  SCLogTypes.m
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import "SCLogTypes.h"

@implementation SCLogContent

+ (SCLogContent *)logContent:(SCLogLevel)level
                         tag:(NSString *)tag
                    selector:(NSString *)selector
                     content:(NSString *)content
                  linenumber:(NSNumber *)linenumber
{
    SCLogContent * logContent = [[SCLogContent alloc] init];
    
    logContent.tag        = tag;
    logContent.level      = level;
    logContent.selector   = selector;
    logContent.content    = content;
    logContent.linenumber = linenumber;
    
    return logContent;
}

@end
