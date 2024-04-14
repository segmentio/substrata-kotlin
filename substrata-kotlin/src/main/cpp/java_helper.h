//
// Created by Wenxi Zeng on 3/1/24.
//

#ifndef SUBSTRATA_KOTLIN_JAVA_HELPER_H
#define SUBSTRATA_KOTLIN_JAVA_HELPER_H

#include <jni.h>

#define CLASS_NAME_ILLEGAL_STATE_EXCEPTION "java/lang/IllegalStateException"

#define THROW_EXCEPTION(ENV, EXCEPTION_NAME, ...)                               \
    do {                                                                        \
        throw_exception((ENV), (EXCEPTION_NAME), __VA_ARGS__);                  \
        return;                                                                 \
    } while (0)
#define THROW_EXCEPTION_RET(ENV, EXCEPTION_NAME, ...)                           \
    do {                                                                        \
        throw_exception((ENV), (EXCEPTION_NAME), __VA_ARGS__);                  \
        return 0;                                                               \
    } while (0)
#define MSG_OOM "Out of memory"
#define MSG_NULL_JS_RUNTIME "Null JSRuntime"
#define MSG_NULL_JS_CONTEXT "Null JSContext"
#define MSG_NULL_JS_VALUE "Null JSValue"
#define THROW_ILLEGAL_STATE_EXCEPTION(ENV, ...)                                 \
    THROW_EXCEPTION(ENV, CLASS_NAME_ILLEGAL_STATE_EXCEPTION, __VA_ARGS__)
#define THROW_ILLEGAL_STATE_EXCEPTION_RET(ENV, ...)                             \
    THROW_EXCEPTION_RET(ENV, CLASS_NAME_ILLEGAL_STATE_EXCEPTION, __VA_ARGS__)
#define CHECK_NULL(ENV, POINTER, MESSAGE)                                       \
    do {                                                                        \
        if ((POINTER) == NULL) {                                                \
            THROW_ILLEGAL_STATE_EXCEPTION((ENV), (MESSAGE));                    \
        }                                                                       \
    } while (0)
#define CHECK_NULL_RET(ENV, POINTER, MESSAGE)                                   \
    do {                                                                        \
        if ((POINTER) == NULL) {                                                \
            THROW_ILLEGAL_STATE_EXCEPTION_RET((ENV), (MESSAGE));                \
        }                                                                       \
    } while (0)

#define MAX_MSG_SIZE 1024

jint throw_exception(JNIEnv *env, const char *exception_name, const char *message, ...);


#endif //SUBSTRATA_KOTLIN_JAVA_HELPER_H
