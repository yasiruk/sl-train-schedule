package com.aselalee.trainschedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBDataAccess extends SQLiteOpenHelper {

	/* public DBDataAccess(Context context, String name, CursorFactory factory,
			int version) { */
	public DBDataAccess(Context context) {
		super(context, Constants.DB_NAME, null, Constants.DB_VER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Constants.TABLE_HIS + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ Constants.COL_START_STATION_TXT + " TEXT,"
        		+ Constants.COL_START_STATION_VAL + " TEXT,"
        		+ Constants.COL_END_STATION_TXT + " TEXT,"
        		+ Constants.COL_END_STATION_VAL + " TEXT,"
        		+ Constants.COL_START_TIME + " TEXT,"
        		+ Constants.COL_END_TIME + " TEXT"
        		+ ");");
        db.execSQL("CREATE TABLE " + Constants.TABLE_FAV + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ Constants.COL_START_STATION_TXT + " TEXT,"
        		+ Constants.COL_START_STATION_VAL + " TEXT,"
        		+ Constants.COL_END_STATION_TXT + " TEXT,"
        		+ Constants.COL_END_STATION_VAL + " TEXT,"
        		+ Constants.COL_START_TIME + " TEXT,"
        		+ Constants.COL_END_TIME + " TEXT"
        		+ ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(Constants.LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
	}
	
	public boolean PushDataHistory(String start_st_txt, String start_st_val,
							String end_st_txt, String end_st_val,
							String start_time, String end_time ) {
		SQLiteDatabase db = null;
		Cursor myCur = null;
		long rowID = -1;
		
		db = this.getWritableDatabase();
		if( db == null ) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return false;
		}

		ContentValues keyValPairs = new ContentValues(6);
		keyValPairs.put(Constants.COL_START_STATION_TXT, start_st_txt);
		keyValPairs.put(Constants.COL_START_STATION_VAL, start_st_val);
		keyValPairs.put(Constants.COL_END_STATION_TXT, end_st_txt);
		keyValPairs.put(Constants.COL_END_STATION_VAL, end_st_val);
		keyValPairs.put(Constants.COL_START_TIME, start_time);
		keyValPairs.put(Constants.COL_END_TIME, end_time);
		db.beginTransaction();
		try {
			if( db.insert(Constants.TABLE_HIS, null, keyValPairs) < 0 ) {
				Log.e(Constants.LOG_TAG, "Error writing to DB");
				db.close();
				return false;
			}
			db.setTransactionSuccessful();
		}catch ( Exception e) {
			Log.e(Constants.LOG_TAG, "Error pushing data to DB" + e);
			return false;
		} finally {
			db.endTransaction();
		}
		try {
			myCur = db.query(Constants.TABLE_HIS, new String [] {"_ID"}, null, null, null, null, null);
			if( myCur == null) {
				Log.e(Constants.LOG_TAG, "Select operation failed");
				db.close();
				return false;
			}
		} catch ( Exception e) {
			Log.e(Constants.LOG_TAG, "Error pushing data to DB" + e);
			return false;
		}
		for( int i = myCur.getCount(); i > Constants.MAX_HIS_COUNT; i--) {
			try {
				myCur.moveToFirst();
				rowID = myCur.getLong(0);
				if( rowID < 0 ) {
					db.close();
					myCur.close();
					Log.e(Constants.LOG_TAG, "Errornous row ID value= " + rowID);
					return false;
				}
				db.beginTransaction();
				int delCount = db.delete(Constants.TABLE_HIS, "WHERE _ID = " + rowID, null);
				if( delCount <= 0) {
					Log.w(Constants.LOG_TAG, "Extra row not deleted...");
				}
				db.setTransactionSuccessful();
			} catch (Exception e) {
				myCur.close();
				db.close();
				Log.e(Constants.LOG_TAG, "Error Deleting a row");
				return false;
			}finally {
				db.endTransaction();
			}
		}
		myCur.close();
		db.close();
		return true;
	}

}