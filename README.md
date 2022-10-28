# webrtc-apm
Android WebRTC APM，解决回音、噪音等音频问题

```java
try {
    Apm apm = new Apm(false, true, true, false, false, false, false);
    apm.AECMSetSuppressionLevel(Apm.AECM_RoutingMode.LoudSpeakerphone);
    apm.AECM(true);
    apm.NSSetLevel(Apm.NS_Level.High);
    apm.NS(true);
} catch (Exception e) {
    e.printStackTrace();
}
```

### 导入依赖
项目根目录 build.gradle 添加仓库:

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
添加项目依赖：

```groovy
dependencies {
        implementation 'com.github.brucekayle:webrtc-apm:Tag'
}
```