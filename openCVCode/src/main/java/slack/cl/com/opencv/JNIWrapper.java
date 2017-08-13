package slack.cl.com.opencv;

/**
 * Created by slack
 * on 17/8/11 下午4:06
 */

public class JNIWrapper {

    public static String stringFromJNI(){
        return JNI.stringFromJNI();
    }

    public static int[] getGrayImage(int[] src, int w, int h) {
        return JNI.getGrayImage(src, w, h);
    }
}
