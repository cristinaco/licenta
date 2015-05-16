package ro.utcn.foodapp.presentation.customViews;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.engenoid.tesseract.Core.CameraEngine;

/**
 * Created by Coni on 25/04/2015.
 */
public class FocusBoxView extends View {

    private final Paint paint;
    private final int maskColor;
    private final int frameColor;
    private final int cornerColor;
    private CameraEngine cameraEngine;

    public FocusBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();

        maskColor = resources.getColor(R.color.focus_box_mask);
        frameColor = resources.getColor(R.color.focus_box_frame);
        cornerColor = resources.getColor(R.color.focus_box_corner);
    }


    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = getCameraEngine().getBoxRect();

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        // Draw a two pixel solid border inside the framing rect
        paint.setAlpha(0);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(frameColor);
        canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
        canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
        canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
        canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);

        // Draw the framing rect corner UI elements
        paint.setColor(cornerColor);
        canvas.drawRect(frame.left - 15, frame.top - 15, frame.left + 15, frame.top, paint);
        canvas.drawRect(frame.left - 15, frame.top, frame.left, frame.top + 15, paint);
        canvas.drawRect(frame.right - 15, frame.top - 15, frame.right + 15, frame.top, paint);
        canvas.drawRect(frame.right, frame.top - 15, frame.right + 15, frame.top + 15, paint);
        canvas.drawRect(frame.left - 15, frame.bottom, frame.left + 15, frame.bottom + 15, paint);
        canvas.drawRect(frame.left - 15, frame.bottom - 15, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right - 15, frame.bottom, frame.right + 15, frame.bottom + 15, paint);
        canvas.drawRect(frame.right, frame.bottom - 15, frame.right + 15, frame.bottom + 15, paint);

    }


    public CameraEngine getCameraEngine() {
        return cameraEngine;
    }

    public void setCameraEngine(CameraEngine cameraEngine) {
        this.cameraEngine = cameraEngine;
    }

    public void redraw() {
        invalidate();
    }
}
