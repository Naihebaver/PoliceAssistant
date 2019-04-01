/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package naihebaver.policeassistantapp.Ocr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import naihebaver.policeassistantapp.Camera.GraphicOverlay;
import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.Helpers.UsefulMethods;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> graphicOverlay;

    private Context context;
    OcrCaptureActivity ocrActivity;
    public String carNumber;
    SharedPreferencesHelper mSharedPrefsHelper;


    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, Context context) {
        graphicOverlay = ocrGraphicOverlay;

        this.context = context;
        ocrActivity = (OcrCaptureActivity) context;
        mSharedPrefsHelper = new SharedPreferencesHelper(ocrActivity);
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @SuppressLint("ResourceAsColor")
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        graphicOverlay.clear();


        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);

            //if (item != null && item.getValue() != null) {
            //    Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
            //    OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
            //    graphicOverlay.add(graphic);
            //}

            try{

                if(UsefulMethods.isUA(item.getValue())){

                    carNumber = item.getValue();
                    setData();

                } else if (mSharedPrefsHelper.getForeignMode() == 1 && UsefulMethods.isSK(item.getValue())) {
                        carNumber = item.getValue();
                        setData();
                } else if (mSharedPrefsHelper.getForeignMode() == 1 && UsefulMethods.isPL(item.getValue())) {
                        carNumber = item.getValue();
                        setData();

                } else if (mSharedPrefsHelper.getForeignMode() == 1 && UsefulMethods.isHU(item.getValue())) {
                        carNumber = item.getValue();
                        setData();

                    }

            }catch (Exception e){}

        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        graphicOverlay.clear();
    }


    public void setData(){
        if(!ocrActivity.checkDialogWindow()) {
            ocrActivity.setCarNumber(carNumber);
            ocrActivity.showDialogInfo();

            //Log.e("CarNumber = ", carNumber);
        }
    }


}



