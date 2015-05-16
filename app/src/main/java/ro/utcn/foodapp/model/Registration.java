package ro.utcn.foodapp.model;

import java.util.Date;

/**
 * Created by coponipi on 16.05.2015.
 */
public class Registration {
    private Date registrationDate;
    private int productId;

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}
