package com.damianhinch.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.damianhinch.sunshine.data.FetchWeatherTask;
import com.novoda.notils.caster.Views;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    public static final int NUM_DAYS = 7;
    public static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private ListView listView;
    private ArrayAdapter<String> forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        forecastAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.list_item_forecast,
                        R.id.list_item_forecast_text_view,
                        new ArrayList<String>());
        setUpListView();
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

}