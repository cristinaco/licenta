package ro.utcn.foodapp.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by coponipi on 28.05.2015.
 */
public class DateUtils {
    private static DateUtils instance;
    private Map<Integer, SimpleDateFormat> dateFormats;

    private DateUtils() {

        dateFormats = new HashMap<>();
        dateFormats.put(0, new SimpleDateFormat("dd-MM-yyyy"));
        dateFormats.put(1, new SimpleDateFormat("dd.MM.yyyy"));
        dateFormats.put(5, new SimpleDateFormat("dd/MM/yyyy"));
        dateFormats.put(2, new SimpleDateFormat("MM-yyyy"));
        dateFormats.put(4, new SimpleDateFormat("MM.yyyy"));
        dateFormats.put(6, new SimpleDateFormat("MM/yyyy"));
        dateFormats.put(7, new SimpleDateFormat("yyyy"));
    }

    public static DateUtils getInstance() {
        if (instance == null) {
            instance = new DateUtils();
        }
        return instance;
    }

    public Map<Integer, SimpleDateFormat> getDateFormats() {
        return dateFormats;
    }
}
