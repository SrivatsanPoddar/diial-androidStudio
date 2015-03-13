package com.SrivatsanPoddar.helpp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/*
 * Activity to handle a standard phone call
 */
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

        //Get intent data
        thisCall = (Call) extras.getSerializable("thisCall");
        companyID = thisCall.company_id;

        //Create and start call
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(numberToCall));
        startActivity(callIntent);
    }

    //Listens for the end of the call and takes back control
    private class MyPhoneListener extends PhoneStateListener {
        
        private boolean onCall = false;
        
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: //Phone is ringing
                    Style.makeToast(PhoneActivity.this, incomingNumber + " is calling...");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Call is either dialing, active or on hold
                    Style.makeToast(PhoneActivity.this, "Making a Call");
                    onCall = true;
                    break;
                case TelephonyManager.CALL_STATE_IDLE: //Occurs when class is initialized and when the phone call ends
                    if (onCall) { //Go to survey
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
