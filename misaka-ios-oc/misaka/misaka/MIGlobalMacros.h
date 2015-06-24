//
//  GlobalMacros.h
//  huwai
//
//  Created by crazylhf on 15/4/15.
//  Copyright (c) 2015年 zq. All rights reserved.
//

#ifndef huwai_GlobalMacros_h
#define huwai_GlobalMacros_h

//===============================================================
#pragma mark - notification

#define MIDEFINE_NOTIFCATION(_notification_) NSString * const _notification_ = @#_notification_
#define MIEXTERN_NOTIFCATION(_notification_) extern NSString * const _notification_

#define MIDEFINE_NOTIFICATIONKEY(_key_) MIDEFINE_NOTIFCATION(_key_)
#define MIEXTERN_NOTIFICATIONKEY(_key_) MIEXTERN_NOTIFCATION(_key_)
#define MIEXTERN_TYPE_NOTIFICATIONKEY(_type_, _key_) MIEXTERN_NOTIFCATION(_key_)

//===============================================================
#pragma mark - global string

#define MIDEFINE_GLOBALSTRING(_name_, _value_) NSString * const _name_ = _value_
#define MIEXTERN_GLOBALSTRING(_name_) extern NSString * const _name_

//===============================================================
#pragma mark - singleton

#define MIDECLARE_SINGLETON() + (instancetype)sharedInstance;

#define MIIMPLEMENT_SINGLETON(_class_name) \
        + (instancetype)sharedInstance \
        { \
            static _class_name * aInstance = nil; \
            static dispatch_once_t onceToken; \
            dispatch_once(&onceToken, ^{ aInstance = [[_class_name alloc] init]; }); \
            return aInstance; \
        }

//===============================================================
#pragma mark - log

#define MIGLLogv(...)   NSLog(__VA_ARGS__)
#define MIGLLogd(...)   NSLog(__VA_ARGS__)
#define MIGLLogw(...)   NSLog(__VA_ARGS__)
#define MIGLLoge(...)   NSLog(__VA_ARGS__)
#define MIGLLogi(...)   NSLog(__VA_ARGS__)

//===============================================================
#pragma mark - assert

#if DEBUG
    #define MIGLAssertW(exp, ...) NSAssert(exp, __VA_ARGS__)
    #define MIGLAssertE(exp, ...) NSAssert(exp, __VA_ARGS__)
#else
    #define MIGLAssertW(exp, ...) \
            do {\
                if (!(exp)) MIGLLogw(__VA_ARGS__);\
            } while(0)

    #define MIGLAssertE(exp, ...) \
            do {\
                if(!(exp)) MIGLLoge(__VA_ARGS__);\
            } while(0)
#endif

//===============================================================
#pragma mark - weak self

#define MIWeakSelf() __weak typeof(self) hwWeakSelf = self;

#endif
