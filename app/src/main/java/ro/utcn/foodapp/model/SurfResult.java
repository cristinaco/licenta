package ro.utcn.foodapp.model;

/**
 * Created by coponipi on 15.06.2015.
 */
public class SurfResult {
    private double score;
    private String productUuid;
    private String matchedPhotoPath;
    private boolean isMatch;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getProductUuid() {
        return productUuid;
    }

    public void setProductUuid(String productUuid) {
        this.productUuid = productUuid;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean isMatch) {
        this.isMatch = isMatch;
    }

    public String getMatchedPhotoPath() {
        return matchedPhotoPath;
    }

    public void setMatchedPhotoPath(String matchedPhotoPath) {
        this.matchedPhotoPath = matchedPhotoPath;
    }
}
