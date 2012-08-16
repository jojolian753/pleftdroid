/*
 * Copyright (C) 2011, 2012 Riccardo Massera, r.massera@thecoder4.eu
 * 
 * This file is part of PleftDroid.
 * 
 * PleftDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PleftDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PleftDroid.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.thecoder4.gpl.pleftdroid;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This is the Class that manages the sqlite DB
 * that stores the Appointments of the user.
 * 
 */
public class PleftDroidDbAdapter {

	// Table Columns
	public static final String COL_ID = "_id";
	public static final String COL_APTID = "aid";
    public static final String COL_DESC = "desc";
    
    public static final String COL_ROLE = "role";
    public static final String COL_STATUS = "status";
    
    public static final String COL_PSERVER = "pserver";
    public static final String COL_USER = "user";
    public static final String COL_VCODE = "vcode";
    
    // Role column values
    public static final String R_INVITOR = "Invitor";
    public static final String R_INVITEE = "Invitee";
    // Status column Values
    public static final String S_CREATED = "Created";
    public static final String S_VERIFIED = "Verified";
    public static final String S_RECEIVED = "Received";
    public static final String S_VOTED = "Voted";
    public static final String S_DELETED = "DELETED";
    

    private static final String TAG = "PleftDroidDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DB_NAME = "data";
    private static final String DB_TABLE = "pleftdroid";
    private static final int DATABASE_VERSION = 1;
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table "+DB_TABLE+" (" +
        "_id integer primary key autoincrement, aid integer not null, desc text not null," +
        "role text not null, status text not null, pserver text, user text, vcode text" +
        ");";

    private final Context mCtx;
	private boolean isDBopen=false;

    private static class DatabaseHelper extends SQLiteOpenHelper {
    	private static String DB_PATH = "/data/data/eu.thecoder4.gpl.pleftdroid/databases/";
    	private SQLiteDatabase myDataBase;
    	private final Context aContext;

        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
            this.aContext=context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.i(TAG, "ONCREATE: "+DATABASE_CREATE);

            try {
				db.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE);
            onCreate(db);
        }
        
        /**
         * Creates a empty database on the system and rewrites it with your own database.
         * */
        public void createDataBase() throws IOException{
     
        	boolean dbExist = checkDataBase();
     
        	if(dbExist){
        		//do nothing - database already exist
        	}else{
     
        		//By calling this method and empty database will be created into the default system path
                   //of your application so we are gonna be able to overwrite that database with our database.
            	this.getReadableDatabase();
     
            	try {
     
        			copyDataBase();
     
        		} catch (IOException e) {
     
            		throw new Error("Error copying database");
     
            	}
        	}
     
        }
     
        /**
         * Check if the database already exist to avoid re-copying the file each time you open the application.
         * @return true if it exists, false if it doesn't
         */
        private boolean checkDataBase(){
     
        	SQLiteDatabase checkDB = null;
     
        	try{
        		String myPath = DB_PATH + DB_NAME;
        		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     
        	}catch(SQLiteException e){
     
        		//database does't exist yet.
     
        	}
     
        	if(checkDB != null){
     
        		checkDB.close();
     
        	}
     
        	return checkDB != null ? true : false;
        }
     
        /**
         * Copies your database from your local assets-folder to the just created empty database in the
         * system folder, from where it can be accessed and handled.
         * This is done by transfering bytestream.
         * */
        private void copyDataBase() throws IOException{
     
        	//Open your local db as the input stream
        	InputStream myInput = aContext.getAssets().open(DB_NAME);
     
        	// Path to the just created empty db
        	String outFileName = DB_PATH + DB_NAME;
     
        	//Open the empty db as the output stream
        	OutputStream myOutput = new FileOutputStream(outFileName);
     
        	//transfer bytes from the inputfile to the outputfile
        	byte[] buffer = new byte[1024];
        	int length;
        	while ((length = myInput.read(buffer))>0){
        		myOutput.write(buffer, 0, length);
        	}
     
        	//Close the streams
        	myOutput.flush();
        	myOutput.close();
        	myInput.close();
     
        }
     
        public void openDataBase() throws SQLException{
     
        	//Open the database
            String myPath = DB_PATH + DB_NAME;
        	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     
        }
     
        @Override
    	public synchronized void close() {
     
        	    if(myDataBase != null)
        		    myDataBase.close();
     
        	    super.close();
     
    	}
    	
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public PleftDroidDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the PleftDroid database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public PleftDroidDbAdapter open() throws SQLException {
    	if(mDbHelper==null) {
    	  mDbHelper = new DatabaseHelper(mCtx);
    	}
    	this.isDBopen=true;

    	///
    	try {

    		mDbHelper.createDataBase();

    	} catch (IOException ioe) {
    		this.isDBopen= false;

    		throw new Error("Unable to create database");

    	}

    	try {

    		mDbHelper.openDataBase();

    	}catch(SQLException sqle){
    		this.isDBopen= false;

    		throw sqle;

    	}
    	///

    	try {
    	mDb = mDbHelper.getWritableDatabase();
    	}catch(SQLException sqle){
    		this.isDBopen= false;

    		throw sqle;

    	}
    	return this;
    }

    public void close() {
        mDbHelper.close();
        mDbHelper=null;
    }
    
    /**
     * Return a Cursor over the list of all Appointments in the database
     * with data for the List
     * 
     * @return Cursor over all Appointments
     */
    public Cursor fetchAllAppointmentsList() {
    	String sortOrder = COL_ID + " DESC";
        return mDb.query(DB_TABLE, new String[] {COL_ID, COL_DESC,
                COL_ROLE, COL_STATUS, COL_PSERVER, COL_USER},
                null, null, null, null, sortOrder);
    }
    
    /**
     * Return a Cursor over the Appointment detail in the database
     * with data for the List
     * 
     * @return Cursor over all Appointments
     */
    public Cursor fetchAppointmentDetails(int aid) {
    	String selection = COL_APTID +" ='"+aid+"'";

        return mDb.query(DB_TABLE, new String[] {COL_ID, COL_DESC, COL_ROLE, COL_PSERVER, COL_USER,COL_VCODE},
                selection, null, null, null, null);
    }
    
    public int getAid(long id) {
    	int aid=0;
    	String selection = COL_ID +" ='"+id+"'";
    	Cursor c=mDb.query(DB_TABLE, new String[] {COL_ID,COL_APTID},
    			selection, null, null, null, null);
    	while (c.moveToNext()) {
    		aid = c.getInt(c.getColumnIndex(COL_APTID));
    	}
    	c.close();
    	
    	return aid;
    }
    public int getId(long aid) {
    	int id=0;
    	//Log.i("DbA:getId: aid=",""+aid);
    	String selection = COL_APTID +" ='"+aid+"'";
    	Cursor c=mDb.query(DB_TABLE, new String[] {COL_ID,COL_APTID},
    			selection, null, null, null, null);
    	while (c.moveToNext()) {
    		id = c.getInt(c.getColumnIndex(COL_ID));
    		Log.i("DbA:getId: the id=",""+id);
    	}
    	c.close();
    	
    	return id;
    }
    public String getRole(long aid) {
    	String role=null;
    	//Log.i("DbA:getId: aid=",""+aid);
    	String selection = COL_APTID +" ='"+aid+"'";
    	Cursor c=mDb.query(DB_TABLE, new String[] {COL_ID,COL_ROLE},
    			selection, null, null, null, null);
    	while (c.moveToNext()) {
    		role = c.getString(c.getColumnIndex(COL_ROLE));
    		//Log.i("DbA:getId: the id=",""+id);
    	}
    	c.close();
    	
    	return role;
    }


    /**
     * Create a new Appointment using the desc and body provided. If the row is
     * successfully created return the new rowId for that Appointment, otherwise return
     * a -1 to indicate failure.
     * 
     * @param 
     * @param 
     * @param 
     * @param 
     * @return rowId or -1 if failed
     */
    public long createAppointmentAsInvitor(long aid, String desc, String pserver, String user) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_APTID, aid);
        initialValues.put(COL_DESC, desc);
        
        initialValues.put(COL_ROLE, R_INVITOR);
        initialValues.put(COL_STATUS, S_CREATED);
        
        initialValues.put(COL_PSERVER, pserver);
        initialValues.put(COL_USER, user);
        initialValues.put(COL_VCODE, "");

        return mDb.insert(DB_TABLE, null, initialValues);
    }
    
    /**
     * Create a new Appointment using the desc and body provided. If the row is
     * successfully created return the new rowId for that Appointment, otherwise return
     * a -1 to indicate failure.
     * 
     * @param 
     * @param 
     * @param 
     * @param 
     * @return rowId or -1 if failed
     */
    public long createAppointmentAsInvitee(long aid, String desc, String pserver, String user, String vcode) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_APTID, aid);
        initialValues.put(COL_DESC, desc);
        
        initialValues.put(COL_ROLE, R_INVITEE);
        initialValues.put(COL_STATUS, S_RECEIVED);
        
        initialValues.put(COL_PSERVER, pserver);
        initialValues.put(COL_USER, user);
        initialValues.put(COL_VCODE, vcode);

        return mDb.insert(DB_TABLE, null, initialValues);
    }
    
    /**
     * Update the appointment using the details provided.
     * The appointment to be updated is specified using the aid.
     * 
     * @param aid id of appointment to update
     * @param vcode Verification Code to update
     * @return true if the update is successful, false otherwise
     */
    public boolean setStatusVerified(int aid, String vcode) {
        ContentValues args = new ContentValues();
        long aptid = (long) aid;
        args.put(COL_APTID, aptid);
        args.put(COL_STATUS, S_VERIFIED);
        args.put(COL_VCODE, vcode);
        
        return mDb.update(DB_TABLE, args, COL_APTID + " = 0 ", null) > 0;
    }
    
    /**
     * Update the appointment using the details provided.
     * The appointment to be updated is specified using the aid.
     * 
     * @param aid id of appointment to update
     * @param vcode Verification Code to update
     * @return true if the update is successful, false otherwise
     */
    public boolean setStatusVoted(int aid) {
        ContentValues args = new ContentValues();
        args.put(COL_STATUS, S_VOTED);

        return mDb.update(DB_TABLE, args, COL_APTID + "=" + aid, null) > 0;
    }
    
    /**
     * Update the appointment using the details provided.
     * The appointment to be updated is specified using the aid.
     * 
     * @param aid id of appointment to update
     * @param vcode Verification Code to update
     * @return true if the update is successful, false otherwise
     */
    public boolean updateVcode(int aid, String vcode) {
        ContentValues args = new ContentValues();
        args.put(COL_VCODE, vcode);

        return mDb.update(DB_TABLE, args, COL_APTID + "=" + aid, null) > 0;
    }
    public boolean updateDesc(int aid, String desc) {
        ContentValues args = new ContentValues();
        args.put(COL_DESC, desc);

        return mDb.update(DB_TABLE, args, COL_APTID + "=" + aid, null) > 0;
    }

    /**
     * Delete the appointment with the given rowId
     * 
     * @param rowId id of appointment to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteAppointment(long rowId) {

        return mDb.delete(DB_TABLE, COL_ID + "=" + rowId, null) > 0;
    }
    
    public boolean isDBopen() {
    	return isDBopen;
    }

}
