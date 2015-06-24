//
//  MIWebSocketFactory.m
//  huwai
//
//  Created by crazylhf on 15/4/17.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "MIWebSocketFactory.h"
#import "MIWebSocketRemote.h"
#import "MIWebSocketLocal.h"

@implementation MIWebSocketFactory

+ (id<MIWebSocketProtocol>)hwWebSocket:(NSString *)url
                                wsMode:(MIWSConfigMode)wsMode
                            wsDelegate:(id<MIWebSocketDelegate>)wsDelegate
{
    if (MIWSConfigMode_LOCAL == wsMode) {
        return [[MIWebSocketLocal alloc] initWithUrl:url wsDelegate:wsDelegate];
    } else if (MIWSConfigMode_REMOTE == wsMode) {
        return [[MIWebSocketRemote alloc] initWithUrl:url wsDelegate:wsDelegate];
    }
    return nil;
}

@end
