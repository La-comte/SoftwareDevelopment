#include <jni.h>
#include <string>
#include <thread>
#include <cstring>
#include <android/log.h>
#include <cstdlib>

// Для spdlog
#include <spdlog/spdlog.h>
#include <spdlog/sinks/android_sink.h>

// Для mbedtls
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>

#define LOG_TAG "fclient_ndk"
#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Контексты для генерации случайных чисел
static mbedtls_entropy_context entropy;
static mbedtls_ctr_drbg_context ctr_drbg;
static const char *pers = "native_rng";

JavaVM *gJvm = nullptr;

// Функции для привязки JNIEnv к потоку
JNIEnv *getEnv(bool &detach) {
    JNIEnv *env = nullptr;
    detach = false;
    int status = gJvm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
    if (status == JNI_EDETACHED) {
        if (gJvm->AttachCurrentThread(&env, nullptr) < 0) {
            return nullptr;
        }
        detach = true;
    }
    return env;
}

void releaseEnv(bool detach, JNIEnv *env) {
    if (detach) {
        gJvm->DetachCurrentThread();
    }
}

// Функция вызывается при загрузке библиотеки и сохраняет указатель на JavaVM
extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    gJvm = vm;
    return JNI_VERSION_1_6;
}

// Пример нативного метода, возвращающего строку из C++
extern "C" JNIEXPORT jstring JNICALL
Java_ru_iu3_myapplication_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    auto android_logger = spdlog::android_logger_mt("android", LOG_TAG);
    std::string hello = "Hello from C++";
    LOG_INFO("Hello from C++ %d", 2023);
    android_logger->info("Hello from spdlog {}", 2023);
    return env->NewStringUTF(hello.c_str());
}

// Инициализация генератора случайных чисел с помощью mbedtls
extern "C" JNIEXPORT jint JNICALL
Java_ru_iu3_myapplication_MainActivity_initRng(JNIEnv *env, jclass clazz) {
    mbedtls_entropy_init(&entropy);
    mbedtls_ctr_drbg_init(&ctr_drbg);
    int ret = mbedtls_ctr_drbg_seed(&ctr_drbg, mbedtls_entropy_func, &entropy,
                                    reinterpret_cast<const unsigned char *>(pers), strlen(pers));
    return ret;
}

// Генерация случайных байт через mbedtls
extern "C" JNIEXPORT jbyteArray JNICALL
Java_ru_iu3_myapplication_MainActivity_randomBytes(JNIEnv *env, jclass, jint no) {
    uint8_t *buf = new uint8_t[no];
    int ret = mbedtls_ctr_drbg_random(&ctr_drbg, buf, no);
    if (ret != 0) {
        delete[] buf;
        return nullptr;
    }
    jbyteArray rnd = env->NewByteArray(no);
    env->SetByteArrayRegion(rnd, 0, no, reinterpret_cast<jbyte *>(buf));
    delete[] buf;
    return rnd;
}

// Примеры нативных функций для шифрования/дешифрования – здесь просто возвращаем входные данные
extern "C" JNIEXPORT jbyteArray JNICALL
Java_ru_iu3_myapplication_MainActivity_encrypt(JNIEnv *env, jclass, jbyteArray key,
                                               jbyteArray data) {
    return data;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_ru_iu3_myapplication_MainActivity_decrypt(JNIEnv *env, jclass, jbyteArray key,
                                               jbyteArray data) {
    return data;
}

// Финальная функция transaction – демонстрирует вызов метода enterPin из C++ и обратный вызов transactionResult
extern "C"
JNIEXPORT jboolean JNICALL
Java_ru_iu3_myapplication_MainActivity_transaction(JNIEnv *xenv, jobject xthiz, jbyteArray xtrd) {
    // Создаём глобальные ссылки, чтобы объекты были доступны в новом потоке
    JNIEnv *env = xenv;
    jobject thiz = xenv->NewGlobalRef(xthiz);
    jbyteArray trd = (jbyteArray) xenv->NewGlobalRef(xtrd);

    std::thread t([thiz, trd]() {
        bool detach = false;
        JNIEnv *env = getEnv(detach);
        if (!env) return;

        // Проверяем формат TRD: должны быть 9 байт и определённые значения тегов
        jsize sz = env->GetArrayLength(trd);
        jbyte *p = env->GetByteArrayElements(trd, nullptr);
        if (sz != 9 || p[0] != (jbyte) 0x9F || p[1] != (jbyte) 0x02 || p[2] != (jbyte) 0x06) {
            env->ReleaseByteArrayElements(trd, p, 0);
            jclass cls = env->GetObjectClass(thiz);
            jmethodID resId = env->GetMethodID(cls, "transactionResult", "(Z)V");
            env->CallVoidMethod(thiz, resId, JNI_FALSE);
            env->DeleteGlobalRef(thiz);
            env->DeleteGlobalRef(trd);
            releaseEnv(detach, env);
            return;
        }

        // Извлекаем сумму транзакции (байты 3–8 в BCD, преобразуем в строку)
        char buf[13] = {0};
        for (int i = 0; i < 6; i++) {
            uint8_t n = static_cast<uint8_t>(p[3 + i]);
            buf[i * 2] = ((n & 0xF0) >> 4) + '0';
            buf[i * 2 + 1] = (n & 0x0F) + '0';
        }
        env->ReleaseByteArrayElements(trd, p, 0);
        jstring jamount = env->NewStringUTF(buf);

        // Получаем метод enterPin по его сигнатуре
        jclass cls = env->GetObjectClass(thiz);
        jmethodID enterPinId = env->GetMethodID(cls, "enterPin",
                                                "(ILjava/lang/String;)Ljava/lang/String;");
        int ptc = 3;
        jstring pinStr = nullptr;
        while (ptc > 0) {
            pinStr = (jstring) env->CallObjectMethod(thiz, enterPinId, ptc, jamount);
            const char *utf = env->GetStringUTFChars(pinStr, nullptr);
            if (utf && strcmp(utf, "1234") == 0) {
                env->ReleaseStringUTFChars(pinStr, utf);
                break;
            }
            env->ReleaseStringUTFChars(pinStr, utf);
            ptc--;
        }

        // Вызываем метод transactionResult с результатом транзакции
        jmethodID resId = env->GetMethodID(cls, "transactionResult", "(Z)V");
        env->CallVoidMethod(thiz, resId, ptc > 0 ? JNI_TRUE : JNI_FALSE);

        env->DeleteGlobalRef(thiz);
        env->DeleteGlobalRef(trd);
        releaseEnv(detach, env);
    });
    t.detach();
    return JNI_TRUE;
}
