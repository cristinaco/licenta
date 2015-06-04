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

    public void saveRegistration(String productUUID, Date date, long productId) {
        DatabaseManager.getInstance().saveRegistration(productUUID, date, productId);
    }

    public List<Registration> getAllRegistrations() {
        return DatabaseManager.getInstance().getAllRegistrations();
    }

    public void updateRegistration(String registrationUuid, Date time, int productId) {
        DatabaseManager.getInstance().updateRegistration(registrationUuid, time, productId);
    }

    public List<Registration> searchRegistrations(String hint) {
        List<Registration> allRegistrations = getAllRegistrations();
        List<Registration> filteredRegistrations = new ArrayList<>();
        for(Registration registration: allRegistrations){
            Product product = ProductManager.getInstance().getProduct(registration.getProductId());
            if(product.getName().toLowerCase().contains(hint.toLowerCase()) || product.getIngredients().toLowerCase().contains(hint.toLowerCase())){
                filteredRegistrations.add(registration);
            }
        }
        return filteredRegistrations;
    }
}
