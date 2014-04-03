/**
 * An Image Resizer Plugin for Cordova/PhoneGap.
 * 
 * More Information : https://github.com/raananw/
 * 
 * The android version of the file stores the images using the local storage.
 * 
 * The software is open source, MIT Licensed.
 * Copyright (C) 2012, webXells GmbH All Rights Reserved.
 * 
 * @author Raanan Weber, webXells GmbH, http://www.webxells.com
 */
package com.synconset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.DisplayMetrics;

public class ImageResizePlugin extends CordovaPlugin {
    public static final String IMAGE_DATA_TYPE_BASE64 = "base64Image";
    public static final String IMAGE_DATA_TYPE_URL = "urlImage";
    public static final String RESIZE_TYPE_FACTOR = "factorResize";
    public static final String RESIZE_TYPE_MIN_PIXEL = "minPixelResize";
    public static final String RESIZE_TYPE_MAX_PIXEL = "maxPixelResize";
    public static final String RETURN_BASE64 = "returnBase64";
    public static final String RETURN_URI = "returnUri";
    public static final String FORMAT_JPG = "jpg";
    public static final String FORMAT_PNG = "png";
    public static final String DEFAULT_FORMAT = "jpg";
    public static final String DEFAULT_IMAGE_DATA_TYPE = IMAGE_DATA_TYPE_BASE64;
    public static final String DEFAULT_RESIZE_TYPE = RESIZE_TYPE_FACTOR;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        JSONObject params = data.getJSONObject(0);
        if (action.equals("resizeImage")) {
            ResizeImage resizeImage = new ResizeImage(params, callbackContext);
            cordova.getThreadPool().execute(resizeImage);
            return true;
        } else if (action.equals("imageSize")) {
            GetImageSize imageSize = new GetImageSize(params, callbackContext);
            cordova.getThreadPool().execute(imageSize);
            return true;
        } else if (action.equals("storeImage")) {
            StoreImage storeImage = new StoreImage(params, callbackContext);
            cordova.getThreadPool().execute(storeImage);
            return true;
        } else {
            Log.d("PLUGIN", "unknown action");
            return false;
        }
    }
    
    private class ImageTools {
        protected JSONObject params;
        protected CallbackContext callbackContext;
        protected String format;
        protected String imageData;
        protected String imageDataType;
        
        public ImageTools(JSONObject params, CallbackContext callbackContext) throws JSONException {
            this.params = params;
            this.callbackContext = callbackContext;
            imageData = params.getString("data");
            imageDataType = DEFAULT_IMAGE_DATA_TYPE;
            if (params.has("imageDataType")) {
                imageDataType = params.getString("imageDataType");
            }
            format = DEFAULT_FORMAT;
            if (params.has("format")) {
                format = params.getString("format");
            }
        }
        
        protected Bitmap getBitmap(String imageData, String imageDataType)
                throws IOException, URISyntaxException {
            Bitmap bmp;
            if (imageDataType.equals(IMAGE_DATA_TYPE_BASE64)) {
                byte[] blob = Base64.decode(imageData, Base64.DEFAULT);
                bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            } else {
                URI uri = new URI(imageData);
                File imageFile = new File(uri);
                bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if (bmp == null) {
                    throw new IOException("The image file could not be opened.");
                }
            }
            return bmp;
        }
        
        protected void storeImage(JSONObject params, String format, Bitmap bmp, CallbackContext callbackContext) throws JSONException, IOException, URISyntaxException {
            int quality = params.getInt("quality");
            String filename = params.getString("filename");
            URI folderUri = new URI(params.getString("directory"));
            URI pictureUri = new URI(params.getString("directory") + "/" + filename);
            File folder = new File(folderUri);
            folder.mkdirs();
            File file = new File(pictureUri);
            OutputStream outStream = new FileOutputStream(file);
            if (format.equals(FORMAT_PNG)) {
                bmp.compress(Bitmap.CompressFormat.PNG, quality,
                        outStream);
            } else {
                bmp.compress(Bitmap.CompressFormat.JPEG, quality,
                        outStream);
            }
            outStream.flush();
            outStream.close();
            JSONObject res = new JSONObject();
            res.put("filename", filename);
            res.put("width", bmp.getWidth());
            res.put("height", bmp.getHeight());
            callbackContext.success(res);
        }
    }
    
    private class GetImageSize extends ImageTools implements Runnable {
        public GetImageSize(JSONObject params, CallbackContext callbackContext) throws JSONException {
            super(params, callbackContext);
        }
        
        @Override
        public void run() {
            try {
                Bitmap bmp = getBitmap(imageData, imageDataType);
                JSONObject res = new JSONObject();
                res.put("width", bmp.getWidth());
                res.put("height", bmp.getHeight());
                callbackContext.success(res);
            } catch (JSONException e) {
                callbackContext.error(e.getMessage());
            } catch (IOException e) {
                Log.d("PLUGIN", e.getMessage());
                callbackContext.error(e.getMessage());
            } catch (URISyntaxException e) {
                Log.d("PLUGIN", e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    }
    
    private class StoreImage extends ImageTools implements Runnable {
        public StoreImage(JSONObject params, CallbackContext callbackContext) throws JSONException {
            super(params, callbackContext);
        }
        
        @Override
        public void run() {
            try {
                Bitmap bmp = getBitmap(imageData, imageDataType);
                this.storeImage(params, format, bmp, callbackContext);
            } catch (JSONException e) {
                Log.d("PLUGIN", e.getMessage());
                callbackContext.error(e.getMessage());
            } catch (IOException e) {
                Log.d("PLUGIN", e.getMessage());
                callbackContext.error(e.getMessage());
            } catch (URISyntaxException e) {
                Log.d("PLUGIN", e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    }
    
    private class ResizeImage extends ImageTools implements Runnable {
        public ResizeImage(JSONObject params, CallbackContext callbackContext) throws JSONException {
            super(params, callbackContext);
        }
        
        @Override
        public void run() {
            double widthFactor;
            double heightFactor;
            
            try {
                Bitmap bmp = getBitmap(imageData, imageDataType);
                // Pixels or Factor resize
                String resizeType = params.getString("resizeType");
        
                // Get width and height parameters
                double width = params.getDouble("width");
                double height = params.getDouble("height");
        
                if (resizeType.equals(RESIZE_TYPE_MIN_PIXEL)) {
                    widthFactor = width / ((double) bmp.getWidth());
                    heightFactor = height / ((double) bmp.getHeight());
                    if (widthFactor > heightFactor && widthFactor <= 1.0) {
                        heightFactor = widthFactor;
                    } else if (heightFactor <= 1.0) {
                        widthFactor = heightFactor;
                    } else {
                        widthFactor = 1.0;
                        heightFactor = 1.0;
                    }
                } else if (resizeType.equals(RESIZE_TYPE_MAX_PIXEL)) {
                    widthFactor = width / ((double) bmp.getWidth());
                    heightFactor = height / ((double) bmp.getHeight());
                    if (widthFactor == 0.0) {
                        widthFactor = heightFactor;
                    } else if (heightFactor == 0.0) {
                        heightFactor = widthFactor;
                    } else if (widthFactor > heightFactor) {
                        widthFactor = heightFactor; // scale to fit height
                    } else {
                        heightFactor = widthFactor; // scale to fit width
                    }
                } else {
                    widthFactor = width;
                    heightFactor = height;
                }
                
                if (params.getBoolean("pixelDensity")) {
                    DisplayMetrics metrics = cordova.getActivity().getResources().getDisplayMetrics();
                    if (metrics.density > 1) {
                        if (widthFactor * metrics.density < 1.0 && heightFactor * metrics.density < 1.0) {
                            widthFactor *= metrics.density;
                            heightFactor *= metrics.density;
                        } else {
                            widthFactor = 1.0;
                            heightFactor = 1.0;
                        }
                    }
                }
        
                Bitmap resized = getResizedBitmap(bmp, (float) widthFactor, (float) heightFactor);
                        
                if (params.getBoolean("storeImage")) {
                    storeImage(params, format, resized, callbackContext);
                } else {
                    int quality = params.getInt("quality");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (format.equals(FORMAT_PNG)) {
                        resized.compress(Bitmap.CompressFormat.PNG, quality, baos);
                    } else {
                        resized.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                    }
                    byte[] b = baos.toByteArray();
                    String returnString = Base64.encodeToString(b, Base64.DEFAULT);
                    // return object
                    JSONObject res = new JSONObject();
                    res.put("imageData", returnString);
                    res.put("width", resized.getWidth());
                    res.put("height", resized.getHeight());
                    callbackContext.success(res);
                }
            } catch (JSONException e) {
                Log.d("PLUGIN", e.getMessage());
                callbackContext.error(e.getMessage());
            } catch (IOException e) {
                Log.d("PLUGIN", e.getMessage());
                callbackContext.error(e.getMessage());
            } catch (URISyntaxException e) {
                Log.d("PLUGIN", e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
        
        private Bitmap getResizedBitmap(Bitmap bm, float widthFactor,
                float heightFactor) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.postScale(widthFactor, heightFactor);
            // recreate the new Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                    matrix, false);
            return resizedBitmap;
        }
    }
}