package com.robotemi.sdk.sample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.activitystream.ActivityStreamObject;
import com.robotemi.sdk.activitystream.ActivityStreamPublishMessage;
import com.robotemi.sdk.activitystream.MediaObject;
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;
import com.robotemi.sdk.listeners.OnConstraintBeWithStatusChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnLocationsUpdatedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.voice.NlpResult;
import com.robotemi.sdk.voice.TtsRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        Robot.NlpListener,
        OnRobotReadyListener,
        Robot.ConversationViewAttachesListener,
        Robot.WakeupWordListener,
        Robot.ActivityStreamPublishListener,
        Robot.TtsListener,
        OnBeWithMeStatusChangedListener,
        OnGoToLocationStatusChangedListener,
        OnLocationsUpdatedListener,
        OnConstraintBeWithStatusChangedListener {

    public static final String ACTION_HOME_WELCOME = "home.welcome", ACTION_HOME_DANCE = "home.dance", ACTION_HOME_SLEEP = "home.sleep",
    ACTION_HOME_APPLIST = "home.applist", ACTION_ON_YOUTUBE = "home.youtube";
    public static final String HOME_BASE_LOCATION = "home base";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public EditText etSpeak, etSaveLocation, etGoTo;
    List<String> locations;
    private Robot robot;
    WebView internet;



    /**
     * Hiding keyboard after every button press
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * Setting up all the event listeners
     */

    @Override
    protected void onStart() {
        super.onStart();
        robot.addOnRobotReadyListener(this);
        robot.addNlpListener(this);
        robot.addOnBeWithMeStatusChangedListener(this);
        robot.addOnGoToLocationStatusChangedListener(this);
        robot.addConversationViewAttachesListenerListener(this);
        robot.addWakeupWordListener(this);
        robot.addTtsListener(this);
        robot.addOnLocationsUpdatedListener(this);
        robot.addOnConstraintBeWithStatusChangedListener(this);
        robot.hideTopBar();
        robot.speak(TtsRequest.create("Hi. Temi's website." +
                "Thank you... Press the bottom Button. you can show Top Menu Bar and robot And More website link icon", false));


    }

    /**
     * Removing the event listeners upon leaving the app.
     */
    @Override
    protected void onStop() {
        super.onStop();
        robot.removeOnRobotReadyListener(this);
        robot.removeNlpListener(this);
        robot.removeOnBeWithMeStatusChangedListener(this);
        robot.removeOnGoToLocationStatusChangedListener(this);
        robot.removeConversationViewAttachesListenerListener(this);
        robot.removeWakeupWordListener(this);
        robot.removeTtsListener(this);
        robot.removeOnLocationsUpdateListener(this);
    }

    /**
     * Places this application in the top bar for a quick access shortcut.
     */
    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            try {
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                robot.onStart(activityInfo);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    EditText address;
    ImageButton btnGo,btnBack,btnFresh;



    private void hidekeyboard(EditText et){
        InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(),0);
    }


    class FABClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            robot = Robot.getInstance(); // get an instance of the robot in order to begin using its features.
            ActionBar ab = getSupportActionBar();
           if(ab.isShowing()){
                ab.hide();
               robot.hideTopBar();
            }else{
                ab.show();
               robot.showTopBar();
               robot.speak(TtsRequest.create("the Third icon show app List", false));
            }
            switch (v.getId()) {
                case R.id.fab_main:
                    toggleFab();
                    break;
                case R.id.fab_sub1:
                    String add1 = "https://www.naver.com/?mobile";
                    internet.loadUrl(add1);
                    toggleFab();
                    break;
                case R.id.fab_sub2:
                    String add2 = "http://www.robotnmore.com";
                    internet.loadUrl(add2);
                    toggleFab();
                    break;
                case R.id.fab_sub3:
                    robot.showAppList();
                    /*
                    String add3 = "http://google.com";
                    internet.loadUrl(add3);*/
                    toggleFab();
                    break;
                case R.id.fab_sub4:
                    String add4 = "http://blog.naver.com/robotnmore?viewType=pc";
                    internet.loadUrl(add4);
                    toggleFab();
                    break;

            }
        }
    }

    private FloatingActionButton fab_main, fab_sub1, fab_sub2, fab_sub3, fab_sub4;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;

    private void toggleFab() {
        if (isFabOpen) {
            fab_main.setImageResource(R.drawable.menu);
            fab_sub1.startAnimation(fab_close);
            fab_sub2.startAnimation(fab_close);
            fab_sub3.startAnimation(fab_close);
            fab_sub4.startAnimation(fab_close);
            fab_sub1.setClickable(false);
            fab_sub2.setClickable(false);
            fab_sub3.setClickable(false);
            fab_sub4.setClickable(false);
            isFabOpen = false;
        } else {
            fab_main.setImageResource(R.drawable.menu_open);
            fab_sub1.startAnimation(fab_open);
            fab_sub2.startAnimation(fab_open);
            fab_sub3.startAnimation(fab_open);
            fab_sub4.startAnimation(fab_open);
            fab_sub1.setClickable(true);
            fab_sub2.setClickable(true);
            fab_sub3.setClickable(true);
            fab_sub4.setClickable(true);
            isFabOpen = true;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //initViews();
        verifyStoragePermissions(this);


        robot = Robot.getInstance(); // get an instance of the robot in order to begin using its features.

        ActionBar ab = getSupportActionBar();
        ab.hide();

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_main = (FloatingActionButton) findViewById(R.id.fab_main);
        fab_sub1 = (FloatingActionButton) findViewById(R.id.fab_sub1);
        fab_sub2 = (FloatingActionButton) findViewById(R.id.fab_sub2);
        fab_sub3 = (FloatingActionButton) findViewById(R.id.fab_sub3);
        fab_sub4 = (FloatingActionButton) findViewById(R.id.fab_sub4);
        fab_main.setOnClickListener(new FABClickListener());
        fab_sub1.setOnClickListener(new FABClickListener());
        fab_sub2.setOnClickListener(new FABClickListener());
        fab_sub3.setOnClickListener(new FABClickListener());
        fab_sub4.setOnClickListener(new FABClickListener());


        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowTitleEnabled(false);

        address = (EditText)findViewById(R.id.address);
        internet = (WebView)findViewById(R.id.internet);
        btnGo = (ImageButton)findViewById(R.id.btnForward);
        btnBack = (ImageButton)findViewById(R.id.btnBack);
        btnFresh = (ImageButton)findViewById(R.id.btnRefresh);


        internet.setWebViewClient(new WebViewClient());
        WebSettings webset = internet.getSettings();
        //webset.setBuiltInZoomControls(true);
        // 웹뷰에 동영상을 바로 실행시키기 위함.
        webset.setMediaPlaybackRequiresUserGesture(false);
        // 자바 스크립트 사용
        webset.setJavaScriptEnabled(true);
        // 뷰 가속 - 가속하지 않으면 영상실행 안됨, 소리만 나온다
        //webset.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webset.setLoadWithOverviewMode(true);
        webset.setUseWideViewPort(true);
        webset.setSupportZoom(true);
        webset.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webset.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webset.setDomStorageEnabled(true);
        internet.setWebChromeClient(new FullscreenableChromeClient(this));
        internet.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        internet.setScrollbarFadingEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            internet.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            internet.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        internet.loadUrl("https://www.naver.com/?mobile");



        internet.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                address.setText(url);
                super.onPageFinished(view, url);
            }

        });


        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internet.goForward();
            }
        });// btnGo

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internet.goBack();
            }
        });//btnBack
        btnFresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String add=address.getText().toString();
                if(!add.contains("http://") && !add.contains("https://"))
                    add="http://"+add;
                internet.loadUrl(add);
            }
        });

        address.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((actionId== EditorInfo.IME_ACTION_NEXT)||(event !=null &&event.getKeyCode()== KeyEvent.KEYCODE_ENTER)){
                    String add=address.getText().toString();
                    if(!add.contains("http://") && !add.contains("https://"))
                        add="http://"+add;
                    internet.loadUrl(add);
                    hidekeyboard(address);
                    return true;
                }
                return false;
            }
        });









        //robot.showAppList();
    }
    class WebViewClient extends android.webkit.WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }//Web


/*
    public void initViews() {
        etSpeak = findViewById(R.id.etSpeak);
        etSaveLocation = findViewById(R.id.etSaveLocation);
        etGoTo = findViewById(R.id.etGoTo);
    }
*/
    /**
     * Have the robot speak while displaying what is being said.
     */
    public void speak(View view) {
        TtsRequest ttsRequest = TtsRequest.create(etSpeak.getText().toString().trim(), true);
        robot.speak(ttsRequest);
        hideKeyboard(MainActivity.this);
    }

    /**
     * This is an example of saving locations.
     */
    public void saveLocation(View view) {
        String location = etSaveLocation.getText().toString().toLowerCase().trim();
        boolean result = robot.saveLocation(location);
        if (result) {
            robot.speak(TtsRequest.create("I've successfully saved the " + location + " location.", true));
        } else {
            robot.speak(TtsRequest.create("Saved the " + location + " location failed.", true));
        }
        hideKeyboard(MainActivity.this);
    }

    /**
     * goTo checks that the location sent is saved then goes to that location.
     */
    public void goTo(View view) {
        for (String location : robot.getLocations()) {
            if (location.equals(etGoTo.getText().toString().toLowerCase().trim())) {
                robot.goTo(etGoTo.getText().toString().toLowerCase().trim());
                hideKeyboard(MainActivity.this);
            }
        }
    }

    /**
     * stopMovement() is used whenever you want the robot to stop any movement
     * it is currently doing.
     */
    public void stopMovement(View view) {
        robot.stopMovement();
        robot.speak(TtsRequest.create("And so I have stopped", true));
    }

    /**
     * Simple follow me example.
     */
    public void followMe(View view) {
        robot.beWithMe();
        hideKeyboard(MainActivity.this);
    }

    /**
     * Manually navigate the robot with skidJoy, tiltAngle, turnBy and tiltBy.
     * skidJoy moves the robot exactly forward for about a second. It controls both
     * the linear and angular velocity. Float numbers must be between -1.0 and 1.0
     */
    public void skidJoy(View view) {
        long t = System.currentTimeMillis();
        long end = t + 1000;
        while (System.currentTimeMillis() < end) {
            robot.skidJoy(1F, 0F);
        }
    }

    /**
     * tiltAngle controls temi's head by specifying which angle you want
     * to tilt to and at which speed.
     */
    public void tiltAngle(View view) {
        robot.tiltAngle(23, 5.3F);
    }

    /**
     * turnBy allows for turning the robot around in place. You can specify
     * the amount of degrees to turn by and at which speed.
     */
    public void turnBy(View view) {
        robot.turnBy(180, 6.2F);
    }

    /**
     * tiltBy is used to tilt temi's head from its current position.
     */
    public void tiltBy(View view) {
        robot.tiltBy(70, 1.2F);
    }

    /**
     * <<<<<<< HEAD
     * getBatteryData can be used to return the current battery status.
     */
    public void getBatteryData(View view) {
        BatteryData batteryData = robot.getBatteryData();
        if (batteryData.isCharging()) {
            TtsRequest ttsRequest = TtsRequest.create(batteryData.getBatteryPercentage() + " percent battery and charging.", true);
            robot.speak(ttsRequest);
        } else {
            TtsRequest ttsRequest = TtsRequest.create(batteryData.getBatteryPercentage() + " percent battery and not charging.", true);
            robot.speak(ttsRequest);
        }
    }

    /**
     * Display the saved locations in a dialog
     */
    public void savedLocationsDialog(View view) {
        hideKeyboard(MainActivity.this);
        locations = robot.getLocations();
        final CustomAdapter customAdapter = new CustomAdapter(MainActivity.this, android.R.layout.simple_selectable_list_item, locations);
        AlertDialog.Builder versionsDialog = new AlertDialog.Builder(MainActivity.this);
        versionsDialog.setTitle("Saved Locations: (Click to delete the location)");
        versionsDialog.setPositiveButton("OK", null);
        versionsDialog.setAdapter(customAdapter, null);
        AlertDialog dialog = versionsDialog.create();
        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Delete location \"" + customAdapter.getItem(position) + "\" ?");
                builder.setPositiveButton("No thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String location = customAdapter.getItem(position);
                        if (location == null) {
                            return;
                        }
                        boolean result = robot.deleteLocation(location);
                        if (result) {
                            locations.remove(position);
                            robot.speak(TtsRequest.create(location + "delete successfully!", false));
                            customAdapter.notifyDataSetChanged();
                        } else {
                            robot.speak(TtsRequest.create(location + "delete failed!", false));
                        }
                    }
                });
                Dialog deleteDialog = builder.create();
                deleteDialog.show();
            }
        });
        dialog.show();
    }

    /**
     * When adding the Nlp Listener to your project you need to implement this method
     * which will listen for specific intents and allow you to respond accordingly.
     * <p>
     * See AndroidManifest.xml for reference on adding each intent.
     */
    @Override
    public void onNlpCompleted(NlpResult nlpResult) {
        //do something with nlp result. Base the action specified in the AndroidManifest.xml
        Toast.makeText(MainActivity.this, nlpResult.action, Toast.LENGTH_SHORT).show();

        switch (nlpResult.action) {
            case ACTION_HOME_WELCOME:
                robot.tiltAngle(23, 5.3F);
                break;

            case ACTION_HOME_DANCE:
                long t = System.currentTimeMillis();
                long end = t + 5000;
                while (System.currentTimeMillis() < end) {
                    robot.skidJoy(0F, 1F);
                }
                break;

            case ACTION_HOME_SLEEP:
                robot.goTo(HOME_BASE_LOCATION);
                break;
            case  ACTION_HOME_APPLIST:
                robot.showAppList();
                break;
            case ACTION_ON_YOUTUBE:
                String youtubeadd = "http://www.youtube.com";
                internet.loadUrl(youtubeadd);
                break;
        }
    }

    /**
     * callOwner is an example of how to use telepresence to call an individual.
     */
    public void callOwner(View view) {
        robot.startTelepresence(robot.getAdminInfo().getName(), robot.getAdminInfo().getUserId());
    }

    /**
     * publishToActivityStream takes an image stored in the resources folder
     * and uploads it to the mobile application under the Activities tab.
     */
    public void publishToActivityStream(View view) throws RemoteException {
        ActivityStreamObject activityStreamObject;
        if (robot != null) {
            final String fileName = "puppy.png";
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.puppy);
            File puppiesFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), fileName);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(puppiesFile);
                bm.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            activityStreamObject = ActivityStreamObject.builder()
                    .activityType(ActivityStreamObject.ActivityType.PHOTO)
                    .title("Puppy")
                    .media(MediaObject.create(MediaObject.MimeType.IMAGE, puppiesFile))
                    .build();

            try {
                robot.shareActivityObject(activityStreamObject);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            robot.speak(TtsRequest.create("Uploading Image", false));
        }
    }

    public void hideTopBar(View view) {
        robot.hideTopBar();
    }

    public void showTopBar(View view) {
        robot.showTopBar();
    }

    @Override
    public void onWakeupWord(String wakeupWord, int direction) {
        // Do anything on wakeup. Follow, go to location, or even try creating dance moves.
    }

    @Override
    public void onTtsStatusChanged(TtsRequest ttsRequest) {
        // Do whatever you like upon the status changing. after the robot finishes speaking
    }

    @Override
    public void onBeWithMeStatusChanged(String status) {
        //  When status changes to "lock" the robot recognizes the user and begin to follow.
        switch (status) {
            case "abort":
                // do something i.e. speak
                robot.speak(TtsRequest.create("Abort", false));
                break;

            case "calculating":
                robot.speak(TtsRequest.create("Calculating", false));
                break;

            case "lock":
                robot.speak(TtsRequest.create("Lock", false));
                break;

            case "search":
                robot.speak(TtsRequest.create("search", false));
                break;

            case "start":
                robot.speak(TtsRequest.create("Start", false));
                break;

            case "track":
                robot.speak(TtsRequest.create("Track", false));
                break;
        }
    }

    @Override
    public void onGoToLocationStatusChanged(String location, String status, int descriptionId, String description) {
        Log.d("GoToStatusChanged", "descriptionId=" + descriptionId + ", description=" + description);
        switch (status) {
            case "start":
                robot.speak(TtsRequest.create("Starting", false));
                break;

            case "calculating":
                robot.speak(TtsRequest.create("Calculating", false));
                break;

            case "going":
                robot.speak(TtsRequest.create("Going", false));
                break;

            case "complete":
                robot.speak(TtsRequest.create("Completed", false));
                break;

            case "abort":
                robot.speak(TtsRequest.create("Cancelled", false));
                break;
        }
    }

    @Override
    public void onConversationAttaches(boolean isAttached) {
        if (isAttached) {
            //Do something as soon as the conversation is displayed.
        }
    }

    @Override
    public void onPublish(ActivityStreamPublishMessage message) {
        //After the activity stream finished publishing (photo or otherwise).
        //Do what you want based on the message returned.
        robot.speak(TtsRequest.create("Uploaded.", false));
    }

    @Override
    public void onLocationsUpdated(List<String> locations) {
        //Saving or deleting a location will update the list.
        Toast.makeText(this, "Locations updated :\n" + locations, Toast.LENGTH_LONG).show();
    }

    public void disableWakeup(View view) {
        robot.toggleWakeup(true);
    }

    public void enableWakeup(View view) {
        robot.toggleWakeup(false);
    }

    public void showBillboard(View view) {
        robot.toggleNavigationBillboard(false);
    }

    public void hideBillboard(View view) {
        robot.toggleNavigationBillboard(true);
    }

    @Override
    public void onConstraintBeWithStatusChanged(boolean isConstraint) {
        Log.d("onConstraintBeWith", "status = " + isConstraint);
    }
}
