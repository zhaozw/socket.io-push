//
//  SCLogUtil.m
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import "SCLogUtil.h"
#import "SCLogStdOutWriter.h"

@interface SCLogUtil()

@property (nonatomic, strong) id<SCLogWriterProtocol> logWriter;

@property (nonatomic, strong) stompclient_log_block_t logBlock;

@end

@implementation SCLogUtil

+ (void)registerLogOption:(stompclient_log_block_t)logBlock
{
    [SCLogUtil sharedInstance].logBlock = logBlock;
}

+ (void)verbose:(NSString *)tag
       selector:(NSString *)selector
     linenumber:(NSNumber *)linenumber format:(NSString *)format, ...
{
    va_list ap;
    va_start(ap, format);
    [self log:SCLogLevel_Verbose tag:tag selector:selector linenumber:linenumber format:format argList:ap];
    va_end(ap);
}

+ (void)debug:(NSString *)tag
     selector:(NSString *)selector
   linenumber:(NSNumber *)linenumber format:(NSString *)format, ...
{
    va_list ap;
    va_start(ap, format);
    [self log:SCLogLevel_Debug tag:tag selector:selector linenumber:linenumber format:format argList:ap];
    va_end(ap);
}

+ (void)warn:(NSString *)tag
    selector:(NSString *)selector
  linenumber:(NSNumber *)linenumber format:(NSString *)format, ...
{
    va_list ap;
    va_start(ap, format);
    [self log:SCLogLevel_Warn tag:tag selector:selector linenumber:linenumber format:format argList:ap];
    va_end(ap);
}

+ (void)error:(NSString *)tag
     selector:(NSString *)selector
   linenumber:(NSNumber *)linenumber format:(NSString *)format, ...
{
    va_list ap;
    va_start(ap, format);
    [self log:SCLogLevel_Error tag:tag selector:selector linenumber:linenumber format:format argList:ap];
    va_end(ap);
}

+ (void)information:(NSString *)tag
           selector:(NSString *)selector
         linenumber:(NSNumber *)linenumber format:(NSString *)format, ...
{
    va_list ap;
    va_start(ap, format);
    [self log:SCLogLevel_Information tag:tag selector:selector linenumber:linenumber format:format argList:ap];
    va_end(ap);
}

#pragma mark - util

+ (void)log:(SCLogLevel)level tag:(NSString *)tag
   selector:(NSString *)selector linenumber:(NSNumber *)linenumber
     format:(NSString *)format argList:(va_list)argList
{
    NSString * content = [[NSString alloc] initWithFormat:format arguments:argList];
    SCLogContent * logContent = [SCLogContent logContent:level tag:tag
                                                selector:selector content:content linenumber:linenumber];
    
    SCLogUtil * logUtil = [SCLogUtil sharedInstance];
    if (logUtil.logBlock) {
        logUtil.logBlock(logContent);
    }
    else {
        [logUtil.logWriter log:logContent];
    }
}

#pragma mark - singleton

+ (instancetype)sharedInstance
{
    static SCLogUtil * aInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{ aInstance = [[SCLogUtil alloc] init]; });
    return aInstance;
}

- (id)init
{
    if (self = [super init]) {
        self.logBlock = nil;
        self.logWriter   = [[SCLogStdOutWriter alloc] init];
    }
    return self;
}

@end
