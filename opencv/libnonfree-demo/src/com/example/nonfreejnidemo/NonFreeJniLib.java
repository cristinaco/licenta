package com.example.nonfreejnidemo;

import android.util.Log;

/**
 * Created by coponipi on 22.05.2015.
 */
public class NonFreeJniLib {
    static
    {
        try
        {
            // Load necessary libraries.
            System.loadLibrary("opencv_java");
            System.loadLibrary("nonfree");
            System.loadLibrary("nonfree_demo");
        }
        catch( UnsatisfiedLinkError e )
        {
            System.err.println("Native code library failed to load.\n" + e);
            Log.d("OPENCV","Native code library failed to load");
        }
    }
    public static native void runDemo();
}
