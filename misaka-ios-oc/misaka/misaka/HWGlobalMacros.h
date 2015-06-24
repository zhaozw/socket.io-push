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

#define HWDEFINE_NOTIFCATION(_notification_) NSString * const _notification_ = @#_notification_
#define HWEXTERN_NOTIFCATION(_notification_) extern NSString * const _notification_

#define HWDEFINE_NOTIFICATIONKEY(_key_) HWDEFINE_NOTIFCATION(_key_)
#define HWEXTERN_NOTIFICATIONKEY(_key_) HWEXTERN_NOTIFCATION(_key_)
#define HWEXTERN_TYPE_NOTIFICATIONKEY(_type_, _key_) HWEXTERN_NOTIFCATION(_key_)

//===============================================================
#pragma mark - global string

#define HWDEFINE_GLOBALSTRING(_name_, _value_) NSString * const _name_ = _value_
#define HWEXTERN_GLOBALSTRING(_name_) extern NSString * const _name_

//===============================================================
#pragma mark - singleton

#define HWDECLARE_SINGLETON() + (instancetype)sharedInstance;

#define HWIMPLEMENT_SINGLETON(_class_name) \
        + (instancetype)sharedInstance \
        { \
            static _class_name * aInstance = nil; \
            static dispatch_once_t onceToken; \
            dispatch_once(&onceToken, ^{ aInstance = [[_class_name alloc] init]; }); \
            return aInstance; \
        }

//===============================================================
#pragma mark - log

#define HWGLLogv(...)   NSLog(__VA_ARGS__)
#define HWGLLogd(...)   NSLog(__VA_ARGS__)
#define HWGLLogw(...)   NSLog(__VA_ARGS__)
#define HWGLLoge(...)   NSLog(__VA_ARGS__)
#define HWGLLogi(...)   NSLog(__VA_ARGS__)

//===============================================================
#pragma mark - assert

#if DEBUG
    #define HWGLAssertW(exp, ...) NSAssert(exp, __VA_ARGS__)
    #define HWGLAssertE(exp, ...) NSAssert(exp, __VA_ARGS__)
#else
    #define HWGLAssertW(exp, ...) \
            do {\
                if (!(exp)) HWGLLogw(__VA_ARGS__);\
            } while(0)

    #define HWGLAssertE(exp, ...) \
            do {\
                if(!(exp)) HWGLLoge(__VA_ARGS__);\
            } while(0)
#endif

//===============================================================
#pragma mark - weak self

#define HWWeakSelf() __weak typeof(self) hwWeakSelf = self;

#endif
