package ro.utcn.foodapp.access.database;

import android.provider.BaseColumns;

/**
 * Created by coponipi on 19.04.2015.
 */
public abstract class Product implements BaseColumns{
    public static final String TABLE_NAME = "products";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_INGREDIENTS = "ingredients";
    public static final String COLUMN_NAME_EXPIRATION_DATE = "expirationdate";
    public static final String COLUMN_NAME_EXPIRATION_STATUS = "expirationstatus";
}
