cmake_minimum_required(VERSION 3.22.1)
project("llamacpp" C CXX)

# Исправленный флаг: отключаем ошибку шаблонов для Clang
add_compile_options(-Wno-error=template-id-cdtor)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Отключаем лишние сборки
set(LLAMA_BUILD_SERVER OFF CACHE INTERNAL "")
set(LLAMA_BUILD_EXAMPLES OFF CACHE INTERNAL "")
set(LLAMA_BUILD_TESTS OFF CACHE INTERNAL "")

# Включаем только нужные модели (Llama, Qwen, DeepSeek, Gemma)
set(LLAMA_ALL_MODELS OFF CACHE BOOL "Cut models" FORCE)
set(LLAMA_MODELS "llama,qwen,deepseek,gemma" CACHE STRING "Target models" FORCE)

# Ручное скачивание ядра через Populate
include(FetchContent)
FetchContent_Declare(
    llamacpp_core
    GIT_REPOSITORY https://github.com/ggml-org/llama.cpp
    GIT_TAG        b9859
)
FetchContent_Populate(llamacpp_core)

# ЦИКЛ ОЧИСТКИ: удаляем все модели, кроме Llama, Qwen, DeepSeek и Gemma
file(GLOB ALL_MODELS_FILES "${llamacpp_core_SOURCE_DIR}/src/models/*.cpp")
foreach(MODEL_FILE ${ALL_MODELS_FILES})
    # Сохраняем поддержку Llama, Qwen, DeepSeek и Gemma
    if(NOT MODEL_FILE MATCHES "llama" AND NOT MODEL_FILE MATCHES "qwen" AND NOT MODEL_FILE MATCHES "deepseek" AND NOT MODEL_FILE MATCHES "gemma")
        file(WRITE ${MODEL_FILE} " ")
    endif()
endforeach()

# Теперь собираем ядро
FetchContent_MakeAvailable(llamacpp_core)

# Объявляем сборку нашего JNI-моста
add_library(${PROJECT_NAME} SHARED LlamaHelperJNI.cpp)

# Передаём заголовочные папки скачанного ядра компилятору для нашего проекта
target_include_directories(${PROJECT_NAME} PRIVATE
    ${llamacpp_core_SOURCE_DIR}
    ${llamacpp_core_SOURCE_DIR}/include
    ${llamacpp_core_SOURCE_DIR}/common
)

# Линкуем наш мост с нативными библиотеками ядра и модулем зрения
target_link_libraries(${PROJECT_NAME} PRIVATE
    llama
    llava
    android
    log
    jnigraphics
)
