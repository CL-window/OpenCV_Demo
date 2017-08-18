# OpenCV learn
![人脸检测](https://github.com/CL-window/OpenCV_Demo/tree/master/pic/pic1.png)
![目标检测](https://github.com/kongqw/OpenCVForAndroid/blob/opencv3.2.0/gif/ObjectDetecting.gif)
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
1. 在 onCameraFrame 里拿到每一帧Camera Preview 的数据，进行人脸检测,
1. OpenCV 人脸检测用的是harr或LBP特征，分类算法用的是adaboost算法。这种算法需要提前训练大量的图片，非常耗时，因此opencv已经训练好了，把训练结果存放在一些xml文件里面。练好的文件放在 /sdk/etc 文件夹下，有两个文件夹haarcascades和lbpcascades，前者存放的是harr特征训练出来的文件，后者存放的是lbp特征训练出来的文件,比如
1. 人脸检测主要用到的是CascadeClassifier这个类，以及该类下的detectMultiScale函数
    ```
    /**
             * detectMultiScale(
             *                  Mat image, //输入图像,一般为灰度图
             *                  MatOfRect objects, //检测到的Rect[],存放所有检测出的人脸，每个人脸是一个矩形
             *                  double scaleFactor, //缩放比例,对图片进行缩放，默认为1.1
             *                  int minNeighbors, //合并窗口时最小neighbor，每个候选矩阵至少包含的附近元素个数，默认为3
             *                  int flags,  //检测标记，只对旧格式的分类器有效，与cvHaarDetectObjects的参数flags相同，在3.0版本中没用处, 默认为0，
             *            可能的取值为CV_HAAR_DO_CANNY_PRUNING(CANNY边缘检测)、CV_HAAR_SCALE_IMAGE(缩放图像)、
             *            CV_HAAR_FIND_BIGGEST_OBJECT(寻找最大的目标)、CV_HAAR_DO_ROUGH_SEARCH(做粗略搜索)；
             *            如果寻找最大的目标就不能缩放图像，也不能CANNY边缘检测
             *                  Size minSize, //检测出的人脸最小尺寸
             *                  Size maxSize //检测出的人脸最大尺寸
             *                  )
             */
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

    ```
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
1. 前后摄像头切换(需要判断是否存在改相机，部分手机只有一个摄像头)
1. 预览画布调整 ：
    1. 前置：默认是横屏的，需要转成竖屏显示, 前置镜像
    1. 后置:默认是横屏的，需要转成竖屏显示
    ```
    protected Mat rotateMat(Mat src) {

        if(mCameraIndex == CAMERA_ID_FRONT) {
            // 竖屏需要选择 rotate
            /**
             * transpose : 矩阵转置
             * 矩阵转置是将矩阵的行与列顺序对调（第i行转变为第i列）形成一个新的矩阵
             */
            Core.transpose(src, mRgbaT); //转置函数，可以水平的图像变为垂直
            Imgproc.resize(mRgbaT,src, src.size(), 0.0D, 0.0D, 0); //将转置后的图像缩放为src的大小
            // 左右镜像
            /**
             * flip(Mat src, //输入矩阵
             *      Mat dst, //翻转后矩阵，类型与src一致
             *      int flipCode //翻转模式，flipCode==0垂直翻转（沿X轴翻转），flipCode>0水平翻转（沿Y轴翻转），
             *                  flipCode<0水平垂直翻转（先沿X轴翻转，再沿Y轴翻转，等价于旋转180°）
             *      )
             */
            Core.flip(src, src, -1);
        } else {
            Core.transpose(src, mRgbaT);
            Imgproc.resize(mRgbaT,src, src.size(), 0.0D, 0.0D, 0);
            Core.flip(src, src, 1);
        }
        return super.rotateMat(src);
    }
    ```
不过OpenCV的库，貌似是横屏是识别的，我改成竖屏后检测效果不好，不过横过来，人脸眼睛检测还是不错的

### 识别
Imgproc.matchTemplate
```
/**
     * 可以用来做 识别
     * 模板匹配是一种在图像中定位目标的方法
     * 通过把输入图像在实际图像上逐像素点滑动，计算特征相似性，以此来判断当前滑块图像所在位置是目标图像的概率。
     * 在目标特征变化不是特别快的情况下，跟踪效果还可以
     * matchTemplate(Mat image, 搜索对象图像
     *              Mat templ, 模板图像，小于image，并且和image有相同的数据类型
     *              Mat result, 比较结果
     *              int method 比较算法总共有六种
     *              TM_SQDIFF 平方差匹配法：该方法采用平方差来进行匹配；最好的匹配值为0；匹配越差，匹配值越大。
     *              TM_CCORR 相关匹配法：该方法采用乘法操作；数值越大表明匹配程度越好。
     *              TM_CCOEFF 相关系数匹配法：1表示完美的匹配；-1表示最差的匹配。
     *              TM_SQDIFF_NORMED 归一化平方差匹配法
     *              TM_CCORR_NORMED 归一化相关匹配法
     *              TM_CCOEFF_NORMED 归一化相关系数匹配法
     *              )
     */
```
识别的思路参考眼睛的识别

1. 检测到眼睛，获得眼睛的模板 Mat 见 com.cl.slack.opencv.facedetect.FaceDetectHelperImpl#getTemplate
1. 调用 Imgproc.matchTemplate 识别 见 com.cl.slack.opencv.facedetect.FaceDetectHelperImpl#match_eye

TODO:
1. 这个是横屏可以检测，竖屏是无法检测的
1. 没有嘴巴，鼻子 眉毛，上下嘴唇的检测
1. 检测效率略低

注：可以参考的文章
* [Android Studio 2.3利用CMAKE进行OpenCV 3.2的NDK开发](http://johnhany.net/2017/07/opencv-ndk-dev-with-cmake-on-android-studio/)               
* [OpenCV—基本矩阵操作与示例](http://blog.csdn.net/iracer/article/details/51296631)
