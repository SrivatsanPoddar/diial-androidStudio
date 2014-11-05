package com.SrivatsanPoddar.helpp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Piyush on 9/10/2014.
 */
public class LogListActivity extends Activity implements Callback<Calls>, ListView.OnItemClickListener {

    private Calls calls;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_list_activity);

        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        Bundle extras = this.getIntent().getExtras();

        String device_id = extras.getString("device_id");

        // Get the nodes from Heroku
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint("http://safe-hollows-9286.herokuapp.com")
                .build();
        HerokuService nodeService = restAdapter.create(HerokuService.class);
        Map<String, String> options = new HashMap<String,String>();
        options.put("device_id", device_id);
        nodeService.getCallLog(options,this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, ParentNodeActivity.class);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

        @Override
        public void success(Calls myCalls, Response response) {
            calls = myCalls;
            ListView stored_info_list = (ListView) findViewById(R.id.call_log_list);
            LogListAdapter<Call> adapter = new LogListAdapter<Call>(this, android.R.layout.simple_list_item_1, calls.Calls.toArray(new Call[0]));
            stored_info_list.setAdapter(adapter);
            stored_info_list.setOnItemClickListener(this);
            Log.e("Successfully retrieved call log", calls.toString());
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e("Failed to retrieve call log", retrofitError.getLocalizedMessage().toString());
        }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        Log.e("Call Log Item Selected", "Woo");
        Intent intent = new Intent(this, LogActivity.class);

        intent.putExtra("thisCall",calls.Calls.get(position));
        startActivity(intent);


    }
}
