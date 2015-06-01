//
//  SCLogDefaultFilter.m
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import "SCLogDefaultFilter.h"

@implementation SCLogDefaultFilter
{
    BOOL _isPermited;
}

+ (id<SCLogFilterProtocol>)filter:(SCLogLevel)level
{
    return [[SCLogDefaultFilter alloc] initWithLevel:level];
}

- (BOOL)isPermited
{
    return _isPermited;
}

#pragma mark - initialization

- (id)initWithLevel:(SCLogLevel)level
{
    if (self = [self init]) {
        _isPermited = level >= SCLogLevel_Verbose;
    }
    return self;
}

@end
