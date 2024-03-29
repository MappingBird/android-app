package com.mappingbird.offline;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.mappingbird.common.DeBug;

public class MapTileDownloaderTask extends AsyncTask<String, Void, JSONArray> {
	static final String API_TILEPATHS = "http://mappingbirds.appspot.com/getMapTilePathZXY?address=%s&zoom=%s";
	static final String API_TILEIMAGE_SRC = "http://api.tiles.mapbox.com/v3/ericyang.hpda7fd5/%s";
	static final String TAG = "MapTileDownloaderTask";
	static final Context context_ = MainActivity.getContext();
		
	
	protected JSONArray doInBackground(String ... params) {
		JSONArray tilePaths = null;	
		
		try {
			for (int zoom = (int) Constants.DEFAULT_MAP_ZOOM; 
					 zoom <= Constants.OFFLINE_MAP_MAX_ZOOM; ++zoom ) {
				//-- get tile download paths from Web
				HttpClient client = new DefaultHttpClient();			
				String uri = String.format(API_TILEPATHS, params[0], zoom);
				HttpGet getRequest = new HttpGet(uri);
				HttpResponse response;			
				response = client.execute(getRequest);
				String resp = EntityUtils.toString(response.getEntity());		
	
				JSONObject json = new JSONObject(resp);
				tilePaths = json.getJSONObject("tile").getJSONArray("paths");
				String formattedAddr = json.getString("formatted_address");
				
				//-- download tile images
				DownloadManager dm = (DownloadManager) context_.getSystemService(Context.DOWNLOAD_SERVICE);			
				for (int i = 0; i < tilePaths.length(); i++) {
					String tileZXY = tilePaths.getString(i);								
					String tileSubPath = String.format("%s/%s/%s", Constants.OFFLINE_TILES_HOME, formattedAddr, tileZXY);
					String tileFullPath = String.format("%s/%s", 
							context_.getExternalFilesDir(null), tileSubPath);
					File tmp_f = new File(tileFullPath);
					if (tmp_f.exists()) continue;
						
					File tileFullPath_f = new File(tileFullPath.substring(0,tileFullPath.lastIndexOf("/")));
					if (! tileFullPath_f.exists()) tileFullPath_f.mkdirs();				
					
					String tileImageUri = String.format(API_TILEIMAGE_SRC, tileZXY);
					Uri tileUri = Uri.parse(tileImageUri);
					DownloadManager.Request req = new DownloadManager.Request(tileUri);
					req.setDestinationInExternalFilesDir(context_, null, tileSubPath);
					dm.enqueue(req);
				}
			}
		} catch (Exception e) {
			java.io.StringWriter sw = new java.io.StringWriter();
			e.printStackTrace(new java.io.PrintWriter(sw));			
			DeBug.e(TAG, sw.toString());
		}
		
		return tilePaths;
	}
	
	protected void onPostExecute(JSONArray tilePaths) {		
		if (null == tilePaths ) return;
		
//		try {			
//			File tilesDir = new File(appFilesDir_, "offline_map_tiles");	    		
//			if (!tilesDir.exists()) tilesDir.mkdir();
//			File cityTilesDir = new File(tilesDir, "Taichung City, Taiwan");
//			if (!cityTilesDir.exists()) cityTilesDir.mkdir();
//									
//			File tile = new File(cityTilesDir, "tile1.tmp");													
//			FileOutputStream fos = new FileOutputStream(tile);					
//			fos.write("hellow string".getBytes());
//			fos.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}

//TODO...
//class DownloadCompleteReceiver extends BroadcastReceiver {  
//    @Override  
//    public void onReceive(Context context, Intent intent) {  
//        if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){  
//            long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);  
//            Log.v(TAG," download complete! id : "+downId);  
//            Toast.makeText(context, intent.getAction()+"id : "+downId, Toast.LENGTH_SHORT).show();  
//        }  
//    }  
//} 

  

