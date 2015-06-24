//
//  HWWebSocketFactory.m
//  huwai
//
//  Created by crazylhf on 15/4/17.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "HWWebSocketFactory.h"
#import "HWWebSocketRemote.h"
#import "HWWebSocketLocal.h"

@implementation HWWebSocketFactory

+ (id<HWWebSocketProtocol>)hwWebSocket:(NSString *)url
                                wsMode:(HWWSConfigMode)wsMode
                            wsDelegate:(id<HWWebSocketDelegate>)wsDelegate
{
    if (HWWSConfigMode_LOCAL == wsMode) {
        return [[HWWebSocketLocal alloc] initWithUrl:url wsDelegate:wsDelegate];
    } else if (HWWSConfigMode_REMOTE == wsMode) {
        return [[HWWebSocketRemote alloc] initWithUrl:url wsDelegate:wsDelegate];
    }
    return nil;
}

@end
