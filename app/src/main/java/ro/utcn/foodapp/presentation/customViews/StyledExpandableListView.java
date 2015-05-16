package ro.utcn.foodapp.presentation.customViews;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

import ro.utcn.foodapp.R;

/**
 * Created by coponipi on 16.05.2015.
 */
public class StyledExpandableListView extends ExpandableListView {
    public StyledExpandableListView(Context context) {
        super(context);
        initialize();
    }

    public StyledExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public StyledExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StyledExpandableListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    @Override
    public void setGroupIndicator(Drawable groupIndicator) {
//        if(groupIndicator != null) {
//            groupIndicator.setColorFilter(getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
//        }

        super.setGroupIndicator(null);
    }

    @Override
    public boolean collapseGroup(int groupPos) {
        // Don't collapse group.
        return true;
    }

    private void initialize() {
        setStyle();
    }

    private void setStyle() {
        setGroupIndicator(getContext().getResources().getDrawable(R.drawable.group_indicator));
    }
}
