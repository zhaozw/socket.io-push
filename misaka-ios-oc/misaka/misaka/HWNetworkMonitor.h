//
//  HWNetworkMonitor.h
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HWGlobalMacros.h"

/// 网络状态变化通知 : userInfo = @{ HWNetworkStatusInfoKey : @(HWNetworkStatus)}
HWEXTERN_NOTIFCATION(KHWNetworkStatusChangedNotify);
HWEXTERN_TYPE_NOTIFICATIONKEY(@(HWNetworkStatus), KHWNetworkStatusInfoKey);

typedef NS_ENUM(unsigned, HWNetworkStatus) {
    HWNetwork_NotReachable = 0,     /// 网络不可达
    HWNetwork_Wifi         = 1,     /// wifi网络
    HWNetwork_2G           = 2,     /// 2G网络
    HWNetwork_3G           = 3,     /// 3G网络
    HWNetwork_4G           = 4,     /// 4G网络
};

/**
 *  @brief 移动网络监听, 监听网络状态变化并抛出KHWNetworkStatusChangedNotify通知.
 */
@interface HWNetworkMonitor : NSObject

/// 开启网络状态监听
- (void)enable;

/// 关闭网络状态监听
- (void)disable;

/// 当前网络状态
- (HWNetworkStatus)networkStatus;

/// 单例
HWDECLARE_SINGLETON()

@end
