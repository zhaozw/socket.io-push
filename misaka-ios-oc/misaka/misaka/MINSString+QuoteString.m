//
//  MINSString+QuoteString.m
//  huwai
//
//  Created by crazylhf on 15/4/26.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import "MINSString+QuoteString.h"

@implementation NSString (QuoteString)

- (NSRange)rangeOfDoubleQuoteStringFromPosition:(NSUInteger)position
{
    NSRange(^rangeOfQuoteCharBlock)(NSString *, NSUInteger) = ^(NSString * source, NSUInteger start) {
        if (start >= source.length) {
            return NSMakeRange(NSNotFound, 1);
        }
        
        NSRange aRange = NSMakeRange(start, source.length - start);
        aRange = [source rangeOfString:@"\"" options:NSLiteralSearch range:aRange];
        
        if (NSNotFound != aRange.location && aRange.location > 0) {
            while ('\\' == [source characterAtIndex:(aRange.location - 1)]) {
                NSUInteger location = aRange.location + 1;
                
                if (location < source.length) {
                    aRange = [source rangeOfString:@"\""
                                           options:NSLiteralSearch
                                             range:NSMakeRange(location, source.length - location)];
                    if (NSNotFound == aRange.location) break;
                }
                else {
                    aRange.location = NSNotFound;
                    break;
                }
            }
        }
        return aRange;
    };
    
    NSRange resultRange = rangeOfQuoteCharBlock(self, position);
    if (NSNotFound != resultRange.location) {
        NSRange aRange = rangeOfQuoteCharBlock(self, resultRange.location + 1);
        if (NSNotFound != aRange.location) {
            resultRange = NSMakeRange(resultRange.location, (aRange.location + 1) - resultRange.location);
        } else {
            resultRange.location = NSNotFound;
        }
    }
    return resultRange;
}

@end
