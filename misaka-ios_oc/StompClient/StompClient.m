//
//  StompClient.m
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import "StompClient.h"
#import "SRWebSocket.h"

@interface StompClient() <SRWebSocketDelegate>

@property (nonatomic, strong) NSString * hostUrl;

@property (nonatomic, strong) SRWebSocket * websocket;

/// @{ @"id" : stompclient_connected_block_t }
@property (nonatomic, strong) NSMutableDictionary * connectedCallbacks;

/// @{ @"appId" : @"requestUrl" }
@property (nonatomic, strong) NSMutableDictionary * requestRouteMap;

@end

@implementation StompClient

- (id)initWithHost:(NSString *)host webSocketConfig:(SCWSConfig *)wsConfig
{
    if (self = [self init]) {
        self.hostUrl = host;
        
        self.requestRouteMap    = [[NSMutableDictionary alloc] init];
        self.connectedCallbacks = [[NSMutableDictionary alloc] init];
        
        [self connectWebSocket];
    }
    return self;
}

- (void)request:(NSString *)appId
    destination:(NSString *)destination
     bodyObject:(SCWSBaseObject *)object
        success:(stompclient_success_block_t)succBlock
         failed:(stompclient_failed_block_t)failBlock
{
    ;
}

#pragma mark - route map handle

- (void)removeRoute:(NSString *)appId
{
    [self.requestRouteMap removeObjectForKey:appId];
}

- (void)addRoute:(NSString *)appId requestUrl:(NSString *)requestUrl
{
    [self.requestRouteMap setValue:requestUrl forKey:appId];
}

#pragma mark - connected callbacks handle

- (void)removeConnectedCallback:(NSString *)callbackId
{
    [self.connectedCallbacks removeObjectForKey:callbackId];
}

- (void)addConnectedCallback:(stompclient_connected_block_t)callback callbackId:(NSString *)callbackId
{
    [self.connectedCallbacks setValue:callback forKey:callbackId];
}

#pragma mark - connect websocket

- (void)connectWebSocket
{
    self.websocket.delegate = nil;
    self.websocket = nil;
    
    SRWebSocket * aWebSocket = [[SRWebSocket alloc] initWithURL:[NSURL URLWithString:self.hostUrl]];
    aWebSocket.delegate = self;
    [aWebSocket open];
}

#pragma mark - SRWebSocketDelegate

- (void)webSocketDidOpen:(SRWebSocket *)webSocket
{
    self.websocket = webSocket;
    for (stompclient_connected_block_t aBlock in self.connectedCallbacks) {
        aBlock();
    }
}

- (void)webSocket:(SRWebSocket *)webSocket didFailWithError:(NSError *)error
{
    SCLoge(@"StompClient", @"webSocket didFailWithError:%@", error);
    [self connectWebSocket];
}

- (void)webSocket:(SRWebSocket *)webSocket didCloseWithCode:(NSInteger)code reason:(NSString *)reason wasClean:(BOOL)wasClean
{
    SCLogi(@"StompClient", @"webSocket didCloseWithCode:%@ , reason:%@, wasClean:%@", @(code), reason, @(wasClean));
    [self connectWebSocket];
}

- (void)webSocket:(SRWebSocket *)webSocket didReceiveMessage:(id)message
{
    ;
}

- (void)webSocket:(SRWebSocket *)webSocket didReceivePong:(NSData *)pongPayload
{
    ;
}

@end
