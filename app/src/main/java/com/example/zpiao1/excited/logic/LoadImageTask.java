package com.example.zpiao1.excited.logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.example.zpiao1.excited.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zpiao on 11/1/2016.
 */

public class LoadImageTask extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = LoadImageTask.class.getSimpleName();

    private ImageView mImageView;
    private String mUrl;
    private Bitmap mBitmap;

    public LoadImageTask(ImageView imageView, String url) {
        mImageView = imageView;
        mUrl = url;
        mBitmap = null;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (TextUtils.isEmpty(mUrl)) {
            Log.d(LOG_TAG, "Url is empty, use empty image instead");
            return null;
        }
        try {
            URL url = new URL(mUrl);
            mBitmap = BitmapFactory.decodeStream(url.openStream());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL of image: " + mUrl, e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error in converting InputStream to Bitmap", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mBitmap == null)
            mImageView.setImageResource(R.mipmap.image_not_available);
        else
            mImageView.setImageBitmap(mBitmap);
    }
}
