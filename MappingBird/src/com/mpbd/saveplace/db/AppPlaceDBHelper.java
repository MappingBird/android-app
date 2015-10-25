package com.mpbd.saveplace.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mappingbird.common.DeBug;


public class AppPlaceDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "AppPlace.db";

    // Add place table
    public static final String ADD_PLACE_TABLE_NAME 	= "AppPlaceTable";
    public static final String ADD_PLACE_ID 			= "_id";
    public static final String ADD_PLACE_NAME 			= "name";
    public static final String ADD_PLACE_OBJECT 		= "object";
    public static final String ADD_PLACE_STATE 			= "state";
    public static final String ADD_PLACE_PLACE_ID 		= "place_id";
    public static final String ADD_PLACE_CREATE_TIME 	= "create_time";

    public static final String[] ADD_PLACE_TABLE_COLUMNS = { 
    	ADD_PLACE_ID, ADD_PLACE_OBJECT, ADD_PLACE_STATE, ADD_PLACE_PLACE_ID};

    private static final String ADD_PLACE_TABLE_CREATED = "CREATE TABLE IF NOT EXISTS "
            +ADD_PLACE_TABLE_NAME+"( "
            +ADD_PLACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            +ADD_PLACE_NAME+" TEXT, "
            +ADD_PLACE_OBJECT+" BLOB, "
            +ADD_PLACE_STATE+" TEXT, "
            +ADD_PLACE_CREATE_TIME+" TEXT, "
            +ADD_PLACE_PLACE_ID+" TEXT"
            +");";

    // Image table
    public static final String ADD_IMAGE_TABLE_NAME 	= "AppImageTable";
    public static final String ADD_IMAGE_ID 			= "_id";
    public static final String ADD_IMAGE_URL 			= "url";
    public static final String ADD_IMAGE_PLACE_DB_ID	= "place_db_id";
    public static final String ADD_IMAGE_STATE 			= "state";
    public static final String ADD_IMAGE_CREATE_TIME 	= "create_time";

    public static final String[] ADD_IMAGE_TABLE_COLUMNS = { 
    	ADD_IMAGE_ID, ADD_IMAGE_URL, ADD_IMAGE_PLACE_DB_ID, ADD_IMAGE_STATE};

    private static final String ADD_IMAGE_TABLE_CREATED = "CREATE TABLE IF NOT EXISTS "
            +ADD_IMAGE_TABLE_NAME+"( "
            +ADD_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            +ADD_IMAGE_URL+" TEXT, "
            +ADD_IMAGE_PLACE_DB_ID+" TEXT, "
            +ADD_IMAGE_STATE+" TEXT,"
            +ADD_PLACE_CREATE_TIME+" TEXT"
            +");";

    public AppPlaceDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    	DeBug.i("DB", "OrderDBHelper init");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	DeBug.i("DB", "DB onCreate");
    	db.execSQL(ADD_PLACE_TABLE_CREATED);
    	db.execSQL(ADD_IMAGE_TABLE_CREATED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	DeBug.i("DB", "DB onUpgrade");
    	db.execSQL("DELETE FROM " + ADD_PLACE_TABLE_NAME);
    	db.execSQL("DELETE FROM " + ADD_IMAGE_TABLE_NAME);
    	
    	db.execSQL(ADD_PLACE_TABLE_CREATED);
    	db.execSQL(ADD_IMAGE_TABLE_CREATED);
    }
}
