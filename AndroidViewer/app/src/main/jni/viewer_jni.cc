/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <android/log.h>
#include <jni.h>

#include <memory>

#include "viewer_app.h"

#define JNI_METHOD(return_type, method_name) \
  JNIEXPORT return_type JNICALL              \
      Java_com_zelgius_hexapod_viewer_VrActivity_##method_name

namespace {

    inline jlong jptr(ndk_viewer::ViewerApp *native_app) {
        return reinterpret_cast<intptr_t>(native_app);
    }

    inline ndk_viewer::ViewerApp *native(jlong ptr) {
        return reinterpret_cast<ndk_viewer::ViewerApp *>(ptr);
    }

    JavaVM *javaVm;

}  // anonymous namespace

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVm = vm;
    return JNI_VERSION_1_6;
}

JNI_METHOD(jlong, nativeOnCreate)
(JNIEnv *env, jobject obj, jobject asset_mgr) {
    return jptr(new ndk_viewer::ViewerApp(javaVm, obj, asset_mgr));
}

JNI_METHOD(void, nativeOnDestroy)
(JNIEnv *env, jobject obj, jlong native_app) { delete native(native_app); }

JNI_METHOD(void, nativeOnSurfaceCreated)
(JNIEnv *env, jobject obj, jlong native_app) {
    native(native_app)->OnSurfaceCreated(env);
}

JNI_METHOD(void, nativeOnDrawFrame)
(JNIEnv *env, jobject obj, jlong native_app, jbyteArray left, jbyteArray right) {
    jbyte *pLeft = env->GetByteArrayElements(left, nullptr);
    jbyte *pRight = env->GetByteArrayElements(right, nullptr);

    native(native_app)->OnDrawFrame(pLeft, pRight);

    env->ReleaseByteArrayElements(left, pLeft, JNI_ABORT);
    env->ReleaseByteArrayElements(right, pRight, JNI_ABORT);
}

JNI_METHOD(void, nativeOnTriggerEvent)
(JNIEnv *env, jobject obj, jlong native_app) {
    native(native_app)->OnTriggerEvent();
}

JNI_METHOD(void, nativeOnPause)
(JNIEnv *env, jobject obj, jlong native_app) { native(native_app)->OnPause(); }

JNI_METHOD(void, nativeOnResume)
(JNIEnv *env, jobject obj, jlong native_app) { native(native_app)->OnResume(); }

JNI_METHOD(void, nativeSetScreenParams)
(JNIEnv *env, jobject obj, jlong native_app, jint width, jint height) {
    native(native_app)->SetScreenParams(width, height);
}

JNI_METHOD(void, nativeSwitchViewer)
(JNIEnv *env, jobject obj, jlong native_app) {
    native(native_app)->SwitchViewer();
}

JNI_METHOD(jintArray, nativeGetTextureHandlers)
(JNIEnv *env, jobject obj, jlong native_app) {
    jintArray result;
    result = (env)->NewIntArray(2);
    if (result == nullptr) {
        return nullptr; /* out of memory error thrown */
    }

     int* textures = (int*) native(native_app)->GetTextureHandlers();

    jint fill [2] = {
            textures[0],
            textures[1]
    };

    (env)->SetIntArrayRegion(result, 0, 2, fill);
    return result;
}

}  // extern "C"
