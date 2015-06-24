//
//  MIStompConnectMgr.m
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "MINSObject+Notification.h"
#import "MIStompConnectMgr.h"

#import "MIWebSocketFactory.h"

#import "MIStompCommand.h"
#import "MINetworkMonitor.h"


#define MIWebSocketConnectInterval  (3)
#define MIHeartbeatInterval         (60 * MIMicroSecondPerSecond)

@interface MIStompConnectMgr() <MIWebSocketDelegate>

@property (nonatomic, weak)   id<MIMisakaConnectDelegate> stompDelegate;

@property (nonatomic, strong) id<MIWebSocketProtocol> hwWebSocket;

@property (nonatomic, assign) BOOL subscribeConnected;

@property (nonatomic, assign) uint64_t requestId;

@property (nonatomic, assign) uint64_t heartbeatInterval;

@property (nonatomic, assign) uint64_t latestHeartbeat;

@end

@implementation MIStompConnectMgr

- (id)init
{
    if (self = [super init]) {
        MIGLAssertE(NO, @"MIStompConnectMgr init is not support");
    }
    return self;
}

- (id)initWithRequestUrl:(NSString *)requestUrl
                  wsMode:(MIWSConfigMode)wsMode
           stompDelegate:(id<MIMisakaConnectDelegate>)stompDelegate
{
    if (self = [super init]) {
        self.heartbeatInterval  = MIHeartbeatInterval;
        self.subscribeConnected = NO;
        self.latestHeartbeat    = 0;
        self.requestId          = 0;
        
        self.stompDelegate = stompDelegate;
        self.hwWebSocket = [MIWebSocketFactory hwWebSocket:requestUrl
                                                    wsMode:wsMode wsDelegate:self];
        [self initNotification];
        [self connectWebSocket];
    }
    return self;
}

- (void)initNotification
{
    [self addObserver:@selector(onMINetworkStatusChangedNotify:)
                 name:KMINetworkStatusChangedNotify
               object:[MINetworkMonitor sharedInstance]];
}

- (void)dealloc
{
    [self removeObserver];
    [NSObject cancelPreviousPerformRequestsWithTarget:self];
}

#pragma mark -

- (void)reconnect
{
    MIGLLogi(@"Take the initiative to trigger reconnect lbs");
    
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
    [self sendCommand:MIStompCmd_SUBSCRIBE body:nil headers:headers];
}

- (uint64_t)request:(NSString *)reqPath body:(NSString *)body headers:(NSDictionary *)headers
{
    if (self.subscribeConnected) {
        self.requestId += 1;
        
        NSMutableDictionary * mutableHeaders = [NSMutableDictionary dictionaryWithDictionary:headers];
        mutableHeaders[STOMPH_DESTINATION]   = reqPath;
        mutableHeaders[STOMPH_REQUEST_ID]    = @(self.requestId).stringValue;
        
        if ([self sendCommand:MIStompCmd_SEND body:body headers:mutableHeaders]) {
            return self.requestId;
        }
    }
    return 0;
}

#pragma mark - nitification

- (void)onMINetworkStatusChangedNotify:(NSNotification *)notification
{
    MINetworkStatus networkStatus = [notification.userInfo[KMINetworkStatusInfoKey] unsignedIntValue];
    if (MINetwork_NotReachable == networkStatus) {
        MIGLLogi(@"network become MINetwork_NotReachable");
        
        self.subscribeConnected = NO;
        [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(connectWebSocket) object:nil];
    }
    else {
        MIGLLogi(@"network status changed, current status : %@", @(networkStatus));
        [self connectWebSocket];
    }
}

#pragma mark - WebSocketDelegate

- (void)onConnected
{
    MIGLLogi(@"lbs link opened");
    [self sendCommand:MIStompCmd_CONNECT body:nil headers:nil];
}

- (void)onMessage:(NSString *)message
{
    MIGLLogi(@"receive stomp message : %@", message);
    
    MIStompMessage * stompMsg = [[MIStompMessage alloc] initWithStompMsgString:message];
    MIGLLogi(@"after parsed, stompCommand[%@], stompHeaders : %@ , stompBody : %@", stompMsg.stompCommand, stompMsg.stompHeaders, stompMsg.stompBody);
    
    if ([stompMsg.stompCommand isEqualToString:MIStompCmd_CONNECTED]) {
        [self subscribe:@"/user/queue/reply"];
    }
    else if ([stompMsg.stompCommand isEqualToString:MIStompCmd_MESSAGE]) {
        if ([stompMsg.stompBody isEqualToString:@"CONNECTED"] &&
            [stompMsg.stompHeaders[STOMPH_DESTINATION] isEqualToString:@"/user/queue/reply"])
        {
            MIGLLogi(@"connected to path[\"/user/queue/reply\"] success");
            self.subscribeConnected = YES;
            [self.stompDelegate onConnected];
        }
        else {
            [self.stompDelegate onMessage:stompMsg];
        }
    }
}

- (void)onConnectFailed:(NSError *)error
{
    MIGLLoge(@"lbs link open failed");
    self.subscribeConnected = NO;
    [self.stompDelegate onConnectFailed:error];
}

- (void)onDisconnected:(NSString *)reason code:(MIWSStatusCode)code
{
    MIGLLogi(@"lbs link closed");
    self.subscribeConnected = NO;
    [self.stompDelegate onDisconnected:reason statusCode:code];
}

- (void)onHeartbeat
{
    self.latestHeartbeat = STOMP_CURRENT_MICROSECONDS;
}

#pragma mark - private

- (void)connectWebSocket
{
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:_cmd object:nil];
    
    if (MINetwork_NotReachable != [MINetworkMonitor sharedInstance].networkStatus)
    {
        if (self.heartbeatInterval && self.hwWebSocket.isConnected && self.latestHeartbeat) {
            uint64_t curMicroSeconds = STOMP_CURRENT_MICROSECONDS;
            
            if (curMicroSeconds - self.latestHeartbeat > self.heartbeatInterval) {
                MIGLLogi(@"heartbeat fail, reconnect lbs");
                [self reconnect];
            }
        }
        
        if (self.hwWebSocket && self.hwWebSocket.isDisconnected) {
            MIGLLogi(@"begin connect lbs");
            [self.hwWebSocket connect];
        }
        else if (nil == self.hwWebSocket) {
            MIGLLogw(@"self.hwWebSocket is nil");
        }
        [self performSelector:_cmd withObject:nil afterDelay:MIWebSocketConnectInterval];
    }
}

- (BOOL)sendCommand:(NSString *)command body:(NSString *)body headers:(NSDictionary *)headers
{
    if (self.hwWebSocket) {
        MIGLLogi(@"send command[%@] with headers : %@ , body : %@", command, headers, body);
        
        MIStompMessage * stompMsg = [[MIStompMessage alloc] initWithCommand:command headers:headers body:body];
        [self.hwWebSocket send:stompMsg.toStompMsgString];
        return YES;
    }
    return NO;
}

@end
