//
//  HWStompClient.h
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HWWebSocketConfig.h"
#import "HWWSObjectProtocol.h"
#import "HWStompDefs.h"

/// StompClient connect failed : userInfo = nil
HWEXTERN_NOTIFCATION(KHWStompClientConnectFailedNotify);

/// StompClient connected : userInfo = nil
HWEXTERN_NOTIFCATION(KHWStompClientConnectedNotify);

/// StompClient disconnected : userInfo = nil
HWEXTERN_NOTIFCATION(KHWStompClientDisconnectedNotify);

@interface HWStompClient : NSObject

- (id)initWithHost:(NSString *)host webSocketConfig:(HWWebSocketConfig *)wsConfig;

- (BOOL)isConnected;

- (void)removeRoute:(NSString *)reqAppId;
- (void)addRoute:(NSString *)reqAppId requestUrl:(NSString *)requestUrl;

- (void)removeConnectCbkId:(NSString *)connectCbkId;

- (void)addConnectCbkId:(NSString *)connectCbkId
           connectedCbk:(stompclient_connected_block_t)connected
        disConnectedCbk:(stompclient_disconnected_block_t)disconnected
       connectFailedCbk:(stompclient_connect_failed_block_t)connectFailed;

- (void)request:(NSString *)reqAppId
    destination:(NSString *)reqDestination
     bodyObject:(id<HWWSObjectProtocol>)reqObject
        success:(stompclient_success_block_t)succBlock
         failed:(stompclient_failed_block_t)failBlock
    resultClass:(Class)resultClass;

- (void)subscribeBroadcast:(NSString *)pushId
                   success:(stompclient_success_block_t)succBlock
                    failed:(stompclient_failed_block_t)failBlock
               resultClass:(Class)resultClass;

- (void)subscribeUserPush:(NSString *)pushId
                  success:(stompclient_success_block_t)succBlock
                   failed:(stompclient_failed_block_t)failBlock
              resultClass:(Class)resultClass;

@end
