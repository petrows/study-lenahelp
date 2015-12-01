project(lena)

add_executable(lena-encrypt config.h encrypt.cpp)
add_executable(lena-decrypt config.h decrypt.cpp)
add_executable(lena-analyze config.h analyze.cpp)

