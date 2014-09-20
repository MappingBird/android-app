/*
 * GameDataBase.java
 * Copyright (c) 2013 Rolltech
 *
 * Licensed under ...
 *
 */
package com.mappingbird.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ConcurrentModificationException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
/**
 * This is for yDoc to filter this class out. So this class will not show in Javadoc.
 * Please add this comment in your class to filter out it.
 * @y.exclude
 */
public class BitmapDataBase {
	private static final String TAG = BitmapDataBase.class.getName();

	private static final int TYPE_ICON = 2;

	private static final String DATABASE_NAME = "loop_image.db";

	private static final String DATABASE_LB_ICON_TABLE = "leaderboard_icon_table";

	private static final int DATABASE_VERSION = 1;

	// common
	protected static final String KEY_ROWID = "_id";
	
	protected static final String KEY_ICON_URL = "iconurl";
	protected static final String KEY_ICON_BYTEARRAY = "iconbytearray";

	// collection
	protected static final String KEY_LEADERBORD = "imagedb";

	private static final String DATABASE_LB_ICON_CREATE = "CREATE TABLE " + DATABASE_LB_ICON_TABLE + "(" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_ICON_BYTEARRAY + " BLOB," + KEY_ICON_URL + " TEXT"
			+ ");";

	private static final String DATABASE_LB_ICON_COLUMNS[] = new String[] { KEY_ROWID, KEY_ICON_BYTEARRAY, KEY_ICON_URL };

	private Context mContext = null;

	public static class DatabaseHelper extends SQLiteOpenHelper {

		private static DatabaseHelper helper = null;

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		public static synchronized DatabaseHelper getInstance(Context context) {
			if (helper == null) {
				helper = new DatabaseHelper(context);
			}
			return helper;
		}

		@Override
		public void onCreate(SQLiteDatabase mDb) {
			mDb.execSQL(DATABASE_LB_ICON_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase mDb, int oldVersion, int newVersion) {
			mDb.execSQL("DROP TABLE IF EXISTS " + DATABASE_LB_ICON_TABLE);
			onCreate(mDb);
		}
	}

	BitmapDataBase(Context context) {
		if (context == null)
			throw new NullPointerException();
		mContext = context;
	}

	DatabaseHelper getDBHelper() {
		return DatabaseHelper.getInstance(mContext);
	}

	protected void deleteTable(SQLiteDatabase db, int type) {
		String table = null;
		if (type == TYPE_ICON) {
			table = DATABASE_LB_ICON_TABLE;
		}
		if (db != null)
			db.execSQL("DROP TABLE IF EXISTS " + table);
	}

	protected void deleteDatabase() {
		if (mContext != null)
			mContext.deleteDatabase(DATABASE_NAME);
	}

	// =======================================
	protected Cursor getAll(SQLiteDatabase db, int type) {
		String table = null;
		String[] columns = null;
		Cursor cursor = null;
		if (type == TYPE_ICON) {
			table = DATABASE_LB_ICON_TABLE;
			columns = DATABASE_LB_ICON_COLUMNS;
		}

		if (db != null) {
			cursor = db.query(table, /* Table */
					columns, /* Column */
					null, /* Selection */
					null, /* Selection Args */
					null, /* Group By */
					null, /* Having */
					KEY_ROWID); /* Order By */
			if (cursor != null) {
				cursor.moveToFirst();
			}
		}
		return cursor;
	}
	
	protected int deleteAll(SQLiteDatabase db, int type) {
		String table = null;
		int rsp = -1;
		if (type == TYPE_ICON) {
			table = DATABASE_LB_ICON_TABLE;
		}
		if (db != null && table != null)
			db.delete(table, /* Table */
					null, /* Selection */
					null /* Selection Args */
			);
		return rsp;
	}
	
	// ================================

	protected long deleteById(SQLiteDatabase db, int type, int id) {
		String table = null;
		long rsp = -1;
		if (type == TYPE_ICON) {
			table = DATABASE_LB_ICON_TABLE;
		}
		if (db != null)
			rsp = db.delete(table, KEY_ROWID + "=" + id, null);
		return rsp;
	}

	protected long updateByURL(SQLiteDatabase db, int type, byte[] object, String url) {
		String table = null;
		long rsp = -1;

		ContentValues args = new ContentValues();
		if (type == TYPE_ICON) {
			table = DATABASE_LB_ICON_TABLE;
		}
		args.put(KEY_ICON_BYTEARRAY, object);
		if (db != null)
			rsp = db.update(table, args, KEY_ICON_URL + "=" + url, null);
		return rsp;
	}

	protected long insert(SQLiteDatabase db, int type, byte[] object, String url) {
		String table = null;
		long rsp = -1;

		ContentValues args = new ContentValues();
		if (type == TYPE_ICON) {
			table = DATABASE_LB_ICON_TABLE;
		}
		args.put(KEY_ICON_BYTEARRAY, object);
		args.put(KEY_ICON_URL, url);

		if (db != null)
			rsp = db.insert(table, null, args);
		return rsp;
	}

	synchronized boolean hasBitmapInDB(DatabaseHelper helper, String url) {
		boolean hasFile = false;
		Cursor cursor = null;
		SQLiteDatabase db = null;
		try {
			if (helper != null) {
				db = helper.getWritableDatabase();
				if (db != null) {
					cursor = getByteArrayByURL(db, TYPE_ICON, url);
					DeBug.i(TAG, "[get]url = " + url+", size = "+cursor.getCount());
					if(cursor.getCount() > 0)
						hasFile = true;
				}
			}
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
		return hasFile;
	}

	protected Cursor getByteArrayByURL(SQLiteDatabase db, int type, String url) {
		String table = null;
		String[] columns = null;
		Cursor cursor = null;
		if (type == TYPE_ICON) {
			table = DATABASE_LB_ICON_TABLE;
			columns = DATABASE_LB_ICON_COLUMNS;
		}

		if (db != null && table != null) {
			cursor = db.query(table, /* Table */
					columns, /* Column */
					KEY_ICON_URL + "=?", /* Selection */
					new String[] { url }, /* Selection Args */
					null, /* Group By */
					null, /* Having */
					null); /* Order By */
			if (cursor != null) {
				cursor.moveToFirst();
			}
		}
		return cursor;
	}

	synchronized Bitmap getBitmapByUrl(DatabaseHelper helper, String url) {
		Bitmap iconbitmap = null;
		byte[] lbbytearray = null;
		Cursor cursor = null;
		SQLiteDatabase db = null;
		try {
			if (helper != null) {
				db = helper.getWritableDatabase();
				if (db != null) {
					cursor = getByteArrayByURL(db, TYPE_ICON, url);
					DeBug.i(TAG, "[get]url = " + url);
					if (cursor != null && cursor.moveToFirst()) {
						lbbytearray = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_ICON_BYTEARRAY));
					}
				}
			}
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
		DeBug.i(TAG, "get lb icon, byte array:" + lbbytearray);
		if (lbbytearray != null)
			iconbitmap = BytesToBimap(lbbytearray);
		return iconbitmap;
	}

	synchronized void setBitmapByUrl(DatabaseHelper helper, Bitmap bitmap, String url) {
		byte[] blaBytes = BitmapToBytes(bitmap);
		setBitmapByUrl(helper, blaBytes, url);
	}

	synchronized void setBitmapByUrl(DatabaseHelper helper, byte[] blaBytes, String url) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		Cursor cursorurl = null;
		int existid = -1;
		
		try {
			db = helper.getWritableDatabase();
			if (helper != null) {
				if (db != null) {
					//check over 100 items
					cursor = getAll(db, TYPE_ICON);
					int size = 0;
					if (cursor != null && cursor.moveToFirst()) {
						int firstindex = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
						DeBug.i(TAG, "[LB]first index:" + firstindex);
						do {
							size++;
							if (size > 100) {
								deleteById(db, TYPE_ICON, firstindex);
							}
						} while (cursor.moveToNext());
					}
					//check this item exist and delete
					cursorurl = getByteArrayByURL(db, TYPE_ICON, url);
					if (cursorurl != null && cursorurl.moveToFirst()) {
						existid = cursorurl.getInt(cursorurl.getColumnIndexOrThrow("_id"));
					}
					DeBug.i(TAG, "[LB]exist id= " + existid);
					if (existid > -1)
						deleteById(db, TYPE_ICON, existid);
					
					DeBug.i(TAG, "[LB]url = " + url);
					DeBug.i(TAG, "size = " + size);
					long id = insert(db, TYPE_ICON, blaBytes, url);
					if (id >= 0) {
						DeBug.i(TAG, "add leaderboard icon success, id =" + id);
					} else {
						DeBug.i(TAG, "add leaderboard fail");
					}
				}
			}
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				try {
					db.close();
				} catch (ConcurrentModificationException e) {
				}
			}
		}

	}

	// ===== set and get byte array =====
	public static Object deserializeObject(byte[] b) {
		try {
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
			Object object = in.readObject();
			in.close();

			return object;
		} catch (ClassNotFoundException cnfe) {
			DeBug.e(TAG, "[deserialize]class not found error", cnfe);

			return null;
		} catch (IOException ioe) {
			DeBug.e(TAG, "[deserialize]io error", ioe);

			return null;
		}
	}

	public static byte[] serializeObject(Object o) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(o);
			out.close();
			// Get the bytes of the serialized object
			byte[] buf = bos.toByteArray();

			return buf;
		} catch (IOException ioe) {
			DeBug.e(TAG, "[serialize]error", ioe);
			return null;
		}
	}

	private byte[] BitmapToBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	private Bitmap BytesToBimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}
}