cmake_minimum_required(VERSION 3.22.1)
project("llamacpp" C CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 1. Скачиваем официальное ядро b9859
include(FetchContent)
FetchContent_Declare(
    llamacpp_core
    GIT_REPOSITORY https://github.com/ggml-org/llama.cpp
    GIT_TAG        b9859
)
FetchContent_MakeAvailable(llamacpp_core)

# 2. Подключаем пути к заголовкам скачанного ядра
target_include_directories(llama PRIVATE
    ${llamacpp_core_SOURCE_DIR}
    ${llamacpp_core_SOURCE_DIR}/include
    ${llamacpp_core_SOURCE_DIR}/common
)

# 3. ИСПРАВЛЕНО: Линкуем системные библиотеки строго через PRIVATE signature
target_link_libraries(llama PRIVATE
    android
    log
    jnigraphics
)
