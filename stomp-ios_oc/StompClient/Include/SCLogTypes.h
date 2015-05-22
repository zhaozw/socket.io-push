//
//  SCLogTypes.h
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * @brief 日志等级
 */
typedef NS_ENUM(NSUInteger, SCLogLevel) {
    SCLogLevel_Unknown     = 0,
    SCLogLevel_Verbose     = 1,
    SCLogLevel_Debug       = 2,
    SCLogLevel_Information = 3,
    SCLogLevel_Warn        = 4,
    SCLogLevel_Error       = 5
};


/**
 * @brief 日志内容
 */
@interface SCLogContent : NSObject

@property (nonatomic, assign) SCLogLevel   level;

@property (nonatomic, strong) NSString   * tag;

@property (nonatomic, strong) NSString   * selector;

@property (nonatomic, strong) NSString   * content;

@property (nonatomic, strong) NSNumber   * linenumber;

+ (SCLogContent *)logContent:(SCLogLevel)level
                         tag:(NSString *)tag
                    selector:(NSString *)selector
                     content:(NSString *)content
                  linenumber:(NSNumber *)linenumber;

@end
