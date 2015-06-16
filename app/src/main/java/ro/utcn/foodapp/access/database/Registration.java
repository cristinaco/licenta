package ro.utcn.foodapp.access.database;

import android.provider.BaseColumns;

/**
 * Created by coponipi on 16.05.2015.
 */
public class Registration implements BaseColumns {
    public static final String TABLE_NAME = "registration";
    public static final String COLUMN_NAME_UUID = "uuid";
    public static final String COLUMN_NAME_REGISTRATION_DATE = "date";
    public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
    public static final String COLUMN_NAME_ITEMS_NUMBER = "itemsnumber";
}
