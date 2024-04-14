#include <stdio.h>

#include "java_helper.h"

#define MAX_MSG_SIZE 1024

jint throw_exception(JNIEnv *env, const char *exception_name, const char *message, ...) {
    char formatted_message[MAX_MSG_SIZE];
    va_list va_args;
    va_start(va_args, message);
    vsnprintf(formatted_message, MAX_MSG_SIZE, message, va_args);
    va_end(va_args);

    jclass exception_class = env->FindClass(exception_name);
    if (exception_class == NULL) {
        return -1;
    }

    return env->ThrowNew(exception_class, formatted_message);
}
