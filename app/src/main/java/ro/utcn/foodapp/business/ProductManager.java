package ro.utcn.foodapp.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import ro.utcn.foodapp.access.database.DatabaseManager;
import ro.utcn.foodapp.model.Product;
import ro.utcn.foodapp.model.Registration;

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

    public long saveProduct(Product newProduct) {
        return DatabaseManager.getInstance().saveProduct(newProduct);
    }

    public Product getProduct(int productId) {
        return DatabaseManager.getInstance().getProduct(productId);
    }

    public TreeMap<Date, List<Product>> groupProductsByRegDate(List<Registration> listProductRegistrationDate, List<Product> productsForReg) {
        TreeMap<Date, List<Product>> productsGroupedByDate = new TreeMap<>();
        for (Registration registration : listProductRegistrationDate) {

            //products.add(product);
            if (productsGroupedByDate.containsKey(registration.getRegistrationDate())) {
                productsGroupedByDate.get(registration.getRegistrationDate()).add(getProductForReg(productsForReg, registration.getProductId()));
            } else {
                List<Product> products = new ArrayList<>();
                products.add(getProductForReg(productsForReg, registration.getProductId()));
                productsGroupedByDate.put(registration.getRegistrationDate(), products);
            }
        }
        return productsGroupedByDate;
    }

    private Product getProductForReg(List<Product> productsForReg, int productId) {
        for (Product product : productsForReg) {
            if (product.getId() == productId) {
                return product;
            }
        }
        return null;
    }
}
