cmake_minimum_required(VERSION 3.22.1)

project("streamflux")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Hardware acceleration support
add_library(streamflux-native SHARED
    native-lib.cpp
)

find_library(log-lib log)
find_library(android-lib android)
find_library(mediandk-lib mediandk)

target_link_libraries(streamflux-native
    ${log-lib}
    ${android-lib}
    ${mediandk-lib}
)
