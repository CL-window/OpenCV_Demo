# OpenCV learn
环境：android studio 配置好NDK
* download [source from offical](http://opencv.org/releases.html) 下载的是3.3.0
* 运行sample需要先安装OpenCVManager，这个让人无语，不过从开发者的角度看，他们是 把OpenCV做成一个系统支持框架，这样需要先安装就很好理解了
* 发现OpenCV/sdk/native 里提供的 xx.a的静态库，所以考虑直接使用静态库

### 直接使用OpenCV 提供的 Java  库
1. 新建一个工程, File > New > New Module, 选择Import Eclipse ADT Project, openCV-android-sdk/sdk/java 下的项目导入到项目里, 自动命名为 openCVLibrary330，
2. 新建 src/main/jniLibs , 复制 OpenCV-android-sdk/sdk/native/libs 下所有文件放在jniLibs下，这样就可以不需要安装OpenCVManager, 直接在java层使用OpenCV
3. opencvjava 里是openCV提供的demo

### 使用C++协同开发<font size=1>我是这么一个步骤，有些可以不用这样</font>
1. 新建一个工程，勾选或者不勾选 include C++都可以
1. 在新建一个openCVCode module
1. 新建一个文件夹 比如我的叫 NativeDependLibs 
1. 新建 src/main/jniLibs , 复制 OpenCV-android-sdk/sdk/native/libs 下文件放在jniLibs下，我只编译armeabi ，所以只复制了 armeabi
1. 新建一个cpp文件夹 src/main/cpp，并暂时随便新建一个.cpp文件
1. 新建 CMakeLists.txt
1. 编辑文件如下
```
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
```
1. 修改build.gradle 支持Cmake编译
```
android {
    compileSdkVersion 25
    buildToolsVersion "25.0.2"

    defaultConfig {
        minSdkVersion 19
        targetSdkVersion 25
        versionCode 1
        versionName "1.0"

        externalNativeBuild {
            cmake {
                arguments "-DANDROID_TOOLCHAIN=gcc",
                        "-DANDROID_STUDIO=TRUE",
                        "-DANDROID_BUILD_TARGET=opencv-lib",// lib name
                        "-DANDROID_ABI=armeabi",
                        "-DBUILD_TYPE=debug",
                        "-DFOR_SDK=NO"

                cppFlags "-std=c++11 -frtti -fexceptions -w"
                abiFilters 'armeabi'//, 'x86', 'x86_64', 'armeabi-v7a', 'arm64-v8a', 'mips', 'mips64'
            }
        }

    }

    externalNativeBuild {
        cmake {
            path "CMakeLists.txt"
        }
    }

}
```
9. build 一下项目，去那个临时的cpp文件里测试一下，可以成功引入OpenCV的库，下面就可以直接在C代码里使用OpenCV

大致文件结构
```
|---app
|---core(module)
|     |___build.gradle
|     |___CMakeLists.txt 
|     |___src
|          |___main
|               |___AndroidManifest.xml
|               |___cpp (cpp src)
|               |___java (java src)
```




注：可以参考的文章
* [Android Studio 2.3利用CMAKE进行OpenCV 3.2的NDK开发](http://johnhany.net/2017/07/opencv-ndk-dev-with-cmake-on-android-studio/)