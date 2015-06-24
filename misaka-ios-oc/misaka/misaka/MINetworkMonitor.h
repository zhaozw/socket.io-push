//
//  MINetworkMonitor.h
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MIGlobalMacros.h"

/// 网络状态变化通知 : userInfo = @{ MINetworkStatusInfoKey : @(MINetworkStatus)}
MIEXTERN_NOTIFCATION(KMINetworkStatusChangedNotify);
MIEXTERN_TYPE_NOTIFICATIONKEY(@(MINetworkStatus), KMINetworkStatusInfoKey);

typedef NS_ENUM(unsigned, MINetworkStatus) {
    MINetwork_NotReachable = 0,     /// 网络不可达
    MINetwork_Wifi         = 1,     /// wifi网络
    MINetwork_2G           = 2,     /// 2G网络
    MINetwork_3G           = 3,     /// 3G网络
    MINetwork_4G           = 4,     /// 4G网络
};

/**
 *  @brief 移动网络监听, 监听网络状态变化并抛出KMINetworkStatusChangedNotify通知.
 */
@interface MINetworkMonitor : NSObject

/// 开启网络状态监听
- (void)enable;

/// 关闭网络状态监听
- (void)disable;

/// 当前网络状态
- (MINetworkStatus)networkStatus;

/// 单例
MIDECLARE_SINGLETON()

@end
