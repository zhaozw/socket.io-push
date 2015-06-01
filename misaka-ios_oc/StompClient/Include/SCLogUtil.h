//
//  SCLogUtil.h
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SCLogTypes.h"

#define SCLogw(_tag, ...) \
            [SCLogUtil warn:_tag \
                   selector:[NSString stringWithUTF8String:__FUNCTION__] \
                 linenumber:@(__LINE__) format:__VA_ARGS__]

#define SCLoge(_tag, ...) \
            [SCLogUtil error:_tag \
                    selector:[NSString stringWithUTF8String:__FUNCTION__] \
                  linenumber:@(__LINE__) format:__VA_ARGS__]

#define SCLogi(_tag, ...) \
            [SCLogUtil information:_tag \
                          selector:[NSString stringWithUTF8String:__FUNCTION__] \
                        linenumber:@(__LINE__) format:__VA_ARGS__]

typedef void (^stompclient_log_block_t)(SCLogContent * logContent);

@interface SCLogUtil : NSObject

/// 日志输出方式的注册接口
+ (void)registerLogOption:(stompclient_log_block_t)logBlock;

+ (void)verbose:(NSString *)tag
       selector:(NSString *)selector linenumber:(NSNumber *)linenumber
         format:(NSString *)format, ...NS_FORMAT_FUNCTION(4,5);

+ (void)debug:(NSString *)tag
     selector:(NSString *)selector linenumber:(NSNumber *)linenumber
       format:(NSString *)format, ...NS_FORMAT_FUNCTION(4,5);

+ (void)warn:(NSString *)tag
    selector:(NSString *)selector linenumber:(NSNumber *)linenumber
      format:(NSString *)format, ...NS_FORMAT_FUNCTION(4,5);

+ (void)error:(NSString *)tag
     selector:(NSString *)selector linenumber:(NSNumber *)linenumber
       format:(NSString *)format, ...NS_FORMAT_FUNCTION(4,5);

+ (void)information:(NSString *)tag
           selector:(NSString *)selector linenumber:(NSNumber *)linenumber
             format:(NSString *)format, ...NS_FORMAT_FUNCTION(4,5);

@end
