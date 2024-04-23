#include <stdio.h>
#include <string>
#include <iostream>

#include "java_helper.h"


void swallow_cpp_exception_and_throw_java(JNIEnv * env) {
    try {
        throw;
    } catch(const ThrownJavaException&) {
        //already reported to Java, ignore
    } catch(const std::bad_alloc& rhs) {
        //translate OOM C++ exception to a Java exception
        NewJavaException(env, "java/lang/OutOfMemoryError", rhs.what());
    } catch(const std::ios_base::failure& rhs) { //sample translation
        //translate IO C++ exception to a Java exception
        NewJavaException(env, "java/io/IOException", rhs.what());

        //TRANSLATE ANY OTHER C++ EXCEPTIONS TO JAVA EXCEPTIONS HERE

    } catch(const std::exception& e) {
        //translate unknown C++ exception to a Java exception
        NewJavaException(env, "java/lang/Error", e.what());
    } catch(...) {
        //translate unknown C++ exception to a Java exception
        NewJavaException(env, "java/lang/Error", "Unknown exception type");
    }
}