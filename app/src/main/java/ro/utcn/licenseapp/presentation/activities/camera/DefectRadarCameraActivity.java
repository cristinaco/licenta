package ro.utcn.licenseapp.presentation.activities.camera;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

import ro.utcn.licenseapp.R;

public class DefectRadarCameraActivity extends Activity implements
        ActionBar.OnNavigationListener {

    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    public static final String TEMP_DIR_PATH = "TEMP_DIR_PATH";
    private static final String STATE_SELECTED_NAVIGATION_ITEM =
            "selected_navigation_item";
    private static final String STATE_SINGLE_SHOT = "single_shot";
    private static final String STATE_LOCK_TO_LANDSCAPE =
            "lock_to_landscape";
    public static File tempDir;
    public static File tempFilePath;
    private boolean hasTwoCameras = (Camera.getNumberOfCameras() > 1);
    private boolean isLockedToLandscape = false;
    private DefectRadarCameraFragment current = null;
    private DefectRadarCameraFragment std = null;
    private DefectRadarCameraFragment ffc = null;
    private boolean useFFC;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defect_radar_camera);
        useFFC = false;


        current = DefectRadarCameraFragment.newInstance(useFFC);
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_defect_radar_camera_container, current).commit();

        Intent intent = getIntent();
        final String fp = intent.getStringExtra(TEMP_FILE_PATH);
        this.tempFilePath = new File(fp);
        final String dp = intent.getStringExtra(TEMP_DIR_PATH);
        this.tempDir = new File(dp);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (hasTwoCameras) {
            if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
                getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
            }
        }

        if (current != null) {
            current.lockToLandscape(isLockedToLandscape);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        if (position == 0) {
            if (std == null) {
                std = DefectRadarCameraFragment.newInstance(false);
            }

            current = std;
        } else {
            if (ffc == null) {
                ffc = DefectRadarCameraFragment.newInstance(true);
            }

            current = ffc;
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.activity_defect_radar_camera_container, current).commit();

        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                current.lockToLandscape(isLockedToLandscape);
            }
        });

        return (true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(null);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA && current != null
                && !current.isSingleShotProcessing()) {
            current.takePicture();

            return (true);
        }

        return (super.onKeyDown(keyCode, event));
    }
}
