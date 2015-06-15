package ro.utcn.foodapp.business;

import ro.utcn.foodapp.access.database.DatabaseManager;

/**
 * Created by coponipi on 16.05.2015.
 */
public class PhotoPathManager {
    private static PhotoPathManager instance;

    private PhotoPathManager() {

    }

    public static PhotoPathManager getInstance() {
        if (instance == null) {
            instance = new PhotoPathManager();
        }
        return instance;
    }


}
