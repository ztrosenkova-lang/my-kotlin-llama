cmake_minimum_required(VERSION 3.22.1)
project("llamacpp" C CXX)

# Отключаем строгую проверку шаблонов для Clang 21 (обход ошибки в eagle3.cpp и dflash.cpp)
add_compile_options(-Wno-error=template-id-cdtor)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ОТКЛЮЧАЕМ КОНФЛИКТУЮЩИЕ И ЛИШНИЕ МОДЕЛИ ДЛЯ ОБХОДА ОШИБКИ CLANG 21
set(LLAMA_BUILD_SERVER OFF CACHE INTERNAL "")
set(LLAMA_BUILD_EXAMPLES OFF CACHE INTERNAL "")
set(LLAMA_BUILD_TESTS OFF CACHE INTERNAL "")

# Скачиваем официальное ядро b9859
include(FetchContent)
FetchContent_Declare(
    llamacpp_core
    GIT_REPOSITORY https://github.com/ggml-org/llama.cpp
    GIT_TAG        b9859
)
FetchContent_MakeAvailable(llamacpp_core)

# Передаем заголовочные папки скачанного ядра компилятору
target_include_directories(llama PRIVATE
    ${llamacpp_core_SOURCE_DIR}
    ${llamacpp_core_SOURCE_DIR}/include
    ${llamacpp_core_SOURCE_DIR}/common
)

# Линкуем системные Android-библиотеки напрямую к собранному ядру llama
target_link_libraries(llama PRIVATE
    android
    log
    jnigraphics
)
