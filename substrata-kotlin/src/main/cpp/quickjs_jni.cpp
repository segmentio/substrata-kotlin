#include <jni.h>
#include "../../../../quickjs/quickjs/quickjs.h"
#include <string.h>
#include <malloc.h>
#include "java_helper.h"
#include "java_callback.h"

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("quickjs_jni");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("quickjs_jni")
//      }
//    }

#define FREED_VALUE JS_UNDEFINED

#define COPY_JS_VALUE(JS_CONTEXT, JS_VALUE, RESULT)                                    \
    do {                                                                               \
        void *__copy__ = js_malloc_rt(JS_GetRuntime(JS_CONTEXT), sizeof(JSValue));     \
        if (__copy__ != NULL) {                                                        \
            memcpy(__copy__, &(JS_VALUE), sizeof(JSValue));                            \
            (RESULT) = __copy__;                                                       \
        } else {                                                                       \
            JS_FreeValue((JS_CONTEXT), (JS_VALUE));                                    \
        }                                                                              \
    } while (0)
// Free a JSValue and set it to the sentinel value
void safeFreeValue(JSContext *ctx, JSValue *value) {
    JS_FreeValue(ctx, *value);
    *value = FREED_VALUE;
}

// Check if a JSValue has been freed
int isValueFreed(JSValue value) {
    return JS_VALUE_GET_TAG(value) == JS_TAG_UNDEFINED;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_freeValue(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong context,
                                                                             jlong value) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL(env, val, MSG_NULL_JS_VALUE);
    if (!isValueFreed(*val)) {
        safeFreeValue(ctx, val);
        js_free_rt(JS_GetRuntime(ctx), val);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_freeRuntime(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong runtime) {

    JSRuntime *rt = (JSRuntime *) runtime;
#ifdef LEAK_TRIGGER
    leak_state = 0;
#endif
    JS_FreeRuntime(rt);
#ifdef LEAK_TRIGGER
    if (leak_state != 0) {
        THROW_ILLEGAL_STATE_EXCEPTION(env, "Memory Leak");
    }
#endif
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isBool(JNIEnv *env, jobject thiz,
                                                                          jlong value) {
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    return (jboolean) JS_IsBool(*val);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getBool(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong value) {
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    return (jboolean) (JS_VALUE_GET_BOOL(*val));
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newBool(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong context,
                                                                           jboolean value) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);

    void *result = NULL;
    JSValue val = JS_NewBool(ctx, value);

    COPY_JS_VALUE(ctx, val, result);
    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isNumber(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong value) {
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    return (jboolean) JS_IsNumber(*val);
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getInt(JNIEnv *env, jobject thiz,
                                                                          jlong value) {
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    return (jint) (JS_VALUE_GET_INT(*val));
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newInt(JNIEnv *env, jobject thiz,
                                                                          jlong context, jint value) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);

    void *result = NULL;
    JSValue val = JS_NewInt32(ctx, value);
    COPY_JS_VALUE(ctx, val, result);
    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getFloat64(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong context,
                                                                              jlong value) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    double d;
    JS_ToFloat64(ctx, &d, *val);
    return d;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newFloat64(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong context,
                                                                              jdouble d) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);

    void *result = NULL;
    JSValue val = JS_NewFloat64(ctx, d);
    COPY_JS_VALUE(ctx, val, result);
    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isString(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong value) {
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    return (jboolean) JS_IsString(*val);
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getString(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong context,
                                                                             jlong value) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);

    const char *str = JS_ToCString(ctx, *val);
    CHECK_NULL_RET(env, str, MSG_OOM);

    jstring j_str = env->NewStringUTF(str);

    JS_FreeCString(ctx, str);

    CHECK_NULL_RET(env, j_str, MSG_OOM);

    return j_str;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newString(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong context,
                                                                             jstring value) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    CHECK_NULL_RET(env, value, "Null value");

    const char *value_utf = env->GetStringUTFChars(value, NULL);
    CHECK_NULL_RET(env, value_utf, MSG_OOM);

    void *result = NULL;
    JSValue val = JS_NewString(ctx, value_utf);
    COPY_JS_VALUE(ctx, val, result);

    env->ReleaseStringUTFChars(value, value_utf);

    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isArray(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong context,
                                                                           jlong value) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    return (jboolean) JS_IsArray(ctx, *val);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newArray(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong context) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);

    void *result = NULL;
    JSValue val = JS_NewArray(ctx);
    COPY_JS_VALUE(ctx, val, result);
    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getProperty__JJI(JNIEnv *env,
                                                                                    jobject thiz,
                                                                                    jlong context,
                                                                                    jlong value,
                                                                                    jint index) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);

    void *result = NULL;

    JSValue prop = JS_GetPropertyUint32(ctx, *val, (uint32_t) index);

    COPY_JS_VALUE(ctx, prop, result);
    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_setProperty__JJIJ(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jlong context,
                                                                                     jlong value,
                                                                                     jint index,
                                                                                     jlong property) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL(env, val, MSG_NULL_JS_VALUE);
    JSValue *prop = (JSValue *) property;
    CHECK_NULL(env, prop, "Null property");

    // JS_SetPropertyUint32 requires a reference count of the property JSValue
    // Meanwhile, it calls JS_FreeValue on the property JSValue if it fails
    JS_DupValue(ctx, *prop);
    JS_SetPropertyUint32(ctx, *val, (uint32_t) index, *prop);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_setProperty__JJLjava_lang_String_2J(
        JNIEnv *env, jobject thiz, jlong context, jlong value, jstring name, jlong property) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL(env, val, MSG_NULL_JS_VALUE);
    CHECK_NULL(env, name, "Null name");
    JSValue *prop = (JSValue *) property;
    CHECK_NULL(env, prop, "Null property");

    const char *name_utf = env->GetStringUTFChars(name, NULL);
    CHECK_NULL(env, name_utf, MSG_OOM);

    // JS_SetPropertyStr requires a reference count of the property JSValue
    // Meanwhile, it calls JS_FreeValue on the property JSValue if it fails
    JS_DupValue(ctx, *prop);
    JS_SetPropertyStr(ctx, *val, name_utf, *prop);
    env->ReleaseStringUTFChars(name, name_utf);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getProperty__JJLjava_lang_String_2(
        JNIEnv *env, jobject thiz, jlong context, jlong value, jstring name) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    CHECK_NULL_RET(env, name, "Null name");

    const char *name_utf = env->GetStringUTFChars(name, NULL);
    CHECK_NULL_RET(env, name_utf, MSG_OOM);

    void *result = NULL;

    JSValue prop = JS_GetPropertyStr(ctx, *val, name_utf);


    COPY_JS_VALUE(ctx, prop, result);
    env->ReleaseStringUTFChars(name, name_utf);

    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isObject(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong value) {
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    return (jboolean) JS_IsObject(*val);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newObject(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong context) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);

    void *result = NULL;
    JSValue val = JS_NewObject(ctx);
    COPY_JS_VALUE(ctx, val, result);
    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getNull(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong context) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);

    void *result = NULL;
    JSValue val = JS_NULL;
    COPY_JS_VALUE(ctx, val, result);
    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getUndefined(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jlong context) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);

    void *result = 0;
    JSValue val = JS_UNDEFINED;
    COPY_JS_VALUE(ctx, val, result);
    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getType(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong value) {
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    return JS_VALUE_GET_NORM_TAG(*val);

}
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getOwnPropertyNames(JNIEnv *env,
                                                                                       jobject thiz,
                                                                                       jlong context,
                                                                                       jlong value) {
    JSContext *ctx = (JSContext *) context;
    JSPropertyEnum *names = NULL;
    uint32_t count = 0;
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);

    JS_GetOwnPropertyNames(ctx, &names, &count, *val, JS_GPN_ENUM_ONLY | JS_GPN_STRING_MASK);

    CHECK_NULL_RET(env, names, MSG_NULL_JS_VALUE);

    jclass stringClass = env->FindClass( "java/lang/String" );
    jobjectArray stringArray = env->NewObjectArray( count, stringClass, 0 );

    JSPropertyEnum* iterator = names;
    for ( uint32_t i = 0; i < count; ++i ) {
        const char* str = JS_AtomToCString(ctx, iterator->atom);
        JS_FreeCString(ctx, str);
        jstring j_str = env->NewStringUTF(str);
        env->SetObjectArrayElement( stringArray, i, j_str );
        iterator++;
    }

    return stringArray;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_call(JNIEnv *env, jobject thiz,
                                                                        jlong context,
                                                                        jlong function, jlong obj,
                                                                        jlongArray args) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *func_obj = (JSValue *) function;
    CHECK_NULL_RET(env, func_obj, "Null function");
    JSValue *this_obj = (JSValue *) obj;
    CHECK_NULL_RET(env, args, "Null arguments");
    jlong *elements = env->GetLongArrayElements(args, NULL);
    CHECK_NULL_RET(env, elements, MSG_OOM);

    int argc = env->GetArrayLength(args);
    JSValueConst argv[argc];
    for (int i = 0; i < argc; i++) {
        argv[i] = *((JSValue *) elements[i]);
    }

    void *result = NULL;

    JSValue ret = JS_Call(ctx, *func_obj, this_obj != NULL ? *this_obj : JS_UNDEFINED, argc, argv);

    COPY_JS_VALUE(ctx, ret, result);

    env->ReleaseLongArrayElements(args, elements, JNI_ABORT);

    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newRuntime(JNIEnv *env,
                                                                              jobject thiz) {
    JSRuntime *rt = JS_NewRuntime();
    CHECK_NULL_RET(env, rt, MSG_OOM);
    return (jlong) rt;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newContext(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong runtime) {
    JSRuntime *rt = (JSRuntime *) runtime;
    JSContext *ctx = JS_NewContext(rt);
    CHECK_NULL_RET(env, ctx, MSG_OOM);

    if (java_callback_init(ctx)) THROW_ILLEGAL_STATE_EXCEPTION_RET(env, MSG_OOM);

    return (jlong) ctx;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getGlobalObject(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jlong context) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);

    void *result = NULL;

    JSValue val = JS_GetGlobalObject(ctx);
    COPY_JS_VALUE(ctx, val, result);

    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_evaluate(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong context,
                                                                            jstring source_code,
                                                                            jstring file_name,
                                                                            jint flags) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    CHECK_NULL_RET(env, source_code, "Null source code");
    CHECK_NULL_RET(env, file_name, "Null file name");

    const char *source_code_utf = NULL;
    jsize source_code_length = 0;
    const char *file_name_utf = NULL;
    void *result = NULL;

    source_code_utf = env->GetStringUTFChars(source_code, NULL);
    source_code_length = env->GetStringUTFLength(source_code);
    file_name_utf = env->GetStringUTFChars(file_name, NULL);

    if (source_code_utf != NULL && file_name_utf != NULL) {
        JSValue val = JS_Eval(ctx, source_code_utf, (size_t) source_code_length, file_name_utf, flags);
        COPY_JS_VALUE(ctx, val, result);
    }

    if (source_code_utf != NULL) {
        env->ReleaseStringUTFChars(source_code, source_code_utf);
    }
    if (file_name_utf != NULL) {
        env->ReleaseStringUTFChars(file_name, file_name_utf);
    }

    CHECK_NULL_RET(env, result, MSG_OOM);

    return (jlong) result;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_freeContext(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong context) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL(env, ctx, MSG_NULL_JS_CONTEXT);
    JS_FreeContext(ctx);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isFunction(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong context,
                                                                              jlong value) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);
    return (jboolean) JS_IsFunction(ctx, *val);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_hasProperty(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong context,
                                                                               jlong value,
                                                                               jstring name) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);

    const char *name_utf = env->GetStringUTFChars(name, NULL);
    CHECK_NULL_RET(env, name_utf, MSG_OOM);
    JSAtom atom = JS_NewAtom(ctx, name_utf);
    int result = 0;
    if (atom != JS_ATOM_NULL) {
        result = JS_HasProperty(ctx, *val, atom);
        JS_FreeAtom(ctx, atom);
        return result;
    }
    env->ReleaseStringUTFChars(name, name_utf);
    return (jboolean)result;
}

static jlong JS_ToPointer(JNIEnv* env, JSContext *ctx, JSValue val) {
    void *result = NULL;

    COPY_JS_VALUE(ctx, val, result);
    CHECK_NULL_RET(env, result, MSG_OOM);
    return (jlong)result;
}

static JSValue invoke(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv, int magic, JSValue *func_data) {
    JavaCallbackData *data = (JavaCallbackData*)JS_GetOpaque(*func_data, java_callback_class_id);

    JNIEnv *env;
    bool attached = false;
    switch(data->vm->GetEnv((void**)&env, JNI_VERSION_1_6)) {
        case JNI_OK:
            break;
        case JNI_EDETACHED:
            data->vm->AttachCurrentThread(&env, NULL);
            attached = true;
            break;
    }

    jclass clazz = env->FindClass( "com/segment/analytics/substrata/kotlin/JSRegistry" );
    jmethodID  method = env->GetMethodID(clazz, "jsCallback","(I[J)J");
    jclass contextClazz = env->FindClass( "com/segment/analytics/substrata/kotlin/JSContext" );

    jlongArray params = env->NewLongArray(argc);
    if (argc > 0) {
        jlong paramsC[argc];
        for (int i = 0; i < argc; i++) {
            paramsC[i] = JS_ToPointer(env, ctx, argv[i]);
        }
        env->SetLongArrayRegion(params, 0, argc, paramsC);
    }

    jfieldID  field = env->GetFieldID(contextClazz, "registry",
                                      "Lcom/segment/analytics/substrata/kotlin/JSRegistry;");
    jobject registry = env->GetObjectField(data->js_context, field);
    jlong ret = env->CallLongMethod(registry, method, (jint)magic, params);
    JSValue *retVal = (JSValue *) ret;

    env->DeleteLocalRef(params);
    if (attached) {
        data->vm->DetachCurrentThread();
    }

    return *retVal;

}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newFunction(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jobject js_context,
                                                                               jlong context,
                                                                               jlong value,
                                                                               jstring name,
                                                                               jint id) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL_RET(env, val, MSG_NULL_JS_VALUE);

    // create JavaCallbackData that carries JSContext instance for later use in callback
    JSRuntime *rt = JS_GetRuntime(ctx);
    JavaCallbackData *data = NULL;
    data = (JavaCallbackData*) js_malloc_rt(rt, sizeof(JavaCallbackData));
    JSValue callback = JS_NewObjectClass(ctx, java_callback_class_id);
    env->GetJavaVM(&data->vm);
    data->js_context =  env->NewGlobalRef(js_context);
    JS_SetOpaque(callback, data);

    const char *name_utf = env->GetStringUTFChars(name, NULL);
    JSValue newFunction = JS_NewCFunctionData(ctx, invoke, 1, id, 2, &callback);

    // JS_SetPropertyStr requires a reference count of the property JSValue
    // Meanwhile, it calls JS_FreeValue on the property JSValue if it fails
    JS_DupValue(ctx, newFunction);
    JS_SetPropertyStr(ctx, *val, name_utf, newFunction);

    env->ReleaseStringUTFChars(name, name_utf);
    JS_FreeValue(ctx, callback);

    void *result = NULL;
    COPY_JS_VALUE(ctx, newFunction, result);
    CHECK_NULL_RET(env, result, MSG_OOM);
    return (jlong) result;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getException(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jlong context) {

    JSContext *ctx = (JSContext *) context;
    CHECK_NULL_RET(env, ctx, MSG_NULL_JS_CONTEXT);

    jclass js_exception_class = env->FindClass( "com/segment/analytics/substrata/kotlin/JSException");
    CHECK_NULL_RET(env, js_exception_class, "Can't find JSException");

    jmethodID constructor_id = env->GetMethodID(js_exception_class, "<init>", "(ZLjava/lang/String;Ljava/lang/String;)V");
    CHECK_NULL_RET(env, constructor_id, "Can't find JSException constructor");

    const char *exception_str = NULL;
    const char *stack_str = NULL;

    JSValue exception = JS_GetException(ctx);
    exception_str = JS_ToCString(ctx, exception);
    jboolean is_error = (jboolean) JS_IsError(ctx, exception);
    if (is_error) {
        JSValue stack = JS_GetPropertyStr(ctx, exception, "stack");
        if (!JS_IsUndefined(stack)) {
            stack_str = JS_ToCString(ctx, stack);
        }
        JS_FreeValue(ctx, stack);
    }
    JS_FreeValue(ctx, exception);

    jstring exception_j_str = (exception_str != NULL) ? env->NewStringUTF(exception_str) : NULL;
    jstring stack_j_str = (stack_str != NULL) ? env->NewStringUTF(stack_str) : NULL;

    if (exception_str != NULL) {
        JS_FreeCString(ctx, exception_str);
    }
    if (stack_str != NULL) {
        JS_FreeCString(ctx, stack_str);
    }

    jobject result = env->NewObject(js_exception_class, constructor_id, is_error, exception_j_str, stack_j_str);
    CHECK_NULL_RET(env, result, "Can't create instance of JSException");

    return result;
}

static JSValue construct(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv, int magic, JSValue *func_data) {
    JavaConstructData *data = (JavaConstructData*)JS_GetOpaque(*func_data, magic);

    JNIEnv *env;
    bool attached = false;
    switch(data->vm->GetEnv((void**)&env, JNI_VERSION_1_6)) {
        case JNI_OK:
            break;
        case JNI_EDETACHED:
            data->vm->AttachCurrentThread(&env, NULL);
            attached = true;
            break;
    }

    JSValue result = js_create_from_ctor(ctx, this_val, magic);
    void *resultPtr = NULL;
    COPY_JS_VALUE(ctx, result, resultPtr);

    jclass clazz = env->FindClass( "com/segment/analytics/substrata/kotlin/JSRegistry" );
    jmethodID  method = env->GetMethodID(clazz, "register","(JI[J)V");
    jclass contextClazz = env->FindClass( "com/segment/analytics/substrata/kotlin/JSContext" );

    jlongArray params = env->NewLongArray(argc);
    if (argc > 0) {
        jlong paramsC[argc];
        for (int i = 0; i < argc; i++) {
            paramsC[i] = JS_ToPointer(env, ctx, argv[i]);
        }
        env->SetLongArrayRegion(params, 0, argc, paramsC);
    }

    jfieldID  field = env->GetFieldID(contextClazz, "registry",
                                      "Lcom/segment/analytics/substrata/kotlin/JSRegistry;");
    jobject registry = env->GetObjectField(data->js_context, field);
    env->CallVoidMethod(registry, method, (jlong)resultPtr, (jint)data->class_id, params);

    if (attached) {
        data->vm->DetachCurrentThread();
    }

    return *(JSValue*)resultPtr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newClass(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jobject context,
                                                                            jlong context_ref,
                                                                            jlong value_ref,
                                                                            jstring name,
                                                                            jint id) {
    JSContext *ctx = (JSContext *) context_ref;
    CHECK_NULL(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value_ref;
    CHECK_NULL(env, val, MSG_NULL_JS_VALUE);
    JSRuntime *rt = JS_GetRuntime(ctx);
    const char *class_name = env->GetStringUTFChars(name, NULL);

    // Create class
    JSClassDef class_def = {
            .class_name = class_name,
            .call = NULL,
            .exotic = NULL,
            .gc_mark = NULL,
            .finalizer = NULL,
    };
    JSClassID class_id;
    JS_NewClassID(&class_id);
    JS_NewClass(rt, class_id, &class_def);

    // create data for constructor callback
    JavaConstructData *data = NULL;
    data = (JavaConstructData*) js_malloc_rt(rt, sizeof(JavaConstructData));
    data->class_id = id;
    data->js_context = env->NewGlobalRef(context);
    env->GetJavaVM(&data->vm);
    JSValue callback = JS_NewObjectClass(ctx, class_id);
    JS_SetOpaque(callback, data);

    // create constructor and prototype
    JSValue constructor = JS_NewCFunctionData2(ctx, construct, 1, JS_CFUNC_constructor, class_id, 2, &callback);
    JSValue prototype = JS_NewObject(ctx);
    JS_DefinePropertyValueStr(ctx, *val, class_name,
                              JS_DupValue(ctx, constructor),
                              JS_PROP_WRITABLE);
    JS_SetConstructor(ctx, constructor, prototype);
    JS_SetClassProto(ctx, class_id, prototype);

    void *prototypePtr = NULL;
    COPY_JS_VALUE(ctx, prototype, prototypePtr);

    // set instance methods, static methods, static properties on prototype
    jclass clazz = env->FindClass( "com/segment/analytics/substrata/kotlin/JSRegistry" );
    jmethodID registerProto = env->GetMethodID(clazz, "register","(JI)V");
    jclass contextClazz = env->FindClass( "com/segment/analytics/substrata/kotlin/JSContext" );
    jfieldID  field = env->GetFieldID(contextClazz, "registry",
                                      "Lcom/segment/analytics/substrata/kotlin/JSRegistry;");
    jobject registry = env->GetObjectField(context, field);
    env->CallVoidMethod(registry, registerProto, (jlong)prototypePtr, id);


    // clean up
    env->ReleaseStringUTFChars(name, class_name);
    JS_FreeValue(ctx, callback);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newProperty(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jobject js_context,
                                                                               jlong context,
                                                                               jlong value,
                                                                               jstring name,
                                                                               jint getter_id,
                                                                               jint setter_id) {
    JSContext *ctx = (JSContext *) context;
    CHECK_NULL(env, ctx, MSG_NULL_JS_CONTEXT);
    JSValue *val = (JSValue *) value;
    CHECK_NULL(env, val, MSG_NULL_JS_VALUE);

    // create JavaCallbackData that carries JSContext instance for later use in callback
    JSRuntime *rt = JS_GetRuntime(ctx);
    JavaCallbackData *data = NULL;
    data = (JavaCallbackData*) js_malloc_rt(rt, sizeof(JavaCallbackData));
    JSValue callback = JS_NewObjectClass(ctx, java_callback_class_id);
    env->GetJavaVM(&data->vm);
    data->js_context =  env->NewGlobalRef(js_context);
    JS_SetOpaque(callback, data);

    const char *name_utf = env->GetStringUTFChars(name, NULL);
    JSAtom propAtom = JS_NewAtom(ctx, name_utf);
    JSValue getter = JS_NewCFunctionData(ctx, invoke, 1, getter_id, 2, &callback);
    JSValue setter = JS_NewCFunctionData(ctx, invoke, 1, setter_id, 2, &callback);

    JS_DefinePropertyGetSet(ctx, *val, propAtom, JS_DupValue(ctx, getter), JS_DupValue(ctx, setter), JS_PROP_HAS_WRITABLE | JS_PROP_HAS_ENUMERABLE | JS_PROP_HAS_GET);

    env->ReleaseStringUTFChars(name, name_utf);
    JS_FreeValue(ctx, callback);
}