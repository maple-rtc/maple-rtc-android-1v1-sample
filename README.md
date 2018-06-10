# maple-rtc-android-1v1-sample
## 简介：
maple-rtc 为蓝蘑云推出的实时音视频系统，包括了实时音频，视频，变声，美颜等功能，适用于娱乐，游戏，教育等实时场景中；

----------
- 完整的 API 文档和集成步骤见 [文档中心](http://doc.lmaple.com/maple-rtc-android-sdk.html)

----------
这个开源项目演示了如何快速集成 maple-rtc SDK 到 Android 原生 中，实现在应用中音视频通话效果。

在这个示例项目中包含了以下功能：

- 加入通话和离开通话；
- 扬声器和听筒切换功能；
- 静音和解除静音；
- 关闭摄像头和开启摄像头；
- 切换前置摄像头和后置摄像头；
- 开关美颜功能；

## 运行示例程序
首先在 联系对接群 **701150764** [蓝蘑云后台管理](http://account.lmaple.com) 注册账号，获取到 AppID。将 AppID 填写进 "app/src/main/res/values/strings.xml"

```
<string name="maple_app_id">#######</string>
```

然后在 [maple-rtc SDK](http://sdk.lmaple.com/MapleRtc_Android_SDK_Release.zip) 下载 **maple-rtc 实时音视频 SDK for Android**，解压后将其中的 **libs** 文件夹复制到本项目的 **app/libs** 下。

在本项目的 "app/build.gradle" 文件依赖属性中添加如下依赖关系：

	compile fileTree(dir: 'libs', include: ['*.jar'])
最后用 Android Studio 打开该项目，连上设备，编译并运行。

也可以使用 Gradle 直接编译运行。

## 运行环境
- Android Studio 2.0 +
- 真实 Android 设备
- 部分模拟器会存在功能缺失或者性能问题，所以推荐使用真机

## 联系我们

对接qq群  **701150764**