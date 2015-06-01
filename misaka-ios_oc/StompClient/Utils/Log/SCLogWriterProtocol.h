//
//  SCLogWriterProtocol.h
//  StompClient
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import <Foundation/Foundation.h>

@class SCLogContent;

@protocol SCLogWriterProtocol <NSObject>

- (void)registerOption:(id)option;

- (void)log:(SCLogContent *)logContent;

@end
