# Intro

This repo contain tools for developing input method for android based on unistroke like alphbats, like on the palm pilot.

# DovUniStroke

Currently the project contains a android gesture based recognition system (together with some internal heuristics). The android application is available in DovUniStroke. It can be built by android studio. The prebuild debug apk file is available in the releases section on github. It can be installed on an android device as follows:

    adb install -r -t DovUniStroke-debug.apk
    
To uninstall the package from the phone:

    adb uninstall com.dovgro.unistroke
    
## Usage

1. Add the DovUniStroke input method through keyboard soft preferences
2. Choose DovUniStroke as the input method
3. Enter text by drawing the grafitti unistroke symbols
4. For Hebrew input, write the stroke for Ayin. Not that in Hebrew mode backspace and space are swapped.
5. To return to latin mode write the unistroke for L
   
