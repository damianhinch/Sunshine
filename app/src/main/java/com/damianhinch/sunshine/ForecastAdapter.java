package com.damianhinch.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.novoda.notils.caster.Views;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    Context context;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        this.context = context;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Helpers.isMetric(mContext);
        String highLowStr = Helpers.formatTemperature(high, isMetric) + "/" + Helpers.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Helpers.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        setIcon(view, cursor);
        setDate(view, cursor);
        setWeatherDescription(view, cursor);
        setMaxTemperature(view, context, cursor);
        setMinTemperature(view, context, cursor);
    }

    private void setMinTemperature(final View view, final Context context, final Cursor cursor) {
        boolean isMetric = Helpers.isMetric(context);
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = (TextView) view.findViewById(R.id.weather_list_view_item_min_temp);
        lowView.setText(Helpers.formatTemperature(low, isMetric));
    }

    private void setMaxTemperature(final View view, final Context context, final Cursor cursor) {
        boolean isMetric = Helpers.isMetric(context);
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = (TextView) view.findViewById(R.id.weather_list_view_item_max_temp);
        highView.setText(Helpers.formatTemperature(high, isMetric));
    }

    private void setWeatherDescription(final View view, final Cursor cursor) {
        String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView textViewForecastString = (TextView) view.findViewById(R.id.weather_list_view_item_weather_condition);
        textViewForecastString.setText(forecast);
    }

    private void setDate(final View view, final Cursor cursor) {
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        TextView textViewDate = (TextView) view.findViewById(R.id.weather_list_view_item_date);
        textViewDate.setText(Helpers.formatDate(date));
    }

    private void setIcon(final View view, final Cursor cursor) {
        // todo select the correct image to show depending on the weather
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        ImageView iconView = (ImageView) view.findViewById(R.id.weather_list_view_item_icon);
        iconView.setImageResource(R.drawable.ic_launcher);
    }
}