cmake_minimum_required(VERSION 3.4.1)


#配置加载native依赖
set(libs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)

#CPP文件夹下带编译的cpp文件
file(GLOB_RECURSE ALL_PROJ_SRC "src/main/cpp/*.cpp" "src/main/cpp/*.h")

add_library( ${ANDROID_BUILD_TARGET}
             SHARED
             ${ALL_PROJ_SRC})


#C++日志
find_library( log-lib
              log )

# 注释掉的方式适合加载少量的几个库
#add_library(libopencv_java3 SHARED IMPORTED )
#set_target_properties(libopencv_java3 PROPERTIES
#    IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_java3.so")

#适合同一个包下多个库 ,原谅我暂时也不知道OpenCV的这些库的区别
file(GLOB OPENCV
        "${libs}/${ANDROID_ABI}/libopencv_java3.so"
        )


target_link_libraries(  ${ANDROID_BUILD_TARGET}
                        android
                        ${log-lib}
                        ${OPENCV}
#                        libopencv_java3
                       )