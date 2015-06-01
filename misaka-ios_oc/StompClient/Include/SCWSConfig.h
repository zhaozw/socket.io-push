//
//  SCWSConfig.h
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, SCWSConfigMode) {
    SCWSConfigMode_MIN,   /// 下界
    SCWSConfigMode_LOCAL,
    SCWSConfigMode_REMOTE,
    SCWSConfigMode_MAX,   /// 上界
};

@interface SCWSConfig : NSObject

- (id)initWithMode:(SCWSConfigMode)mode;

@property (nonatomic, assign) SCWSConfigMode mode;

@property (nonatomic, assign) BOOL isDataAsBody;

@property (nonatomic, assign) uint timeout;

@property (nonatomic, strong) NSString * timeoutTips;

@property (nonatomic, strong) NSString * connectFailTips;

@property (nonatomic, strong) NSString * resDataParseFailTips;

@end
