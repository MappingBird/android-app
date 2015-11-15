package com.mappingbird.api;

import java.io.Serializable;
import java.util.ArrayList;

public class MBSharePlaceData implements Serializable {

    private static final String TAG = MBSharePlaceData.class.getName();
    public String title = "";
    public String tags = "";
    public String url = "";
    public String description = "";
    public String placeName = "";
    public String placeAddress = "";
    public String placePhone = "";
    public String placeOpenTime = "";
    public String lat = null;
    public String lng = null;
}
