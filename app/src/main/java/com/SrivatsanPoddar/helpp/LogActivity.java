package com.SrivatsanPoddar.helpp;

import com.SrivatsanPoddar.helpp.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LogActivity extends Activity {

    private Call thisCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        Bundle extras = this.getIntent().getExtras();
        thisCall = (Call) extras.getSerializable("thisCall");


        //Set elements of Call Log entry
        TextView company_name = (TextView) findViewById(R.id.company_name_log);
        TextView date_log = (TextView) findViewById(R.id.date_log);
        TextView duration_log = (TextView) findViewById(R.id.duration_log);
        TextView call_path_log = (TextView) findViewById(R.id.call_path_log);
        TextView call_log_title = (TextView) findViewById(R.id.call_log_title);
        TextView saved_info_label = (TextView) findViewById(R.id.saved_info_log);

        Style.toOpenSans(this,company_name,"light");
        Style.toOpenSans(this,date_log,"light");
        Style.toOpenSans(this,duration_log,"light");
        Style.toOpenSans(this,call_path_log,"light");
        Style.toOpenSans(this,call_log_title,"bold");
        Style.toOpenSans(this,saved_info_label,"bold");

        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold

        final SpannableStringBuilder sb = new SpannableStringBuilder("HELLOO");

        if (thisCall.call_path != null)
            this.setBoldLabel(company_name, thisCall.call_path[0].toString());
        else
            this.setBoldLabel(company_name, thisCall.company_name);

        this.setBoldLabel(call_path_log, thisCall.call_path_string);
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        SimpleDateFormat dfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dfUTC.setTimeZone(UTC);

        SimpleDateFormat dfLocal = new SimpleDateFormat("EEEE, MMM d, yyyy",Locale.US);
        dfLocal.setTimeZone(TimeZone.getDefault());

        try {
            Date stringDate = dfUTC.parse(thisCall.start_time);
            String nowAsCurrentTimezone = dfLocal.format(stringDate);
            this.setBoldLabel(date_log, nowAsCurrentTimezone);
        }
         catch (ParseException e) {
            // TODO Auto-generated catch block
            Log.e("Date parsing error!",e.toString());
            e.printStackTrace();
        }

        try {
            long millis = dfUTC.parse(thisCall.end_time).getTime() - dfUTC.parse(thisCall.start_time).getTime();
            String duration = String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
            this.setBoldLabel(duration_log, duration);
        }
        catch (ParseException e) {
            // TODO Auto-generated catch block
            Log.e("Duration parsing error!",e.toString());
            e.printStackTrace();
        }

        ListView stored_info_list = (ListView) findViewById(R.id.stored_info_list);
        if (thisCall.stored_information != null && thisCall.stored_information.size() > 0) {
            LogListAdapter<String> adapter = new LogListAdapter<String>(this, android.R.layout.simple_list_item_1, thisCall.stored_information.toArray(new String[0]));
            stored_info_list.setAdapter(adapter);
        }
        else {
            saved_info_label.setVisibility(View.GONE);
        }
    }

    public void toSearch(View v) {
        Style.makeToast(this, "Thanks for your time!");
        Intent intent = new Intent(this, SearchActivity.class);
//          Post the new call to the database (and update with each response to a survey question)
        startActivity(intent);
    }

    public void setBoldLabel(TextView view, String info) {
        final SpannableStringBuilder sb = new SpannableStringBuilder(view.getText() + " " + info);

        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, view.getText().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
        view.setText(sb);
    }
}
