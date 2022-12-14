package com.bk.webrtc;

import android.util.Log;

import java.nio.ByteBuffer;

public class Apm {

    public enum AEC_SuppressionLevel{
        LowSuppression,
        ModerateSuppression,
        HighSuppression
    }

    // Recommended settings for particular audio routes. In general, the louder
    // the echo is expected to be, the higher this value should be set. The
    // preferred setting may vary from device to device.

    public enum AECM_RoutingMode {
        QuietEarpieceOrHeadset,
        Earpiece,
        LoudEarpiece,
        Speakerphone,
        LoudSpeakerphone
    };


    public enum AGC_Mode {
        // Adaptive mode intended for use if an analog volume control is available
        // on the capture device. It will require the user to provide coupling
        // between the OS mixer controls and AGC through the |stream_analog_level()|
        // functions.
        //
        // It consists of an analog gain prescription for the audio device and a
        // digital compression stage.
        AdaptiveAnalog,

        // Adaptive mode intended for situations in which an analog volume control
        // is unavailable. It operates in a similar fashion to the adaptive analog
        // mode, but with scaling instead applied in the digital domain. As with
        // the analog mode, it additionally uses a digital compression stage.
        AdaptiveDigital,

        // Fixed mode which enables only the digital compression stage also used by
        // the two adaptive modes.
        //
        // It is distinguished from the adaptive modes by considering only a
        // short time-window of the input signal. It applies a fixed gain through
        // most of the input level range, and compresses (gradually reduces gain
        // with increasing level) the input signal at higher levels. This mode is
        // preferred on embedded devices where the capture signal level is
        // predictable, so that a known gain can be applied.
        FixedDigital
    }


    // Determines the aggressiveness of the suppression. Increasing the level
    // will reduce the noise level at the expense of a higher speech distortion.
    public enum NS_Level {
        Low,
        Moderate,
        High,
        VeryHigh
    }

    // Specifies the likelihood that a frame will be declared to contain voice.
    // A higher value makes it more likely that speech will not be clipped, at
    // the expense of more noise being detected as voice.
    public enum VAD_Likelihood {
        VeryLowLikelihood,
        LowLikelihood,
        ModerateLikelihood,
        HighLikelihood
    }

    static {
        System.loadLibrary("webrtc_apms"); // to load the libwebrtc_apms.so library.
    }


    //  aecExtendFilter, delayAgnostic, nextGenerationAec
    // The above configurations only apply to EchoCancellation and not EchoControlMobile.



    public Apm(boolean aecExtendFilter, boolean speechIntelligibilityEnhance, boolean delayAgnostic, boolean beamforming, boolean nextGenerationAec, boolean experimentalNs, boolean experimentalAgc) throws Exception {
        if(!Create(aecExtendFilter, speechIntelligibilityEnhance, delayAgnostic, beamforming, nextGenerationAec, experimentalNs, experimentalAgc)){
            throw new Exception("create apm failed!");
        }

        Log.d("APM", "Apm: create success");
        _init = true;
    }


    public void close() {
        if(_init){
            Destroy();
            _init = false;
        }
    }

    public int HighPassFilter(boolean enable){
        return high_pass_filter_enable(enable);
    }

    //AEC PC
    public int AECClockDriftCompensation(boolean enable){
        return aec_clock_drift_compensation_enable(enable);
    }

    public int AECSetSuppressionLevel(AEC_SuppressionLevel level){ //[0, 1, 2]
        return aec_set_suppression_level(level.ordinal());
    }
    public int AEC(boolean enable){ return aec_enable(enable); }


    // AEC Mobile
    public int AECMSetSuppressionLevel(AECM_RoutingMode level){ //[0, 1, 2]
        return aecm_set_suppression_level(level.ordinal());
    }
    public int AECM(boolean enable){
        return aecm_enable(enable);
    }

    // NS
    public int NS(boolean enable){ return ns_enable(enable); }
    public int NSSetLevel(NS_Level level){ // [0, 1, 2, 3]
        return ns_set_level(level.ordinal());
    }

    // AGC
    public int AGCSetAnalogLevelLimits(int minimum, int maximum) { // limit to [0, 65535]
        return agc_set_analog_level_limits(minimum, maximum);
    }
    public int AGCSetMode(AGC_Mode mode) { // [0, 1, 2]
        return agc_set_mode(mode.ordinal());
    }
    public int AGCSetTargetLevelDbfs(int level){ return agc_set_target_level_dbfs(level);}
    public int AGCSetcompressionGainDb(int gain){return agc_set_compression_gain_db(gain);}
    public int AGCEnableLimiter(boolean enable){return agc_enable_limiter(enable);}

    public int AGCSetStreamAnalogLevel(int level){ return agc_set_stream_analog_level(level); }
    public int AGCStreamAnalogLevel(){
        return agc_stream_analog_level();
    }
    public int AGC(boolean enable){
        return agc_enable(enable);
    }

    // VAD
    public int VAD(boolean enable){
        return vad_enable(enable);
    }
    public int VADSetLikeHood(VAD_Likelihood likelihood){return vad_set_likelihood(likelihood.ordinal());}
    public boolean VADHasVoice(){
        return vad_stream_has_voice();
    }

    //
    public int ProcessCaptureStream(short[] nearEnd, int offset) { // 16K, 16bits, mono??? 10ms
        return ProcessStream(nearEnd, offset);
    }

    //ProcessRenderStream: It is only necessary to provide this if echo processing is enabled, as the
    // reverse stream forms the echo reference signal. It is recommended, but not
    // necessary, to provide if gain control is enabled.
    // may modify |farEnd| if intelligibility is enabled.
    public int ProcessRenderStream(short[] farEnd, int offset) { // 16K, 16bits, mono??? 10ms
        return ProcessReverseStream(farEnd, offset);
    }
    public int SetStreamDelay(int delay_ms){ return set_stream_delay_ms(delay_ms);  }


    /////////////////////////////////////////////////////////////////////////////////////
    public int CaptureStreamCacheDirectBufferAddress(ByteBuffer byteBuffer){ return nativeCaptureStreamCacheDirectBufferAddress(byteBuffer); }
    public int RenderStreamCacheDirectBufferAddress(ByteBuffer byteBuffer){ return nativeRenderStreamCacheDirectBufferAddress(byteBuffer); }

    public int ProcessCaptureStream() { // 16K, 16bits, mono??? 10ms
        return ProcessStreamEx();
    }
    public int ProcessRenderStream() { // 16K, 16bits, mono??? 10ms
        return ProcessReverseStreamEx();
    }
    /////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    private native boolean Create(boolean aecExtendFilter, boolean speechIntelligibilityEnhance, boolean delayAgnostic, boolean beamforming, boolean nextGenerationAec, boolean experimentalNs, boolean experimentalAgc);
    private native void Destroy();

    private native int high_pass_filter_enable(boolean enable);

    private native int aec_enable(boolean enable);
    private native int aec_set_suppression_level(int level); //[0, 1, 2]
    private native int aec_clock_drift_compensation_enable(boolean enable);

    private native int aecm_enable(boolean enable);
    private native int aecm_set_suppression_level(int level); //[0, 1, 2, 3, 4]


    private native int ns_enable(boolean enable);
    private native int ns_set_level(int level); // [0, 1, 2, 3]


    private native int agc_enable(boolean enable);

    // Sets the target peak |level| (or envelope) of the AGC in dBFs (decibels
    // from digital full-scale). The convention is to use positive values. For
    // instance, passing in a value of 3 corresponds to -3 dBFs, or a target
    // level 3 dB below full-scale. Limited to [0, 31].
    private native int agc_set_target_level_dbfs(int level); //[0,31]

    // Sets the maximum |gain| the digital compression stage may apply, in dB. A
    // higher number corresponds to greater compression, while a value of 0 will
    // leave the signal uncompressed. Limited to [0, 90].
    private native int agc_set_compression_gain_db(int gain); //[0,90]

    // When enabled, the compression stage will hard limit the signal to the
    // target level. Otherwise, the signal will be compressed but not limited
    // above the target level.
    private native int agc_enable_limiter(boolean enable);

    // Sets the |minimum| and |maximum| analog levels of the audio capture device.
    // Must be set if and only if an analog mode is used. Limited to [0, 65535].
    private native int agc_set_analog_level_limits(int minimum, int maximum); // limit to [0, 65535]

    private native int agc_set_mode(int mode); // [0, 1, 2]

    // When an analog mode is set, this must be called prior to |ProcessStream()|
    // to pass the current analog level from the audio HAL. Must be within the
    // range provided to |set_analog_level_limits()|.
    private native int agc_set_stream_analog_level(int level);

    // When an analog mode is set, this should be called after |ProcessStream()|
    // to obtain the recommended new analog level for the audio HAL. It is the
    // users responsibility to apply this level.
    private native int agc_stream_analog_level();

    private native int vad_enable(boolean enable);
    private native int vad_set_likelihood(int likelihood);
    private native boolean vad_stream_has_voice();


    private native int ProcessStream(short[] nearEnd, int offset); //???????????? // 16K, 16Bits, ???????????? 10ms
    private native int ProcessReverseStream(short[] farEnd, int offset); // ???????????? // 16K, 16Bits, ???????????? 10ms
    private native int set_stream_delay_ms(int delay);

    ///////////////////////////////////////////////////////////////////
    private native int nativeCaptureStreamCacheDirectBufferAddress(ByteBuffer byteBuffer);
    private native int nativeRenderStreamCacheDirectBufferAddress(ByteBuffer byteBuffer);

    private native int ProcessStreamEx(); //???????????? // 16K, 16Bits, ???????????? 10ms
    private native int ProcessReverseStreamEx(); // ???????????? // 16K, 16Bits, ???????????? 10ms
    //////////////////////////////////////////////////////////////////

    private boolean _init = false;

    private long objData; // do not modify it.
}
