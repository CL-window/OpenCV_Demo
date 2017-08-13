package slack.cl.com.opencv;

/**
 * Created by slack
 * on 17/8/10 下午2:03
 */

public class JNI {

    static {
        System.loadLibrary("opencv-lib");
    }

    public static native String stringFromJNI();

    public static native int[] getGrayImage(int[] src, int w, int h);
}
