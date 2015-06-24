//
//  HWStompConnectMgr.m
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "HWNSObject+Notification.h"
#import "HWStompConnectMgr.h"

#import "HWWebSocketFactory.h"

#import "HWStompCommand.h"
#import "HWNetworkMonitor.h"


#define HWWebSocketConnectInterval  (3)
#define HWHeartbeatInterval         (60 * HWMicroSecondPerSecond)

@interface HWStompConnectMgr() <HWWebSocketDelegate>

@property (nonatomic, weak)   id<HWStompConnectDelegate> stompDelegate;

@property (nonatomic, strong) id<HWWebSocketProtocol> hwWebSocket;

@property (nonatomic, assign) BOOL subscribeConnected;

@property (nonatomic, assign) uint64_t requestId;

@property (nonatomic, assign) uint64_t heartbeatInterval;

@property (nonatomic, assign) uint64_t latestHeartbeat;

@end

@implementation HWStompConnectMgr

- (id)init
{
    if (self = [super init]) {
        HWGLAssertE(NO, @"HWStompConnectMgr init is not support");
    }
    return self;
}

- (id)initWithRequestUrl:(NSString *)requestUrl
                  wsMode:(HWWSConfigMode)wsMode
           stompDelegate:(id<HWStompConnectDelegate>)stompDelegate
{
    if (self = [super init]) {
        self.heartbeatInterval  = HWHeartbeatInterval;
        self.subscribeConnected = NO;
        self.latestHeartbeat    = 0;
        self.requestId          = 0;
        
        self.stompDelegate = stompDelegate;
        self.hwWebSocket = [HWWebSocketFactory hwWebSocket:requestUrl
                                                    wsMode:wsMode wsDelegate:self];
        [self initNotification];
        [self connectWebSocket];
    }
    return self;
}

- (void)initNotification
{
    [self addObserver:@selector(onHWNetworkStatusChangedNotify:)
                 name:KHWNetworkStatusChangedNotify
               object:[HWNetworkMonitor sharedInstance]];
}

- (void)dealloc
{
    [self removeObserver];
    [NSObject cancelPreviousPerformRequestsWithTarget:self];
}

#pragma mark -

- (void)reconnect
{
    HWGLLogi(@"Take the initiative to trigger reconnect lbs");
    
    self.subscribeConnected = NO;
    [self.hwWebSocket close];
}

- (BOOL)isConnected
{
    return self.subscribeConnected;
}

- (void)subscribe:(NSString *)reqPath
{
    NSString * subscribeId = [NSString stringWithFormat:@"sub_%@", reqPath];
    NSDictionary * headers = @{
                               STOMPH_DESTINATION : reqPath,
                               STOMPH_SUBSCRIPTION_ID : subscribeId
                               };
    [self sendCommand:HWStompCmd_SUBSCRIBE body:nil headers:headers];
}

- (uint64_t)request:(NSString *)reqPath body:(NSString *)body headers:(NSDictionary *)headers
{
    if (self.subscribeConnected) {
        self.requestId += 1;
        
        NSMutableDictionary * mutableHeaders = [NSMutableDictionary dictionaryWithDictionary:headers];
        mutableHeaders[STOMPH_DESTINATION]   = reqPath;
        mutableHeaders[STOMPH_REQUEST_ID]    = @(self.requestId).stringValue;
        
        if ([self sendCommand:HWStompCmd_SEND body:body headers:mutableHeaders]) {
            return self.requestId;
        }
    }
    return 0;
}

#pragma mark - nitification

- (void)onHWNetworkStatusChangedNotify:(NSNotification *)notification
{
    HWNetworkStatus networkStatus = [notification.userInfo[KHWNetworkStatusInfoKey] unsignedIntValue];
    if (HWNetwork_NotReachable == networkStatus) {
        HWGLLogi(@"network become HWNetwork_NotReachable");
        
        self.subscribeConnected = NO;
        [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(connectWebSocket) object:nil];
    }
    else {
        HWGLLogi(@"network status changed, current status : %@", @(networkStatus));
        [self connectWebSocket];
    }
}

#pragma mark - WebSocketDelegate

- (void)onConnected
{
    HWGLLogi(@"lbs link opened");
    [self sendCommand:HWStompCmd_CONNECT body:nil headers:nil];
}

- (void)onMessage:(NSString *)message
{
    HWGLLogi(@"receive stomp message : %@", message);
    
    HWStompMessage * stompMsg = [[HWStompMessage alloc] initWithStompMsgString:message];
    HWGLLogi(@"after parsed, stompCommand[%@], stompHeaders : %@ , stompBody : %@", stompMsg.stompCommand, stompMsg.stompHeaders, stompMsg.stompBody);
    
    if ([stompMsg.stompCommand isEqualToString:HWStompCmd_CONNECTED]) {
        [self subscribe:@"/user/queue/reply"];
    }
    else if ([stompMsg.stompCommand isEqualToString:HWStompCmd_MESSAGE]) {
        if ([stompMsg.stompBody isEqualToString:@"CONNECTED"] &&
            [stompMsg.stompHeaders[STOMPH_DESTINATION] isEqualToString:@"/user/queue/reply"])
        {
            HWGLLogi(@"connected to path[\"/user/queue/reply\"] success");
            self.subscribeConnected = YES;
            [self.stompDelegate onStompConnected];
        }
        else {
            [self.stompDelegate onStompMessage:stompMsg];
        }
    }
}

- (void)onConnectFailed:(NSError *)error
{
    HWGLLoge(@"lbs link open failed");
    self.subscribeConnected = NO;
    [self.stompDelegate onStompConnectFailed:error];
}

- (void)onDisconnected:(NSString *)reason code:(HWWSStatusCode)code
{
    HWGLLogi(@"lbs link closed");
    self.subscribeConnected = NO;
    [self.stompDelegate onStompDisconnected:reason statusCode:code];
}

- (void)onHeartbeat
{
    self.latestHeartbeat = STOMP_CURRENT_MICROSECONDS;
}

#pragma mark - private

- (void)connectWebSocket
{
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:_cmd object:nil];
    
    if (HWNetwork_NotReachable != [HWNetworkMonitor sharedInstance].networkStatus)
    {
        if (self.heartbeatInterval && self.hwWebSocket.isConnected && self.latestHeartbeat) {
            uint64_t curMicroSeconds = STOMP_CURRENT_MICROSECONDS;
            
            if (curMicroSeconds - self.latestHeartbeat > self.heartbeatInterval) {
                HWGLLogi(@"heartbeat fail, reconnect lbs");
                [self reconnect];
            }
        }
        
        if (self.hwWebSocket && self.hwWebSocket.isDisconnected) {
            HWGLLogi(@"begin connect lbs");
            [self.hwWebSocket connect];
        }
        else if (nil == self.hwWebSocket) {
            HWGLLogw(@"self.hwWebSocket is nil");
        }
        [self performSelector:_cmd withObject:nil afterDelay:HWWebSocketConnectInterval];
    }
}

- (BOOL)sendCommand:(NSString *)command body:(NSString *)body headers:(NSDictionary *)headers
{
    if (self.hwWebSocket) {
        HWGLLogi(@"send command[%@] with headers : %@ , body : %@", command, headers, body);
        
        HWStompMessage * stompMsg = [[HWStompMessage alloc] initWithCommand:command headers:headers body:body];
        [self.hwWebSocket send:stompMsg.toStompMsgString];
        return YES;
    }
    return NO;
}

@end
