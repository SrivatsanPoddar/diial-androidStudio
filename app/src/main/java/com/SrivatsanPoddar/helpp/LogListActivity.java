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

/*
 * Displays a list of all past calls to access previous call logs
 */
public class LogListActivity extends Activity implements Callback<Calls>, ListView.OnItemClickListener {

    //Activity variables
    private Calls calls;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_list_activity);

        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        //Get intent data
        Bundle extras = this.getIntent().getExtras();
        String device_id = extras.getString("device_id");

        // Retrieve past call list
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
    public void success(Calls myCalls, Response response) { //Successfully retrieved call list

        calls = myCalls;

        ListView stored_info_list = (ListView) findViewById(R.id.call_log_list);
        LogListAdapter<Call> adapter = new LogListAdapter<Call>(this, android.R.layout.simple_list_item_1, calls.Calls.toArray(new Call[0]));
        stored_info_list.setAdapter(adapter);
        stored_info_list.setOnItemClickListener(this);

        Log.e("Retrieved call log", calls.toString());
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.e("Cant get call log", retrofitError.getLocalizedMessage().toString());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Log.e("Call Log Item Selected", "Woo");

        //Go to the Log corresponding to the tapped call
        Intent intent = new Intent(this, LogActivity.class);
        intent.putExtra("thisCall",calls.Calls.get(position));
        startActivity(intent);
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
}
