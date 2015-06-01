//
//  StompClient.h
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SCWSConfig.h"
#import "SCWSBaseObject.h"

typedef void(^stompclient_connected_block_t)(void);
typedef void(^stompclient_success_block_t)(id result);
typedef void(^stompclient_failed_block_t)(int resCode, NSString * message);

@interface StompClient : NSObject

- (id)initWithHost:(NSString *)host webSocketConfig:(SCWSConfig *)wsConfig;

- (void)removeRoute:(NSString *)appId;
- (void)addRoute:(NSString *)appId requestUrl:(NSString *)requestUrl;

- (void)removeConnectedCallback:(NSString *)callbackId;
- (void)addConnectedCallback:(stompclient_connected_block_t)callback callbackId:(NSString *)callbackId;

- (void)request:(NSString *)appId
    destination:(NSString *)destination
     bodyObject:(SCWSBaseObject *)object
        success:(stompclient_success_block_t)succBlock
         failed:(stompclient_failed_block_t)failBlock;

@end
