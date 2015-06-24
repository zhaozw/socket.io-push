//
//  HWWebSocketConfig.h
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, HWWSConfigMode) {
    HWWSConfigMode_MIN,   /// 下界
    HWWSConfigMode_LOCAL,
    HWWSConfigMode_REMOTE,
    HWWSConfigMode_MAX,   /// 上界
};

@interface HWWebSocketConfig : NSObject

- (id)initWithMode:(HWWSConfigMode)mode;

@property (nonatomic, assign) HWWSConfigMode mode;

@property (nonatomic, assign) BOOL isDataAsBody;

@property (nonatomic, assign) uint timeout;

@property (nonatomic, strong) NSString * timeoutTips;

@property (nonatomic, strong) NSString * connectFailTips;

@property (nonatomic, strong) NSString * resDataParseFailTips;

@end
