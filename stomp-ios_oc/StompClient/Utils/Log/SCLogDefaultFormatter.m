//
//  SCLogDefaultFormatter.m
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import "SCLogDefaultFormatter.h"
#import "SCLogTypes.h"

#define SYSTEMCALL_GETTID   (286)

static NSDateFormatter * g_dateFormat = nil;
static NSString        * g_logFormat  = nil;
static NSArray         * g_levelNames = nil;

@implementation SCLogDefaultFormatter
{
    NSString           * _formatLogContent;
}

+ (id<SCLogFormatterProtocol>)formatter:(SCLogContent *)logContent
{
    SCLogDefaultFormatter * formatter = [[SCLogDefaultFormatter alloc] init];
    [formatter setFormatLogContent:logContent];
    return formatter;
}

- (void)setFormatLogContent:(SCLogContent *)logContent
{
    _formatLogContent = [NSString stringWithFormat: self.logFormat, self.curDateString, self.curPID, self.curTID, self.levelNames[logContent.level], logContent.tag, logContent.linenumber, logContent.selector, logContent.content];
}

- (NSString *)formatLogContent {
    return _formatLogContent;
}

#pragma mark - format util

- (NSString *)curDateString {
    return [self.dateFormat stringFromDate:[NSDate date]];
}

- (NSUInteger)curPID {
    return getegid();
}

- (NSUInteger)curTID {
    return syscall(SYSTEMCALL_GETTID);
}

#pragma mark - private

- (NSDateFormatter *)dateFormat {
    if (!g_dateFormat) {
        g_dateFormat = [[NSDateFormatter alloc] init];
        g_dateFormat.dateFormat = @"yyyy-MM-dd HH:mm:ss.SSS";
    }
    return g_dateFormat;
}

- (NSArray *)levelNames {
    if (!g_levelNames) {
        g_levelNames = @[ @"Unknown", @"V", @"D", @"I", @"W", @"E" ];
    }
    return g_levelNames;
}

- (NSString *)logFormat {
    if (!g_logFormat) {
        g_logFormat  = @"%@ [pid:%04u-tid:%04lu][%@][%@]%@ %@ %@";
    }
    return g_logFormat;
}

@end
