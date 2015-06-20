package me.declangao.yawa.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.declangao.yawa.util.Config;
import me.declangao.yawa.model.Day;
import me.declangao.yawa.adaptor.DayListAdaptor;
import me.declangao.yawa.R;
import me.declangao.yawa.util.Util;


public class WeatherActivity extends ActionBarActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = WeatherActivity.class.getSimpleName();
    //private String mCity;
    private ProgressDialog progressDialog;
    private ListView listView;
    private DayListAdaptor dayListAdaptor;
    private List<Day> dayList = new ArrayList<Day>();
    // Keep track of pending requests
    private int pendingRequests = 0;

    // UI widgets for current weather
    private NetworkImageView currentIcon;
    private TextView tvCurrentDesc;
    private TextView tvCurrentTemp;
    private TextView tvCurrentHumidity;
    private TextView tvCurrentSunrise;
    private TextView tvCurrentSunset;

    // Collapsible search menu item
    private MenuItem searchMenuItem;

    private TextView tvHint;
    private RelativeLayout currentWeatherContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialise and setup the ListView
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        dayListAdaptor = new DayListAdaptor(this, dayList);
        listView.setAdapter(dayListAdaptor);

        currentIcon = (NetworkImageView) findViewById(R.id.current_icon);
        tvCurrentDesc = (TextView) findViewById(R.id.tv_current_desc);
        tvCurrentTemp = (TextView) findViewById(R.id.tv_current_temp);
        tvCurrentHumidity = (TextView) findViewById(R.id.tv_current_humidity);
        tvCurrentSunrise = (TextView) findViewById(R.id.tv_current_sunrise);
        tvCurrentSunset = (TextView) findViewById(R.id.tv_current_sunset);

        tvHint = (TextView) findViewById(R.id.tv_hint);
        currentWeatherContainer = (RelativeLayout) findViewById(R.id.current_weather_container);
        //Intent i = getIntent();
        //mCity = i.getStringExtra("city");
        //loadForecast(mCity);
    }

    /**
     * Download current weather and 7-day forecast data for a specific city
     * @param city name of the city
     */
    private void loadForecast(String city) {
        Log.d(TAG, "Loading weather - " + city);

        String currentWeatherUrl = Config.CURRENT_URL + city;
        String forecastUrl = Config.FORECAST_URL + city;

        // Clear the ListView so we don't have leftover data for previous cities
        dayList.clear();
        dayListAdaptor.notifyDataSetChanged();

        // Display a ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.progress_loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // Create 7-day weather forecast request
        JsonObjectRequest forecastRequest = new JsonObjectRequest(Request.Method.GET,
                forecastUrl,
                null,
                // Received result from server
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        // Subtract 1 from pending request count since the request is finished
                        pendingRequests--;
                        dismissProgressDialog(); // Dismiss the ProgressDialog

                        try {
                            // Code 200, all good
                            if ("200".equals(jsonObject.getString("cod"))) {
                                // Parse JSON data
                                JSONObject city = jsonObject.getJSONObject("city");

                                //getSupportActionBar().setTitle(city.getString("name") + ", "
                                //        + city.getString("country"));
                                // Set ActionBar title
                                getSupportActionBar().setTitle(
                                        "".equals(city.getString("name").trim())?
                                        city.getString("country"):
                                        city.getString("name") + ", " + city.getString("country")
                                );

                                JSONArray list = jsonObject.getJSONArray("list");
                                // Go through each day
                                for (int i = 0; i < list.length(); i++) {
                                    JSONObject obj = list.getJSONObject(i);

                                    // Temp day object to hold data
                                    Day day = new Day();
                                    // Set date
                                    day.setDate(Util.timestampToDate(obj.getLong("dt")));
                                    JSONObject weather = obj.getJSONArray("weather")
                                            .getJSONObject(0);
                                    // Set long description
                                    day.setDescription(weather.getString("description")
                                            .toUpperCase());
                                    // Set icon URL
                                    day.setIconUrl(Config.WEATHER_ICON_PREFIX +
                                            weather.getString("icon") + ".png");
                                    JSONObject temp = obj.getJSONObject("temp");
                                    // Set max and min temperature
                                    day.setMax(temp.getDouble("max"));
                                    day.setMin(temp.getDouble("min"));
                                    // Set wind speed
                                    day.setWindSpeed(obj.getDouble("speed"));
                                    // Set humidity
                                    day.setHumidity(obj.getInt("humidity"));
                                    // Set air pressure
                                    day.setPressure(obj.getDouble("pressure"));
                                    // Set clouds
                                    day.setClouds(obj.getInt("clouds"));

                                    // Add the temp day object to the list
                                    dayList.add(day);
                                }
                            } // City not found
                            else if ("404".equals(jsonObject.getString("cod"))) {
                                Log.d(TAG, "---------- City not found");
                                showErrorMessage(getString(R.string.error),
                                        getString(R.string.error_city_not_found));
                                // Set current weather invisible in case previous result
                                // is still showing.
                                currentWeatherContainer.setVisibility(View.INVISIBLE);
                                // Set ActionBar title to default value
                                getSupportActionBar().setTitle(getString(R.string.app_name));
                            }
                            else { // Unknown error
                                showErrorMessage(getString(R.string.error_unknown),
                                        getString(R.string.error_code_msg) +
                                        jsonObject.getString("cod"));
                            }
                        } catch (JSONException e) {
                            // JSON exception
                            e.printStackTrace();
                            showErrorMessage(getString(R.string.error_json),
                                    getString(R.string.error_json_msg));
                        }
                        // Notify the adaptor of data change
                        dayListAdaptor.notifyDataSetChanged();
                        // Go back to top of the list in case user scrolls to some other position
                        // in previous search
                        listView.setSelection(0);
                    }
                },
                // Oops, something went wrong
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // Error still means request finished
                        pendingRequests--;
                        dismissProgressDialog();
                        Log.d(TAG, "---------- VolleyError");
                        volleyError.printStackTrace();
                        showErrorMessage(getString(R.string.error_network),
                                getString(R.string.error_network_forecast));
                    }
                });

        pendingRequests++; // Add 1 to pending request count
        // Add the request to RequestQueue
        AppController.getInstance().addToRequestQueue(forecastRequest, TAG);

        // Create the current weather request
        JsonObjectRequest currentWeatherRequest = new JsonObjectRequest(Request.Method.GET,
                currentWeatherUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        pendingRequests--;
                        dismissProgressDialog();

                        try {
                            if ("200".equals(jsonObject.getString("cod"))) {
                                currentWeatherContainer.setVisibility(View.VISIBLE);
                                // Parse JSON data
                                JSONObject weather = jsonObject.getJSONArray("weather")
                                        .getJSONObject(0);
                                currentIcon.setImageUrl(Config.WEATHER_ICON_PREFIX +
                                                weather.getString("icon") + ".png",
                                        AppController.getInstance().getImageLoader());
                                tvCurrentDesc.setText(weather.getString("description")
                                        .toUpperCase());
                                JSONObject main = jsonObject.getJSONObject("main");
                                tvCurrentTemp.setText(getString(R.string.temperature) +
                                        Util.formatTemperature(main.getDouble("temp")) + "°C");
                                tvCurrentHumidity.setText(getString(R.string.humidity) +
                                        main.getInt("humidity") + "%");
                                JSONObject sys = jsonObject.getJSONObject("sys");
                                tvCurrentSunrise.setText(getString(R.string.sunrise) +
                                        Util.timestampToTimeOfTheDay(sys.getLong("sunrise")));
                                tvCurrentSunset.setText(getString(R.string.sunset) +
                                        Util.timestampToTimeOfTheDay(sys.getLong("sunset")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showErrorMessage(getString(R.string.error_json),
                                    getString(R.string.error_json_msg));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        pendingRequests--;
                        dismissProgressDialog();
                        Log.d(TAG, "---------- VolleyError");
                        volleyError.printStackTrace();
                        showErrorMessage(getString(R.string.error_network),
                                getString(R.string.error_network_current));
                    }
                });
        pendingRequests++;
        AppController.getInstance().addToRequestQueue(currentWeatherRequest, TAG);
    }

    /**
     * Use AlertDialog with custom View to display detailed weather data for a specific day
     * @param title title for the dialog
     * @param iconUrl URL for weather icon
     * @param text detailed weather info
     */
    private void showWeatherDetails(String title, String iconUrl, String text) {
        // Create an AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(WeatherActivity.this).create();
        alertDialog.setTitle(title); // Set title
        //alertDialog.setMessage(text);
        // Create and setup a custom View
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.weather_details, null);
        TextView tvWeatherDetailsText = (TextView) v.findViewById(R.id.tv_weather_details_text);
        NetworkImageView weatherDetailsIcon =
                (NetworkImageView) v.findViewById(R.id.weather_details_icon);
        tvWeatherDetailsText.setText(text);
        weatherDetailsIcon.setImageUrl(iconUrl, AppController.getInstance().getImageLoader());

        alertDialog.setView(v); // Display the View in the AlertDialog
        // Set a button
        alertDialog.setButton(getString(R.string.got_it), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        alertDialog.show(); // Show the AlertDialog
    }

    /**
     * Show an error dialog
     * @param title title of the error dialog
     * @param msg message of the error
     */
    private void showErrorMessage(String title, String msg) {
        // Create and setup the AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(WeatherActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);

        // Create a collapsible search menu item
        // @see http://developer.android.com/guide/topics/ui/actionbar.html#ActionView
        searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setQueryHint(getString(R.string.hint_placeholder));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Collapse the search view when user hits enter
                searchMenuItem.collapseActionView();
                // Start downloading weather data
                loadForecast(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) { // Lose focus
                    searchMenuItem.collapseActionView(); // Collapse search view
                } else { // Has focus
                    tvHint.setVisibility(View.INVISIBLE); // Hide the hint message
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == android.R.id.home) {
        //    finish();
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel all requests if any
        AppController.getInstance().getRequestQueue().cancelAll(TAG);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Get selected day
        Day day = dayList.get(position);
        Log.d(TAG, "Selected " + day.getDate());
        // Get weather data for that day
        String title = day.getDescription().toUpperCase() + "\n";
        String text = getString(R.string.max) + day.getMax() + "°C\t" +
                getString(R.string.min) + day.getMin() + "°C\n";
        text += getString(R.string.wind_speed) + day.getWindSpeed() + "m/s\t" +
                getString(R.string.humidity) + day.getHumidity() + "%\n";
        text += getString(R.string.pressure) + day.getPressure() + "hpa\t" +
                getString(R.string.clouds) + day.getClouds() + "%\n";
        String iconUrl = day.getIconUrl();
        // Show the weather data
        showWeatherDetails(title, iconUrl, text);
    }

    /**
     * Dismiss the ProgressDialog
     */
    private void dismissProgressDialog() {
        // So we don't dismiss a ProgressDialog that has already been dismissed
        if (progressDialog.isShowing() && pendingRequests == 0) {
            progressDialog.dismiss();
        }
    }
}
