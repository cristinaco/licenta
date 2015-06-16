package ro.utcn.foodapp.business;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import ro.utcn.foodapp.access.database.DatabaseManager;
import ro.utcn.foodapp.model.Product;
import ro.utcn.foodapp.model.Registration;

/**
 * Created by coponipi on 15.05.2015.
 */
public class StockManager {
    private static StockManager instance;

    private StockManager() {

    }

    public static StockManager getInstance() {
        if (instance == null) {
            instance = new StockManager();
        }
        return instance;
    }

    public long saveProduct(Product newProduct) {
        return DatabaseManager.getInstance().saveProduct(newProduct);
    }

    public Product getProduct(int productId) {
        return DatabaseManager.getInstance().getProduct(productId);
    }

    public TreeMap<Date, List<Product>> groupProductsByRegDate(List<Registration> listProductRegistrationDate, List<Product> productsForReg) {
        TreeMap<Date, List<Product>> productsGroupedByDate = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        for (Registration registration : listProductRegistrationDate) {

            calendar.setTime(registration.getRegistrationDate());
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            //products.add(product);
            if (productsGroupedByDate.containsKey(calendar.getTime())) {
                productsGroupedByDate.get(calendar.getTime()).add(getProductForReg(productsForReg, registration.getProductId()));
            } else {
                List<Product> products = new ArrayList<>();
                products.add(getProductForReg(productsForReg, registration.getProductId()));
                productsGroupedByDate.put(calendar.getTime(), products);
            }
        }
        return productsGroupedByDate;
    }

    public TreeMap<Date, List<Registration>> groupRegistrationsByDate(List<Registration> listProductRegistrationDate) {
        TreeMap<Date, List<Registration>> registrationsGroupedByDate = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        for (Registration registration : listProductRegistrationDate) {

            calendar.setTime(registration.getRegistrationDate());
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            if (registrationsGroupedByDate.containsKey(calendar.getTime())) {
                registrationsGroupedByDate.get(calendar.getTime()).add(registration);
            } else {
                List<Registration> registrations = new ArrayList<>();
                registrations.add(registration);
                registrationsGroupedByDate.put(calendar.getTime(), registrations);
            }
        }
        return registrationsGroupedByDate;
    }

    private Product getProductForReg(List<Product> productsForReg, int productId) {
        for (Product product : productsForReg) {
            if (product.getId() == productId) {
                return product;
            }
        }
        return null;
    }

    public void deleteProduct(int uuid) {
        DatabaseManager.getInstance().deleteProduct(uuid);
    }

    public void deleteAllProducts() {
        DatabaseManager.getInstance().deleteAllProducts();
    }

    public void deleteRegistration(int id) {
        DatabaseManager.getInstance().deleteRegistration(id);
    }

    public void updateProduct(Product product) {
        DatabaseManager.getInstance().updateProduct(product);
    }

    public void saveRegistration(String productUUID, Date date, long productId, int itemsNumber) {
        DatabaseManager.getInstance().saveRegistration(productUUID, date, productId, itemsNumber);
    }

    public List<Registration> getAllRegistrations() {
        return DatabaseManager.getInstance().getAllRegistrations();
    }

    public void updateRegistration(String registrationUuid, Date time, int productId, int itemsNumber) {
        DatabaseManager.getInstance().updateRegistration(registrationUuid, time, productId, itemsNumber);
    }

    public List<Registration> searchRegistrations(String hint) {
        List<Registration> allRegistrations = getAllRegistrations();
        List<Registration> filteredRegistrations = new ArrayList<>();
        for (Registration registration : allRegistrations) {
            Product product = StockManager.getInstance().getProduct(registration.getProductId());
            if (product.getName().toLowerCase().contains(hint.toLowerCase()) || product.getIngredients().toLowerCase().contains(hint.toLowerCase())) {
                filteredRegistrations.add(registration);
            }
        }
        return filteredRegistrations;
    }

    public void savePhotoPath(String url, long productId) {
        DatabaseManager.getInstance().savePhotoPath(url, productId);
    }

    public Registration getRegistration(String uuid) {
        return DatabaseManager.getInstance().getRegistration(uuid);
    }
}
