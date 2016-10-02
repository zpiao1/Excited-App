package com.example.zpiao1.excited;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GetTravelTimeTask extends AsyncTask<LatLng, Void, String[]> {
    private static final Uri QUERY_BASE = Uri.parse("https://maps.googleapis.com/maps/api/directions/json?");
    private static final String QUERY_PARAM_ORIGIN = "origin";
    private static final String QUERY_PARAM_DESTINATION = "destination";
    private static final String QUERY_PARAM_MODE = "mode";
    private static final String QUERY_PARAM_MODE_DRIVING = "driving";
    private static final String QUERY_PARAM_MODE_TRANSIT = "transit";
    private static final String QUERY_PARAM_KEY = "key";
    private static final String JSON_ROUTES = "routes";
    private static final String JSON_LEGS = "legs";
    private static final String JSON_DURATION = "duration";
    private static final String JSON_TEXT = "text";
    private static final String LOG_TAG = GetTravelTimeTask.class.getSimpleName();
    private Activity mActivity;

    public GetTravelTimeTask(Activity activity) {
        super();
        mActivity = activity;
    }

    private static String formatLatLng(LatLng latLng) {
        return String.format("%f,%f", latLng.latitude, latLng.longitude);
    }

    @Override
    protected String[] doInBackground(LatLng... latLngs) {
        if (latLngs.length < 2)
            throw new IllegalArgumentException("Incorrect number of LatLngs");

        String travelDrivingUrlStr = formatRequestUrl(formatLatLng(latLngs[0]),
                formatLatLng(latLngs[1]), QUERY_PARAM_MODE_DRIVING);
        String travelTransitUrlStr = formatRequestUrl(formatLatLng(latLngs[0]),
                formatLatLng(latLngs[1]), QUERY_PARAM_MODE_TRANSIT);

        String drivingJsonStr = makeHttpRequest(travelDrivingUrlStr);
        String transitJsonStr = makeHttpRequest(travelTransitUrlStr);

        String drivingTime = getTravelTimeFromJsonStr(drivingJsonStr);
        String transitTime = getTravelTimeFromJsonStr(transitJsonStr);

        Log.v(LOG_TAG, "drivingTime: " + drivingTime);
        Log.v(LOG_TAG, "transitTime: " + transitTime);

        return new String[]{drivingTime, transitTime};
    }

    private String getDirectionsKey() {
        return mActivity.getString(R.string.google_maps_directions_key);
    }

    private String formatRequestUrl(String origin, String destination, String mode) {
        Uri travelUri = QUERY_BASE.buildUpon()
                .appendQueryParameter(QUERY_PARAM_ORIGIN, origin)
                .appendQueryParameter(QUERY_PARAM_DESTINATION, destination)
                .appendQueryParameter(QUERY_PARAM_MODE, mode)
                .appendQueryParameter(QUERY_PARAM_KEY, getDirectionsKey())
                .build();
        return travelUri.toString();
    }

    private String makeHttpRequest(String requestUrlStr) {
        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        String result = null;
        try {
            URL requestUrl = new URL(requestUrlStr);

            urlConnection = (HttpsURLConnection) requestUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    stringBuilder.append(line).append('\n');
                result = stringBuilder.toString();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "makeHttpRequest ", e);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            try {
                if (reader != null)
                    reader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "makeHttpRequest: closing InputStream and BufferedReader ", e);
            }
        }
        return result;
    }

    private String getTravelTimeFromJsonStr(String travelTimeJsonStr) {
        JSONObject travelTimeJsonObject;
        String result = null;
        try {
            travelTimeJsonObject = new JSONObject(travelTimeJsonStr);
            JSONArray routesArray = travelTimeJsonObject.getJSONArray(JSON_ROUTES);
            JSONObject routeObject = routesArray.getJSONObject(0);
            JSONArray legsArray = routeObject.getJSONArray(JSON_LEGS);
            JSONObject legObject = legsArray.getJSONObject(0);
            JSONObject durationObject = legObject.getJSONObject(JSON_DURATION);
            result = durationObject.getString(JSON_TEXT);
        } catch (JSONException e) {
            Log.v(LOG_TAG, "getTravelTimeFromJsonStr ", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        TextView detailDrivingTimeText = (TextView)
                mActivity.findViewById(R.id.detail_driving_time);
        detailDrivingTimeText.setText(strings[0]);
        TextView detailTransitTimeText = (TextView)
                mActivity.findViewById(R.id.detail_transit_time);
        detailTransitTimeText.setText(strings[1]);
    }
}
