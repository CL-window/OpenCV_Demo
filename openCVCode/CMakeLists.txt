cmake_minimum_required(VERSION 3.4.1)


#配置加载native依赖
set(libs "${CMAKE_SOURCE_DIR}/NativeDependLibs/OpenCV/libs")
include_directories(
        NativeDependLibs/OpenCV/jni/include
)

#CPP文件夹下带编译的cpp文件
file(GLOB_RECURSE ALL_PROJ_SRC "src/main/cpp/*.cpp" "src/main/cpp/*.h")

add_library( ${ANDROID_BUILD_TARGET}
             SHARED
             ${ALL_PROJ_SRC})


#C++日志
find_library( log-lib
              log )

file(GLOB OPENCV
        "${libs}/${ANDROID_ABI}/libopencv_java3.so"
        )


target_link_libraries(  ${ANDROID_BUILD_TARGET}
                        android
                        ${log-lib}
                        ${OPENCV}
#                        libopencv_java3
                       )