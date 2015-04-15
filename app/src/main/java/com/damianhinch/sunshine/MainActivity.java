package com.damianhinch.sunshine;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.damianhinch.sunshine.data.WeatherContract;
import com.novoda.notils.caster.Views;

public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private ListView listView;

    private static final int FORECAST_LOADER = 0;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    private ForecastAdapter forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        forecastAdapter = new ForecastAdapter(this, null, 0);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWeather();
    }

    private void setUpListView() {
        listView = Views.findById(this, R.id.weather_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final String text = listView.getItemAtPosition(position).toString();
                startDetailView(text);
            }
        });
    }


    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(this);
        String location = Helpers.getUserPreferredLocation(this);
        weatherTask.execute(location);
    }

    private void startDetailView(final String text) {
        Intent launchDetailView = new Intent(this, DetailView.class);
        launchDetailView.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(launchDetailView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intentToOpenSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(intentToOpenSettingsActivity);
        }
        if (id == R.id.refresh_button) {
            updateWeather();
            return true;
        }
        if (id == R.id.set_preferred_location) {
            openPreferredLocationOnMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationOnMap() {
        final String location = Helpers.getUserPreferredLocation(this);
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent geoLocationIntent = new Intent(Intent.ACTION_VIEW);
        geoLocationIntent.setData(geoLocation);

        if (geoLocationIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(geoLocationIntent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + location + " ,no maps app to view location");
            Toast.makeText(this, getString(R.string.toast_preferred_location_no_app_installed), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(final int i, final Bundle bundle) {
        String locateSetting = Helpers.getUserPreferredLocation(this);

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + "ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locateSetting, System.currentTimeMillis());

        return new CursorLoader(this, weatherForLocationUri, null, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
        forecastAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> cursorLoader) {

    }
}