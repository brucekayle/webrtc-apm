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