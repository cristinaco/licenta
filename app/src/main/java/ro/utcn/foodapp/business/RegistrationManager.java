package ro.utcn.foodapp.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ro.utcn.foodapp.access.database.DatabaseManager;
import ro.utcn.foodapp.model.Product;
import ro.utcn.foodapp.model.Registration;

/**
 * Created by coponipi on 16.05.2015.
 */
public class RegistrationManager {
    private static RegistrationManager instance;

    private RegistrationManager() {

    }

    public static RegistrationManager getInstance() {
        if (instance == null) {
            instance = new RegistrationManager();
        }
        return instance;
    }


}
