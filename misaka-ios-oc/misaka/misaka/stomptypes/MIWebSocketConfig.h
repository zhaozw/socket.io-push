//
//  MIWebSocketConfig.h
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, MIWSConfigMode) {
    MIWSConfigMode_MIN,   /// 下界
    MIWSConfigMode_LOCAL,
    MIWSConfigMode_REMOTE,
    MIWSConfigMode_MAX,   /// 上界
};

@interface MIWebSocketConfig : NSObject

- (id)initWithMode:(MIWSConfigMode)mode;

@property (nonatomic, assign) MIWSConfigMode mode;

@property (nonatomic, assign) BOOL isDataAsBody;

@property (nonatomic, assign) uint timeout;

@property (nonatomic, strong) NSString * timeoutTips;

@property (nonatomic, strong) NSString * connectFailTips;

@property (nonatomic, strong) NSString * resDataParseFailTips;

@end
