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

# Включаем только нужные модели (Llama, Qwen, Gemma)
set(LLAMA_ALL_MODELS OFF CACHE BOOL "Cut models" FORCE)
set(LLAMA_MODELS "llama,qwen,gemma" CACHE STRING "Target models" FORCE)

# Скачиваем официальное ядро b9859
include(FetchContent)
FetchContent_Declare(
    llamacpp_core
    GIT_REPOSITORY https://github.com/ggml-org/llama.cpp
    GIT_TAG        b9859
)
FetchContent_MakeAvailable(llamacpp_core)

# Создаём пустые заглушки для проблемных файлов, чтобы обойти ошибки компиляции
file(WRITE ${llamacpp_core_SOURCE_DIR}/src/models/eagle3.cpp " ")
file(WRITE ${llamacpp_core_SOURCE_DIR}/src/models/dflash.cpp " ")

# Передаём заголовочные папки скачанного ядра компилятору
target_include_directories(llama PRIVATE
    ${llamacpp_core_SOURCE_DIR}
    ${llamacpp_core_SOURCE_DIR}/include
    ${llamacpp_core_SOURCE_DIR}/common
)

# Линкуем системные Android-библиотеки
target_link_libraries(llama PRIVATE
    android
    log
    jnigraphics
)
