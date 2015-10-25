package com.mpbd.saveplace.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnUploadImageListener;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;


public class MBPlaceSubmitImageData {
	public final int mImageId;
	public final String mFileUrl;
	public int mFileState;
	private SubmitImageDataListener mListener = null;
	public MBPlaceSubmitImageData(int imageId, String url, int state) {
		mImageId = imageId;
		mFileUrl = url;
		mFileState = state;
	}

	public boolean submitImage(String placeId, OnUploadImageListener listener) {
		MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance());

		byte[] object = getBitmapBytArray(mFileUrl);
		if(object != null) {
			api.uploadImage(listener, 
			placeId,
			object);
			return true;
		} else {
			return false;
		}
	}

	public static byte[] getBitmapBytArray(String path) {
		File file = new File(path);
		if(null != file && file.exists()) {
			byte[] bytes = null;
		    try {
				Bitmap bm = BitmapFactory.decodeFile(path);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
				bytes = baos.toByteArray();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    if(bytes != null)
		    	DeBug.d("getBitmapBytArray : bytes size = "+bytes.length);
		    return bytes;
		}
		return null;
	}

	/**
	 * 請記得是非Ui-Thread
	 * @author Hao
	 *
	 */
	public interface SubmitImageDataListener {
		public void submitSuccessd();
		public void submitFailed();
	}

	private byte[] getBitmapBytArraySession(String path) {
		File file = new File(path);
		if(null != file && file.exists()) {
			byte[] bytes = null;
		    try {
		    	// 先解析多大的圖
		    	BitmapFactory.Options opts = new BitmapFactory.Options();
		    	opts.inJustDecodeBounds = true;
		    	BitmapFactory.decodeFile(path, opts);
		    	opts.inSampleSize = checkSize(opts.outWidth, opts.outHeight);
		    	opts.inJustDecodeBounds = false;
				Bitmap bm = BitmapFactory.decodeFile(path, opts);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
				bytes = baos.toByteArray();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    if(bytes != null)
		    	DeBug.d("getBitmapBytArray : bytes size = "+bytes.length);
		    return bytes;
		}
		return null;
	}

	private static final int DEFAULT_WIDTH = 480;
	private static final int DEFAULT_HEIGHT = 800;
	private static final int MIN_WIDTH = 100;
	private static final int MIN_HEIGHT = 100;
	private int checkSize(int bmpWidth, int bmpHeight) {
		
		int width = bmpWidth;
		int height = bmpHeight;
		if(bmpWidth > bmpHeight) {
			width = bmpHeight;
			height = bmpWidth;
		}
		
		int sample = 1;
		while(width > DEFAULT_WIDTH || height > DEFAULT_HEIGHT) {
			sample = sample * 2;
			width = width /2;
			height = height / 2;
			if(width < MIN_WIDTH || height < MIN_HEIGHT)
				break;
		}
		return sample;
	}
	
	public boolean updateImageTempBySession(final String placeId, final String csrfToken, final String session,
			SubmitImageDataListener listener) {
		final byte[] bmp = getBitmapBytArraySession(mFileUrl);
		if(bmp != null) {
			mListener = listener;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					uploadFile("https://mappingbird.com/api/upload",
							Integer.parseInt(placeId),
							bmp,
							"p"+placeId+".jpg",
							csrfToken,
							session
							);					
				}
			}).start();
			return true;
		} else {
			return false;
		}
	}

	private void uploadFile(String apiUrl, int point, byte[] object, String fileName, String csrftoken, String sessionid) {		
		String lineEnd = "\r\n";
		String twoHyphens = "--";
        String boundary = "----WebKitFormBoundaryMRUCc049xtgXiwJZ";
        
    	if(DeBug.DEBUG) {
    		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "update image path : "+apiUrl+"/"+fileName);
    	}
        DataOutputStream dos = null;
		try {
			//-- field names
			String key_image = "media";
			String key_point = "point";

			int serverResponseCode;
			String serverResponseMessage;			
			
	        URL url = new URL(apiUrl);
	        DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "uploadFile by session");
	        //-- open a HTTP  connection to  the URL
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
	        conn.setDoInput(true); // Allow Inputs
	        conn.setDoOutput(true); // Allow Outputs
	        conn.setUseCaches(false); // Don't use a Cached Copy
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Connection", "Keep-Alive");
	        //-- setup boundary string for fields 
	        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
	        //-- setup authentication info, e.g. csrftoken and sessionid
	        conn.setRequestProperty("Cookie", "csrftoken=" + csrftoken + "; sessionid=" + sessionid);
	        	           
	        dos = new DataOutputStream(conn.getOutputStream());
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(lineEnd);
	        
	        //-- assign csrftoken
	        dos.writeBytes(twoHyphens + boundary + lineEnd);	        
	        dos.writeBytes("Content-Disposition: form-data; name=\"csrfmiddlewaretoken\"" + lineEnd);
	        dos.writeBytes(lineEnd);	        
	        dos.writeBytes(csrftoken + lineEnd);
	        
	        //-- assign bytes chunk of the upload image
	        dos.writeBytes(twoHyphens + boundary + lineEnd); 	        
	        dos.writeBytes("Content-Disposition: form-data; name=\"" + key_image + "\";filename=\"" 
	                       + fileName + "\"" + lineEnd);
	        dos.writeBytes("Content-Type: image/jpg" +  lineEnd);	           
	        dos.writeBytes(lineEnd);
	 
            dos.write(object, 0, object.length);
	        dos.writeBytes(lineEnd);
	        
	        //-- assign point number
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);	        
	        dos.writeBytes("Content-Disposition: form-data; name=\"" + key_point + "\"" + lineEnd);
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(String.valueOf(point) + lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	        dos.flush();
	        
	        //-- responses from the server (code and message)
	        serverResponseCode = conn.getResponseCode();
	        serverResponseMessage = conn.getResponseMessage();	        
	        System.out.println("HTTP Response is : " + 
	        					serverResponseCode + " " + serverResponseMessage);	   
        	if(DeBug.DEBUG) {
        		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "update image response : "+serverResponseCode);
        	}
	        
//	        //-- get body content
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine).append(System.getProperty("line.separator"));
			}
			in.close();
			DeBug.e(MBPlaceSubmitUtil.ADD_TAG,"respone body : "+response.toString());
//			System.out.println(response.toString());
	        	
	        conn.disconnect();

	        if(serverResponseCode == 200){
	        	if(DeBug.DEBUG) {
	        		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "update image success");
	        	}
	        	if(null != mListener)
	        		mListener.submitSuccessd();
	        } else {
	        	if(DeBug.DEBUG) {
	        		DeBug.e(MBPlaceSubmitUtil.ADD_TAG, "update image failed");
	        	}
	        	if(null != mListener)
	        		mListener.submitFailed();
	        }

		} catch (Exception e) {
			if(e != null) {
//				DeBug.e(MBPlaceSubmitUtil.ADD_TAG,e.getMessage());
				e.printStackTrace();
			} else {
				DeBug.e(MBPlaceSubmitUtil.ADD_TAG,"update load failed");
			}
		} finally{
	        //-- close the streams
			if (null != dos) {
				try {
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
	        }	
		}
	}
}

