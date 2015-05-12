package ro.utcn.foodapp.model;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by coponipi on 01.05.2015.
 */
public class OcrResult implements Serializable{
    private String text;
    private Bitmap bitmap;
    private List<Rect> wordBoundingBoxes;
    private String filePath;


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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
