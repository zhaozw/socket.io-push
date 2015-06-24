//
//  HWWSObjectProtocol.h
//  huwai
//
//  Created by crazylhf on 15/4/19.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 *  @brief 上层通过StompClient发送出去的对象都必须实现HWWSObjectProtocol
 */
@protocol HWWSObjectProtocol <NSObject>

/**
 *  @brief 反向序列化接口, 将字符串形式的数据反向序列化为数据对象.
 *  @attention 接口的实现者, 在反向序列化失败时, 应返回nil .
 */
+ (id)parseJsonString:(NSString *)jsonString;

/// 序列化接口, 将对象序列化为指定格式的数据, 序列化形式由业务自定.
- (NSString *)toJsonString;

@end

#define AppendJsonKeyValue(_mutable_string_, _json_key_, _json_value_, _need_comma_) \
        do {\
            if (_need_comma_) {\
                AppendJsonKeyValueComma(_mutable_string_, _json_key_, _json_value_);\
            } else {\
                [_mutable_string_ appendFormat:@"\"%@\":\"%@\"", _json_key_, _json_value_];\
            }\
        } while(NO)

#define AppendJsonKeyValueComma(_mutable_string_, _json_key_, _json_value_) \
        [_mutable_string_ appendFormat:@",\"%@\":\"%@\"", _json_key_, _json_value_]
