//
//  SCSecurityUtil.h
//  StompClient
//
//  Created by crazylhf on 15/4/16.
//  Copyright (c) 2015年 yy. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SCSHAUtil : NSObject

+ (NSData *)sha1Data:(NSData *)rawData;

+ (NSData *)sha256Data:(NSData *)rawData;

@end

@interface SCMD5Util : NSObject

+ (NSData *)md5Data:(NSData *)rawData;

@end

@interface SCSecurityUtil : NSObject

+ (NSString *)md5HexString:(NSString *)rawString;

+ (NSString *)sha1HexString:(NSString *)rawString;

+ (NSString *)sha256HexString:(NSString *)rawString;

+ (NSString *)hexString:(NSData *)rawData;

@end
