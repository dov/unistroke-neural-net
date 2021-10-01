package com.groodov.dovunistroke;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;
import java.util.HashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.os.Environment;
import android.util.Log;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.Paint.Align;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.util.AttributeSet;
import android.gesture.Gesture;
import android.gesture.GesturePoint;
import android.gesture.GestureStroke;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;
import android.graphics.Typeface;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class DovUniStrokeGestureView extends View implements OnTouchListener, OnClickListener {
    ArrayList<GesturePoint> points = new ArrayList<GesturePoint>();
    Paint paint = new Paint();
    List<GestureLibrary> mGestures;
    String glyphName = null;
    HashMap<String, String> glyphMap;
    Typeface mTypeface;
    int mCurrentGestureSet = 0;
    final String[] mSymbolSetChar = {"L","ע","123"};
    int mModifier = 0;
    int mGlyphIndex=0;
    Context mContext;
    
    private OnCharacterEnteredListener mOnCharacterEnteredListener;
    private OnBackspaceListener mOnBackspaceListener;
    private OnReturnListener mOnReturnListener;

    private static final String TAG = "UniPen";

    public DovUniStrokeGestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        this.setOnClickListener(this);

        // Search for the next index for the next glyph
        while(true) {
            File folder = new File(Environment.getExternalStorageDirectory() + "/DbgGlyph");
            String filename = String.format("dbg-glyph-%05d.json",mGlyphIndex);
            File path = new File(folder, filename);
            if (!path.exists()) {
                break;
            }
            mGlyphIndex++;
        }

        glyphMap = new HashMap<String, String>();
        glyphMap.put("aleph","א");
        glyphMap.put("bet","ב");
        glyphMap.put("gimel", "ג");
        glyphMap.put("daled", "ד");
        glyphMap.put("heh", "ה");
        glyphMap.put("vav", "ו");
        glyphMap.put("zayin", "ז");
        glyphMap.put("chet", "ח");
        glyphMap.put("tet", "ט");
        glyphMap.put("yud", "י");
        glyphMap.put("kaf", "כ");
        glyphMap.put("lamed", "ל");
        glyphMap.put("mem", "מ");
        glyphMap.put("nun", "נ");
        glyphMap.put("ayin", "ע");
        glyphMap.put("samech", "ס");
        glyphMap.put("peh", "פ");
        glyphMap.put("tsadik", "צ");
        glyphMap.put("kuf", "ק");
        glyphMap.put("resh", "ר");
        glyphMap.put("shin", "ש");
        glyphMap.put("taf", "ת");
        glyphMap.put("space", " ");

        for (char ch='a'; ch<='z'; ch++) {
            String s = String.format("%c", ch);
            glyphMap.put(s,s);
        }
        for (char ch='0'; ch<='9'; ch++) {
            String s = String.format("%c", ch);
            glyphMap.put(s,s);
        }
    }

    void setInfo(List<GestureLibrary> gestures) {
        mGestures = gestures;
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(Color.WHITE);
        for (GesturePoint point : points) {
            canvas.drawCircle(point.x, point.y, 2, paint);  
        }
        paint.setColor(Color.RED);
        
        boolean first=true;
        GesturePoint last_point = new GesturePoint(0.f,0.f,0);
        for (GesturePoint point : points) {
            if (first)
                first = false;
            else if (point.x != -999 && last_point.x != -999)
                canvas.drawLine(last_point.x, last_point.y,
                                point.x, point.y, 
                                paint);
            last_point = point;
        }

        paint.setColor(Color.WHITE);
        for (GesturePoint point : points) {
            canvas.drawCircle(point.x, point.y, 4, paint);  
        }

        // Write symbol set
        paint.setColor(Color.YELLOW);
        paint.setStyle(Style.FILL);
        paint.setTextSize(60);
        paint.setTextAlign(Align.RIGHT);
        canvas.drawText(mSymbolSetChar[mCurrentGestureSet],getWidth()-10,60,paint);
        // Write Logo
        paint.setColor(Color.CYAN);
        paint.setTextSize(50);
        paint.setTextAlign(Align.LEFT);
        canvas.drawText("DovUnistroke",10,50,paint);
        
        // Write modifier indicator
        float modifier_x = 25;
        float modifier_y = 18;
        if (mModifier == 1) {
            Path path = new Path();
            path.moveTo(modifier_x,modifier_y+10);
            path.lineTo(modifier_x,modifier_y-10);
            path.close();
            paint.setStrokeWidth(2);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, paint);
        }
        else if (mModifier == 2) {
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(modifier_x, modifier_y, 7, paint);  
        }
    }


    public void onClick(View view) {
    }

    public boolean onTouch(View view, MotionEvent event) {
        Point point = new Point();
        point.x = event.getX();
        point.y = event.getY();
        points.add(new GesturePoint(event.getX(), event.getY(), event.getEventTime()));

        if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
            // Build a gesture
            Gesture gest = new Gesture();

            // Get bounding box
            double minx=1.e6, miny=1.e6, maxx=-1.e6, maxy=-1.e6;
            for (GesturePoint p : points) {
                if (p.x < minx) {
                    minx = p.x;
                }
                if (p.y < miny) {
                    miny = p.y;
                }
                if (p.x > maxx) {
                    maxx = p.x;
                }
                if (p.y > maxy) {
                    maxy = p.y;
                }
            }
            double maxWidth = maxx-minx;
            double maxHeight = maxy-miny;

            GestureStroke stroke = new GestureStroke(points);
            gest.addStroke(stroke);
            Log.v(TAG, "Dov: Interpreting a gesture of length=" + points.size());

            // special heuristic for space and backspace.
            // TBD - Make this dependent on the widget size!
            int len = points.size();
            if (points.get(len-1).x < points.get(0).x-50
                && 1.0*(maxy-miny)/(maxx-minx) < 0.35) {
                processRecognizedGlyph("backspace");
            }
            else if (points.get(len-1).x > points.get(0).x+50
                && 1.0*(maxy-miny)/(maxx-minx) < 0.35) {
                processRecognizedGlyph("space");
            }
            else if (maxWidth < 20 && maxHeight < 20) {
                processRecognizedGlyph("dot");
            }
            else {
                // Get prediction
                ArrayList<Prediction> preds = mGestures.get(mCurrentGestureSet).recognize(gest);

  
                // Since the predictions make quite a bad job for some letters. Improve it
                // by some heuristics.

                // Only allow V,U,or Y predictions if the first and the last point are
                // extremes.
                double firstx = points.get(0).x;
                double lastx = points.get(len-1).x;
                boolean v_shaped = (((firstx-minx)/maxWidth < 0.1
                                     && (maxx-lastx)/maxWidth < 0.1)
                                    || ((lastx-minx)/maxWidth < 0.1
                                     && (maxx-firstx)/maxWidth < 0.1));
                Log.v(TAG, "Dov: V-shaped="+String.valueOf(v_shaped));

                if (preds.size() > 0) {
                    // mInfo.setText(String.format("Best prediction is \"%s\" with score %.3f", preds.get(0).name, preds.get(0).score));

                    // loop until we find a match matching the v_shaped (and other heuristics)
                    for (int i=0; i<preds.size(); i++)
                    {
                        String name = preds.get(i).name;
                        if (!v_shaped &&
                            (name.equals("v")
                             || name.equals("y")
                             || name.equals("u")))
                        {
                            if (name.equals("u")) {
                                Log.v(TAG, "Dov: Promoting u->o");
                                name= "o"; // promotion u->o
                            }
                            else {
                                continue; // Ignoring false alarms!
                            }
                        }
                        processRecognizedGlyph(name);
                        break;
                    }
                }
                else {
                    // mInfo.setText("Failed finding gesture");
                }

                // Save the gesture and the predictions in a dump file.
                String filename = String.format("dbg-glyph-%05d.json",mGlyphIndex);
                mGlyphIndex+=1;
                try {
                    //                    File folder = new File(Environment.getExternalStorageDirectory() + "/DbgGlyph");
                    File folder = mContext.getDataDir();
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    File path = new File(folder, filename);
                    FileOutputStream fstr = new FileOutputStream(path);
                    fstr.write(("{\n"
                                + "  \"mModifier\" : " + mModifier + ",\n"
                                + "  \"mGestureSet\" : " + mCurrentGestureSet + ",\n"
                                + "  \"predictions\" : [\n").getBytes());
                    List<String> list = new ArrayList<String>();
                    for (Prediction pred : preds) {
                        list.add(String.format(
                             "    { \"name\":\"%s\", \"score\":%f } ",
                             pred.name,
                             pred.score));
                    }
                    fstr.write(("    "
                                + TextUtils.join(",\n    ", list)
                                + "  ],\n"
                                + "  \"gesture\": [\n").getBytes()
                               );
                    list.clear();
                    for (GesturePoint p : points) {
                        list.add(String.format("{ \"x\": %f, \"y\": %f, \"t\": %d }",p.x,p.y,p.timestamp));
                    }
                    fstr.write(("    "
                                + TextUtils.join(",\n    ", list)
                                + "\n"
                                + "  ]\n"
                                + "}\n").getBytes());
                    fstr.close();
                    Log.v(TAG, "Dov: Created file " + path);
                }
                catch(FileNotFoundException e) {
                    String stackTrace = Log.getStackTraceString(e);
                    Log.v(TAG, "Dov: File not found exception:" + e);
                }
                catch(IOException e) {
                    Log.v(TAG, "Dov: IOException");
                }
            }
            

            points.clear();
        }
        invalidate();
        return false;
    }

    public void setOnCharacterEnteredListener(OnCharacterEnteredListener onCharacterEnteredListener) {
        mOnCharacterEnteredListener = onCharacterEnteredListener;
    }

    public void setOnBackspaceListener(OnBackspaceListener onBackSpaceListener) {
        mOnBackspaceListener = onBackSpaceListener;
    }

    public void setOnReturnListener(OnReturnListener onReturnListener) {
        mOnReturnListener = onReturnListener;
    }

    String stringToSymbol(String name)
    {
        String symbol = glyphMap.get(name);
        Log.v(TAG, "Dov: name symbol mCurrentGestureSet mModifier=" + name + " " + symbol + " " + mCurrentGestureSet + " " + mModifier);

        if (mModifier == 2) { // Dot
            if (name.equals("space")) {
                symbol = "-";
            }
            else if (name.equals("dot")) {
                symbol = ".";
            }
            else if (name.equals("enter")) {
                symbol = ",";
            }
            else if (name.equals("up")) {
                symbol = "!";
            }
            else if (name.equals("backslash")) {
                symbol = "\\";
            }
            else if (name.equals("up-down-backslash")) {
                symbol = "`";
            }
            else if (name.equals("t") || name.equals("a") || name.equals("resh")) {
                symbol = "?";
            }
            else if (name.equals("i")) {
                symbol = "'";
            }
            else if (name.equals("z")) {
                symbol = "=";
            }
            else if (name.equals("down-up")) {
                symbol = ":";
            }
            else if (name.equals("up-down")) {
                symbol = "|";
            }
            Log.v(TAG, "Dov: Produced name->symbol " + name + "->" + "symbol");
            mModifier = 0;

            return symbol;
        }
        
        if (symbol == null) {
            return null;
        }

        if (mCurrentGestureSet == 0) {// Latin
            if (mModifier == 1) {
                symbol = symbol.toUpperCase();
                mModifier = 0;
            }
        }
        else if (mCurrentGestureSet == 1) { // Hebrew
            if (mModifier == 1) {
                if (name.equals("kaf")) {
                    symbol = "ך";
                }
                else if (name.equals("mem")) {
                    symbol = "ם";
                }
                else if (name.equals("nun")) {
                    symbol = "ן";
                }
                else if (name.equals("peh")) {
                    symbol = "ף";
                }
                else if (name.equals("tsadik")) {
                    symbol = "ץ";
                }
                // tbd
                mModifier = 0;
                Log.v(TAG, "Dov: Modified name->symbol =" + "'"+name+"'->"+symbol);
            }
        }
        return symbol;
    }

    void processRecognizedGlyph(String glyph)
    {
        Log.v(TAG, "Dov: glyph mModifier = " + glyph + " " + mModifier);
        if (glyph.equals("hebrew")) {
            mCurrentGestureSet = 1;
            //            mInfo.setText("Switching to Hebrew");            
        }
        else if (glyph.equals("latin")) {
            mCurrentGestureSet = 0;
            //            mInfo.setText("Switching to Latin");
        }
        else if (glyph.equals("number")) {
            mCurrentGestureSet = 2;
            //            mInfo.setText("Switching to Numbers");
        }
        else if (glyph.equals("up") && mModifier != 2) {
            mModifier = 1;
            Log.v(TAG, "Dov: mModifier = 1");
        }
        else if (glyph.equals("dot") && mModifier != 2) { // Fallthrough to stringToSymbol!
            mModifier = 2;
        }
        else if (glyph.equals("backspace")) {
            // swap space and backspace for hebrew
            if (mCurrentGestureSet == 1) {
                mOnCharacterEnteredListener.characterEntered(" ");
            }
            else {
                mOnBackspaceListener.backspacePressed();
            }
        }
        else if (mModifier != 2 && glyph.equals("space")) {
            // swap space and backspace for hebrew
            if (mCurrentGestureSet == 1) {
                mOnBackspaceListener.backspacePressed();
            }
            else {
                mOnCharacterEnteredListener.characterEntered(" ");
            }
        }
        else if (mModifier != 2 && glyph.equals("enter")) {
            //            mOnCharacterEnteredListener.characterEntered("\n");
            mOnReturnListener.returnPressed();
        }
        else {
            String value = stringToSymbol(glyph);
            if (value != null) {
                mOnCharacterEnteredListener.characterEntered(value);
            }
        }
    }
}


class Point {
    float x, y;
}

