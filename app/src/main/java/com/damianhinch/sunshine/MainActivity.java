package com.damianhinch.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends ActionBarActivity {

    public static final int NUM_DAYS = 7;
    public static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String userPreferredLocation = getUserPreferredLocation();
        populateListViewWithWeatherData(userPreferredLocation);
    }

    private String getUserPreferredLocation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString(getString(R.string.preference_location), getString(R.string.preference_default_value_location));
    }

    private void setUpListView() {
        listView = (ListView) findViewById(R.id.weather_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final String text = listView.getItemAtPosition(position).toString();
                startDetailView(text);
            }
        });
    }

    private AsyncTask<String, Void, String[]> populateListViewWithWeatherData(final String location) {
        return new FetchWeatherAsyncTask().execute(location);
    }

    private void startDetailView(final String text) {
        Intent launchDetailView = new Intent(this, DetailView.class);
        launchDetailView.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(launchDetailView);
    }

    private class FetchWeatherAsyncTask extends AsyncTask<String, Void, String[]> {

        // onPreExecute is run in th main thread and can be used to set up interface stuff like a loading bar

        @Override
        protected String[] doInBackground(final String... params) { // Run immediately after onPreExecute and gets given the parameters for the task
            return fetchWeather(params);
        }

        private String[] fetchWeather(String[] params) {
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String postalCode = params[0];
            final String apiCall = getUri(postalCode, NUM_DAYS);

            forecastJsonStr = getForecastJsonString(apiCall);

            try {
                return getWeatherDataFromJson(forecastJsonStr, NUM_DAYS);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                return null;
            }

        }

        private void closeReader(final BufferedReader reader) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("DetailViewFragment", "Error closing stream", e);
                }
            }
        }

        private void closeUrlConnection(final HttpURLConnection urlConnection) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        private String getForecastJsonString(final String apiCall) {
            // These two need to be declared outside the try/catch so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(apiCall);
                Log.v("API call", apiCall);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect(); // This would throw an Exception (NetworkOnMainThread) so this needs to be done as an AsyncTask
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                readBuffer(reader, buffer);

                if (buffer.length() == 0) {
                    return null;
                }
                return buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString(), e);
                return null;
            } finally {
                closeUrlConnection(urlConnection);
                closeReader(reader);
            }
        }

        private void readBuffer(final BufferedReader reader, final StringBuffer buffer) throws IOException {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
        }

        private String getUri(final String postCode, int numDays) {

            Uri.Builder builder = new Uri.Builder();
            buildUri(postCode, numDays, builder);
            return builder.build().toString();
        }

        private void buildUri(final String postCode, final int numDays, final Uri.Builder builder) {
            builder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data")
                    .appendPath("2.5")
                    .appendPath("forecast")
                    .appendPath("daily")
                    .appendQueryParameter("q", postCode)
                    .appendQueryParameter("mode", "json")
                    .appendQueryParameter("unit", "metric")
                    .appendQueryParameter("cnt", String.valueOf(numDays));
        }

        @Override
        protected void onPostExecute(final String[] forecastJsonStr) {
            super.onPostExecute(forecastJsonStr);
            // Adapter - bind raw data to ListView - requires a ListView item, and what to but there
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
                    R.layout.list_item_forecast,
                    R.id.list_item_forecast_text_view,
                    forecastJsonStr);
            // ListView - Display it
            listView.setAdapter(arrayAdapter);
        }
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
            final String userPreferedLocation = getUserPreferredLocation();
            populateListViewWithWeatherData(userPreferedLocation);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        final String OWM_LIST = "list";
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        Time dayTime = new Time();
        dayTime.setToNow();

        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        dayTime = new Time();

        return getWeatherDataStringsFrom(numDays, weatherArray, dayTime, julianStartDay);

    }

    private String[] getWeatherDataStringsFrom(final int numDays, final JSONArray weatherArray, final Time dayTime, final int julianStartDay) throws JSONException {

        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";
        String[] resultStrings = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            String day;
            String description;
            String highAndLow;

            JSONObject dayForecast = weatherArray.getJSONObject(i);

            long dateTime;
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = Helpers.getReadableDateString(dateTime);

            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = Helpers.formatHighLows(high, low);
            resultStrings[i] = day + " - " + description + " - " + highAndLow;
        }
        return resultStrings;
    }
}
