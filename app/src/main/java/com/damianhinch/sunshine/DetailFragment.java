package com.damianhinch.sunshine;

import com.damianhinch.sunshine.data.WeatherContract;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    public static final String DETAIL_URI = "URI";

    private ShareActionProvider mShareActionProvider;
    private String mForecast;


    private TextView dateDateTextView;
    private TextView dateMonthWithDayTextView;
    private TextView temperatureMaxTextView;
    private TextView temperatureMinTextView;
    private ImageView conditionImage;
    private TextView humidityTextView;
    private TextView windTextView;
    private TextView pressureTextView;


    private static final int DETAIL_LOADER = 0;

    private Uri uri;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." +
                    WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_PRESSURE = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            uri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_view, container, false);
        dateDateTextView = (TextView) rootView.findViewById(R.id.detail_date_day);
        dateMonthWithDayTextView = (TextView) rootView.findViewById(R.id.detail_date_month_and_day);
        temperatureMaxTextView = (TextView) rootView.findViewById(R.id.detail_temp_max);
        temperatureMinTextView = (TextView) rootView.findViewById(R.id.detail_temp_min);
        humidityTextView = (TextView) rootView.findViewById(R.id.detail_humidity);
        windTextView = (TextView) rootView.findViewById(R.id.detail_wind);
        pressureTextView = (TextView) rootView.findViewById(R.id.detail_pressure);
        conditionImage = (ImageView) rootView.findViewById(R.id.detail_weather_image);
        return rootView;
    }

//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {     // Called after onCreateView
//        super.onViewCreated(view, savedInstanceState);
//
//        dateDateTextView = (TextView) view.findViewById(R.id.detail_date_day);
//        dateMonthWithDayTextView = (TextView) view.findViewById(R.id.detail_date_month_and_day);
//        temperatureMaxTextView = (TextView) view.findViewById(R.id.detail_temp_max);
//        temperatureMinTextView = (TextView) view.findViewById(R.id.detail_temp_min);
//        humidityTextView = (TextView) view.findViewById(R.id.detail_humidity);
//        windTextView = (TextView) view.findViewById(R.id.detail_wind);
//        pressureTextView = (TextView) view.findViewById(R.id.detail_pressure);
//        conditionImage = (ImageView) view.findViewById(R.id.detail_weather_image);
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != uri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    uri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }

        String dateString = Helpers.formatDate(
                data.getLong(COL_WEATHER_DATE));

        String weatherDescription =
                data.getString(COL_WEATHER_DESC);

        boolean isMetric = Helpers.isMetric(getActivity());

        String high = Helpers.formatTemperature(getActivity(),
                data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Helpers.formatTemperature(getActivity(),
                data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        String humidity = data.getString(COL_WEATHER_HUMIDITY);
        String wind = data.getString(COL_WEATHER_WIND_SPEED);
        String pressure = data.getString(COL_WEATHER_PRESSURE);
        int weatherId = data.getInt(COL_WEATHER_ID);

        mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        dateMonthWithDayTextView.setText(dateString);
        temperatureMaxTextView.setText(high);
        temperatureMinTextView.setText(low);
        humidityTextView.setText(humidityTextView.getText() + " " + humidity);
        windTextView.setText(windTextView.getText() + " " + wind);
        pressureTextView.setText(pressureTextView.getText() + " " + pressure);
        conditionImage.setImageResource(Helpers.getArtResourceForWeatherCondition(500));


        TextView detailTextView = (TextView) getView().findViewById(R.id.detail_date_day);
        detailTextView.setText(mForecast);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
