package ro.utcn.foodapp.business;

import ro.utcn.foodapp.access.database.DatabaseManager;
import ro.utcn.foodapp.model.Product;

/**
 * Created by coponipi on 15.05.2015.
 */
public class ProductManager {
    private static ProductManager instance;

    private ProductManager() {

    }

    public static ProductManager getInstance() {
        if (instance == null) {
            instance = new ProductManager();
        }
        return instance;
    }

    public void saveProduct(Product newProduct) {
        DatabaseManager.getInstance().saveProduct(newProduct);
    }
}
