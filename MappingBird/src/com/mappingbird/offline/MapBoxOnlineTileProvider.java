package com.mappingbird.offline;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.android.gms.maps.model.UrlTileProvider;
import com.mappingbird.common.DeBug;

public class MapBoxOnlineTileProvider extends UrlTileProvider {	    
    private static final String TAG = "MapBoxOnlineTileProvider";
 
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
 
    public MapBoxOnlineTileProvider() {
        super(256, 256);        
    }
 
    // ------------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------------
 
    @Override
    public URL getTileUrl(int x, int y, int z) {
        try {
        	DeBug.i(TAG, String.format(Constants.MAPBOX_URL_FORMAT, Constants.MAPBOX_ID, z, x, y));        	
            return new URL(String.format(Constants.MAPBOX_URL_FORMAT, Constants.MAPBOX_ID, z, x, y));
        }
        catch (MalformedURLException e) {
            return null;
        }
    }
}
