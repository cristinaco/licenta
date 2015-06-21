package ro.utcn.foodapp.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.opencv.surf.SurfBaseJni;

import java.util.ArrayList;
import java.util.List;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.business.StockManager;
import ro.utcn.foodapp.model.Registration;
import ro.utcn.foodapp.model.SurfResult;
import ro.utcn.foodapp.presentation.activities.RegisterProductActivity;

/**
 * Created by coponipi on 15.06.2015.
 */
public class SurfProcessingTask extends AsyncTask<Void, Void, Void> {
    private List<SurfResult> surfResults;
    private RegisterProductActivity registerProductActivity;
    private List<String> objectImgPaths;
    private MaterialDialog ocrProgressDialog;

    public SurfProcessingTask(RegisterProductActivity registerProductActivity, List<String> objectImgPaths) {
        this.registerProductActivity = registerProductActivity;
        this.objectImgPaths = objectImgPaths;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ocrProgressDialog = new MaterialDialog.Builder(registerProductActivity)
                .content(R.string.wait_while_performing_surf)
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        List<Registration> allRegistrations = StockManager.getInstance().getAllRegistrations();
        if (allRegistrations.size() == 0) {
            return null;
        }
        surfResults = new ArrayList<>();
        for (String objectPath : objectImgPaths) {
            for (Registration registration : allRegistrations) {
                List<String> scenePaths = StockManager.getInstance().getProduct(registration.getProductId()).getUrls();

                boolean found = false;
                for (String scenePath : scenePaths) {
                    if (!found) {
                        double score = SurfBaseJni.computeMatchingPoints(objectPath, scenePath);
                        Log.d("Score:", String.valueOf(score));

                        if (score >=0 && score <= Constants.SURF_MIN_SCORE) {
                            SurfResult surfResult = new SurfResult();
                            surfResult.setScore(score);
                            surfResult.setRegistrationUuid(registration.getUuid());
                            surfResult.setMatch(true);
                            surfResult.setMatchedPhotoPath(scenePath);
                            //surfResults.add(surfResult);
                            found = true;
                            boolean exists = false;
                            for (SurfResult surf : surfResults) {

                                if (!surf.getRegistrationUuid().equals(surfResult.getRegistrationUuid())) {
                                    exists = false;
                                    continue;
                                } else {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                surfResults.add(surfResult);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        registerProductActivity.computeSurfResult(surfResults);
        ocrProgressDialog.dismiss();
    }
}
