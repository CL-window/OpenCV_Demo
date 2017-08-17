# OpenCV learn
环境：android studio 配置好NDK
* download [source from offical](http://opencv.org/releases.html) 下载的是3.3.0
* 运行sample需要先安装OpenCVManager，这个让人无语，不过从开发者的角度看，他们是 把OpenCV做成一个系统支持框架，这样需要先安装就很好理解了
* 发现OpenCV/sdk/native 里提供的 xx.a的静态库，所以考虑直接使用静态库

### 直接使用OpenCV 提供的 Java  库
1. 新建一个工程, File > New > New Module, 选择Import Eclipse ADT Project, openCV-android-sdk/sdk/java 下的项目导入到项目里, 自动命名为 openCVLibrary330，
2. 新建 src/main/jniLibs , 复制 OpenCV-android-sdk/sdk/native/libs 下所有文件放在jniLibs下，这样就可以不需要安装OpenCVManager, 直接在java层使用OpenCV
3. opencvjava 里是openCV提供的demo

### 使用C++协同开发 Cmake <font size=1>我是这么一个步骤，有些可以不用这样</font>
1. 新建一个工程，勾选或者不勾选 include C++都可以
1. 在新建一个openCVCode module
1. 新建一个文件夹 比如我的叫 NativeDependLibs 用来存放相关的依赖包
1. 复制 OpenCV-android-sdk/sdk/native/jni 下文件放在NativeDependLibs/OpenCV下
1. 新建 src/main/jniLibs , 复制 OpenCV-android-sdk/sdk/native/libs 下文件放在jniLibs下，我只编译armeabi ，所以只复制了 armeabi
1. 新建一个cpp文件夹 src/main/cpp，并暂时随便新建一个.cpp文件
1. 新建 CMakeLists.txt
1. 编辑文件如下
```
cmake_minimum_required(VERSION 3.4.1)


#配置加载native依赖
set(libs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")
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
1. build 一下项目，去那个临时的cpp文件里测试一下，可以成功引入OpenCV的库，下面就可以直接在C代码里使用OpenCV

大致文件结构
```
|---app
|---core(module)
|     |---NativeDependLibs/OpenCV
                |---jni/include
|     |___build.gradle
|     |___CMakeLists.txt 
|     |___src
|          |___main
|               |___AndroidManifest.xml
|               |___cpp (cpp src)
|               |___java (java src)
|               |___jniLibs (libs)
```

## Face Detection
opencvSample/facedetect/FaceDetectionActivity.java
1. onResume 里加载OpenCV的库，回调里面加载人脸识别库
1. lbpcascade_frontalface.xml 这个文件是官方提供的人脸检测的LBP分类器，检测到人脸画一个方形框
1. 在 onCameraFrame 里拿到每一帧Camera Preview 的数据，进行人脸检测,
1. 总结起来就是 OpenCV有一个自己的org.opencv.android.JavaCameraView自定义控件，它循环的从摄像头抓取数据，在回调方法中，我们能获取到Mat数据，然后通过调用检测当前是否有人脸，我们会获取到一个MatOfRect 是一个Rect数组，里面会有人脸数据，最后将人脸画在屏幕上
1. 拿到人脸后，绘制人脸框使用的是 Imgproc.rectangle
```
/**
             * Mat类型的图上绘制矩形
             * rectangle(Mat img, //图像
             *           Point pt1, //矩形的一个顶点
             *           Point pt2, //矩形对角线上的另一个顶点
             *           Scalar color, //线条颜色 (RGB) 或亮度（灰度图像 ）
             *           int thickness, //组成矩形的线条的粗细程度。取负值时（如 CV_FILLED）函数绘制填充了色彩的矩形
             *           int lineType, //线条的类型
             *           int shift //坐标点的小数点位数
             *           )
             */
```
1. 这位小哥的[人脸眼睛检测](http://romanhosek.cz/android-eye-detection-and-tracking-with-opencv/) 感觉检测的效果，模拟来说够了，但是实用就不行了
1. 下面是在[官方文档](http://docs.opencv.org/3.3.0/index.html)中列出的最重要的模块。C层和java层都有相应的库
    1. core：简洁的核心模块，定义了基本的数据结构，包括稠密多维数组 Mat 和其他模块需要的基本函数。
    1. imgproc：图像处理模块，包括线性和非线性图像滤波、几何图像转换 (缩放、仿射与透视变换、一般性基于表的重映射)、颜色空间转换、直方图等等。
    1. video：视频分析模块，包括运动估计、背景消除、物体跟踪算法。
    1. calib3d：包括基本的多视角几何算法、单体和立体相机的标定、对象姿态估计、双目立体匹配算法和元素的三维重建。
    1. features2d：包含了显著特征检测算法、描述算子和算子匹配算法。
    1. objdetect：物体检测和一些预定义的物体的检测 (如人脸、眼睛、杯子、人、汽车等)。
    1. ml：多种机器学习算法，如 K 均值、支持向量机和神经网络。
    1. highgui：一个简单易用的接口，提供视频捕捉、图像和视频编码等功能，还有简单的 UI 接口 
    1. ... and so on



注：可以参考的文章
* [Android Studio 2.3利用CMAKE进行OpenCV 3.2的NDK开发](http://johnhany.net/2017/07/opencv-ndk-dev-with-cmake-on-android-studio/                |___jniLibs (libs)