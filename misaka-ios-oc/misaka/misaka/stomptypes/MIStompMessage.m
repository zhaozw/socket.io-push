//
//  MIStompMessage.m
//  huwai
//
//  Created by crazylhf on 15/4/17.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "MIStompMessage.h"

@implementation MIStompMessage
{
    NSDictionary * _stompHeaders;
    NSString     * _stompCommand;
    NSString     * _stompBody;
}

- (id)initWithCommand:(NSString *)command
              headers:(NSDictionary *)headers body:(NSString *)body
{
    if (self = [super init]) {
        _stompHeaders = headers;
        _stompCommand = command;
        _stompBody    = body;
    }
    return self;
}

- (id)initWithStompMsgString:(NSString *)stompMsgString
{
    if (self = [super init]) {
        stompMsgString = [stompMsgString stringByReplacingOccurrencesOfString:@"\\u0000" withString:@"\000"];
        stompMsgString = [stompMsgString stringByReplacingOccurrencesOfString:@"\\n" withString:@"\n"];
        stompMsgString = [stompMsgString stringByReplacingOccurrencesOfString:@"\\" withString:@""];
        
        NSArray * stompPieces = [stompMsgString componentsSeparatedByString:@"\n"];
        if (stompPieces.count) {
            NSCharacterSet      * chSet    = [NSCharacterSet whitespaceCharacterSet];
            NSMutableDictionary * aHeaders = [NSMutableDictionary dictionary];
            
            _stompCommand = [stompPieces.firstObject stringByTrimmingCharactersInSet:chSet];
            
            NSUInteger index = 1;
            while (index < stompPieces.count) {
                NSString * aPiece = stompPieces[index++];
                if (0 == aPiece.length) break;
                
                NSArray * subs = [aPiece componentsSeparatedByString:@":"];
                if (2 == subs.count) {
                    NSString * key   = [subs.firstObject stringByTrimmingCharactersInSet:chSet];
                    NSString * value = [subs.lastObject stringByTrimmingCharactersInSet:chSet];
                    [aHeaders setValue:value forKey:key];
                }
            }
            
            _stompHeaders = aHeaders;
            
            if (index < stompPieces.count) {
                NSRange bodyRange = NSMakeRange(index, stompPieces.count - index);
                stompPieces = [stompPieces subarrayWithRange:bodyRange];
                _stompBody  = [stompPieces componentsJoinedByString:@"\n"];
                _stompBody  = [NSString stringWithCString:_stompBody.UTF8String encoding:NSUTF8StringEncoding];
            }
        }
    }
    return self;
}

- (NSString *)toStompMsgString
{
    NSMutableString * msgString = [[NSMutableString alloc] init];
    
    [msgString appendFormat:@"%@\n", _stompCommand];
    
    for (NSString * hKey in _stompHeaders.allKeys) {
        [msgString appendFormat:@"%@:%@\n", hKey, _stompHeaders[hKey]];
    }
    
    [msgString appendString:@"\n"];
    
    if (_stompBody.length) {
        [msgString appendString:_stompBody];
    }
    
    [msgString appendString:@"\000"];
    
    return msgString;
}

#pragma mark -

- (NSDictionary *)stompHeaders { return _stompHeaders; }

- (NSString *)stompCommand { return _stompCommand; }

- (NSString *)stompBody { return _stompBody; }

@end
