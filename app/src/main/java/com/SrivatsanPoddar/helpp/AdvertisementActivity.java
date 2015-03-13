package com.SrivatsanPoddar.helpp;

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

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/*
 * Activity to show an advertisement for the company that was called
 */
public class AdvertisementActivity extends Activity implements Callback<StringResponse> {

    private Call thisCall;
    private ImageView adImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove title bars
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_advertisement);

        //Get intent data
        Bundle extras = this.getIntent().getExtras();
        thisCall = (Call) extras.getSerializable("thisCall");

        //Retrieve Ad URL
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint("http://safe-hollows-9286.herokuapp.com")
                .build();
        HerokuService ui = restAdapter.create(HerokuService.class);
        ui.getAdImageURL(thisCall.company_id, this);

        //Retrieve components
        adImageView = (ImageView) findViewById(R.id.ad_image_view);

        //Set listeners
        adImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //Move on to Log
                Intent intent = new Intent(AdvertisementActivity.this, LogActivity.class);
                intent.putExtra("thisCall",thisCall);
                startActivity(intent);
            }
        });
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
        if (ad_url.response.equals("NO_ADS")) { //If no ad, then move on to Log
            Intent intent = new Intent(AdvertisementActivity.this, LogActivity.class);
            intent.putExtra("thisCall",thisCall);
            startActivity(intent);
        }
        else {
            Picasso.with(this.getApplicationContext()).load(ad_url.response).into(adImageView); //Set imageView to show ad
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.advertisement, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
