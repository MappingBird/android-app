package com.mappingbird.offline;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.mappingbird.common.DeBug;


public class OfflineTileProvider implements TileProvider {	
    private static final String TAG = "OfflineTileProvider";
    
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;
    
    private static final Context context_ = MainActivity.getContext();

///    private AssetManager mAssets;

    public OfflineTileProvider() {
///        mAssets = context_.getResources().getAssets();
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        byte[] image = readTileImage(x, y, zoom);
        return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
    }

    private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;

        try {        	
            in = new BufferedInputStream(new FileInputStream(getTileFilename(x, y, zoom)));
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
        	java.io.StringWriter sw = new java.io.StringWriter(); 
            e.printStackTrace(new java.io.PrintWriter(sw));
            DeBug.e(TAG, sw.toString());
            return null;
        } catch (OutOfMemoryError e) {
        	java.io.StringWriter sw = new java.io.StringWriter();        	
            e.printStackTrace(new java.io.PrintWriter(sw));
            DeBug.e(TAG, sw.toString());
            return null;
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
        }
    }

    private String getTileFilename(int x, int y, int zoom) {
//////TODO...  formattedAddr
String formattedAddr = "Taipei City, Taiwan";
    	String tileZXY = String.format("%s/%s/%s.png", zoom, x, y);    	
		String tileSubPath = String.format("%s/%s/%s", Constants.OFFLINE_TILES_HOME, formattedAddr, tileZXY);
		String tileFullPath = String.format("%s/%s", 
				context_.getExternalFilesDir(null), 
				tileSubPath);
        return tileFullPath;
    }
}
