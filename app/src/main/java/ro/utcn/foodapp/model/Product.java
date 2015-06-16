package ro.utcn.foodapp.model;

import android.os.Parcelable;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by coponipi on 19.04.2015.
 */
public class Product implements Serializable{
    private int id;
    private String name;
    private String ingredients;
    private Date expirationDate;
    private String expirationStatus;
    private List<String> urls;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
