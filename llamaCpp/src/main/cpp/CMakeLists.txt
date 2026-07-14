cmake_minimum_required(VERSION 3.22.1)
project("llamacpp" C CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 1. Настраиваем автоматическое скачивание официального ядра b9859 с GitHub
include(FetchContent)
FetchContent_Declare(
    llamacpp_core
    GIT_REPOSITORY https://github.com/ggml-org/llama.cpp
    GIT_TAG        b9859
)
FetchContent_MakeAvailable(llamacpp_core)

# 2. Объявляем сборку нашего JNI-моста
add_library(${PROJECT_NAME} SHARED LlamaHelperJNI.cpp)

# 3. ЖЕСТКО УКАЗЫВАЕМ ПУТИ К ИСХОДНИКАМ СКАЧАННОГО ЯДРА (Исправление бага):
target_include_directories(${PROJECT_NAME} PRIVATE
    ${llamacpp_core_SOURCE_DIR}
    ${llamacpp_core_SOURCE_DIR}/include
    ${llamacpp_core_SOURCE_DIR}/common
)

# 4. Намертво связываем наш мост с нативными библиотеками ядра и модулем зрения
target_link_libraries(${PROJECT_NAME}
    llama
    llava
    android
    log
    jnigraphics
)
