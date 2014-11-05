package com.SrivatsanPoddar.helpp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneActivity extends Activity
{
    
    private String companyID;
    private Call thisCall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        
        MyPhoneListener phoneListener = new MyPhoneListener();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        
        telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
        Bundle extras = this.getIntent().getExtras();
        String numberToCall = "tel:" + extras.getString("phone_number");

        thisCall = (Call) extras.getSerializable("thisCall");
        companyID = thisCall.company_id;
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(numberToCall));
        startActivity(callIntent);
        
        
    }
    
    private class MyPhoneListener extends PhoneStateListener {
        
        private boolean onCall = false;
        
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //Phone is ringing
                    Style.makeToast(PhoneActivity.this, incomingNumber + " is calling...");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Call is either dialing, active or on hold
                    Style.makeToast(PhoneActivity.this, "Making a Call");
                    onCall = true;
//                    Intent chatIntent = new Intent(PhoneActivity.this, ChatActivity.class);
//                    PhoneActivity.this.startActivity(chatIntent);
                    break;
                    
                case TelephonyManager.CALL_STATE_IDLE:
                    //Occurs when class is initialized and when the phone call ends
                    
                    if (onCall) {
                        Style.makeToast(PhoneActivity.this, "Phone Call Ended");
                        
                        Intent intent = new Intent(PhoneActivity.this, SurveyActivity.class);


                        intent.putExtra("company_id", PhoneActivity.this.companyID);
                        intent.putExtra("thisCall", PhoneActivity.this.thisCall);
                        PhoneActivity.this.startActivity(intent);
                        
                        onCall = false;
                    }
                    break;
                default:
                    break;
            }
                
        }
        
    }
}
