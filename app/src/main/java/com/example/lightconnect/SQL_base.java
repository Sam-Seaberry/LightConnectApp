package com.example.lightconnect;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.google.android.material.internal.NavigationMenu;

import java.util.ArrayList;
import java.util.List;

public class SQL_base extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "presets_db";

    // below int is our database version
    private static final int DB_VERSION = 2;


    private static final String TABLE_NAME = "presets";


    private static final String PRESET_COL = "preset";


    private static final String RED_COL = "redvalue";


    private static final String GREEN_COL = "greenval";


    private static final String BLUE_COL = "blueval";

    private static final String BRIGHTNESS_COL = "brightval";


    private static final String TABLE_CREATE ="CREATE TABLE presets (preset TEXT, " +
            " redval INTEGER, greenval INTEGER, blueval INTEGER, brightval INTEGER)";


    SQLiteDatabase db;

    // creating a constructor for our database handler.
    public SQL_base(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + " ( "
                + PRESET_COL+" , "
                + RED_COL+" , "
                + GREEN_COL+" , "
                + BLUE_COL+" , "
                + BRIGHTNESS_COL+" )";


        //db.execSQL(TABLE_CREATE);
        db.execSQL(query);
        this.db = db;

    }

    // this method is use to add new preset to the sqlite database.
    public void addNewPreset(String presetname, int red, int blue, int green, int brightness) {


        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();


        values.put(PRESET_COL, presetname);
        values.put(RED_COL, red);
        values.put(GREEN_COL, green);
        values.put(BLUE_COL, blue);
        values.put(BRIGHTNESS_COL, brightness);


        db.insert(TABLE_NAME, null, values);


        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getData(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereclause = null;
        String[] whereargs = null;
        if (category.length() > 0 ) {
            whereclause = PRESET_COL + " LIKE ?";
            whereargs = new String[]{"%" + category + "%"};
        }

        return db.query(TABLE_NAME,null,whereclause,whereargs,null,null,PRESET_COL,null);
        //Cursor res = db.rawQuery("SELECT * FROM "+TABLE_NAME+" ORDER BY "+ PRESET_COL +" DESC ",null);
        //return res;
    }

    public Cursor getPresetMatches(String query, String[] columns) {
        String selection = PRESET_COL + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);

        Cursor cursor = builder.query(getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }


    // to create a list from PRESET_COL to then be used in the spinner
    public List<String> getPresetLabels(){
        List<String> list = new ArrayList<String>();

        // Select All Query
        String selectQuery = " SELECT " + PRESET_COL + " FROM " + TABLE_NAME;


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);//selectQuery,selectedArguments

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));//adding 2nd column data
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();

        // returning lables
        return list;
    }
    public void clearDatabase(String TABLE_NAME) {
        String clearDBQuery = "DELETE FROM "+TABLE_NAME;
        db.execSQL(clearDBQuery);
    }
}