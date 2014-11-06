package com.SrivatsanPoddar.helpp;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.twilio.client.Twilio;

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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class TwilioActivity extends Activity implements View.OnClickListener, Camera.AutoFocusCallback
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
    private boolean speakerPhoneOn = false;
    private ImageButton backCameraButton;
    //Snake
    private SnakeView mSnakeView;
    private static final int SWIPE_MIN_DISTANCE = 80;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 150;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    private static String ICICLE_KEY = "snake-view";

    private Camera mCamera = null;
    private CameraPreview mPreview = null;

    private boolean backCameraActive = false;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_twilio);
        
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true); 
        
        Bundle extras = this.getIntent().getExtras();
        thisCall = (Call) extras.getSerializable("thisCall");
        String stringPath = thisCall.call_path_string;
        company_id = thisCall.company_id;
        ChatMessage m = new ChatMessage(stringPath);
        m.setTargetCompany(company_id);
        
        String JSONMessage = this.gson.toJson(m);
        //((SearchActivity) getActivity()).mConnection.sendTextMessage(JSONMessage);
        this.start(JSONMessage);
        
        variableLayout = (LinearLayout) findViewById(R.id.variable_layout);

        backCameraButton = (ImageButton) findViewById(R.id.back_camera_button);
        backCameraButton.setVisibility(View.INVISIBLE);
        backCameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (backCameraActive) {
                    closeCamera();
                }
                else {
                    takeBackPhoto();
                }
            }
        });
        phone = new TwilioPhone(getApplicationContext(), company_id);
        //phone.connect(company_id);
//        ImageButton dialButton = (ImageButton)findViewById(R.id.dialButton);
//        dialButton.setOnClickListener(this);
 
        ImageButton hangupButton = (ImageButton)findViewById(R.id.hangupButton);
        hangupButton.setOnClickListener(this);

//        ImageButton sendDigit = (ImageButton)findViewById(R.id.send_digit);
//        sendDigit.setOnClickListener(this);

        ToggleButton speakerPhone = (ToggleButton) findViewById(R.id.speaker_button);
        speakerPhone.setOnClickListener(this);

        TextView callingIndicator = (TextView) findViewById(R.id.calling_indicator);
        Style.toOpenSans(this, callingIndicator,"light");

        TextView snakeText = (TextView) findViewById(R.id.text);
        Style.toOpenSans(this,snakeText,"light");
        callingAnimation = ObjectAnimator.ofInt(callingIndicator,  "textColor",  Color.rgb(0x00, 0x00, 0x00), Color.rgb(0xC4, 0xD5, 0xE0));
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

       // setContentView(R.layout.snake_layout);

        mSnakeView = (SnakeView) findViewById(R.id.snake);
        mSnakeView.setTextView((TextView) findViewById(R.id.text));

        if (bundle == null) {
            // We were just launched -- set up a new game
            mSnakeView.setMode(SnakeView.READY);
        } else {
            // We are being restored
            Bundle map = bundle.getBundle(ICICLE_KEY);
            if (map != null) {
                mSnakeView.restoreState(map);
            } else {
                mSnakeView.setMode(SnakeView.PAUSE);
            }
        }

        // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };

        //RelativeLayout twilioLayout = (RelativeLayout) findViewById(R.id.twilio_layout);
        mSnakeView.setOnClickListener(this);
        mSnakeView.setOnTouchListener(gestureListener);


        //Set-up custom message sending and saving
        final EditText customTextView = (EditText) findViewById(R.id.custom_message_edit_text);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf");
        customTextView.setTypeface(typeface);
        Button sendButton = (Button) findViewById(R.id.custom_message_send_button);
        sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                if (!customTextView.getText().toString().equals("")) {
                    ChatMessage toSend = new ChatMessage(customTextView.getText().toString(), pairsIndex);

                    String JSONMessage = gson.toJson(toSend);
                    mConnection.sendTextMessage(JSONMessage);

                    TextView sentMessage = new TextView(TwilioActivity.this);
                    Style.toOpenSans(TwilioActivity.this, sentMessage, "light");
                    sentMessage.setGravity(Gravity.CENTER_HORIZONTAL);
                    sentMessage.setText("Sent: " + customTextView.getText().toString());
                    variableLayout.addView(sentMessage,0);
                    stored_information.add("Sent: " + customTextView.getText().toString());
                    customTextView.setText("");

                }
            }
        });

        Button saveButton = (Button) findViewById(R.id.custom_message_save_button);
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                if (!customTextView.getText().toString().equals("")) {

                    TextView savedMessage = new TextView(TwilioActivity.this);
                    Style.toOpenSans(TwilioActivity.this, savedMessage, "light");
                    savedMessage.setGravity(Gravity.CENTER_HORIZONTAL);
                    savedMessage.setText("Saved: " + customTextView.getText().toString());
                    variableLayout.addView(savedMessage,0);
                    stored_information.add("Saved: " + customTextView.getText().toString());
                    customTextView.setText("");

                }
            }
        });

        //showPaymentBar();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        mSnakeView.setMode(SnakeView.PAUSE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Store the game state
        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
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

//        else if (view.getId() == R.id.send_digit) {
//            Log.e("Send digit pushed","woo");
//            phone.sendDigit();
//        }
        else if (view.getId() == R.id.speaker_button) {
            Log.e("Speaker phone toggled", "woo");
            speakerPhoneOn = !speakerPhoneOn;
            if (speakerPhoneOn) {
                AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.setSpeakerphoneOn(true);
            }
            else {
                AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.setSpeakerphoneOn(false);
            }
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
        if (mHandler != null) {
            mHandler.removeCallbacks(pingServer);
        }

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
                     FrameLayout snakeGame = (FrameLayout) findViewById(R.id.snake_game);
                     ViewGroup parentOfSnake = (ViewGroup) (snakeGame.getParent());
                     parentOfSnake.removeView(snakeGame);
                     LinearLayout customMessage = (LinearLayout) findViewById(R.id.custom_message);
                     customMessage.setVisibility(View.VISIBLE);
                     backCameraButton.setVisibility(View.VISIBLE);

                 }
                 
//                 if(m.message != null) {
//                     instructionField.setText(m.message);
//                 }
                 
                 
                 //If String information was requested from caller, then add UI to give info and pre-fill with Shared Preferences if it exists
                 if (m.request_format != null && m.request_format.equals("edit_text")) {
                                         
                     TwilioActivity.this.addEditText(m);  //Add instructions, text field and button UI
   
                 }
                 //If a link was sent by the caller, then show a clickable link
                 else if (m.request_format != null && m.request_format.equals("link") && m.message != null) {
                    String sentURL = m.message;

                     //Check if the URL is an image retrievable by Square Picasso, if so, then display that image...
                    String[] imageFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
                    boolean isImage = false;
                     for (String extension : imageFileExtensions)
                     {
                        if (sentURL.toLowerCase().endsWith(extension))
                        {
                            isImage = true;
                            break;
                        }
                     }

                     if (isImage) {
                         TwilioActivity.this.addPicture(sentURL);

                     }
                     else {

                         TwilioActivity.this.addLink(m);  //Add instructions, text field and button UI
                     }
                 }

                 if(m.request_type != null) {
                     if (m.request_type.equals("payment")) {
                         showPaymentBar(m);
                     }
                     else if (m.request_type.equals("authentication")) {
                         takePhoto();
                     }
                 }


              }

              @Override
              public void onClose(int code, String reason) {
                 if(mHandler!= null) {
                     mHandler.removeCallbacks(pingServer);
                     Log.d(TAG, "Connection lost.");
                 }

              }
           });
        } catch (WebSocketException e) {

           Log.d(TAG, e.toString());
        }
     }

    //Adds a picture if sent by agent
    public void addPicture(String sentURL) {

        long pattern[]={0,50,50,50};
        //Start the vibration
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        //start vibration with repeated count, use -1 if you don't want to repeat the vibration
        vibrator.vibrate(pattern, -1);

        Log.e("Picture detected with URL: ", sentURL);
        final LinearLayout toAdd = new LinearLayout(TwilioActivity.this);
        toAdd.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams linLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        ImageView imageView = new ImageView(TwilioActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(5,5,5,5);
        imageView.setLayoutParams(new LayoutParams(lp));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        Picasso.with(TwilioActivity.this.getApplicationContext()).load(sentURL).into(imageView);

        toAdd.addView(imageView);
        variableLayout.addView(toAdd,0);

        stored_information.add(sentURL);


    }

    public void showPaymentBar(ChatMessage m) {

        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, r.getDisplayMetrics());
        int px60 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, r.getDisplayMetrics());

        final LinearLayout toAdd = new LinearLayout(TwilioActivity.this);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        toAdd.setOrientation(LinearLayout.VERTICAL);

        final String company_name = this.thisCall.company_name;
        TextView paymentDescription = new TextView(getApplicationContext());
        paymentDescription.setPadding(px,px,px,(int)Math.floor(px/2));
        paymentDescription.setGravity(Gravity.CENTER_HORIZONTAL);
        paymentDescription.setTextColor(0xFF000000);

        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        final String amount = formatter.format(m.amount);
        paymentDescription.setText(company_name + " is requesting " + amount + " for '" + m.message + "'");
        Style.toOpenSans(getApplicationContext(),paymentDescription,"light");
        toAdd.addView(paymentDescription);

        final FrameLayout frameToAdd = new FrameLayout(TwilioActivity.this);
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        ImageView slideButtonBackground = new ImageView(TwilioActivity.this);
        FrameLayout.LayoutParams imageLayout = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, px60);
        imageLayout.setMargins(px,0,px,px);
        slideButtonBackground.setLayoutParams(imageLayout);
        slideButtonBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
        slideButtonBackground.setImageResource(R.drawable.slide_button_background);

        final SlideButton slideButton = new SlideButton(this,null);
        Drawable thumb = getResources().getDrawable( R.drawable.slide_button );
        slideButton.setThumb(thumb);
        slideButton.setPadding((int)(px60/1.3),px60/12,(int)(px60/1.3),0);
        Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
        slideButton.setProgressDrawable(transparentDrawable);
        slideButton.setMax(100);
        slideButton.setClickable(false);
        FrameLayout.LayoutParams slideLayout = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, px60);
        slideLayout.setMargins(px,0,px,px);
        slideButton.setSlideButtonListener(new SlideButtonListener() {
            @Override
            public void handleSlide() {
                Style.makeToast(TwilioActivity.this, "Payment of " +  amount + " made to " + company_name);
                ((ViewGroup) toAdd.getParent()).removeView(toAdd);
            }
        });

        frameToAdd.addView(slideButtonBackground, imageLayout);
        frameToAdd.addView(slideButton,slideLayout);
        toAdd.addView(frameToAdd, frameLayoutParams);
        variableLayout.addView(toAdd,linearLayoutParams);
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
        if (linkDescription != null) {
            sb.setSpan(bss, 0, linkDescription.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
        }


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
        variableLayout.addView(toAdd,0);
        
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
        Style.toOpenSans(this, instructions, "bold");
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
        variableLayout.addView(toAdd,0);
    }

    public void takePhoto() {
        closeCamera();
        mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);


        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.setVisibility(View.VISIBLE);
        preview.addView(mPreview);

        final ImageView captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.bringToFront();
        captureButton.setEnabled(true);
//        captureButton.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // TODO Auto-generated method stub
//                if(event.getAction()==MotionEvent.ACTION_DOWN)
//                    ((ImageView)v.findViewById(R.id.button_capture)).setImageResource(R.drawable.camera_button_pushed);
//                else if(event.getAction()==MotionEvent.ACTION_UP)
//                    ((ImageView)v.findViewById(R.id.button_capture)).setImageResource(R.drawable.camera_button);
//                return false;
//
//            }
//        });

        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Camera.Parameters p = mCamera.getParameters();
                        // get an image from the camera
                        if (mCamera != null){
                            captureButton.setEnabled(false);

                                mCamera.takePicture(null,null,null, mPicture);


                        }


                    }
                }
        );

    };


    public void closeCamera() {

        backCameraActive = false;

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.removeCallback();
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.setVisibility(View.GONE);
            preview.removeView(mPreview);
            mPreview = null;
        }
    };

    public void takeBackPhoto() {
        closeCamera();
        backCameraActive = true;
        mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.setVisibility(View.VISIBLE);
        preview.addView(mPreview);

        final ImageView captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.bringToFront();
        captureButton.setEnabled(true);
//        captureButton.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // TODO Auto-generated method stub
//                if(event.getAction()==MotionEvent.ACTION_DOWN)
//                    ((ImageView)v.findViewById(R.id.button_capture)).setImageResource(R.drawable.camera_button_pushed);
//                else if(event.getAction()==MotionEvent.ACTION_UP)
//                    ((ImageView)v.findViewById(R.id.button_capture)).setImageResource(R.drawable.camera_button);
//                return false;
//
//            }
//        });

        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Camera.Parameters p = mCamera.getParameters();
                        // get an image from the camera
                        if (mCamera != null){
                            captureButton.setEnabled(false);
                            mCamera.autoFocus(TwilioActivity.this);

                        }


                    }
                }
        );

    };

    private Camera.PictureCallback mBackCameraPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            options.inMutable = true;

            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length,options);
            data = null;
            Log.e("Image width:", bmp.getWidth() + "");
            Log.e("Image height:", bmp.getHeight() + "");
//            if (bmp.getWidth() > bmp.getHeight()) {


            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bmp = Bitmap.createBitmap(bmp , 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            bmp.compress(Bitmap.CompressFormat.JPEG,80, baos);
            bmp.recycle();
            String imgString = Base64.encodeToString(baos.toByteArray(),
                    Base64.NO_WRAP);

            ChatMessage picToSend = new ChatMessage(imgString, pairsIndex);
            picToSend.request_type = "BACK CAMERA PICTURE";

            String JSONMessage = gson.toJson(picToSend);
            mConnection.sendTextMessage(JSONMessage);

            try {
                baos.close();
                baos = null;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            closeCamera();
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.setVisibility(View.GONE);



        }
    };

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            options.inMutable = true;

            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length,options);
            data = null;
            Log.e("Image width:", bmp.getWidth() + "");
            Log.e("Image height:", bmp.getHeight() + "");
//            if (bmp.getWidth() > bmp.getHeight()) {


                Matrix matrix = new Matrix();
                matrix.postRotate(-90);
                bmp = Bitmap.createBitmap(bmp , 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            bmp.compress(Bitmap.CompressFormat.JPEG,25, baos);
            bmp.recycle();
            String imgString = Base64.encodeToString(baos.toByteArray(),
                    Base64.NO_WRAP);

            ChatMessage picToSend = new ChatMessage(imgString, pairsIndex);
            picToSend.request_type = "CURRENT AUTHENTICATION PICTURE";

            String JSONMessage = gson.toJson(picToSend);
            mConnection.sendTextMessage(JSONMessage);

            //Retrieve shared preferences
            SharedPreferences prefs = TwilioActivity.this.getApplicationContext().getSharedPreferences("com.SrivatsanPoddar.helpp", Context.MODE_PRIVATE);
            final String preferenceKey = "com.SrivatsanPoddar.helpp." + "ORIGINAL_AUTHENTICATION_PICTURE";
            String originalPicString = prefs.getString(preferenceKey, "");

            if (originalPicString == null || originalPicString.equals("")) {
                Editor editor = prefs.edit();
                editor.putString(preferenceKey, imgString);
                editor.commit();

                ChatMessage originalPic = new ChatMessage(null, pairsIndex);
                originalPic.request_type = "ORIGINAL AUTHENTICATION PICTURE";
                JSONMessage = gson.toJson(originalPic);
                mConnection.sendTextMessage(JSONMessage);
            }
            else {
                ChatMessage originalPic = new ChatMessage(originalPicString, pairsIndex);
                originalPic.request_type = "ORIGINAL AUTHENTICATION PICTURE";
                JSONMessage = gson.toJson(originalPic);
                mConnection.sendTextMessage(JSONMessage);
            }


            imgString = "";




            try {
                baos.close();
                baos = null;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            closeCamera();
        }
    };


    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(int cameraType){
        Camera c = null;
        //Camera.CameraInfo.CAMERA_FACING_FRONT
        try {
            int cameraCount = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
                Camera.getCameraInfo( camIdx, cameraInfo );
                if ( cameraInfo.facing == cameraType  ) {
                    try {
                        c = Camera.open( camIdx );

                        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                        android.hardware.Camera.getCameraInfo(camIdx, info);
                        int rotation = TwilioActivity.this.getWindowManager().getDefaultDisplay().getRotation();
                        int degrees = 0;
                        switch (rotation) {
                            case Surface.ROTATION_0:
                                degrees = 0;
                                break;
                            case Surface.ROTATION_90:
                                degrees = 90;
                                break;
                            case Surface.ROTATION_180:
                                degrees = 180;
                                break;
                            case Surface.ROTATION_270:
                                degrees = 270;
                                break;
                        }

                        int result;
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            result = (info.orientation + degrees) % 360;
                            result = (360 - result) % 360; // compensate the mirror
                        } else { // back-facing
                            result = (info.orientation - degrees + 360) % 360;
                        }
                        c.setDisplayOrientation(result);

                        Camera.Parameters p = c.getParameters();
                        p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                        p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                        List<Camera.Size> sizes = p.getSupportedPictureSizes();
                        Camera.Size maxSize = sizes.get(0);
                        for (int i=1;i<sizes.size();i++){
                            Camera.Size cur = sizes.get(i);
                            Log.i("PictureSize", "Supported Size: " + cur.width + " height : " + cur.height);
                            if(cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                //Style.makeToast(TwilioActivity.this,"width: " + cur.width + " and height: " + cur.height);

                                if (maxSize.height > 1300 || maxSize.width > 1300 || Math.abs(cur.height/cur.width - 1) < Math.abs(maxSize.height/maxSize.width-1)) {
                                    maxSize = cur;
                                }
                            }
                            else {
                                if (cur.height >  (maxSize.height)) {
                                    maxSize = cur;
                                }
                            }

                        }
                        Log.i("Selected picture size:", "width: " + maxSize.width + " and height: " + maxSize.height);
                        //Style.makeToast(TwilioActivity.this,"width: " + maxSize.width + " and height: " + maxSize.height);
                        p.setPictureSize(maxSize.width, maxSize.height);
                        c.setParameters(p);

                    } catch (RuntimeException e) {
                        Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                    }
                }
            }
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }

        return c; // returns null if camera is unavailable
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
//		if (success)
        camera.takePicture(null,null,null, mBackCameraPicture);
//		else {
//			int duration = Toast.LENGTH_SHORT;
//	    	CharSequence text = "Could not Autofocus--try again!";
//	    	Context context = getApplicationContext();
//			Toast toast = Toast.makeText(context, text, duration);
//			toast.show();
//		}
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.e("Fling detected", "woo");
            try {
                if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(Snake.this, "Up Swipe", Toast.LENGTH_SHORT).show();
                    mSnakeView.giveSwipe("up");
                }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(Snake.this, "Down Swipe", Toast.LENGTH_SHORT).show();
                    mSnakeView.giveSwipe("down");
                }
                else if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(Snake.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                    mSnakeView.giveSwipe("left");
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(Snake.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                    mSnakeView.giveSwipe("right");
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

}
