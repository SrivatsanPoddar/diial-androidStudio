package com.SrivatsanPoddar.helpp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class AdvertisementActivity extends Activity implements Callback<StringResponse> {

    private Call thisCall;
    private ActionBar actionBar;
    private ImageView adImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_advertisement);


        Bundle extras = this.getIntent().getExtras();
        thisCall = (Call) extras.getSerializable("thisCall");

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint("http://safe-hollows-9286.herokuapp.com")
                .build();
        HerokuService ui = restAdapter.create(HerokuService.class);

        ui.getAdImageURL(thisCall.company_id, this);

        adImageView = (ImageView) findViewById(R.id.ad_image_view);

        adImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdvertisementActivity.this, LogActivity.class);
                intent.putExtra("thisCall",thisCall);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.advertisement, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void failure(RetrofitError err)
    {
        Log.e("Error Getting Ad URL", err.toString());
    }

    @Override
    public void success(StringResponse ad_url, Response arg1)
    {
        Log.e("Ad Fetch Successful:", "woo");
        if (ad_url.response.equals("NO_ADS")) {
            Intent intent = new Intent(AdvertisementActivity.this, LogActivity.class);
            intent.putExtra("thisCall",thisCall);
            startActivity(intent);
        }
        else {
            Picasso.with(this.getApplicationContext()).load(ad_url.response).into(adImageView);
        }

    }
}
