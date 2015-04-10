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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.novoda.notils.logger.simple.Log;

import static com.damianhinch.sunshine.data.WeatherContract.WeatherEntry;
import static com.damianhinch.sunshine.data.WeatherContract.LocationEntry;


/**
 * Manages a local database for weather data.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE = getWeatherTableSql();
        final String SQL_CREATE_LOCATION_TABLE = getLocationTableSql();

        Log.v("Sql", SQL_CREATE_WEATHER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
        Log.v("Sql", SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);

    }

    private String getLocationTableSql() {
        // There must be a better way using arrays just to cycle through
        // Names in one array, corresponding conditions in another
        return "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL " +
                " );";
    }

    private String getWeatherTableSql() {
        return "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                    WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    WeatherEntry.COLUMN_LOC_KEY     + " INTEGER NOT NULL, " +
                    WeatherEntry.COLUMN_DATE        + " INTEGER NOT NULL, " +
                    WeatherEntry.COLUMN_SHORT_DESC  + " TEXT NOT NULL, "    +
                    WeatherEntry.COLUMN_WEATHER_ID  + " INTEGER NOT NULL,"  +
                    WeatherEntry.COLUMN_MIN_TEMP    + " REAL NOT NULL, "    +
                    WeatherEntry.COLUMN_MAX_TEMP    + " REAL NOT NULL, "    +
                    WeatherEntry.COLUMN_HUMIDITY    + " REAL NOT NULL, "    +
                    WeatherEntry.COLUMN_PRESSURE    + " REAL NOT NULL, "    +
                    WeatherEntry.COLUMN_WIND_SPEED  + " REAL NOT NULL, "    +
                    WeatherEntry.COLUMN_DEGREES     + " REAL NOT NULL, "    +
                    " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                    LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +
                    " UNIQUE (" + WeatherEntry.COLUMN_DATE + ", " +
                    WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
