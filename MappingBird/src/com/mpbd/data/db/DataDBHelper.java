package com.mpbd.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mappingbird.common.DeBug;


public class DataDBHelper extends SQLiteOpenHelper {
	private final static String TAG = "DataDB";

    private static final int DB_VERSION = 4;
    private static final String DB_NAME = "DataDB.db";

    /*
     * Collection List專用DB
     * 只存在一個,  所以只需要一個欄位放Collection物件和更新時間就可以了
     */
    public static final String COLLECTION_LIST_TABLE_NAME = "CollectionListTable";
    public static final String COLLECTION_LIST_OBJECT 		= "OBJECT";
    public static final String COLLECTION_LIST_UPDATE_TIME 	= "UPDATETIME";
    public static final String[] COLLECTION_LIST_TABLE_COLUMNS = { 
    	COLLECTION_LIST_OBJECT, COLLECTION_LIST_UPDATE_TIME};
    
    private static final String COLLECTION_LIST_TABLE_CREATED = "CREATE TABLE IF NOT EXISTS "
            +COLLECTION_LIST_TABLE_NAME+"( "+
            	COLLECTION_LIST_UPDATE_TIME+" INTEGER, "+
            	COLLECTION_LIST_OBJECT+" BLOB"+
                        ");";
    
    /*
     * Collection Item專用DB
     * id : Server上面會有一組ID給我們使用
     * object : 物件
     * update : 更新時間, Server給得值
     */
    public static final String COLLECTION_ITEM_TABLE_NAME = "CollectionItemTable";
    public static final String COLLECTION_ITEM_ID = "id";
    public static final String COLLECTION_ITEM_OBJECT = "OBJECT";
    public static final String COLLECTION_ITEM_UPDATE_TIME = "UPDATETIME";
    public static final String[] COLLECTION_ITEM_TABLE_COLUMNS = { 
    	COLLECTION_ITEM_ID, COLLECTION_ITEM_OBJECT, COLLECTION_ITEM_UPDATE_TIME};
    
    private static final String COLLECTION_ITEM_TABLE_CREATED = "CREATE TABLE IF NOT EXISTS "
            +COLLECTION_ITEM_TABLE_NAME+"( "+
            	COLLECTION_ITEM_ID + " INTEGER, "+
            	COLLECTION_ITEM_OBJECT+" BLOB, "+
            	COLLECTION_ITEM_UPDATE_TIME+" TEXT"+
                        ");";
    
    /*
     * Place Item 專用DB
     * 
     */    
    public static final String PLACE_ITEM_TABLE_NAME = "PlaceItemTable";
    public static final String PLACE_ITEM_ID = "id";
    public static final String PLACE_ITEM_OBJECT = "OBJECT";
    public static final String PLACE_ITEM_UPDATE_TIME = "UPDATETIME";
    public static final String[] PLACE_ITEM_TABLE_COLUMNS = { 
    	PLACE_ITEM_ID, PLACE_ITEM_OBJECT, PLACE_ITEM_UPDATE_TIME};
    
    private static final String PLACE_ITEM_TABLE_CREATED = "CREATE TABLE IF NOT EXISTS "
            +PLACE_ITEM_TABLE_NAME+"( "+
            	PLACE_ITEM_ID + " INTEGER, "+
            	PLACE_ITEM_OBJECT+" BLOB, "+
            	PLACE_ITEM_UPDATE_TIME+" TEXT"+
                        ");";
    
    public DataDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    	DeBug.i(TAG, "DataDBHelper init");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	DeBug.i(TAG, "DB onCreate");
    	db.execSQL(COLLECTION_LIST_TABLE_CREATED);
        db.execSQL(COLLECTION_ITEM_TABLE_CREATED);
        db.execSQL(PLACE_ITEM_TABLE_CREATED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	DeBug.i(TAG, "DB onUpgrade");
    	db.execSQL("DROP TABLE IF EXISTS " + COLLECTION_LIST_TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + COLLECTION_ITEM_TABLE_NAME);
    	db.execSQL("DROP TABLE IF EXISTS " + PLACE_ITEM_TABLE_NAME);
    	
    	db.execSQL(COLLECTION_LIST_TABLE_CREATED);
        db.execSQL(COLLECTION_ITEM_TABLE_CREATED);
        db.execSQL(PLACE_ITEM_TABLE_CREATED);
    }
}
