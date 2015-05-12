package ro.utcn.foodapp.model;

import android.graphics.Rect;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by coponipi on 01.05.2015.
 */
public class OcrResult implements Serializable {
    private String text;
    private List<Rect> wordBoundingBoxes;
    private File photoFilePath;
    private File photoDirPath;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Rect> getWordBoundingBoxes() {
        return wordBoundingBoxes;
    }

    public void setWordBoundingBoxes(ArrayList<Rect> wordBoundingBoxes) {
        this.wordBoundingBoxes = wordBoundingBoxes;
    }

    public File getPhotoFilePath() {
        return photoFilePath;
    }

    public void setPhotoFilePath(File filePath) {
        this.photoFilePath = filePath;
    }

    public File getPhotoDirPath() {
        return photoDirPath;
    }

    public void setPhotoDirPath(File photoDirPath) {
        this.photoDirPath = photoDirPath;
    }
}
