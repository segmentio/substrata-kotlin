//
// See the original post here: https://stackoverflow.com/a/12014833/8296631
//

#ifndef SUBSTRATA_KOTLIN_JAVA_HELPER_H
#define SUBSTRATA_KOTLIN_JAVA_HELPER_H

#include <jni.h>
#include <string>
#include <stdexcept>

#define MSG_OOM "Out of memory"
#define CLASS_NAME_ILLEGAL_STATE_EXCEPTION "java/lang/IllegalStateException"

//This is how we represent a Java exception already in progress
struct ThrownJavaException : std::runtime_error {
    ThrownJavaException() :std::runtime_error("") {}
    ThrownJavaException(const std::string& msg ) :std::runtime_error(msg) {}
};

//used to throw a new Java exception. use full paths like:
//"java/lang/NoSuchFieldException"
//"java/lang/NullPointerException"
//"java/security/InvalidParameterException"
struct NewJavaException : public ThrownJavaException{
    NewJavaException(JNIEnv * env, const char* type, const char* message="")
            :ThrownJavaException(type+std::string(" ")+message)
    {
        jclass newExcCls = env->FindClass(type);
        if (newExcCls != NULL)
            env->ThrowNew(newExcCls, message);
        //if it is null, a NoClassDefFoundError was already thrown
    }
};

inline void assert_no_exception(JNIEnv * env) {
    if (env->ExceptionCheck()==JNI_TRUE)
        throw ThrownJavaException("assert_no_exception");
}

void swallow_cpp_exception_and_throw_java(JNIEnv * env);

#endif //SUBSTRATA_KOTLIN_JAVA_HELPER_H
