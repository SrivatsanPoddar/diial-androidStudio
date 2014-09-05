package com.SrivatsanPoddar.helpp;

import com.google.gson.Gson;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class TwilioActivity extends Activity implements View.OnClickListener
{
    private TwilioPhone phone;
    private EditText numberField;
    private String company_id;
    private static final WebSocketConnection mConnection = new WebSocketConnection();
    protected static final String TAG = "Twilio Activity";
    private Gson gson = new Gson();
    private String pairsIndex;
    TextView instructionField;
    private Handler mHandler; 
    private LinearLayout variableLayout;
    private Call thisCall;
    ValueAnimator callingAnimation;
    private ArrayList<String> stored_information = new ArrayList<String>(5);

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_twilio);
        
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true); 
        
        Bundle extras = this.getIntent().getExtras();
        company_id = extras.getString("company_id");
        String stringPath = extras.getString("string_path");
        thisCall = (Call) extras.getSerializable("thisCall");
        
        ChatMessage m = new ChatMessage(stringPath);
        m.setTargetCompany(company_id);
        
        String JSONMessage = this.gson.toJson(m);
        //((SearchActivity) getActivity()).mConnection.sendTextMessage(JSONMessage);
        this.start(JSONMessage);
        
        variableLayout = (LinearLayout) findViewById(R.id.variable_layout);
        
        phone = new TwilioPhone(getApplicationContext(), company_id);
        //phone.connect(company_id);
//        ImageButton dialButton = (ImageButton)findViewById(R.id.dialButton);
//        dialButton.setOnClickListener(this);
 
        ImageButton hangupButton = (ImageButton)findViewById(R.id.hangupButton);
        hangupButton.setOnClickListener(this);
        
        ImageButton sendDigit = (ImageButton)findViewById(R.id.send_digit);
        sendDigit.setOnClickListener(this);
        
        //TextView callingIndicator = (TextView) findViewById(R.id.calling_indicator);
        callingAnimation = ObjectAnimator.ofInt(findViewById(R.id.calling_indicator),  "textColor",  Color.rgb(0x00, 0x00, 0x00), Color.rgb(0xB2, 0xBE, 0xFF));
        callingAnimation.setDuration(1000);
        callingAnimation.setRepeatCount(ValueAnimator.INFINITE);
        callingAnimation.setRepeatMode(ValueAnimator.REVERSE);
        callingAnimation.setEvaluator(new ArgbEvaluator());
        
//        AnimatorSet callingAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.calling_blink);
//        callingAnimation.setTarget(callingIndicator);
        callingAnimation.start();
        
// 
//        numberField = (EditText)findViewById(R.id.numberField);
//        instructionField = (TextView) findViewById(R.id.live_instructions);
//        
//        Button sendMessage = (Button) findViewById(R.id.send_message);
//        sendMessage.setOnClickListener(this);
        
    }
 
    @Override
    public void onClick(View view)
    {
//        if (view.getId() == R.id.dialButton)
//            phone.connect(company_id);
//        else 
        if (view.getId() == R.id.hangupButton) {
            phone.disconnect();
            mHandler.removeCallbacks(pingServer);
            mConnection.disconnect();

            Intent intent = new Intent(TwilioActivity.this, SurveyActivity.class);

            thisCall.stored_information = stored_information;
            intent.putExtra("company_id", TwilioActivity.this.company_id);
            intent.putExtra("thisCall", TwilioActivity.this.thisCall);
            TwilioActivity.this.startActivity(intent);
        }

        else if (view.getId() == R.id.send_digit) {
            Log.e("Send digit pushed","woo");
            phone.sendDigit();
        }
//        else if (view.getId() == R.id.send_message) {
//            String messageToSend = numberField.getText().toString();
//            ChatMessage m = new ChatMessage(messageToSend,pairsIndex);
//            String JSONMessage = gson.toJson(m);
//            mConnection.sendTextMessage(JSONMessage);
//        }
    }
    
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(pingServer);
        mConnection.disconnect();
        phone.disconnect();
    }
    
    Runnable pingServer = new Runnable() {
        
        public void run () {
            ChatMessage m = new ChatMessage("Ping from android");
            String JSONMessage = gson.toJson(m);
            mConnection.sendTextMessage(JSONMessage);
            mHandler.postDelayed(pingServer, 30000);
        }
    };
    
    /*
     * Initiates a web-socket connection to send the node path to company
     */
    private void start(final String initialMessage) {

        final String wsuri = "ws://safe-hollows-9286.herokuapp.com/live";

        try {
           mConnection.connect(wsuri, new WebSocketHandler() {

              @Override
              public void onOpen() {
                 Log.d(TAG, "Status: Connected to " + wsuri);
//                 ChatMessage m = new ChatMessage();
//                 m.setString("Hello Bob!");
//                 
//                 String JSONMessage = gson.toJson(m);
                 mConnection.sendTextMessage(initialMessage);
                 Log.e("Sending initial message to target_id of:",  initialMessage);
                 //CLOSE WEB SOCKET
                 //mConnection.disconnect();
                 mHandler = new Handler();
                 pingServer.run();

              }

              @Override
              public void onTextMessage(String payload) {
                 Log.d(TAG, "Got echo: " + payload);
                 ChatMessage m = gson.fromJson(payload, ChatMessage.class);
                 
                 //If just paired, then set pairsIndex
                 if(m.pair != null) {
                     pairsIndex = m.pairsIndex;
                     Log.e("Pairing Request Received with pairsIndex:", pairsIndex + "");
                     phone.connect(company_id); //Make call once the pairing occurs!
                     TextView callingIndicator = (TextView) findViewById(R.id.calling_indicator);
                     callingAnimation.end();
                     callingIndicator.setText("Call in Progress");
                     callingIndicator.setTextColor(Color.rgb(0x00, 0x72, 0x55));
                     
                     long pattern[]={0,200,200,200};
                     //Start the vibration
                     Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                     //start vibration with repeated count, use -1 if you don't want to repeat the vibration
                     vibrator.vibrate(pattern, -1);   
                 }
                 
//                 if(m.message != null) {
//                     instructionField.setText(m.message);
//                 }
                 
                 
                 //If String information was requested from caller, then add UI to give info and pre-fill with Shared Preferences if it exists
                 if (m.request_format != null && m.request_format.equals("edit_text")) {
                                         
                     TwilioActivity.this.addEditText(m);  //Add instructions, text field and button UI
   
                 }
                 //If a link was sent by the caller, then show a clickable link
                 else if (m.request_format != null && m.request_format.equals("link")) {

                         TwilioActivity.this.addLink(m);  //Add instructions, text field and button UI

                 }
                 
                 
              }

              @Override
              public void onClose(int code, String reason) {
                 mHandler.removeCallbacks(pingServer);
                 Log.d(TAG, "Connection lost.");
              }
           });
        } catch (WebSocketException e) {

           Log.d(TAG, e.toString());
        }
     }
    
    //Adds a clickable link to the UI
    public void addLink(ChatMessage m) {


        long pattern[]={0,50,50,50};
        //Start the vibration
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        //start vibration with repeated count, use -1 if you don't want to repeat the vibration
        vibrator.vibrate(pattern, -1);  
        
        String displayText = m.message;  //Extract the link URL
        String linkDescription = m.request_type;  
        if ((linkDescription != null) && !linkDescription.isEmpty()) {
            displayText = linkDescription + ": " + displayText;
        }
        stored_information.add(displayText);
        final SpannableStringBuilder sb = new SpannableStringBuilder(displayText);

        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, linkDescription.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold


        final LinearLayout toAdd = new LinearLayout(TwilioActivity.this);
        toAdd.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams linLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        
        LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        linkParams.setMargins(20, 20, 20, 5);
        TextView linkView = new TextView(TwilioActivity.this);
        Style.toOpenSans(this, linkView, "light");
        linkView.setGravity(Gravity.CENTER_HORIZONTAL);
        
        //linkView.setMovementMethod(LinkMovementMethod.getInstance());
        linkView.setAutoLinkMask(Linkify.ALL);
        linkView.setText(sb);
        toAdd.addView(linkView, linkParams);
        variableLayout.addView(toAdd);
        
    }
    
    public void addEditText(ChatMessage m) {
        
        long pattern[]={0, 150};
        //Start the vibration
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        //start vibration with repeated count, use -1 if you don't want to repeat the vibration
        vibrator.vibrate(pattern, -1);  
        
        //Retrieve shared preferences
        SharedPreferences prefs = TwilioActivity.this.getApplicationContext().getSharedPreferences("com.SrivatsanPoddar.helpp", Context.MODE_PRIVATE);
        final String preferenceKey = "com.SrivatsanPoddar.helpp." + m.request_type;
        String preference = prefs.getString(preferenceKey, "");
        Log.e("Retrieved preference: " + preference + " for preference key: ", preferenceKey);
        
        
        //Log.e("Adding view","WOO");
        final LinearLayout toAdd = new LinearLayout(TwilioActivity.this);
        toAdd.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams linLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        
        LinearLayout.LayoutParams instructionsParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        instructionsParams.setMargins(20, 20, 20, 5);

        TextView instructions = new TextView(TwilioActivity.this);
        Style.toOpenSans(this, instructions, "light");
        instructions.setGravity(Gravity.CENTER_HORIZONTAL);
        
        instructions.setText(m.message);
        toAdd.addView(instructions,instructionsParams);
        
        LinearLayout horizontalLayout = new LinearLayout(TwilioActivity.this);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams horizontalLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
       
        final EditText toEdit = new EditText(TwilioActivity.this);
        Style.toOpenSans(this, toEdit, "light");
        toEdit.setTextSize(13);
        toEdit.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
        editParams.weight = 1;
        toEdit.setHint("type...");
        if (!preference.equals("")) {
            toEdit.setText(preference);
        }
        
        horizontalLayout.addView(toEdit, editParams);
        
        Button send = new Button(TwilioActivity.this);
        Style.toOpenSans(this, send, "light");
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        send.setGravity(Gravity.BOTTOM);
        send.setText("Send");
        horizontalLayout.addView(send, sendParams);
        send.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                if (!toEdit.getText().toString().equals("")) {
                    SharedPreferences prefs = TwilioActivity.this.getSharedPreferences("com.SrivatsanPoddar.helpp", Context.MODE_PRIVATE);
                    Editor editor = prefs.edit();
                    editor.putString(preferenceKey, toEdit.getText().toString());
                    editor.commit();
                    Log.e("Adding preference: " + toEdit.getText().toString() + " for preference key: ", preferenceKey);
                    ChatMessage toSend = new ChatMessage(toEdit.getText().toString(), pairsIndex);

                    String JSONMessage = gson.toJson(toSend);
                    mConnection.sendTextMessage(JSONMessage);
                    
                    //Remove the view!
                    variableLayout.removeView(toAdd);
                    
                }

            }
        });
        
        toAdd.addView(horizontalLayout, horizontalLayoutParams);
        variableLayout.addView(toAdd);
    }
}
