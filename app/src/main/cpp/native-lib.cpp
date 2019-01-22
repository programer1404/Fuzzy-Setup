#include <jni.h>
#include <string>
#include <vector>
#include <map>
#include <cmath>

using namespace std;

extern "C" JNIEXPORT jstring JNICALL
Java_ir_sinapp_fuzzy_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
