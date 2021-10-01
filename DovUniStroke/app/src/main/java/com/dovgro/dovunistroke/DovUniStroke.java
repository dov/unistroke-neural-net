/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dovgro.dovunistroke;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Color;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class DovUniStroke extends InputMethodService {
    static final boolean DEBUG = false;
    private DovUniStrokeView mInputView;
    int mSelStart = 0;
    int mSelEnd = 0;
    boolean mActionOnReturn = false;

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override public View onCreateInputView() {
        final DovUniStrokeView view = (DovUniStrokeView) getLayoutInflater().inflate(R.layout.dovinput, null);
      
        view.setOnCharacterEnteredListener(new OnCharacterEnteredListener() {
                @Override
                public void characterEntered(String character) {
                    InputConnection ic = getCurrentInputConnection();

                    ic.commitText(character, 1);
                }
            });

        view.setOnReturnListener(new OnReturnListener() {
                @Override
                public void returnPressed() {
                    InputConnection ic = getCurrentInputConnection();

                    if (mActionOnReturn) {
                        ic.performEditorAction(EditorInfo.IME_MASK_ACTION); // Should be ACTION_GO
                    }
                    else {
                        ic.commitText("\n", 1);
                    }
                }
            });

        view.setOnBackspaceListener(new OnBackspaceListener() {
                @Override
                public void backspacePressed() {
                    InputConnection ic = getCurrentInputConnection();
                    if (mSelEnd - mSelStart == 0) {
                        ic.deleteSurroundingText(1, 0);
                    }
                    else {
                        ic.setSelection(mSelStart, mSelStart);
                        ic.deleteSurroundingText(mSelStart, mSelEnd);
                    }
                }
            });

        mInputView = view;
        return view;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        // Log.v("Dov","Dov: onStartInput(): actionId actionLabel imeOptions inputType = " + attribute.actionId + " " + attribute.actionLabel + " " + attribute.imeOptions + " " + attribute.inputType);
        // Whether the return character should trigger an action or do a
        // newline depends on whether we do multiline or not. Is there
        // a better heuristics?
        mActionOnReturn = (attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) == 0; 
    }

     
    /**
     * Deal with the editor reporting movement of its cursor. 
     */ 
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd)  
    { 
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd); 
        mSelStart = newSelStart;
        mSelEnd = newSelEnd;
    }

}
