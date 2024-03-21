//
// Created by Wenxi Zeng on 3/21/24.
//

#include <jni.h>
#include "../../../../quickjs/quickjs/quickjs.h"

#ifndef SUBSTRATA_KOTLIN_JAVA_CALLBACK_H
#define SUBSTRATA_KOTLIN_JAVA_CALLBACK_H

#endif //SUBSTRATA_KOTLIN_JAVA_CALLBACK_H

typedef struct {
    JavaVM *vm;
    jobject js_context;
} JavaCallbackData;


static JSClassID java_callback_class_id;

int java_callback_init(JSContext *ctx);