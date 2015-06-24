//
//  HWStompClient.m
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//
#import "HWNSObject+Notification.h"
#import "HWStompConnectMgr.h"
#import "HWStompRequest.h"
#import "HWStompClient.h"

#import "HWStompReply.h"
#import "HWStompConnectCbk.h"

#import "HWRandomUtil.h"
#import "HWNetworkMonitor.h"

#import <objc/runtime.h>

#define HWStompReqDetectionInterval     (1)

HWDEFINE_NOTIFCATION(KHWStompClientConnectFailedNotify);

HWDEFINE_NOTIFCATION(KHWStompClientConnectedNotify);

HWDEFINE_NOTIFCATION(KHWStompClientDisconnectedNotify);

@interface HWStompClient() <HWStompConnectDelegate>

@property (nonatomic, strong) HWStompConnectMgr * hwStompConnect;

@property (nonatomic, strong) HWWebSocketConfig * wsConfig;


/// @{ @"appId" : @"requestUrl" }
@property (nonatomic, strong) NSMutableDictionary * requestRouteMap;


/// @{ @"reqId" : HWStompRequest }
@property (nonatomic, strong) NSMutableDictionary * requestMap;

/// @{ @"destinationPath" : HWStompReply }
@property (nonatomic, strong) NSMutableDictionary * subscribeMap;

/// @{ @"id" : stompclient_connected_block_t }
@property (nonatomic, strong) NSMutableDictionary * connectedCbkMap;

@end

@implementation HWStompClient

- (id)init
{
    if (self = [super init]) {
        HWGLAssertW(NO, @"HWStompClient init is not support");
    }
    return self;
}

- (id)initWithHost:(NSString *)host webSocketConfig:(HWWebSocketConfig *)wsConfig
{
    if (self = [super init]) {
        [[HWNetworkMonitor sharedInstance] enable];
        
        NSString * wsUrl = [NSString stringWithFormat:@"%@/stomp/1/%@/websocket", host, [HWRandomUtil randomAlphaNumeric:12]];
        self.hwStompConnect = [[HWStompConnectMgr alloc] initWithRequestUrl:wsUrl wsMode:wsConfig.mode stompDelegate:self];
        
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
     bodyObject:(id<HWWSObjectProtocol>)reqObject
        success:(stompclient_success_block_t)succBlock
         failed:(stompclient_failed_block_t)failBlock
    resultClass:(Class)resultClass
{
    HWStompReply * stompReply = [[HWStompReply alloc] initWithReplyfailed:failBlock replySucceed:succBlock resultClass:resultClass];
    HWStompRequest * stompReq = [[HWStompRequest alloc] initWithAppId:reqAppId destination:reqDestination body:reqObject stompReply:stompReply];
    
    if (self.hwStompConnect.isConnected) {
        NSString * reqUrl = [NSString stringWithFormat:@"%@%@", self.requestRouteMap[reqAppId], reqDestination];
        NSString * reqJsonString = reqObject.toJsonString;
        
        HWGLLogi(@"send request[%@], destination[%@], reqUrl[%@], object : %@", reqAppId, reqDestination, reqUrl, reqObject.toJsonString);
        
        NSDictionary * headerDic = nil;
        if (self.wsConfig.isDataAsBody) {
            headerDic = @{ @"url" : reqUrl, @"appId" : reqAppId, @"dataAsBody" : @"true" };
        } else {
            headerDic = @{ @"url" : reqUrl, @"appId" : reqAppId };
        }
        
        uint64_t reqId = [self.hwStompConnect request:@"/request" body:reqJsonString headers:headerDic];
        if (reqId) {
            self.requestMap[@(reqId).stringValue] = stompReq;
        } else if (failBlock) {
            failBlock(HWStompRes_ConnectFailed, self.wsConfig.connectFailTips);
        }
    }
    else {
        self.requestMap[[HWRandomUtil randomAlphaNumeric:12]] = stompReq;
    }
}

#pragma mark - subscribe

- (void)subscribeBroadcast:(NSString *)pushId
                   success:(stompclient_success_block_t)succBlock
                    failed:(stompclient_failed_block_t)failBlock
               resultClass:(Class)resultClass
{
    HWGLLogi(@"subscribe broadcast, pushId[%@]", pushId);
    
    HWStompReply * stompReply = [[HWStompReply alloc] initWithReplyfailed:failBlock replySucceed:succBlock resultClass:resultClass];
    [self subscribe:[NSString stringWithFormat:@"/topic/%@", pushId] stompReply:stompReply];
}

- (void)subscribeUserPush:(NSString *)pushId
                  success:(stompclient_success_block_t)succBlock
                   failed:(stompclient_failed_block_t)failBlock
              resultClass:(Class)resultClass
{
    HWGLLogi(@"subscribe user push, pushId[%@]", pushId);
    
    HWStompReply * stompReply = [[HWStompReply alloc] initWithReplyfailed:failBlock replySucceed:succBlock resultClass:resultClass];
    [self subscribe:[NSString stringWithFormat:@"/user/queue/%@", pushId] stompReply:stompReply];
}

- (void)subscribe:(NSString *)destination stompReply:(HWStompReply *)stompReply
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
    HWStompConnectCbk * connectCbk = [[HWStompConnectCbk alloc] initWithConnectCbkId:connectCbkId connectedCbk:connected disConnectedCbk:disconnected connectFailedCbk:connectFailed];
    [self.connectedCbkMap setValue:connectCbk forKey:connectCbkId];
}

#pragma mark - HWStompConnectDelegate

- (void)onStompConnected
{
    for (HWStompConnectCbk * connectCbk in self.connectedCbkMap.allValues) {
        connectCbk.connectedCallback();
    }
    [self postNotificationName:KHWStompClientConnectedNotify];
    
    NSArray * requests = self.requestMap.allValues;
    [self.requestMap removeAllObjects];
    
    for (HWStompRequest * aRequest in requests) {
        HWGLLogi(@"StompClient onConnected repost request, reqAppId[%@] , reqDestination[%@]", aRequest.reqAppId, aRequest.reqDestination);
        
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
    for (HWStompConnectCbk * connectCbk in self.connectedCbkMap.allValues) {
        connectCbk.connectFailedCallback(error);
    }
    [self postNotificationName:KHWStompClientConnectFailedNotify];
}

- (void)onStompMessage:(HWStompMessage *)message
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
    
    HWGLLogi(@"receive message for request[%@ : %@] respCode[%@] respMsg[%@] command[%@], body : %@", requestId, destination, @(respCode), responseMsg, message.stompCommand, message.stompBody);
    
    if ([destination isEqualToString:@"/user/queue/reply"]) {
        HWStompRequest * aRequest = self.requestMap[requestId];
        [self.requestMap removeObjectForKey:requestId];
        
        if (aRequest && aRequest.stompReply) {
            [self handleMessage:respCode responseMsg:responseMsg messageBody:message.stompBody stompReply:aRequest.stompReply];
        }
    } else {
        HWStompReply * aReply = self.subscribeMap[destination];
        if (aReply) {
            [self handleMessage:respCode responseMsg:responseMsg messageBody:message.stompBody stompReply:aReply];
        }
    }
}

- (void)onStompDisconnected:(NSString *)reason statusCode:(HWWSStatusCode)statusCode
{
    for (HWStompConnectCbk * connectCbk in self.connectedCbkMap.allValues) {
        connectCbk.disconnectedCallback(reason, statusCode);
    }
    [self postNotificationName:KHWStompClientDisconnectedNotify];
}

#pragma mark -

- (void)handleMessage:(int)aRespCode
          responseMsg:(NSString *)responseMsg
          messageBody:(NSString *)messageBody
           stompReply:(HWStompReply *)aStompReply
{
    Class aResultClass = aStompReply.resutlClass;
    id aResultObject = (messageBody.length ? messageBody : @"");
    
    if (1 != aRespCode) {
        if (aStompReply.replyFailBlock) {
            if (responseMsg.length) {
                aStompReply.replyFailBlock(HWStompRes_ReqFailed, responseMsg);
            } else {
                aStompReply.replyFailBlock(HWStompRes_ConnectFailed, self.wsConfig.connectFailTips);
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
                aStompReply.replyFailBlock(HWStompRes_ParseResDataFailed, self.wsConfig.resDataParseFailTips);
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
        HWStompRequest * request = self.requestMap[requestId];
        
        if (request.timeoutForRequest) {
            HWGLLogi(@"Stomp Request timeout, reqAppId[%@] , reqDestination[%@]", request.reqAppId, request.reqDestination);
            [processedReqIds addObject:requestId];
            
            if (request.stompReply && request.stompReply.replyFailBlock) {
                request.stompReply.replyFailBlock(HWStompRes_ReqTimeout, self.wsConfig.timeoutTips);
            }
        }
        else if (request.timeoutForReconnect && self.hwStompConnect.isConnected) {
            HWGLLogi(@"Stomp Request timeout and begin reconnect stomp client, reqAppId[%@] , reqDestination[%@]", request.reqAppId, request.reqDestination);
            [self.hwStompConnect reconnect];
        }
    }
    [self.requestMap removeObjectsForKeys:processedReqIds];
    [self performSelector:_cmd withObject:nil afterDelay:HWStompReqDetectionInterval];
}

@end
