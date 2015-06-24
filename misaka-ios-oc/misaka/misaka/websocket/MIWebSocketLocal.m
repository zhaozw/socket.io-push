//
//  MIWebSocketLocal.m
//  huwai
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "MIGlobalMacros.h"
#import "MIWebSocketLocal.h"
#import "SRWebSocket.h"
#import "JSONKit.h"

#define WebSocketTimeoutSeconds     (4)

@interface MIWebSocketLocal() <SRWebSocketDelegate>

@property (nonatomic, weak)   id<MIWebSocketDelegate> wsDelegate;

@property (nonatomic, strong) SRWebSocket * realSRWebSocket;

@property (nonatomic, strong) SRWebSocket * srWebSocket;

@property (nonatomic, strong) NSString * url;

@end

@implementation MIWebSocketLocal

- (id)init
{
    if (self = [super init]) {
        MIGLAssertW(self.url.length, @"url property mustn't 0 length");
    }
    return self;
}

- (id)initWithUrl:(NSString *)url wsDelegate:(id<MIWebSocketDelegate>)wsDelegate
{
    if (self = [super init]) {
        self.wsDelegate  = wsDelegate;
        self.srWebSocket = nil;
        self.realSRWebSocket = nil;
        
        MIGLAssertW(url.length, @"url param mustn't be 0 length");
        self.url         = url;
    }
    return self;
}

#pragma mark -

- (void)connect
{
    MIGLLogi(@"begin connect websocket, url:%@", self.url);
    
    NSURL * reqURL = [NSURL URLWithString:self.url];
    NSMutableURLRequest * wsRequest = [[NSMutableURLRequest alloc] initWithURL:reqURL];
    wsRequest.timeoutInterval = WebSocketTimeoutSeconds;
    
    self.srWebSocket = [[SRWebSocket alloc] initWithURLRequest:wsRequest];
    if (self.srWebSocket) {
        self.realSRWebSocket = nil;
        self.realSRWebSocket.delegate = nil;
        
        self.srWebSocket.delegate = self;
        [self.srWebSocket open];
    }
}

- (void)close
{
    MIGLLogi(@"close websocket");
    [self.realSRWebSocket close];
}

- (BOOL)isConnected
{
    return (self.realSRWebSocket && (SR_OPEN == self.realSRWebSocket.readyState));
}

- (BOOL)isDisconnected
{
    return (nil == self.srWebSocket) && (nil == self.realSRWebSocket || (SR_CLOSED == self.realSRWebSocket.readyState));
}

- (BOOL)send:(NSString *)data
{
    MIGLLogi(@"send websocket data, srWebSocket[%@], curState[%@], data: %@",
             self.realSRWebSocket, @(self.realSRWebSocket.readyState), data);
    
    if (self.isConnected) {
        NSString * sendData = [NSString stringWithFormat:@"[%@]", data.JSONString];
        [self.realSRWebSocket send:sendData];
        return YES;
    }
    return NO;
}

#pragma mark - SRWebSocketDelegate

- (void)webSocket:(SRWebSocket *)webSocket didReceiveMessage:(id)message
{
    MIGLLogi(@"WebSocket receive message:%@", message);
    if ([message isKindOfClass:[NSString class]]) {
        NSString * strMsg = message;
        
        if ([strMsg hasPrefix:@"c[\""])
        {
            [self close];
        }
        else if (self.wsDelegate)
        {
            if ([strMsg isEqualToString:@"o"])
            {
                [self.wsDelegate onConnected];
            }
            else if ([strMsg hasPrefix:@"a[\""])
            {
                NSRange range = NSMakeRange(3, strMsg.length - 4);
                [self.wsDelegate onMessage:[message substringWithRange:range]];
            }
            [self.wsDelegate onHeartbeat];
        }
    }
}

- (void)webSocketDidOpen:(SRWebSocket *)webSocket
{
    self.srWebSocket.delegate = nil;
    self.srWebSocket = nil;
    
    self.realSRWebSocket = webSocket;
    if (webSocket) {
        MIGLLogi(@"WebSocket opened!");
        self.realSRWebSocket.delegate = self;
    }
    else {
        MIGLLoge(@"WebSocket opened, but webSocket is nil!");
        [self.wsDelegate onConnectFailed:nil];
    }
}

- (void)webSocket:(SRWebSocket *)webSocket didFailWithError:(NSError *)error
{
    MIGLLoge(@"WebSocket open fail!");
    self.srWebSocket.delegate = nil;
    self.srWebSocket = nil;
    
    self.realSRWebSocket.delegate = nil;
    self.realSRWebSocket = nil;
    
    [self.wsDelegate onConnectFailed:error];
}

- (void)webSocket:(SRWebSocket *)webSocket didCloseWithCode:(NSInteger)code reason:(NSString *)reason wasClean:(BOOL)wasClean
{
    MIGLLogi(@"WebSocket closed, code:%@, reason:%@!", @(code), reason);
    self.srWebSocket.delegate = nil;
    self.srWebSocket = nil;
    
    self.realSRWebSocket.delegate = nil;
    self.realSRWebSocket = nil;
    
    [self.wsDelegate onDisconnected:reason code:(MIWSStatusCode)code];
}

- (void)webSocket:(SRWebSocket *)webSocket didReceivePong:(NSData *)pongPayload
{
    MIGLLogi(@"WebSocket receive pong, length:%@", @(pongPayload.length));
}

@end
