package com.damianhinch.sunshine;

import com.novoda.notils.exception.DeveloperError;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;              // final on primitives means the value can't change
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(final int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Helpers.isMetric(mContext);
        String highLowStr = Helpers.formatTemperature(mContext, high, isMetric) + "/" + Helpers.formatTemperature(mContext, low, isMetric);
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
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId;

        if (viewType == VIEW_TYPE_FUTURE_DAY) {
            layoutId = R.layout.list_item_forecast;
        } else if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forcecast_today;
        } else {
            throw new DeveloperError("New type was added. ");
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        setDate(context, cursor, viewHolder);
        setDescription(cursor, viewHolder);
        setTempMin(context, cursor, viewHolder);
        setTempMax(context, cursor, viewHolder);
        setImage(context, cursor, viewHolder);

    }

    private void setImage(Context context, Cursor cursor, ViewHolder viewHolder) {
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        final int artResourceForWeatherCondition = Helpers.getArtResourceForWeatherCondition(weatherId);
        viewHolder.iconView.setImageResource(artResourceForWeatherCondition);
    }


    private void setTempMax(Context context, Cursor cursor, ViewHolder viewHolder) {
        boolean isMetric = Helpers.isMetric(context);
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Helpers.formatTemperature(context, high, isMetric));
    }

    private void setTempMin(Context context, Cursor cursor, ViewHolder viewHolder) {
        boolean isMetric = Helpers.isMetric(context);
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Helpers.formatTemperature(context, low, isMetric));
    }

    private void setDescription(Cursor cursor, ViewHolder viewHolder) {
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(description);
    }

    private void setDate(Context context, Cursor cursor, ViewHolder viewHolder) {
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Helpers.getFriendlyDayString(context, date));
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            // todo select the correct image to show depending on the weather
            iconView = (ImageView) view.findViewById(R.id.weather_list_view_item_icon);
            dateView = (TextView) view.findViewById(R.id.weather_list_view_item_date);
            descriptionView = (TextView) view.findViewById(R.id.weather_list_view_item_weather_condition);
            highTempView = (TextView) view.findViewById(R.id.weather_list_view_item_max_temp);
            lowTempView = (TextView) view.findViewById(R.id.weather_list_view_item_min_temp);
        }


    }


}
