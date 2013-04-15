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
package com.webXells.ImageResizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

public class ImageResizePlugin extends CordovaPlugin {

	public static String IMAGE_DATA_TYPE_BASE64 = "base64Image";
	public static String IMAGE_DATA_TYPE_URL = "urlImage";
	public static String RESIZE_TYPE_FACTOR = "factorResize";
	public static String RESIZE_TYPE_PIXEL = "pixelResize";
	public static String RETURN_BASE64 = "returnBase64";
	public static String RETURN_URI = "returnUri";
	public static String FORMAT_JPG = "jpg";
	public static String FORMAT_PNG = "png";
	public static String DEFAULT_FORMAT = "jpg";
	public static String DEFAULT_IMAGE_DATA_TYPE = IMAGE_DATA_TYPE_BASE64;
	public static String DEFAULT_RESIZE_TYPE = RESIZE_TYPE_FACTOR;

	@Override
	public boolean execute(String action, JSONArray data,
			CallbackContext callbackContext) {
		JSONObject params;
		String imageData;
		String imageDataType;
		String format;
		Bitmap bmp;
		Log.d("PLUGIN", action);
		try {
			// parameters (forst object of the json array)
			params = data.getJSONObject(0);
			// image data, either base64 or url
			imageData = params.getString("data");
			// which data type is that, defaults to base64
			imageDataType = params.has("imageDataType") ? params
					.getString("imageDataType") : DEFAULT_IMAGE_DATA_TYPE;
			// which format should be used, defaults to jpg
			format = params.has("format") ? params.getString("format")
					: DEFAULT_FORMAT;
			// create the Bitmap object, needed for all functions
			bmp = getBitmap(imageData, imageDataType);
		} catch (JSONException e) {
			callbackContext.error(e.getMessage());
			return false;
		} catch (IOException e) {
			callbackContext.error(e.getMessage());
			return false;
		}
		// resize the image
		Log.d("PLUGIN", "passed init");
		if (action.equals("resizeImage")) {
			try {
				double widthFactor;
				double heightFactor;

				// compression quality
				int quality = params.getInt("quality");

				// Pixels or Factor resize
				String resizeType = params.getString("resizeType");

				// Get width and height parameters
				double width = params.getDouble("width");
				double height = params.getDouble("height");

				if (resizeType.equals(RESIZE_TYPE_PIXEL)) {
					widthFactor = width / ((double) bmp.getWidth());
					heightFactor = height / ((double) bmp.getHeight());
				} else {
					widthFactor = width;
					heightFactor = height;
				}

				Bitmap resized = getResizedBitmap(bmp, (float) widthFactor,
						(float) heightFactor);
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
				return true;
			} catch (JSONException e) {
				callbackContext.error(e.getMessage());
				return false;
			}
		} else if (action.equals("imageSize")) {
			try {

				JSONObject res = new JSONObject();
				res.put("width", bmp.getWidth());
				res.put("height", bmp.getHeight());
				Log.d("PLUGIN", "finished get image size");
				callbackContext.success(res);
				return true;
			} catch (JSONException e) {
				callbackContext.error(e.getMessage());
				return false;
			}
		} else if (action.equals("storeImage")) {
			try {
				// Obligatory Parameters, throw JSONException if not found
				String filename = params.getString("filename");
				filename = (filename.contains(".")) ? filename : filename + "."
						+ format;
				String directory = params.getString("directory");
				directory = directory.startsWith("/") ? directory : "/"
						+ directory;
				int quality = params.getInt("quality");

				OutputStream outStream;
				// store the file locally using the external storage directory
				File file = new File(Environment.getExternalStorageDirectory()
						.toString() + directory, filename);
				try {
					outStream = new FileOutputStream(file);
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
					res.put("url", "file://" + file.getAbsolutePath());
					callbackContext.success(res);
					return true;
				} catch (IOException e) {
					callbackContext.error(e.getMessage());
					return false;
				}
			} catch (JSONException e) {
				callbackContext.error(e.getMessage());
				return false;
			}
		}
		Log.d("PLUGIN", "unknown action");
		return false;
	}

	public Bitmap getResizedBitmap(Bitmap bm, float widthFactor,
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

	private Bitmap getBitmap(String imageData, String imageDataType)
			throws IOException {
		Bitmap bmp;
		if (imageDataType.equals(IMAGE_DATA_TYPE_BASE64)) {
			byte[] blob = Base64.decode(imageData, Base64.DEFAULT);
			bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		} else {
			File imagefile = new File(imageData);
			FileInputStream fis = new FileInputStream(imagefile);
			bmp = BitmapFactory.decodeStream(fis);
		}
		return bmp;
	}

}
