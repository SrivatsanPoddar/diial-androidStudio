package com.SrivatsanPoddar.helpp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.provider.Settings.Secure;

/**
 * Activity that handles distributing and answering of survey questions post-call
 */
public class SurveyActivity extends Activity implements ListView.OnItemClickListener, Callback<ArrayList<SurveyQuestion>>{

    //Activity components
    ListView optionsList;
    TextView questionDisplayText;

    //Activity variables
	ArrayList<SurveyQuestion> questions;
	int currentQuestionsIndex = 0;
	HerokuService ui;
	Call thisCall;
	boolean poorExperience = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        //Retrieve components
		TextView surveyIntro = (TextView) findViewById(R.id.survey_intro);
        optionsList = (ListView)findViewById(R.id.question_options_list);

        //Style components
        Style.toOpenSans(this, surveyIntro, "light");

        //Get intent data
		Bundle extras = this.getIntent().getExtras();
		String company_id = extras.getString("company_id");
		thisCall = (Call) extras.getSerializable("thisCall");

		// Initialize the question list
		questions = new ArrayList<SurveyQuestion>();

		//Make retrofit GET call to '/:store_id/questions' to get survey questions associated with a given store
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint("http://safe-hollows-9286.herokuapp.com")
            .build();
        ui = restAdapter.create(HerokuService.class);
        ui.getQuestions(company_id,this);

        //Set listeners
		optionsList.setOnItemClickListener(this);

        //Set current time as end of call
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        SimpleDateFormat dfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dfUTC.setTimeZone(UTC);
        String end_time = dfUTC.format(new Date());
        thisCall.end_time = end_time;
	}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {

        //Make a retrofit POST call to '/responses/questionID' here, sending questions[currentQuestionIndex].getOptions()[position] as the response!
        SurveyQuestion curQuestion = questions.get(currentQuestionsIndex);
        curQuestion.setResponse(curQuestion.getOptions()[position]);
        curQuestion.setDeviceID(Secure.getString(this.getApplicationContext().getContentResolver(),Secure.ANDROID_ID));
        ui.addResponse(curQuestion, new postResponse());

        //If current question is the default question and the response is negative, then mark for advertisement
        if (curQuestion.getQuestionID().equals("0") && position >= 2) {
            poorExperience = true;
        }

        //If there are more questions, then show next question
        if (currentQuestionsIndex < questions.size() - 1) {
            currentQuestionsIndex++;
            questionDisplayText.setText(questions.get(currentQuestionsIndex).getDisplayText());
            CustomListAdapter<String> adapter = new CustomListAdapter(this,android.R.layout.simple_list_item_1,questions.get(currentQuestionsIndex).getOptions());
            optionsList.setAdapter(adapter);
        }
        else { //If this was the last question, show ad or go to log
            Intent intent;
            if (poorExperience) {
                intent = new Intent(this, AdvertisementActivity.class);
            }
            else {
                intent = new Intent(this, LogActivity.class);
            }

            intent.putExtra("thisCall",thisCall);

            //Post this call to the database (and update with each response to a survey question)
            ui.addCall(thisCall, new postCall());
            startActivity(intent);
        }
    }

    /**
     * The response when posting the answer to a survey question
     */
	private class postResponse implements Callback<String> {

        @Override
        public void failure(RetrofitError err)
        {
            Log.e("Error Posting Response",err.toString());
        }

        @Override
        public void success(String result, Response arg1) {
            Log.e("Posted with result:", result);
            SurveyActivity.this.thisCall.response_ids.add(result);
        }
	}

    /**
     * The response when posting the call
     */
	private class postCall implements Callback<Call> {
	    
        @Override
        public void failure(RetrofitError err) {
            Log.e("Can't post to Call Log",err.toString());
        }

        @Override
        public void success(Call result, Response arg1) {
            Log.e("Call Posted:", "woo");
        }
	}

    @Override
    public void failure(RetrofitError err) {
        Log.e("Can't get Questions", err.toString());
    }

    @Override
    public void success(ArrayList<SurveyQuestion> returnedList, Response res) { //Successfully retrieved questions
        for(SurveyQuestion sq : returnedList) {
            questions.add(sq);
        }
        
        if (questions.size() == 0) { //If there are no questions, go back to main screen
            Style.makeToast(this, "Thanks for the call!");
            Intent intent = new Intent(this, ParentNodeActivity.class);
            ui.addCall(thisCall, new postCall()); //Post the call to the database
            startActivity(intent);
        }
        else { //Set the screen to show the first question and options
            questionDisplayText = (TextView)findViewById(R.id.question_display_text);

            //If 'default' question...
            if (questions.get(currentQuestionsIndex).getQuestionID().equals("0")) {
                questionDisplayText.setText("How likely is it that you would recommend " + thisCall.company_name+" to a friend?");
            }
            else {
                questionDisplayText.setText(questions.get(currentQuestionsIndex).getDisplayText());
            }

            Style.toOpenSans(this, questionDisplayText, "light");
            CustomListAdapter<String> adapter = new CustomListAdapter(this,android.R.layout.simple_list_item_1,questions.get(currentQuestionsIndex).getOptions());
            optionsList.setAdapter(adapter);   
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Style.makeToast(this, "Thanks for your time!");
                Intent intent = new Intent(this, ParentNodeActivity.class);
                ui.addCall(thisCall, new postCall()); //Post the new call to the database (and update with each response to a survey question)
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

