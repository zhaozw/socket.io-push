//
//  HWStompDefs.h
//  huwai
//
//  Created by crazylhf on 15/4/17.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#ifndef huwai_HWStompDefs_h
#define huwai_HWStompDefs_h

/**
 *  stomp protocol header defines
 *  STOMPH prefix means stomp protocol header
 */
#define STOMPH_SUBSCRIPTION_ID      @"id"
#define STOMPH_USER                 @"simpUser"
#define STOMPH_REQUEST_ID           @"request-id"
#define STOMPH_DESTINATION          @"destination"
#define STOMPH_RESPONSE_CODE        @"response-code"
#define STOMPH_SESSION_ID           @"simpSessionId"
#define STOMPH_MESSAGE_TYPE         @"simpMessageType"
#define STOMPH_RESPONSE_MESSAGE     @"response-message"
#define STOMPH_CONNECT_MESSAGE      @"simpConnectMessage"
#define STOMPH_SESSION_ATTRIBUTES   @"simpSessionAttributes"

#define HWMicroSecondPerSecond      (1000000)
#define STOMP_CURRENT_MICROSECONDS  ((uint64_t)([NSDate date].timeIntervalSince1970 * HWMicroSecondPerSecond))

typedef NS_ENUM(unsigned, HWWSStatusCode) {
    HWWSStatusCode_Normal             = 1000,
    HWWSStatusCode_GoingAway          = 1001,
    HWWSStatusCode_ProtocolError      = 1002,
    HWWSStatusCode_UnhandledType      = 1003,
    
    // 1004 and 1006 reserved.
    HWWSStatusCode_NoStatusReceived   = 1005,
    
    HWWSStatusCode_InvalidUTF8        = 1007,
    HWWSStatusCode_PolicyViolated     = 1008,
    HWWSStatusCode_MessageTooBig      = 1009,
};

typedef NS_ENUM(unsigned, HWStompResCode) {
    HWStompRes_ReqFailed              = 0001,
    HWStompRes_ReqTimeout             = 0002,
    HWStompRes_ConnectFailed          = 0003,
    HWStompRes_ParseResDataFailed     = 0004,
};

typedef void(^stompclient_connect_failed_block_t) (NSError * error);
typedef void(^stompclient_connected_block_t)      (void);
typedef void(^stompclient_disconnected_block_t)   (NSString * reason, HWWSStatusCode statusCode);

typedef void(^stompclient_success_block_t)(id result);
typedef void(^stompclient_failed_block_t)(HWStompResCode resCode, NSString * failTips);

#endif
