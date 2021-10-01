package com.dovgro.dovunistroke;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View.OnLongClickListener;
import android.util.AttributeSet;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;


public class DovUniStrokeView extends RelativeLayout  {
    Context mContext;
    DovUniStrokeGestureView mGestureView;
    // at most 8 directions can be recognized
    static final int ORIENTATION_SENSITIVE_8 = 8;


    public DovUniStrokeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        DovUniStrokeGestureView gestureView = (DovUniStrokeGestureView) findViewById(R.id.drawing_space_view);
        // Is this needed
        mGestureView = gestureView;

        // Meanwhile make the position important
        int[] GestureResorces = {
            R.raw.latin_gestures,
            R.raw.hebrew_gestures,
            R.raw.number_gestures
        };

        List<GestureLibrary> gestures = new ArrayList<GestureLibrary>();

        int sumGestures = 0;
        for (int Resource : GestureResorces) {
            GestureLibrary gestureLib = GestureLibraries.fromRawResource(mContext, Resource);
            gestureLib.setOrientationStyle(ORIENTATION_SENSITIVE_8);
            gestureLib.load();
            gestures.add(gestureLib);
            sumGestures += gestureLib.getGestureEntries().size();
        }

        gestureView.setInfo(gestures);

    }

    public void setOnCharacterEnteredListener(OnCharacterEnteredListener onCharacterEnteredListener) {
        mGestureView.setOnCharacterEnteredListener(onCharacterEnteredListener);
    }

    public void setOnBackspaceListener(OnBackspaceListener onBackspaceListener) {
        mGestureView.setOnBackspaceListener(onBackspaceListener);
    }

    public void setOnReturnListener(OnReturnListener onReturnListener) {
        mGestureView.setOnReturnListener(onReturnListener);
    }

}
