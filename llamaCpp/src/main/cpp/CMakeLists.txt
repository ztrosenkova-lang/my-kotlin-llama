cmake_minimum_required(VERSION 3.22.1)
project("llamacpp" C CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Скачиваем официальное ядро b9859
include(FetchContent)
FetchContent_Declare(
    llamacpp_core
    GIT_REPOSITORY https://github.com/ggml-org/llama.cpp
    GIT_TAG        b9859
)
FetchContent_MakeAvailable(llamacpp_core)

# Подключаем пути к заголовкам скачанного ядра
target_include_directories(llama PRIVATE
    ${llamacpp_core_SOURCE_DIR}
    ${llamacpp_core_SOURCE_DIR}/include
    ${llamacpp_core_SOURCE_DIR}/common
)

# Линкуем системные библиотеки Android напрямую к ядру llama
target_link_libraries(llama
    android
    log
    jnigraphics
)
