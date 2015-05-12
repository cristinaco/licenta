package ro.utcn.foodapp.model;

import java.util.Date;
import java.util.List;

/**
 * Created by coponipi on 19.04.2015.
 */
public class Product {
    private String uid;
    private String name;
    private String ingredients;
    private int piecesNumber;
    private Date expirationDate;
    private String expirationStatus;
    private List<String> urls;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public int getPiecesNumber() {
        return piecesNumber;
    }

    public void setPiecesNumber(int piecesNumber) {
        this.piecesNumber = piecesNumber;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getExpirationStatus() {
        return expirationStatus;
    }

    public void setExpirationStatus(String expirationStatus) {
        this.expirationStatus = expirationStatus;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
}
