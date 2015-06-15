package com.opencv.surf;

/**
 * Created by coponipi on 12.06.2015.
 */
public class SurfBaseJni {
    static {
        System.loadLibrary("opencv_java");
        System.loadLibrary("nonfree");
        System.loadLibrary("nonfree_demo");
    }

    public static native double computeMatchingPoints(String objectImgPath, String sceneImgPath);
}
