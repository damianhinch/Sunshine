package com.damianhinch.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    public static final String URL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
    public static final String POST_CODE = "10247";
    public static final int NUM_DAYS = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Raw data
        List<String> arrayList = Arrays.asList("Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday" ,
                "Saturday",
                "Sunday");
        // Adapter - bind raw data to ListView - requires a ListView item, and what to but there
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                R.layout.list_item_forecast,
                R.id.list_item_forecast_text_view,
                arrayList);
        // ListView - Display it
        ListView listView = (ListView) findViewById(R.id.weather_list_view);
        listView.setAdapter(arrayAdapter);

        new FetchWeatherAsyncTask().execute(POST_CODE);

    }

    private class FetchWeatherAsyncTask extends AsyncTask<String, Void, String> {

        // onPreExecute is run in th main thread and can be used to set up interface stuff like a loading bar

        @Override
        protected String doInBackground(final String... params) { // Run immediately after onPreExecute and gets given the parameters for the task
            return fetchWeather(params);
        }

        private String fetchWeather(String[] params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String postalCode = params[0];

            final String apiCall = getUri(POST_CODE, NUM_DAYS);

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(apiCall);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect(); // This would throw an Exception (NetworkOnMainThread) so this needs to be done as an AsyncTask

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            return forecastJsonStr;
        }

        private String getUri(final String postCode, int numDays) {

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data")
                    .appendPath("2.5")
                    .appendPath("forecast")
                    .appendPath("daily")
                    .appendQueryParameter("q", postCode)
                    .appendQueryParameter("mode", "json")
                    .appendQueryParameter("unit", "metric")
                    .appendQueryParameter("cnt", String.valueOf(numDays))
                    .fragment("section-name");
            return builder.build().toString();
        }

        @Override
        protected void onPostExecute(final String forecastJsonStr) {
            super.onPostExecute(forecastJsonStr);
            if (forecastJsonStr != null) {
                Log.v("Dogs are sad", forecastJsonStr);
            }
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id== R.id.refresh_button){
            new FetchWeatherAsyncTask().execute(POST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


