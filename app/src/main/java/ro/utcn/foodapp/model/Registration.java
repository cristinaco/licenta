package ro.utcn.foodapp.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by coponipi on 16.05.2015.
 */
public class Registration implements Serializable{
    private int id;
    private String uuid;
    private Date registrationDate;
    private int productId;
    private int itemsNumber;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getItemsNumber() {
        return itemsNumber;
    }

    public void setItemsNumber(int itemsNumber) {
        this.itemsNumber = itemsNumber;
    }
}
