package com.damianhinch.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novoda.notils.logger.simple.Log;

public class DetailView extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailViewFragment())
                    .commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intentToOpenSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(intentToOpenSettingsActivity);
        }

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DetailViewFragment extends Fragment {
        private final String FORECAST_SHARE_HASHTAG = "#SunShine";

        private String forecastString;

        private TextView textView;
        private String LOG_TAG = "DetailViewFragment";

        public DetailViewFragment() {
            setHasOptionsMenu(true); // calls onCreateOptions
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail_view, container, false);

            textView = (TextView) rootView.findViewById(R.id.detail_view_text_view_date_day);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                setTextViewWithExtraText(intent);
                forecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(R.menu.detailfragment, menu);
            MenuItem menuItem = menu.findItem(R.id.action_share);
            ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }

        }

        private void setTextViewWithExtraText(final Intent intent) {
            String stringExtra = intent.getStringExtra(Intent.EXTRA_TEXT);
            textView.setText(stringExtra);
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);  // non-deprecated: FLAG_ACTIVITY_NEW_DOCUMENT but requires min sdk=21
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, forecastString + " " + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

    }
}
