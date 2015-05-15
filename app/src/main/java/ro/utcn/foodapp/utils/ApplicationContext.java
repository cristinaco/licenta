package ro.utcn.foodapp.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by coponipi on 15.05.2015.
 */
public class ApplicationContext extends Application {
    private static Context sContext;

    public ApplicationContext() {

    }

    /**
     * Application context, use only to load resources (strings, dimensions, preferences)
     * and NOT to create views or open Activities
     */
    public static Context context() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

    }

}
