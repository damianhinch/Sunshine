/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.damianhinch.sunshine.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public void testLocationTable() {
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);
        final boolean insertFailed = (locationRowId != -1);
        assertTrue(insertFailed);

        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME, null, null, null, null, null, null, null);

        assertTrue("Error: No records ret`urned from location query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Error: Location query validation failed", cursor, testValues);

        assertFalse("Error: more than one record in location query", cursor.moveToNext());

        cursor.close();
        db.close();
    }

    public void testWeatherTable() {

        final long locationRowId = insertLocation();
        final ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);

        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        long weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
        final boolean insertFailed = (weatherRowId != -1);
        assertTrue(insertFailed);

        Cursor cursor = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME, null, null, null, null, null, null, null);

        assertTrue("Error: No records found", cursor.moveToNext());

        TestUtilities.validateCurrentRecord("Error: resultant row did not match what was entered", cursor, weatherValues);

        cursor.close();
        db.close();
    }


    private long insertLocation() {
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);

        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME, null, null, null, null, null, null, null);

        TestUtilities.validateCurrentRecord("Error: Location query validation failed", cursor, testValues);
        cursor.close();
        db.close();

        return locationRowId;
    }

}
