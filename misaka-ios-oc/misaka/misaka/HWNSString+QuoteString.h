//
//  HWNSString+QuoteString.h
//  huwai
//
//  Created by crazylhf on 15/4/26.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString (QuoteString)

/**
 *  @brief 获取格式为 "************" 的子字符串
 *  
 *  如: "012\"abc\"89" , 则返回的NSRange为 {location : 3, length : 5}
 */
- (NSRange)rangeOfDoubleQuoteStringFromPosition:(NSUInteger)position;

@end
