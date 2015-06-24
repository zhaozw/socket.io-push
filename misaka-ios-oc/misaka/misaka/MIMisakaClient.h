//
//  MIMisakaClient.h
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MIWebSocketConfig.h"
#import "MIWSObjectProtocol.h"
#import "MIStompDefs.h"

/// StompClient connect failed : userInfo = nil
MIEXTERN_NOTIFCATION(KMIMisakaClientConnectFailedNotify);

/// StompClient connected : userInfo = nil
MIEXTERN_NOTIFCATION(KMIMisakaClientConnectedNotify);

/// StompClient disconnected : userInfo = nil
MIEXTERN_NOTIFCATION(KMIMisakaClientDisconnectedNotify);

@interface MIMisakaClient : NSObject

- (id)initWithHost:(NSString *)host webSocketConfig:(MIWebSocketConfig *)wsConfig;

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
     bodyObject:(id<MIWSObjectProtocol>)reqObject
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
