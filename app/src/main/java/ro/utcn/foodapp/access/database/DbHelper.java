package ro.utcn.foodapp.access.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by coponipi on 19.04.2015.
 */
public class DbHelper extends SQLiteOpenHelper {
    // String for null columns
    public static final String COLUMN_NAME_NULLABLE = "NULL";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "OcrFoodApp.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    // String for creating Bookings table
    private static final String SQL_PRODUCTS_CREATE_ENTRIES =
            "CREATE TABLE " + Product.TABLE_NAME + " (" +
                    Product._ID + " INTEGER PRIMARY KEY," +
                    Product.COLUMN_NAME_UID + TEXT_TYPE + COMMA_SEP +
                    Product.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    Product.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    Product.COLUMN_NAME_PIECES_NUMBER + INTEGER_TYPE + COMMA_SEP +
                    Product.COLUMN_NAME_EXPIRATION_DATE + INTEGER_TYPE + COMMA_SEP +
                    Product.COLUMN_NAME_EXPIRATION_STATUS + TEXT_TYPE + " )";
    // Strings for deleting tables
    private static final String SQL_PRODUCTS_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Product.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_PRODUCTS_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_PRODUCTS_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
