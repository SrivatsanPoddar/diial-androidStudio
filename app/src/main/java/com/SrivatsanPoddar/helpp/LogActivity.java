package com.SrivatsanPoddar.helpp;

import com.SrivatsanPoddar.helpp.util.SystemUiHider;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Activity to show the call log information following a given call
 */
public class LogActivity extends Activity {

    //Activity components
    private Call thisCall;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        //Get intent data
        Bundle extras = this.getIntent().getExtras();
        thisCall = (Call) extras.getSerializable("thisCall");

        //Retrieve components
        TextView company_name = (TextView) findViewById(R.id.company_name_log);
        TextView date_log = (TextView) findViewById(R.id.date_log);
        TextView duration_log = (TextView) findViewById(R.id.duration_log);
        TextView call_path_log = (TextView) findViewById(R.id.call_path_log);
        TextView call_log_title = (TextView) findViewById(R.id.call_log_title);
        TextView saved_info_label = (TextView) findViewById(R.id.saved_info_log);

        //Style components
        Style.toOpenSans(this,company_name,"light");
        Style.toOpenSans(this,date_log,"light");
        Style.toOpenSans(this,duration_log,"light");
        Style.toOpenSans(this,call_path_log,"light");
        Style.toOpenSans(this,call_log_title,"bold");
        Style.toOpenSans(this,saved_info_label,"bold");

        this.setBoldLabel(company_name, thisCall.company_name);
        this.setBoldLabel(call_path_log, thisCall.call_path_string);

        //Format the times for the start of this call
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        SimpleDateFormat dfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dfUTC.setTimeZone(UTC);

        SimpleDateFormat dfLocal = new SimpleDateFormat("EEEE, MMM d, yyyy",Locale.US);
        dfLocal.setTimeZone(TimeZone.getDefault());

        try { //Format start time
            Date stringDate = dfUTC.parse(thisCall.start_time);
            String nowAsCurrentTimezone = dfLocal.format(stringDate);
            this.setBoldLabel(date_log, nowAsCurrentTimezone);
        }
         catch (ParseException e) {
            Log.e("Date parsing error!",e.toString());
            e.printStackTrace();
        }

        try { //Format duration
            long millis = dfUTC.parse(thisCall.end_time).getTime() - dfUTC.parse(thisCall.start_time).getTime();
            String duration = String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
            this.setBoldLabel(duration_log, duration);
        }
        catch (ParseException e) {
            Log.e("Duration parsing error!",e.toString());
            e.printStackTrace();
        }

        //Display the stored information from this call--appt times, images, etc. if exists
        ListView stored_info_list = (ListView) findViewById(R.id.stored_info_list);
        if (thisCall.stored_information != null && thisCall.stored_information.size() > 0) {
            LogListAdapter<String> adapter = new LogListAdapter<String>(this, android.R.layout.simple_list_item_1, thisCall.stored_information.toArray(new String[0]));
            stored_info_list.setAdapter(adapter);
        }
        else {
            saved_info_label.setVisibility(View.GONE);
        }
    }

    public void toSearch(View v) { //Called when button is pressed. Goes back to Parent search activity
        Style.makeToast(this, "Thanks for your time!");
        Intent intent = new Intent(this, ParentNodeActivity.class);
        startActivity(intent);
    }

    /*
     * Sets the label to bold
     */
    public void setBoldLabel(TextView view, String info) {
        final SpannableStringBuilder sb = new SpannableStringBuilder(view.getText() + " " + info);
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, view.getText().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
        view.setText(sb);
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
