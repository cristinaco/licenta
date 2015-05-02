package ro.utcn.foodapp.model;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by coponipi on 01.05.2015.
 */
public class OcrResult {
    private String text;
    private Bitmap bitmap;
    private List<Rect> wordBoundingBoxes;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public List<Rect> getWordBoundingBoxes() {
        return wordBoundingBoxes;
    }

    public void setWordBoundingBoxes(ArrayList<Rect> wordBoundingBoxes) {
        this.wordBoundingBoxes = wordBoundingBoxes;
    }
}
