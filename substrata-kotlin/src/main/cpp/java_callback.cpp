
#include "java_callback.h"

static void java_callback_finalizer(JSRuntime *rt, JSValue val)
{
    JavaCallbackData *data = (JavaCallbackData*) JS_GetOpaque(val, java_callback_class_id);

    JNIEnv *env;
    data->vm->AttachCurrentThread(&env, NULL);
    if (env != NULL) {
        env->DeleteGlobalRef(data->js_context);
    }
    data->vm->DetachCurrentThread();

    js_free_rt(rt, data);
}

static JSClassDef java_callback_class = {
        "JavaCallback",
        .finalizer = java_callback_finalizer,
};

int java_callback_init(JSContext *ctx) {
    JS_NewClassID(&java_callback_class_id);
    if (JS_NewClass(JS_GetRuntime(ctx), java_callback_class_id, &java_callback_class)) return -1;
    return 0;
}