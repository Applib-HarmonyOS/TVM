#ifndef _UTIL_HPP_
#define _UTIL_HPP_

#include <jni.h>

extern "C" {
#include <stdlib.h>
}

#include <hilog/log.h>

#define JNI_FUNC(retType, bindClass, name) JNIEXPORT retType JNICALL Java_com_example_jaihanumanapplication_##bindClass##_##name
#define JNI_ARGS JNIEnv *env, jobject this

#define LOG_CUSTOM_TAG "JaiHanuman"
#define LOGInfo(...) HiLogPrint(LOG_APP, LOG_INFO, 0xD000F00, LOG_CUSTOM_TAG, __VA_ARGS__)
#define LOGError(...) HiLogPrint(LOG_APP, LOG_ERROR, 0xD000F00, LOG_CUSTOM_TAG, __VA_ARGS__)
#define LOGDebug(...) HiLogPrint(LOG_APP, LOG_DEBUG, 0xD000F00, LOG_CUSTOM_TAG, __VA_ARGS__)

#endif