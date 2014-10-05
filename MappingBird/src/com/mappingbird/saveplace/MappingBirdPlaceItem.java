package com.mappingbird.saveplace;

import com.mappingbird.api.Location;


public class MappingBirdPlaceItem  {
	public static final int TYPE_PLACE 		= 0;
	public static final int TYPE_SUGGEST 	= 1;
	public static final int TYPE_THIS_PLACE	= 2;
	
	public static final int TYPE_NUMBER = 3;
	
	private String mName = "";
	private String mAddress = "";
	private Location mLocation;
	
	public MappingBirdPlaceItem(String name, String address) {
		
	}

}