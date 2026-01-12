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

int32_t js_get_refcount(JSValue v) {
    JSRefCountHeader *p = (JSRefCountHeader *)JS_VALUE_GET_PTR(v);
    return p->ref_count;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_freeValue(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong context,
                                                                             jlong value) {

    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;
        JSRuntime *rt = JS_GetRuntime(ctx);
        if (JS_IsLiveObject(rt, *val)) {
            if (js_get_refcount(*val) > 0) {
                JS_FreeValue(ctx, *val);
                js_free_rt(rt, val);
            }
        }
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_freeRuntime(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong runtime) {
    try {
        JSRuntime *rt = (JSRuntime *) runtime;
        JS_FreeRuntime(rt);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
    }
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isBool(JNIEnv *env, jobject thiz,
                                                                          jlong value) {
    try {
        JSValue *val = (JSValue *) value;
        return (jboolean) JS_IsBool(*val);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return false;
    }
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getBool(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong value) {
    try {
        JSValue *val = (JSValue *) value;
        return (jboolean) (JS_VALUE_GET_BOOL(*val));
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return false;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newBool(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong context,
                                                                           jboolean value) {
    try {
        JSContext *ctx = (JSContext *) context;

        void *result = NULL;
        JSValue val = JS_NewBool(ctx, value);

        COPY_JS_VALUE(ctx, val, result);
        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isNumber(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong value) {
    try {
        JSValue *val = (JSValue *) value;
        return (jboolean) JS_IsNumber(*val);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return false;
    }
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getInt(JNIEnv *env, jobject thiz,
                                                                          jlong value) {
    try {
        JSValue *val = (JSValue *) value;
        return (jint) (JS_VALUE_GET_INT(*val));
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newInt(JNIEnv *env, jobject thiz,
                                                                          jlong context, jint value) {
    try {
        JSContext *ctx = (JSContext *) context;

        void *result = NULL;
        JSValue val = JS_NewInt32(ctx, value);
        COPY_JS_VALUE(ctx, val, result);

        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getFloat64(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong context,
                                                                              jlong value) {
    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;
        double d;
        JS_ToFloat64(ctx, &d, *val);
        return d;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newFloat64(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong context,
                                                                              jdouble d) {
    try {
        JSContext *ctx = (JSContext *) context;

        void *result = NULL;
        JSValue val = JS_NewFloat64(ctx, d);
        COPY_JS_VALUE(ctx, val, result);

        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getLong(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong context,
                                                                              jlong value) {
    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;
        int64_t d;
        JS_ToInt64(ctx, &d, *val);
        return d;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newLong(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong context,
                                                                              jlong d) {
    try {
        JSContext *ctx = (JSContext *) context;

        void *result = NULL;
        JSValue val = JS_NewInt64(ctx, d);
        COPY_JS_VALUE(ctx, val, result);

        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isString(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong value) {
    try {
        JSValue *val = (JSValue *) value;
        return (jboolean) JS_IsString(*val);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return false;
    }
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getString(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong context,
                                                                             jlong value) {
    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;

        const char *str = JS_ToCString(ctx, *val);
        jstring j_str = env->NewStringUTF(str);
        JS_FreeCString(ctx, str);

        assert_no_exception(env);
        return j_str;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return (jstring) "";
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newString(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong context,
                                                                             jstring value) {

    try {
        JSContext *ctx = (JSContext *) context;

        const char *value_utf = env->GetStringUTFChars(value, NULL);

        void *result = NULL;
        JSValue val = JS_NewString(ctx, value_utf);
        COPY_JS_VALUE(ctx, val, result);

        env->ReleaseStringUTFChars(value, value_utf);

        assert_no_exception(env);
        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isArray(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong context,
                                                                           jlong value) {

    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;
        return (jboolean) JS_IsArray(ctx, *val);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return false;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newArray(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong context) {

    try {
        JSContext *ctx = (JSContext *) context;

        void *result = NULL;
        JSValue val = JS_NewArray(ctx);
        COPY_JS_VALUE(ctx, val, result);

        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getProperty__JJI(JNIEnv *env,
                                                                                    jobject thiz,
                                                                                    jlong context,
                                                                                    jlong value,
                                                                                    jint index) {

    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;

        void *result = NULL;
        JSValue prop = JS_GetPropertyUint32(ctx, *val, (uint32_t) index);
        COPY_JS_VALUE(ctx, prop, result);

        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_setProperty__JJIJ(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jlong context,
                                                                                     jlong value,
                                                                                     jint index,
                                                                                     jlong property) {

    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;
        JSValue *prop = (JSValue *) property;

        // JS_SetPropertyUint32 requires a reference count of the property JSValue
        // Meanwhile, it calls JS_FreeValue on the property JSValue if it fails
        JS_DupValue(ctx, *prop);
        JS_SetPropertyUint32(ctx, *val, (uint32_t) index, *prop);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_setProperty__JJLjava_lang_String_2J(
        JNIEnv *env, jobject thiz, jlong context, jlong value, jstring name, jlong property) {
    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;
        JSValue *prop = (JSValue *) property;

        const char *name_utf = env->GetStringUTFChars(name, NULL);

        // JS_SetPropertyStr requires a reference count of the property JSValue
        // Meanwhile, it calls JS_FreeValue on the property JSValue if it fails
        JS_DupValue(ctx, *prop);
        JS_SetPropertyStr(ctx, *val, name_utf, *prop);
        env->ReleaseStringUTFChars(name, name_utf);

        assert_no_exception(env);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getProperty__JJLjava_lang_String_2(
        JNIEnv *env, jobject thiz, jlong context, jlong value, jstring name) {
    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;

        const char *name_utf = env->GetStringUTFChars(name, NULL);
        void *result = NULL;

        JSValue prop = JS_GetPropertyStr(ctx, *val, name_utf);
        COPY_JS_VALUE(ctx, prop, result);
        env->ReleaseStringUTFChars(name, name_utf);

        assert_no_exception(env);
        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isObject(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong value) {
    try {
        JSValue *val = (JSValue *) value;
        return (jboolean) JS_IsObject(*val);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return false;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newObject(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong context) {

    try {
        JSContext *ctx = (JSContext *) context;

        void *result = NULL;
        JSValue val = JS_NewObject(ctx);
        COPY_JS_VALUE(ctx, val, result);

        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getNull(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong context) {

    try {
        JSContext *ctx = (JSContext *) context;

        void *result = NULL;
        JSValue val = JS_NULL;
        COPY_JS_VALUE(ctx, val, result);

        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getUndefined(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jlong context) {

    try {
        JSContext *ctx = (JSContext *) context;

        void *result = 0;
        JSValue val = JS_UNDEFINED;
        COPY_JS_VALUE(ctx, val, result);

        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getType(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong value) {
    try {
        JSValue *val = (JSValue *) value;
        return JS_VALUE_GET_NORM_TAG(*val);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }

}
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getOwnPropertyNames(JNIEnv *env,
                                                                                       jobject thiz,
                                                                                       jlong context,
                                                                                       jlong value) {
    try {
        JSContext *ctx = (JSContext *) context;
        JSPropertyEnum *names = NULL;
        uint32_t count = 0;
        JSValue *val = (JSValue *) value;

        JS_GetOwnPropertyNames(ctx, &names, &count, *val, JS_GPN_ENUM_ONLY | JS_GPN_STRING_MASK);

        jclass stringClass = env->FindClass("java/lang/String");
        jobjectArray stringArray = env->NewObjectArray(count, stringClass, 0);

        JSPropertyEnum *iterator = names;
        for (uint32_t i = 0; i < count; ++i) {
            const char *str = JS_AtomToCString(ctx, iterator->atom);
            JS_FreeCString(ctx, str);
            jstring j_str = env->NewStringUTF(str);
            env->SetObjectArrayElement(stringArray, i, j_str);
            iterator++;
        }

        assert_no_exception(env);
        return stringArray;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return nullptr;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_call(JNIEnv *env, jobject thiz,
                                                                        jlong context,
                                                                        jlong function, jlong obj,
                                                                        jlongArray args) {

    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *func_obj = (JSValue *) function;
        JSValue *this_obj = (JSValue *) obj;
        jlong *elements = env->GetLongArrayElements(args, NULL);

        int argc = env->GetArrayLength(args);
        JSValueConst argv[argc];
        for (int i = 0; i < argc; i++) {
            argv[i] = *((JSValue *) elements[i]);
        }

        void *result = NULL;
        JSValue ret = JS_Call(ctx, *func_obj, this_obj != NULL ? *this_obj : JS_UNDEFINED, argc,
                              argv);
        COPY_JS_VALUE(ctx, ret, result);
        env->ReleaseLongArrayElements(args, elements, JNI_ABORT);

        assert_no_exception(env);
        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newRuntime(JNIEnv *env,
                                                                              jobject thiz) {
    try {
        JSRuntime *rt = JS_NewRuntime();
        return (jlong) rt;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_newContext(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong runtime) {
    try {
        JSRuntime *rt = (JSRuntime *) runtime;
        JSContext *ctx = JS_NewContext(rt);

        if (java_callback_init(ctx)) throw NewJavaException(env, CLASS_NAME_ILLEGAL_STATE_EXCEPTION, MSG_OOM);

        return (jlong) ctx;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getGlobalObject(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jlong context) {
    try {
        JSContext *ctx = (JSContext *) context;

        void *result = NULL;
        JSValue val = JS_GetGlobalObject(ctx);
        COPY_JS_VALUE(ctx, val, result);

        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_evaluate(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong context,
                                                                            jstring source_code,
                                                                            jstring file_name,
                                                                            jint flags) {
    try {
        JSContext *ctx = (JSContext *) context;

        const char *source_code_utf = NULL;
        jsize source_code_length = 0;
        const char *file_name_utf = NULL;
        void *result = NULL;

        source_code_utf = env->GetStringUTFChars(source_code, NULL);
        source_code_length = env->GetStringUTFLength(source_code);
        file_name_utf = env->GetStringUTFChars(file_name, NULL);

        if (source_code_utf != NULL && file_name_utf != NULL) {
            JSValue val = JS_Eval(ctx, source_code_utf, (size_t) source_code_length, file_name_utf,
                                  flags);
            COPY_JS_VALUE(ctx, val, result);
        }

        if (source_code_utf != NULL) {
            env->ReleaseStringUTFChars(source_code, source_code_utf);
        }
        if (file_name_utf != NULL) {
            env->ReleaseStringUTFChars(file_name, file_name_utf);
        }

        assert_no_exception(env);
        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_freeContext(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong context) {
    try {
        JSContext *ctx = (JSContext *) context;
        JS_FreeContext(ctx);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
    }
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_isFunction(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong context,
                                                                              jlong value) {
    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;
        return (jboolean) JS_IsFunction(ctx, *val);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return false;
    }
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_hasProperty(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong context,
                                                                               jlong value,
                                                                               jstring name) {
    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;

        const char *name_utf = env->GetStringUTFChars(name, NULL);
        JSAtom atom = JS_NewAtom(ctx, name_utf);
        int result = 0;
        if (atom != JS_ATOM_NULL) {
            result = JS_HasProperty(ctx, *val, atom);
            JS_FreeAtom(ctx, atom);
            return result;
        }
        env->ReleaseStringUTFChars(name, name_utf);

        assert_no_exception(env);
        return (jboolean) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return false;
    }
}

static jlong JS_ToPointer(JNIEnv* env, JSContext *ctx, JSValue val) {
    void *result = NULL;
    COPY_JS_VALUE(ctx, val, result);
    return (jlong)result;
}

static JSValue invoke(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv, int magic, JSValue *func_data) {
    JNIEnv *env;
    try {
        JavaCallbackData *data = (JavaCallbackData *) JS_GetOpaque(*func_data,
                                                                   java_callback_class_id);

        bool attached = false;
        switch (data->vm->GetEnv((void **) &env, JNI_VERSION_1_6)) {
            case JNI_OK:
                break;
            case JNI_EDETACHED:
                data->vm->AttachCurrentThread(&env, NULL);
                attached = true;
                break;
        }

        jclass clazz = env->FindClass("com/segment/analytics/substrata/kotlin/JSRegistry");
        jmethodID method = env->GetMethodID(clazz, "jsCallback", "(Ljava/lang/Object;I[J)J");
        jclass contextClazz = env->FindClass("com/segment/analytics/substrata/kotlin/JSContext");

        jlongArray params = env->NewLongArray(argc);
        if (argc > 0) {
            jlong paramsC[argc];
            for (int i = 0; i < argc; i++) {
                paramsC[i] = JS_ToPointer(env, ctx, argv[i]);
            }
            env->SetLongArrayRegion(params, 0, argc, paramsC);
        }

        jfieldID field = env->GetFieldID(contextClazz, "registry",
                                         "Lcom/segment/analytics/substrata/kotlin/JSRegistry;");
        jobject registry = env->GetObjectField(data->js_context, field);

        JSAtom atom = JS_NewAtom(ctx, "__instanceAtom");
        jlong ret;
        if (JS_HasProperty(ctx, this_val, atom)) {
            JSValue instanceClassId = JS_GetProperty(ctx, this_val, atom);
            int classId = 0;
            JS_ToInt32(ctx, &classId, instanceClassId);
            JavaInstanceData *instanceData = (JavaInstanceData *) JS_GetOpaque(this_val, classId);
            ret = env->CallLongMethod(registry, method, instanceData->instance, (jint) magic,
                                      params);
        } else {
            jobject nullObject = NULL;
            ret = env->CallLongMethod(registry, method, nullObject, (jint) magic, params);
        }
        JSValue *retVal = (JSValue *) ret;

        JS_FreeAtom(ctx, atom);
        env->DeleteLocalRef(params);
        if (attached) {
            data->vm->DetachCurrentThread();
        }

        assert_no_exception(env);
        return *retVal;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return JS_UNDEFINED;
    }
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
    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;

        // create JavaCallbackData that carries JSContext instance for later use in callback
        JSRuntime *rt = JS_GetRuntime(ctx);
        JavaCallbackData *data = NULL;
        data = (JavaCallbackData *) js_malloc_rt(rt, sizeof(JavaCallbackData));
        JSValue callback = JS_NewObjectClass(ctx, java_callback_class_id);
        env->GetJavaVM(&data->vm);
        data->js_context = env->NewGlobalRef(js_context);
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

        assert_no_exception(env);
        return (jlong) result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return 0;
    }
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_segment_analytics_substrata_kotlin_QuickJS_00024Companion_getException(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jlong context) {

    try {
        JSContext *ctx = (JSContext *) context;

        jclass js_exception_class = env->FindClass(
                "com/segment/analytics/substrata/kotlin/JSException");
        jmethodID constructor_id = env->GetMethodID(js_exception_class, "<init>",
                                                    "(ZLjava/lang/String;Ljava/lang/String;)V");

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

        jobject result = env->NewObject(js_exception_class, constructor_id, is_error,
                                        exception_j_str, stack_j_str);

        assert_no_exception(env);
        return result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return nullptr;
    }
}

static JSValue construct(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv, int magic, JSValue *func_data) {
    JNIEnv *env;

    try {
        JavaConstructData *data = (JavaConstructData *) JS_GetOpaque(*func_data, magic);
        bool attached = false;
        switch (data->vm->GetEnv((void **) &env, JNI_VERSION_1_6)) {
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

        jclass clazz = env->FindClass("com/segment/analytics/substrata/kotlin/JSRegistry");
        jmethodID method = env->GetMethodID(clazz, "registerInstance", "(JI[J)Ljava/lang/Object;");
        jclass contextClazz = env->FindClass("com/segment/analytics/substrata/kotlin/JSContext");

        jlongArray params = env->NewLongArray(argc);
        if (argc > 0) {
            jlong paramsC[argc];
            for (int i = 0; i < argc; i++) {
                paramsC[i] = JS_ToPointer(env, ctx, argv[i]);
            }
            env->SetLongArrayRegion(params, 0, argc, paramsC);
        }

        jfieldID field = env->GetFieldID(contextClazz, "registry",
                                         "Lcom/segment/analytics/substrata/kotlin/JSRegistry;");
        jobject registry = env->GetObjectField(data->js_context, field);
        jobject instance = env->CallObjectMethod(registry, method, (jlong) resultPtr,
                                                 (jint) data->class_id, params);

        JSRuntime *rt = JS_GetRuntime(ctx);
        JavaInstanceData *instanceData = (JavaInstanceData *) js_malloc_rt(rt,
                                                                           sizeof(JavaInstanceData));
        instanceData->instance = env->NewGlobalRef(instance);;
        JS_SetPropertyStr(ctx, JS_DupValue(ctx, result), "__instanceAtom", JS_NewInt32(ctx, magic));
        JS_SetOpaque(result, instanceData);

        env->DeleteLocalRef(params);
        if (attached) {
            data->vm->DetachCurrentThread();
        }

//    DO NOT release JavaConstructData, it is shared for all instance creation of the same class!!
//    env->DeleteGlobalRef(data->js_context);
//    js_free_rt(rt, data);
        assert_no_exception(env);
        return result;
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
        return JS_UNDEFINED;
    }
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
    try {
        JSContext *ctx = (JSContext *) context_ref;
        JSValue *val = (JSValue *) value_ref;
        JSRuntime *rt = JS_GetRuntime(ctx);
        const char *class_name = env->GetStringUTFChars(name, NULL);

        // Create class
        JSClassDef class_def = {
                .class_name = class_name,
                .finalizer = NULL,
        };
        JSClassID class_id = 0;
        JS_NewClassID(&class_id);
        JS_NewClass(rt, class_id, &class_def);

        // create data for constructor callback
        JavaConstructData *data = NULL;
        data = (JavaConstructData *) js_malloc_rt(rt, sizeof(JavaConstructData));
        JSValue callback = JS_NewObjectClass(ctx, class_id);
        env->GetJavaVM(&data->vm);
        data->js_context = env->NewGlobalRef(context);
        data->class_id = id;
        JS_SetOpaque(callback, data);

        // create constructor and prototype
        JSValue constructor = JS_NewCFunctionData2(ctx, construct, 1, JS_CFUNC_constructor,
                                                   class_id, 2, &callback);
        JSValue prototype = JS_NewObject(ctx);
        JS_DefinePropertyValueStr(ctx, *val, class_name,
                                  JS_DupValue(ctx, constructor),
                                  JS_PROP_WRITABLE);
        JS_SetConstructor(ctx, constructor, prototype);
        JS_SetClassProto(ctx, class_id, prototype);

        void *ctorPtr = NULL;
        void *prototypePtr = NULL;
        COPY_JS_VALUE(ctx, constructor, ctorPtr);
        COPY_JS_VALUE(ctx, prototype, prototypePtr);

        // set instance methods, static methods, static properties on prototype
        jclass clazz = env->FindClass("com/segment/analytics/substrata/kotlin/JSRegistry");
        jmethodID registerCtor = env->GetMethodID(clazz, "registerConstructor", "(JI)V");
        jmethodID registerProto = env->GetMethodID(clazz, "registerPrototype", "(JI)V");
        jclass contextClazz = env->FindClass("com/segment/analytics/substrata/kotlin/JSContext");
        jfieldID field = env->GetFieldID(contextClazz, "registry",
                                         "Lcom/segment/analytics/substrata/kotlin/JSRegistry;");
        jobject registry = env->GetObjectField(context, field);
        env->CallVoidMethod(registry, registerCtor, (jlong) ctorPtr, id);
        env->CallVoidMethod(registry, registerProto, (jlong) prototypePtr, id);

        // clean up
        env->ReleaseStringUTFChars(name, class_name);
        JS_FreeValue(ctx, callback);

        assert_no_exception(env);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
    }
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
    try {
        JSContext *ctx = (JSContext *) context;
        JSValue *val = (JSValue *) value;

        // create JavaCallbackData that carries JSContext instance for later use in callback
        JSRuntime *rt = JS_GetRuntime(ctx);
        JavaCallbackData *data = NULL;
        data = (JavaCallbackData *) js_malloc_rt(rt, sizeof(JavaCallbackData));
        JSValue callback = JS_NewObjectClass(ctx, java_callback_class_id);
        env->GetJavaVM(&data->vm);
        data->js_context = env->NewGlobalRef(js_context);
        JS_SetOpaque(callback, data);

        const char *name_utf = env->GetStringUTFChars(name, NULL);
        JSAtom propAtom = JS_NewAtom(ctx, name_utf);
        JSValue getter = JS_NewCFunctionData(ctx, invoke, 1, getter_id, 2, &callback);
        JSValue setter = JS_NewCFunctionData(ctx, invoke, 1, setter_id, 2, &callback);

        JS_DefinePropertyGetSet(ctx, *val, propAtom, JS_DupValue(ctx, getter),
                                JS_DupValue(ctx, setter),
                                JS_PROP_HAS_WRITABLE | JS_PROP_HAS_ENUMERABLE | JS_PROP_HAS_GET |
                                JS_PROP_HAS_SET);

        env->ReleaseStringUTFChars(name, name_utf);
        JS_FreeValue(ctx, callback);
        JS_FreeAtom(ctx, propAtom);
        JS_FreeValue(ctx, getter);
        JS_FreeValue(ctx, setter);

        assert_no_exception(env);
    }
    catch (...) {
        swallow_cpp_exception_and_throw_java(env);
    }
}