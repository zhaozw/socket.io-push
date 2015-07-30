//
//  MIMisakaClient.m
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//
#import "MINSObject+Notification.h"
#import "MIStompConnectMgr.h"
#import "MIStompRequest.h"
#import "MIMisakaClient.h"

#import "MIStompReply.h"
#import "MIStompConnectCbk.h"

#import "MIRandomUtil.h"
#import "MINetworkMonitor.h"

#import <objc/runtime.h>

#define MIStompReqDetectionInterval     (1)

MIDEFINE_NOTIFCATION(KMIMisakaClientConnectFailedNotify);

MIDEFINE_NOTIFCATION(KMIMisakaClientConnectedNotify);

MIDEFINE_NOTIFCATION(KMIMisakaClientDisconnectedNotify);

@interface MIMisakaClient() <MIMisakaConnectDelegate>

@property (nonatomic, strong) MIStompConnectMgr * hwStompConnect;

@property (nonatomic, strong) MIWebSocketConfig * wsConfig;


/// @{ @"appId" : @"requestUrl" }
@property (nonatomic, strong) NSMutableDictionary * requestRouteMap;


/// @{ @"reqId" : MIStompRequest }
@property (nonatomic, strong) NSMutableDictionary * requestMap;

/// @{ @"destinationPath" : MIStompReply }
@property (nonatomic, strong) NSMutableDictionary * subscribeMap;

/// @{ @"id" : stompclient_connected_block_t }
@property (nonatomic, strong) NSMutableDictionary * connectedCbkMap;

@end

@implementation MIMisakaClient

- (id)init
{
    if (self = [super init]) {
        MIGLAssertW(NO, @"MIMisakaClient init is not support");
    }
    return self;
}

- (id)initWithHost:(NSString *)host webSocketConfig:(MIWebSocketConfig *)wsConfig
{
    if (self = [super init]) {
        [[MINetworkMonitor sharedInstance] enable];
        
        NSString * wsUrl = [NSString stringWithFormat:@"%@/stomp/1/%@/websocket", host, [MIRandomUtil randomAlphaNumeric:12]];
        self.hwStompConnect = [[MIStompConnectMgr alloc] initWithRequestUrl:wsUrl wsMode:wsConfig.mode stompDelegate:self];
        
        self.wsConfig = wsConfig;
        
        self.requestRouteMap = [[NSMutableDictionary alloc] init];
        
        self.requestMap      = [[NSMutableDictionary alloc] init];
        self.subscribeMap    = [[NSMutableDictionary alloc] init];
        self.connectedCbkMap = [[NSMutableDictionary alloc] init];
        
        [self timeoutDetection];
    }
    return self;
}

- (BOOL)isConnected { return self.hwStompConnect.isConnected; }

#pragma mark - request

- (void)request:(NSString *)reqAppId
    destination:(NSString *)reqDestination
     bodyObject:(id<MIWSObjectProtocol>)reqObject
        success:(stompclient_success_block_t)succBlock
         failed:(stompclient_failed_block_t)failBlock
    resultClass:(Class)resultClass
{
    MIStompReply * stompReply = [[MIStompReply alloc] initWithReplyfailed:failBlock replySucceed:succBlock resultClass:resultClass];
    MIStompRequest * stompReq = [[MIStompRequest alloc] initWithAppId:reqAppId destination:reqDestination body:reqObject stompReply:stompReply];
    
    if (self.hwStompConnect.isConnected) {
        NSString * reqUrl = [NSString stringWithFormat:@"%@%@", self.requestRouteMap[reqAppId], reqDestination];
        NSString * reqJsonString = reqObject.toJsonString;
        
        MIGLLogi(@"send request[%@], destination[%@], reqUrl[%@], object : %@", reqAppId, reqDestination, reqUrl, reqObject.toJsonString);
        
        NSDictionary * headerDic = nil;
        if (self.wsConfig.isDataAsBody) {
            headerDic = @{ @"url" : reqUrl, @"appId" : reqAppId, @"useNyy" : @"true" };
        } else {
            headerDic = @{ @"url" : reqUrl, @"appId" : reqAppId };
        }
        
        uint64_t reqId = [self.hwStompConnect request:@"/request" body:reqJsonString headers:headerDic];
        if (reqId) {
            self.requestMap[@(reqId).stringValue] = stompReq;
        } else if (failBlock) {
            failBlock(MIStompRes_ConnectFailed, self.wsConfig.connectFailTips);
        }
    }
    else {
        self.requestMap[[MIRandomUtil randomAlphaNumeric:12]] = stompReq;
    }
}

#pragma mark - subscribe

- (void)subscribeBroadcast:(NSString *)pushId
                   success:(stompclient_success_block_t)succBlock
                    failed:(stompclient_failed_block_t)failBlock
               resultClass:(Class)resultClass
{
    MIGLLogi(@"subscribe broadcast, pushId[%@]", pushId);
    
    MIStompReply * stompReply = [[MIStompReply alloc] initWithReplyfailed:failBlock replySucceed:succBlock resultClass:resultClass];
    [self subscribe:[NSString stringWithFormat:@"/topic/%@", pushId] stompReply:stompReply];
}

- (void)subscribeUserPush:(NSString *)pushId
                  success:(stompclient_success_block_t)succBlock
                   failed:(stompclient_failed_block_t)failBlock
              resultClass:(Class)resultClass
{
    MIGLLogi(@"subscribe user push, pushId[%@]", pushId);
    
    MIStompReply * stompReply = [[MIStompReply alloc] initWithReplyfailed:failBlock replySucceed:succBlock resultClass:resultClass];
    [self subscribe:[NSString stringWithFormat:@"/user/queue/%@", pushId] stompReply:stompReply];
}

- (void)subscribe:(NSString *)destination stompReply:(MIStompReply *)stompReply
{
    [self.subscribeMap setValue:stompReply forKey:destination];
    [self.hwStompConnect subscribe:destination];
}

#pragma mark - route map handle

- (void)removeRoute:(NSString *)reqAppId
{
    [self.requestRouteMap removeObjectForKey:reqAppId];
}

- (void)addRoute:(NSString *)reqAppId requestUrl:(NSString *)requestUrl
{
    [self.requestRouteMap setValue:requestUrl forKey:reqAppId];
}

#pragma mark - connected callbacks handle

- (void)removeConnectCbkId:(NSString *)connectCbkId
{
    [self.connectedCbkMap removeObjectForKey:connectCbkId];
}

- (void)addConnectCbkId:(NSString *)connectCbkId
           connectedCbk:(stompclient_connected_block_t)connected
        disConnectedCbk:(stompclient_disconnected_block_t)disconnected
       connectFailedCbk:(stompclient_connect_failed_block_t)connectFailed
{
    MIStompConnectCbk * connectCbk = [[MIStompConnectCbk alloc] initWithConnectCbkId:connectCbkId connectedCbk:connected disConnectedCbk:disconnected connectFailedCbk:connectFailed];
    [self.connectedCbkMap setValue:connectCbk forKey:connectCbkId];
}

#pragma mark - MIMisakaConnectDelegate

- (void)onStompConnected
{
    for (MIStompConnectCbk * connectCbk in self.connectedCbkMap.allValues) {
        connectCbk.connectedCallback();
    }
    [self postNotificationName:KMIMisakaClientConnectedNotify];
    
    NSArray * requests = self.requestMap.allValues;
    [self.requestMap removeAllObjects];
    
    for (MIStompRequest * aRequest in requests) {
        MIGLLogi(@"StompClient onConnected repost request, reqAppId[%@] , reqDestination[%@]", aRequest.reqAppId, aRequest.reqDestination);
        
        Class                      resultClass = [NSObject class];
        stompclient_failed_block_t failedBlock = nil;
        stompclient_success_block_t successBlock = nil;
        
        if (aRequest.stompReply) {
            resultClass = aRequest.stompReply.resutlClass;
            failedBlock = aRequest.stompReply.replyFailBlock;
            successBlock = aRequest.stompReply.replySuccessBlock;
        }
        [self request:aRequest.reqAppId destination:aRequest.reqDestination bodyObject:aRequest.reqBody success:successBlock failed:failedBlock resultClass:resultClass];
    }
}

- (void)onStompConnectFailed:(NSError *)error
{
    for (MIStompConnectCbk * connectCbk in self.connectedCbkMap.allValues) {
        connectCbk.connectFailedCallback(error);
    }
    [self postNotificationName:KMIMisakaClientConnectFailedNotify];
}

- (void)onStompMessage:(MIStompMessage *)message
{
    if (nil == message) return;
    
    NSString * requestId    = message.stompHeaders[STOMPH_REQUEST_ID];
    NSString * destination  = message.stompHeaders[STOMPH_DESTINATION];
    NSString * responseCode = message.stompHeaders[STOMPH_RESPONSE_CODE];
    NSString * responseMsg  = message.stompHeaders[STOMPH_RESPONSE_MESSAGE];
    
    int respCode = responseCode.intValue;
    if (!responseCode || (0 == respCode && ![responseCode hasPrefix:@"0"])) {
        respCode = -1;
    }
    
    MIGLLogi(@"receive message for request[%@ : %@] respCode[%@] respMsg[%@] command[%@], body : %@", requestId, destination, @(respCode), responseMsg, message.stompCommand, message.stompBody);
    
    if ([destination isEqualToString:@"/user/queue/reply"]) {
        MIStompRequest * aRequest = self.requestMap[requestId];
        [self.requestMap removeObjectForKey:requestId];
        
        if (aRequest && aRequest.stompReply) {
            [self handleMessage:respCode responseMsg:responseMsg messageBody:message.stompBody stompReply:aRequest.stompReply];
        }
    } else {
        MIStompReply * aReply = self.subscribeMap[destination];
        if (aReply) {
            [self handleMessage:respCode responseMsg:responseMsg messageBody:message.stompBody stompReply:aReply];
        }
    }
}

- (void)onStompDisconnected:(NSString *)reason statusCode:(MIWSStatusCode)statusCode
{
    for (MIStompConnectCbk * connectCbk in self.connectedCbkMap.allValues) {
        connectCbk.disconnectedCallback(reason, statusCode);
    }
    [self postNotificationName:KMIMisakaClientDisconnectedNotify];
}

#pragma mark -

- (void)handleMessage:(int)aRespCode
          responseMsg:(NSString *)responseMsg
          messageBody:(NSString *)messageBody
           stompReply:(MIStompReply *)aStompReply
{
    Class aResultClass = aStompReply.resutlClass;
    id aResultObject = (messageBody.length ? messageBody : @"");
    
    if (1 != aRespCode) {
        if (aStompReply.replyFailBlock) {
            if (responseMsg.length) {
                aStompReply.replyFailBlock(MIStompRes_ReqFailed, responseMsg);
            } else {
                aStompReply.replyFailBlock(MIStompRes_ConnectFailed, self.wsConfig.connectFailTips);
            }
        }
    }
    else {
        if (messageBody.length
            && ![aResultClass isSubclassOfClass:[NSString class]]
            && ![NSStringFromClass(aResultClass) isEqualToString:@"NSObject"]
            && [object_getClass(aResultClass) instancesRespondToSelector:@selector(parseJsonString:)]) {
            aResultObject = [aResultClass parseJsonString:messageBody];
        }
        
        if (nil == aResultObject) {
            if (aStompReply.replyFailBlock) {
                aStompReply.replyFailBlock(MIStompRes_ParseResDataFailed, self.wsConfig.resDataParseFailTips);
            }
        } else {
            if (aStompReply.replySuccessBlock) {
                aStompReply.replySuccessBlock(aResultObject);
            }
        }
    }
}

#pragma mark - timeout detection

- (void)timeoutDetection
{
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:_cmd object:nil];
    NSMutableArray * processedReqIds = [NSMutableArray arrayWithCapacity:self.requestMap.count];
    
    for (NSString * requestId in self.requestMap.allKeys) {
        MIStompRequest * request = self.requestMap[requestId];
        
        if (request.timeoutForRequest) {
            MIGLLogi(@"Stomp Request timeout, reqAppId[%@] , reqDestination[%@]", request.reqAppId, request.reqDestination);
            [processedReqIds addObject:requestId];
            
            if (request.stompReply && request.stompReply.replyFailBlock) {
                request.stompReply.replyFailBlock(MIStompRes_ReqTimeout, self.wsConfig.timeoutTips);
            }
        }
        else if (request.timeoutForReconnect && self.hwStompConnect.isConnected) {
            MIGLLogi(@"Stomp Request timeout and begin reconnect stomp client, reqAppId[%@] , reqDestination[%@]", request.reqAppId, request.reqDestination);
            [self.hwStompConnect reconnect];
        }
    }
    [self.requestMap removeObjectsForKeys:processedReqIds];
    [self performSelector:_cmd withObject:nil afterDelay:MIStompReqDetectionInterval];
}

@end
