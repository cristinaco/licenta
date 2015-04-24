package ro.utcn.foodapp.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import java.io.File;
import java.util.List;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.presentation.customview.CameraBoxView;
import ro.utcn.foodapp.utils.BitmapUtils;

/**
 * Created by cristinaco on 06.03.15.
 */
public class FCameraFragment extends com.commonsware.cwac.camera.CameraFragment implements
        OnSeekBarChangeListener {

    public static final int SELECT_PICTURE = 0x833F;
    // We can be in one of these 3 states when touch the screen
    static final int MOTION_EVENT_MODE_NONE = 0;
    private int mode = MOTION_EVENT_MODE_NONE;
    static final int MOTION_EVENT_MODE_DRAG = 1;
    static final int MOTION_EVENT_MODE_ZOOM = 2;
    private static final String KEY_USE_FFC =
            "com.defectradar.activity.defect.camera.USE_FFC";
    private static final int SAVE_PHOTO = 0x833F5;
    // Constants used to detect the screen orientation
    private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;
    private boolean singleShotProcessing = false;
    private boolean flashMenuDisplayed;
    private boolean hasTwoCameras;
    private boolean useFrontCamera;
    private boolean hasFlash;
    //private SeekBar zoom = null;
    private ImageView takePicture;
    //    private ImageButton showFlashTypes;
//    private TextView cancelTakePhoto;
//    private TextView flashAuto;
//    private TextView flashOn;
//    private TextView flashOff;
    private FrameLayout cameraContainer;
    private CameraBoxView cameraBoxView;
    private CameraManager cameraManager;
    private LinearLayout takePictureContainer;
    private Rect rect;
    private String selectedFlashType;
    private List<TextView> flashTypes;
    private OrientationEventListener orientationEventListener;
    private long lastFaceToast = 0L;
    private int mOrientation = -1;
    private int zoomProgress = 0;
    private float oldDist = 1f;


    public static FCameraFragment newInstance(boolean useFFC) {
        FCameraFragment f = new FCameraFragment();
        Bundle args = new Bundle();

        args.putBoolean(KEY_USE_FFC, useFFC);
        f.setArguments(args);

        return (f);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        SimpleCameraHost.Builder builder =
                new SimpleCameraHost.Builder(new DemoCameraHost(getActivity()));
        setHost(builder.useFullBleedPreview(true).build());
        cameraManager = new CameraManager(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View cameraView =
                super.onCreateView(inflater, container, savedInstanceState);
        View results = inflater.inflate(R.layout.camera_fragment, container, false);
        ((ViewGroup) results.findViewById(R.id.camera_container)).addView(cameraView);

        //zoom = (SeekBar) results.findViewById(R.id.zoom);
        takePicture = (ImageView) results.findViewById(R.id.take_photo);
        //takePictureContainer = (LinearLayout) results.findViewById(R.id.take_picture_container);
//        showFlashTypes = (ImageButton) results.findViewById(R.id.camera_display_flash_types);
//        cancelTakePhoto = (TextView) results.findViewById(R.id.camera_cancel_take_photo);
//        flashAuto = (TextView) results.findViewById(R.id.camera_flash_type_auto);
//        flashOn = (TextView) results.findViewById(R.id.camera_flash_type_on);
//        flashOff = (TextView) results.findViewById(R.id.camera_flash_type_off);
        cameraContainer = (FrameLayout) results.findViewById(R.id.camera_container);
        cameraBoxView = (CameraBoxView) results.findViewById(R.id.camera_box_view);
//        RelativeLayout flashBtnContainer = (RelativeLayout) results.findViewById(R.id.camera_flash_buttons_container);

        cameraBoxView.setCameraManager(cameraManager);

//        flashTypes = new ArrayList<TextView>();
//        flashTypes.add(flashAuto);
//        flashTypes.add(flashOn);
//        flashTypes.add(flashOff);

        hasTwoCameras = (Camera.getNumberOfCameras() > 1);

        if (savedInstanceState != null) {
            selectedFlashType = savedInstanceState.getString("selectedFlashType");
            zoomProgress = savedInstanceState.getInt("zoomProgress");
            CameraActivity.tempFilePath = new File(savedInstanceState.getString(CameraActivity.TEMP_FILE_PATH));
            CameraActivity.tempDir = new File(savedInstanceState.getString(CameraActivity.TEMP_DIR_PATH));
            //zoom.setProgress(zoomProgress);
            //doCameraZoom();
        } else {
            //selectedFlashType = (String) flashAuto.getTag();
            //zoomProgress = zoom.getProgress();
        }

        useFrontCamera = getArguments().getBoolean(KEY_USE_FFC, false);

//        flashOff.setVisibility(View.GONE);
//        flashOn.setVisibility(View.GONE);
//        flashAuto.setVisibility(View.VISIBLE);

        flashMenuDisplayed = false;

        // Check if the selected camera has flash
        Camera camera;
        if (useFrontCamera) {
            camera = Camera.open(1);
            hasFlash = hasFlash(camera);
            cameraManager.initCamera(camera);
            camera.release();
        } else {
            camera = Camera.open(0);
            hasFlash = hasFlash(camera);
            cameraManager.initCamera(camera);
            camera.release();
        }

//        if (!hasFlash) {
//            for (TextView txtView : flashTypes) {
//                txtView.setEnabled(false);
//            }
//            showFlashTypes.setEnabled(false);
//            showFlashTypes.setBackground(getResources().getDrawable(R.drawable.ic_action_flash_off));
//            flashAuto.setVisibility(View.GONE);
//        }

        // zoom.setKeepScreenOn(true);
        setListeners();

        // TODO uncomment this
//        flashBtnContainer.setVisibility(View.GONE);
//        showFlashTypes.setVisibility(View.GONE);
//        flashAuto.setVisibility(View.GONE);
//        flashOff.setVisibility(View.GONE);
//        flashOn.setVisibility(View.GONE);
        rect = cameraManager.getFramingRect();

        return (results);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().invalidateOptionsMenu();
        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.
        //orientationEventListener.disable();
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (flashMenuDisplayed) {
//            showFlashTypes.setEnabled(false);
//        }
//        takePicture.setEnabled(true);
//
//        // Detect the screen orientation using SensorManager
//        if (orientationEventListener == null) {
//            orientationEventListener = new OrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_NORMAL) {
//
//                @Override
//                public void onOrientationChanged(int orientation) {
//                    int lastOrientation = mOrientation;
//
//                    if (orientation >= 315 || orientation < 45) {
//                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
//                            mOrientation = ORIENTATION_PORTRAIT_NORMAL;
//                        }
//                    } else if (orientation < 315 && orientation >= 225) {
//                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
//                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
//                        }
//                    } else if (orientation < 225 && orientation >= 135) {
//                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
//                            mOrientation = ORIENTATION_PORTRAIT_INVERTED;
//                        }
//                    } else {
//                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
//                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
//                        }
//                    }
//
//                    if (lastOrientation != mOrientation) {
//                        changeRotation(mOrientation);
//                    }
//                }
//            };
//        }
//        if (orientationEventListener.canDetectOrientation()) {
//            orientationEventListener.enable();
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("selectedFlashType", selectedFlashType);
        outState.putInt("zoomProgress", zoomProgress);
        outState.putString(CameraActivity.TEMP_FILE_PATH, CameraActivity.tempFilePath.getAbsolutePath());
        outState.putString(CameraActivity.TEMP_DIR_PATH, CameraActivity.tempDir.getAbsolutePath());
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(null);
//    }

    public boolean isSingleShotProcessing() {
        return (singleShotProcessing);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (fromUser) {
            //zoom.setEnabled(false);

            //doCameraZoom();
            //zoomProgress = zoom.getProgress();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // ignore
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // ignore
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        switch (event.getAction() & MotionEvent.ACTION_MASK) {
//
//            case MotionEvent.ACTION_DOWN:
//                mode = MOTION_EVENT_MODE_DRAG;
//                break;
//
//            case MotionEvent.ACTION_UP:
//
//            case MotionEvent.ACTION_POINTER_UP:
//                mode = MOTION_EVENT_MODE_NONE;
//                zoom.setProgress(zoomProgress);
//                break;
//
//            case MotionEvent.ACTION_POINTER_DOWN:
//                // Calculates the distance between two points where user touched.
//                oldDist = spacing(event);
//                // Minimal distance between both the fingers
//                if (oldDist > 0) {
//                    mode = MOTION_EVENT_MODE_ZOOM;
//                    zoom.setVisibility(View.VISIBLE);
//                }
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                // Pinch zooming in
//                if (mode == MOTION_EVENT_MODE_ZOOM) {
//                    float newDist = spacing(event);
//                    zoom.setVisibility(View.VISIBLE);
//
//                    // Zooming in
//                    if (newDist > oldDist && zoom.getProgress() < zoom.getMax()) {
//                        zoomProgress += 1;
//                        zoom.setProgress(zoomProgress);
//                        doCameraZoom();
//                    }
//                    // Zooming out
//                    if (newDist < oldDist && (zoom.getProgress() <= zoom.getMax()) && (zoom.getProgress() > 0)) {
//                        zoomProgress--;
//                        zoom.setProgress(zoomProgress);
//                        doCameraZoom();
//                    }
//                }
//                break;
//        }
//
//        return true;
//    }

    /**
     * Check if flash is available for current camera
     *
     * @param camera the camera object
     * @return true if flash is available, false otherwise
     */
    public boolean hasFlash(Camera camera) {
        if (camera == null) {
            return false;
        }

        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        return true;
    }

//    private void doCameraZoom() {
//        zoomTo(zoom.getProgress()).onComplete(new Runnable() {
//            @Override
//            public void run() {
//                zoom.setEnabled(true);
//            }
//        }).go();
//    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        return x;
    }

//    private void hideSeekBar() {
//        zoom.postDelayed(new Runnable() {
//            public void run() {
//                zoom.setVisibility(View.GONE);
//            }
//        }, 5000);
//    }

    private void setListeners() {

//        cancelTakePhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getActivity().finish();
//            }
//        });

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeSimplePicture();
            }
        });

//        takePictureContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                takeSimplePicture();
//            }
//        });
// Set listener to change the size of the viewfinder rectangle.
//        cameraBoxView.setOnTouchListener(new View.OnTouchListener() {
//            int lastX = -1;
//            int lastY = -1;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        lastX = -1;
//                        lastY = -1;
//                        return true;
//                    case MotionEvent.ACTION_MOVE:
//                        int currentX = (int) event.getX();
//                        int currentY = (int) event.getY();
//
//                        try {
//                            rect = cameraManager.getFramingRect();
//
//                            final int BUFFER = 50;
//                            final int BIG_BUFFER = 60;
//                            if (lastX >= 0) {
//                                // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
//                                if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
//                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
//                                    // Top left corner: adjust both top and left sides
//                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (lastY - currentY));
//                                    //cameraBoxView.removeResultText();
//                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
//                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
//                                    // Top right corner: adjust both top and right sides
//                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (lastY - currentY));
//                                    //cameraBoxView.removeResultText();
//                                } else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
//                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
//                                    // Bottom left corner: adjust both bottom and left sides
//                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
//                                    //cameraBoxView.removeResultText();
//                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
//                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
//                                    // Bottom right corner: adjust both bottom and right sides
//                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
//                                    //cameraBoxView.removeResultText();
//                                } else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
//                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
//                                    // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
//                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 0);
//                                    //cameraBoxView.removeResultText();
//                                } else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
//                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
//                                    // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
//                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 0);
//                                    //cameraBoxView.removeResultText();
//                                } else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
//                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
//                                    // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
//                                    cameraManager.adjustFramingRect(0, 2 * (lastY - currentY));
//                                    //cameraBoxView.removeResultText();
//                                } else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
//                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
//                                    // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
//                                    cameraManager.adjustFramingRect(0, 2 * (currentY - lastY));
//                                    //cameraBoxView.removeResultText();
//                                }
//                            }
//                        } catch (NullPointerException e) {
//                            Log.e("Error", "Framing rect not available", e);
//                        }
//                        v.invalidate();
//                        lastX = currentX;
//                        lastY = currentY;
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        lastX = -1;
//                        lastY = -1;
//                        return true;
//                }
//                return false;
//            }
//        });

//        showFlashTypes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Flash types menu is closing
//                if (flashMenuDisplayed) {
//                    for (TextView txView : flashTypes) {
//
//                        if (!txView.getTag().equals(selectedFlashType)) {
//                            txView.setVisibility(View.GONE);
//                        }
//                        txView.setTextColor(getResources().getColor(R.color.app_text_white));
//                    }
//                    flashMenuDisplayed = false;
//                }
//            }
//        });
//
//        flashAuto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final Animation toLeftAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.translate_in_left);
//
//                if (!flashMenuDisplayed) {
//                    showFlashTypes.setEnabled(true);
//                    for (TextView txView : flashTypes) {
//                        txView.setEnabled(true);
//                        txView.setVisibility(View.VISIBLE);
//
//                        if (txView.getTag().equals(selectedFlashType)) {
//                            txView.setTextColor(getResources().getColor(R.color.orange));
//                            txView.startAnimation(toLeftAnimation);
//                            toLeftAnimation.setAnimationListener(this.setVisibilityOnFinish(txView, View.VISIBLE, new Runnable() {
//
//                                public void run() {
//
//                                }
//                            }));
//                        } else {
//                            txView.setTextColor(getResources().getColor(R.color.app_text_white));
//                        }
//                    }
//                    flashMenuDisplayed = true;
//                } else {
//                    selectedFlashType = (String) view.getTag();
//                    setFlashMode(Parameters.FLASH_MODE_AUTO);
//
//                    flashOn.setVisibility(View.GONE);
//                    flashOff.setVisibility(View.GONE);
//                    flashMenuDisplayed = false;
//
//                    final Animation toRightAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.translate_out_left);
//                    toRightAnimation.setAnimationListener(this.setVisibilityOnFinish(flashAuto, View.VISIBLE, new Runnable() {
//
//                        public void run() {
//
//                        }
//                    }));
//                }
//            }
//
//            private Animation.AnimationListener setVisibilityOnFinish(TextView flashAuto, int visible, Runnable runnable) {
//                return null;
//            }
//        });
//
//        flashOn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final Animation toLeftAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.translate_in_left);
//
//                if (!flashMenuDisplayed) {
//                    showFlashTypes.setEnabled(true);
//                    for (TextView txView : flashTypes) {
//                        txView.setEnabled(true);
//                        txView.setVisibility(View.VISIBLE);
//
//                        if (txView.getTag().equals(selectedFlashType)) {
//                            txView.setTextColor(getResources().getColor(R.color.orange));
//                            txView.startAnimation(toLeftAnimation);
//                            toLeftAnimation.setAnimationListener(this.setVisibilityOnFinish(txView, View.VISIBLE, new Runnable() {
//
//                                public void run() {
//
//                                }
//                            }));
//                        } else {
//                            txView.setTextColor(getResources().getColor(R.color.app_text_white));
//                        }
//                    }
//                    flashMenuDisplayed = true;
//                } else {
//                    selectedFlashType = (String) view.getTag();
//                    setFlashMode(Parameters.FLASH_MODE_ON);
//
//                    flashOn.setTextColor(getResources().getColor(R.color.orange));
//                    flashAuto.setVisibility(View.GONE);
//                    flashOff.setVisibility(View.GONE);
//                    flashMenuDisplayed = false;
//
//                    final Animation toRightAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.translate_out_left);
//                    toRightAnimation.setAnimationListener(this.setVisibilityOnFinish(flashOn, View.VISIBLE, new Runnable() {
//
//                        public void run() {
//
//                        }
//                    }));
//                }
//            }
//
//            private Animation.AnimationListener setVisibilityOnFinish(TextView flashOn, int visible, Runnable runnable) {
//                return null;
//            }
//        });
//
//        flashOff.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final Animation toLeftAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.translate_in_left);
//
//                if (!flashMenuDisplayed) {
//                    showFlashTypes.setEnabled(true);
//                    for (TextView txView : flashTypes) {
//                        txView.setEnabled(true);
//                        txView.setVisibility(View.VISIBLE);
//
//                        if (txView.getTag().equals(selectedFlashType)) {
//                            txView.setTextColor(getResources().getColor(R.color.orange));
//                            txView.startAnimation(toLeftAnimation);
//                            toLeftAnimation.setAnimationListener(this.setVisibilityOnFinish(txView, View.VISIBLE, new Runnable() {
//
//                                public void run() {
//
//                                }
//                            }));
//                        } else {
//                            txView.setTextColor(getResources().getColor(R.color.app_text_white));
//                        }
//                    }
//                    flashMenuDisplayed = true;
//                } else {
//                    selectedFlashType = (String) view.getTag();
//                    setFlashMode(Parameters.FLASH_MODE_OFF);
//
//                    flashOn.setVisibility(View.GONE);
//                    flashAuto.setVisibility(View.GONE);
//                    flashMenuDisplayed = false;
//
//                    final Animation toRightAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.translate_out_left);
//                    toRightAnimation.setAnimationListener(this.setVisibilityOnFinish(flashOff, View.VISIBLE, new Runnable() {
//
//                        public void run() {
//
//                        }
//                    }));
//                }
//            }
//
//            private Animation.AnimationListener setVisibilityOnFinish(TextView flashOff, int visible, Runnable runnable) {
//                return null;
//            }
//        });
    }

//    /**
//     * Performs required action to accommodate new orientation
//     *
//     * @param orientation
//     */
//    private void changeRotation(int orientation) {
//        switch (orientation) {
//
//            case ORIENTATION_PORTRAIT_NORMAL:
//                takePicture.setRotation(0);
//                showFlashTypes.setRotation(0);
//                cancelTakePhoto.setRotation(0);
//                flashAuto.setRotation(0);
//                flashOn.setRotation(0);
//                flashOff.setRotation(0);
//                break;
//            case ORIENTATION_LANDSCAPE_NORMAL:
//                takePicture.setRotation(90);
//                showFlashTypes.setRotation(90);
//                cancelTakePhoto.setRotation(90);
//                flashAuto.setRotation(90);
//                flashOn.setRotation(90);
//                flashOff.setRotation(90);
//                break;
//            case ORIENTATION_PORTRAIT_INVERTED:
//                takePicture.setRotation(180);
//                showFlashTypes.setRotation(180);
//                cancelTakePhoto.setRotation(180);
//                flashAuto.setRotation(180);
//                flashOn.setRotation(180);
//                flashOff.setRotation(180);
//                break;
//            case ORIENTATION_LANDSCAPE_INVERTED:
//                takePicture.setRotation(270);
//                showFlashTypes.setRotation(270);
//                cancelTakePhoto.setRotation(270);
//                flashAuto.setRotation(270);
//                flashOn.setRotation(270);
//                flashOff.setRotation(270);
//                break;
//        }
//    }


    private void takeSimplePicture() {
        if (!flashMenuDisplayed) {
            takePicture.setEnabled(false);
            singleShotProcessing = true;
            PictureTransaction pictureTransaction = new PictureTransaction(getHost());

            if (hasFlash) {
                pictureTransaction.flashMode(getFlashMode());
            }
            pictureTransaction.needBitmap(true);
            takePicture(pictureTransaction);
        } else {
            closeFlashMenu();

        }
    }

    private void closeFlashMenu() {
        // Flash types menu is closing
        for (TextView txView : flashTypes) {

            if (!txView.getTag().equals(selectedFlashType)) {
                txView.setVisibility(View.GONE);
            }
            txView.setTextColor(getResources().getColor(R.color.app_text_white));
        }
        flashMenuDisplayed = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SAVE_PHOTO) {
            // Make sure the request was successful
            if (resultCode == getActivity().RESULT_OK) {
                int value = data.getExtras().getInt("save");
                if (value == 1) {

                    Intent intent = new Intent();
                    FCameraFragment.this.getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
            }
        }
    }


    class DemoCameraHost extends SimpleCameraHost implements
            Camera.FaceDetectionListener, CameraHost {
        boolean supportsFaces = false;

        public DemoCameraHost(Context _ctxt) {
            super(_ctxt);
        }

        @Override
        public boolean useFrontFacingCamera() {
            if (getArguments() == null) {
                return (false);
            }
            return (getArguments().getBoolean(KEY_USE_FFC));
        }

        @Override
        public boolean useSingleShotMode() {
            return (true);
        }

        @Override
        public void saveImage(PictureTransaction pictureTransaction, Bitmap image) {
            if (useSingleShotMode()) {
                singleShotProcessing = false;
                // TODO crop the image: get only the image within the camera box
                //Bitmap bmp = BitmapUtils.cropImage(image, cameraBoxView.getRect());
                Bitmap bmp = BitmapUtils.cropImage(image, cameraManager.getFramingRectInPreview());

                super.saveImage(pictureTransaction, bmp);
                Intent displayPhotoIntent = new Intent(getActivity(), PreviewPhotoActivity.class);

                displayPhotoIntent.putExtra(CameraActivity.TEMP_FILE_PATH, CameraActivity.tempFilePath.getAbsolutePath());
                FCameraFragment.this.startActivityForResult(displayPhotoIntent, SAVE_PHOTO);

            }
        }

        @Override
        protected File getPhotoPath() {
            return CameraActivity.tempFilePath;
        }

        @Override
        protected File getPhotoDirectory() {
            return CameraActivity.tempDir;

        }

        @Override
        public void autoFocusAvailable() {
            if (supportsFaces) {
                startFaceDetection();
                //doCameraZoom();
            }
            takePicture.setEnabled(true);
        }

        @Override
        public void autoFocusUnavailable() {
            if (supportsFaces) {
                stopFaceDetection();
            }
            takePicture.setEnabled(false);
        }

        @Override
        public void onCameraFail(FailureReason reason) {
            super.onCameraFail(reason);

            Toast.makeText(getActivity(),
                    "Sorry, but you cannot use the camera now!",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public Parameters adjustPreviewParameters(Parameters parameters) {
            if (hasFlash) {

//                if (selectedFlashType.equals(flashOn.getTag())) {
//                    setFlashMode(Parameters.FLASH_MODE_ON);
//                } else if (selectedFlashType.equals(flashOff.getTag())) {
//                    setFlashMode(Parameters.FLASH_MODE_OFF);
//                } else if (selectedFlashType.equals(flashAuto.getTag())) {
//                    setFlashMode(Parameters.FLASH_MODE_AUTO);
//                } else {
//                    setFlashMode(Parameters.FLASH_MODE_AUTO);
//                }
                setFlashMode(Parameters.FLASH_MODE_OFF);
            }

            if (doesZoomReallyWork() && parameters.getMaxZoom() > 0) {
                zoomTo((int) zoomProgress).onComplete(new Runnable() {
                    @Override
                    public void run() {
                        // zoom.setEnabled(true);
                    }
                }).go();
                ///zoom.setMax(parameters.getMaxZoom());
                //zoom.setOnSeekBarChangeListener(FCameraFragment.this);
                //cameraContainer.setOnTouchListener(FCameraFragment.this);
            } else {
                // zoom.setEnabled(false);
            }

            if (parameters.getMaxNumDetectedFaces() > 0) {
                supportsFaces = true;
            }

            parameters.setFocusMode(Parameters.FOCUS_MODE_MACRO);

            return (super.adjustPreviewParameters(parameters));
        }

        @Override
        public Camera.Parameters adjustPictureParameters(PictureTransaction xact,
                                                         Camera.Parameters parameters) {
//            if (hasFlash) {
//                if (selectedFlashType.equals(flashOn.getTag())) {
//                    setFlashMode(Parameters.FLASH_MODE_ON);
//                } else if (selectedFlashType.equals(flashOff.getTag())) {
//                    setFlashMode(Parameters.FLASH_MODE_OFF);
//                } else if (selectedFlashType.equals(flashAuto.getTag())) {
//                    setFlashMode(Parameters.FLASH_MODE_AUTO);
//                } else {
//                    setFlashMode(Parameters.FLASH_MODE_AUTO);
//                }
//            }
            setFlashMode(Parameters.FLASH_MODE_OFF);
            parameters.setFocusMode(Parameters.FOCUS_MODE_MACRO);
            return (parameters);
        }

        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {
            if (faces.length > 0) {
                long now = SystemClock.elapsedRealtime();

                if (now > lastFaceToast + 10000) {
                    lastFaceToast = now;
                }
            }
        }

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            super.onAutoFocus(success, camera);
        }

        @Override
        public boolean mirrorFFC() {
            return true;
        }
    }
}