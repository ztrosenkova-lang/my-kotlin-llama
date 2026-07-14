cmake_minimum_required(VERSION 3.22.1)
project("llamacpp" C CXX)

# Включаем поддержку современного стандарта C++23 для ядра b9859
set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Настраиваем автоматическое скачивание официального ядра b9859 с GitHub
include(FetchContent)
FetchContent_Declare(
    llamacpp_core
    GIT_REPOSITORY https://github.com/ggerganov/llama.cpp
    GIT_TAG        b9859
)
FetchContent_MakeAvailable(llamacpp_core)

# Указываем компилятору собрать наш JNI-мост
# (Проверь, как точно называется твой главный .cpp файл обёртки, и укажи его вместо net_wrapper.cpp)
add_library(${PROJECT_NAME} SHARED net_wrapper.cpp)

# Намертво связываем наш мост с официальными модулями llama и llava (зрение)
target_link_libraries(${PROJECT_NAME}
    llama
    llava
    android
    log
    jnigraphics
)
