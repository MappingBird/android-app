package com.mpbd.saveplace.services;


public class MBPlaceSubmitUtil {
	public static final String ADD_TAG = "AddPlace";
	public static final int RESULT_OK 		= 0x0000;
	public static final int RESULT_FAILED 	= 0x0001;

	// Submit Place state
    public static final int SUBMIT_STATE_WAIT = 0;
    public static final int SUBMIT_STATE_PLACE_FAILED = 1;
    public static final int SUBMIT_STATE_PLACE_READY = 2;
    public static final int SUBMIT_STATE_PHOTO_FAILED = 3;
    public static final int SUBMIT_STATE_PHOTO_READY = 4;
    public static final int SUBMIT_STATE_FINISHED = 10;
    public static final int SUBMIT_STATE_CANCEL = 11;


	
	// Submit Image File state
	public static final int SUBMIT_IMAGE_STATE_WAIT 	= 0;
	public static final int SUBMIT_IMAGE_STATE_FAILED 		= 1;
	public static final int SUBMIT_IMAGE_STATE_FINISHED 	= 5;
}

