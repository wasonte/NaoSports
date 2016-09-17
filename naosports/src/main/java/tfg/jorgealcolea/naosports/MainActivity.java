package tfg.jorgealcolea.naosports;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import tfg.jorgealcolea.naosports.fragments.ResultFragment;
import tfg.jorgealcolea.naosports.fragments.ScoreListFragment;

import com.aldebaran.qi.CallError;
import com.aldebaran.qi.EmbeddedTools;

import java.io.File;

public class MainActivity extends AppCompatActivity implements
        SensorEventListener {

    private static final String TIMEFORMAT = "%02d:%02d";

    private static final String TAG = "MainActivity";

    private Context context;

    private FrameLayout scoreTable;

    private LinearLayout timerLayout;
    private TextView timerTextView;

    private TextView textViewPlayerName;
    private TextView textViewRivalname;
    private LinearLayout linearLayoutPlayer2Score;
    private ImageView imageViewVS;

    private TextView scorePlayerTextView;
    private TextView scoreRivalTextView;
    // TODO arm movement

    private RelativeLayout buttonsLayout;
    private ToggleButton armToggle;
    private ToggleButton headToggle;
    private ToggleButton handToggle;
    private ToggleButton ballTrackerToggle;

    private Button talkButton;
    private EditText talkEditText;

    private SensorManager mgr=null;
    private float movementX;
    private float movementY;

    private Handler handler;
    private ImageView image;

    private CountDownTimer timer;

    private ProgressDialog progress;

    private BroadcastReceiver mScoreBroadcastReceiver;
    private BroadcastReceiver mChallengeBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        // Necesary to run the robot
        EmbeddedTools ebt = new EmbeddedTools();
        File cacheDir = getApplicationContext().getCacheDir();
        ebt.overrideTempDirectory(cacheDir);
        ebt.loadEmbeddedLibraries();

        // Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        timerLayout = (LinearLayout)findViewById(R.id.timerLayout);
        timerTextView = (TextView) findViewById(R.id.timer_text_view);
        image = (ImageView) findViewById(R.id.image_view);

        scoreTable = (FrameLayout)findViewById(R.id.fragment_frame);

        textViewPlayerName = (TextView)findViewById(R.id.textview_playername);
        textViewPlayerName.setText(RobotSession.getInstance().getPlayerName());

        scorePlayerTextView = (TextView)findViewById(R.id.score_player_textview);
        scoreRivalTextView = (TextView)findViewById(R.id.score_rival_textview);

        imageViewVS = (ImageView)findViewById(R.id.vs_image);
        linearLayoutPlayer2Score = (LinearLayout)findViewById(R.id.player2_score);
        textViewRivalname = (TextView)findViewById(R.id.textview_rivalname);

        buttonsLayout = (RelativeLayout)findViewById(R.id.buttonsLayout);
        armToggle = (ToggleButton)findViewById(R.id.armToggle);
        headToggle = (ToggleButton)findViewById(R.id.headToggle);
        handToggle = (ToggleButton)findViewById(R.id.handToggle);
        ballTrackerToggle = (ToggleButton)findViewById(R.id.ballTrackToggle);

        talkButton = (Button)findViewById(R.id.talkButton);

        if (RobotSession.getInstance().getMode().equals("versus")){
            imageViewVS.setVisibility(View.VISIBLE);
            linearLayoutPlayer2Score.setVisibility(View.VISIBLE);
            textViewRivalname.setText(RobotSession.getInstance().getRivalName());
        }

        // Robot controllers
        handler = new Handler();
        mgr=(SensorManager)getSystemService(Context.SENSOR_SERVICE);

        initializeReceiver();
        initiazeVideo();
        initiazeTimer();
        initializeButtons();
        unRegisterdMovementManager();
    }

    @Override
    protected void onPause(){
        unRegisterdMovementManager();
        RobotSession.getInstance().unRegisterBallTracker();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mScoreBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (RobotSession.getInstance().isAlMotion()){
            if (headToggle.isChecked() || armToggle.isChecked())
                registerdMovementManager();
            if(ballTrackerToggle.isChecked()){
                RobotSession.getInstance().registerBallTracker();
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mScoreBroadcastReceiver,
                new IntentFilter(MyGcmManager.GCM_UPDATE_SCORE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mChallengeBroadcastReceiver,
                new IntentFilter(MyGcmManager.GCM_CHALLENGE));
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        RobotSession.getInstance().unsuscribeToVideo();
        RobotSession.getInstance().cloneConnection();
        super.onDestroy();
    }

    public void onBackPressed() {

        if (scoreTable.getVisibility() == View.VISIBLE && timerTextView.getText().toString() == "00:00") {
            finish();
        } else if (scoreTable.getVisibility() == View.VISIBLE){
            scoreTable.setVisibility(View.GONE);
            showButtons(View.VISIBLE);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            leaveGame();
                        }
                    }).create().show();

            // TODO Desconectar sesion y guardar puntuacion si sale
        }
    }

    public void showButtons(int visibility){
        buttonsLayout.setVisibility(visibility);
    }

    public void stopRobotActions(){
        onStop(null);
        headToggle.setChecked(false);
        armToggle.setChecked(false);
        ballTrackerToggle.setChecked(false);
    }



    public void leaveGame(){
        stopRobotActions();
        showButtons(View.GONE);
        timer.cancel();
        timerTextView.setText("00:00");
        Bundle bundle = new Bundle();
        bundle.putBoolean("leave", true);
        Fragment fragment = new ResultFragment();
        fragment.setArguments(bundle);
        setContentFragment(fragment);
        scoreTable.setVisibility(View.VISIBLE);
    }



    ////////////////////
    //
    // Video Image and timer
    //
    ////////////////////
    public void initiazeVideo() {
        Thread routine = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            image.setImageBitmap(RobotSession.getInstance().getVideo());
                            handler.postDelayed(this, 20);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
        routine.start();
    }

    public void initiazeTimer() {
        timer = new CountDownTimer(getIntent().getIntExtra("gameDuration", 5*1000),1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minute = millisUntilFinished / (60 * 1000);
                long second = (millisUntilFinished / 1000) % 60;
                timerTextView.setText("" + String.format(TIMEFORMAT,
                        minute,
                        second));

                if (minute == 1 && second == 0){
                    timerLayout.setBackground(getDrawable(R.drawable.timer_warning));
                } else if (minute == 3 && second == 0){
                    timerLayout.setBackground(getDrawable(R.drawable.timer_caution));
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText("00:00");
                if (RobotSession.getInstance().getMode().equals("solo")){
                    RobotSession.getInstance().setPlayerScore(Integer.parseInt(scorePlayerTextView.getText().toString()));
                    RobotSession.getInstance().insertSoloScore((Activity)context);
                } else {
                    RobotSession.getInstance().setPlayerScore(Integer.parseInt(scorePlayerTextView.getText().toString()));
                    RobotSession.getInstance().setRivalScore(Integer.parseInt(scoreRivalTextView.getText().toString()));
                }
                stopRobotActions();
                showButtons(View.GONE);
                setContentFragment(new ResultFragment());
                scoreTable.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    public void initializeReceiver(){
        mScoreBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // TODO controlar el modo versus
                if (RobotSession.getInstance().getMode().equals("solo")){
                    scorePlayerTextView.setText(intent.getStringExtra("playerScore"));
                } else {
                    // Versus mode
                    scorePlayerTextView.setText(intent.getStringExtra("playerScore"));
                    scoreRivalTextView.setText(intent.getStringExtra("rivalScore"));
                }
                Toast.makeText(context, "Score updated", Toast.LENGTH_SHORT).show();
            }
        };

        mChallengeBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, intent.getStringExtra("message"), Toast.LENGTH_LONG).show();
            }
        };
    }

    public void initializeButtons(){
        armToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    headToggle.setChecked(false);
                    registerdMovementManager();
                } else if (!armToggle.isChecked() && !headToggle.isChecked()){
                    unRegisterdMovementManager();
                }
            }
        });

        headToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    armToggle.setChecked(false);
                    registerdMovementManager();
                } else if (!armToggle.isChecked() && !headToggle.isChecked()){
                    unRegisterdMovementManager();
                }
            }
        });

        handToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    RobotSession.getInstance().openHand();
                } else {
                    RobotSession.getInstance().closeHand();
                }
            }
        });



        ballTrackerToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    RobotSession.getInstance().setSubscribedToRedBall(true);
                    RobotSession.getInstance().registerBallTracker();
                } else {
                    RobotSession.getInstance().setSubscribedToRedBall(false);
                    RobotSession.getInstance().unRegisterBallTracker();
                }
            }
        });

        talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("What you wanna say?")
                        .setView(resetTalkEditText())
                        .setNegativeButton("Nothing", null)
                        .setPositiveButton("Say it!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (talkEditText.getText().toString() != "") {
                                    RobotSession.getInstance().say(talkEditText.getText().toString());
                                    Toast.makeText(context, talkEditText.getText().toString(), Toast.LENGTH_SHORT).show();
                                } else {
                                    RobotSession.getInstance().say("What?");
                                }
                            }
                        }).create().show();
            }
        });

    }

    public EditText resetTalkEditText(){
        talkEditText= new EditText(this);
        talkEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        return talkEditText;
    }


    ////////////////////
    //
    // Movement manager
    //
    ////////////////////
    public void registerdMovementManager() {
        mgr.registerListener(this, mgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unRegisterdMovementManager(){
        mgr.unregisterListener(this);
    }

    ////////////////////
    //
    // Implemented Sensor methods
    //
    ////////////////////
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // unused
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        movementX = e.values[0];
        movementY = e.values[1];

        Thread routine = new Thread(new Runnable() {
            @Override
            public void run() {
                if (headToggle.isChecked()){
                    RobotSession.getInstance().moveWithAccelerometer(RobotSession.getInstance().HeadYaw, movementX);
                    RobotSession.getInstance().moveWithAccelerometer(RobotSession.getInstance().HeadPitch, movementY);
                } else if (armToggle.isChecked()){
                    RobotSession.getInstance().moveWithAccelerometer(RobotSession.getInstance().ShoulderRoll, movementX);
                    RobotSession.getInstance().moveWithAccelerometer(RobotSession.getInstance().ShoulderPitch, movementY);
                }
            }
        });
        routine.start();
    }


    ////////////////////
    //
    // MovementBottons
    //
    ////////////////////
    public void onGoToFront(View view) {
        try {
            RobotSession.getInstance().onGoToFront();
        } catch(CallError c){
            Toast.makeText(context, "Connection lost, check your robot and try to reconnect", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onStop(View view) {
        try {
            RobotSession.getInstance().onStop();
        } catch(CallError c){
            Toast.makeText(context, "Connection lost, check your robot and try to reconnect", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void onGoToLeft(View view) {
        try {
            RobotSession.getInstance().onGoToLeft();
        } catch(CallError c){
            Toast.makeText(context, "Connection lost, check your robot and try to reconnect", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onGoToRight(View view) {
        try {
            RobotSession.getInstance().onGoToRight();
        } catch(CallError c){
            Toast.makeText(context, "Connection lost, check your robot and try to reconnect", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onGoToBack(View view) {
        try {
            RobotSession.getInstance().onGoToBack();
        } catch(CallError c){
            Toast.makeText(context, "Connection lost, check your robot and try to reconnect", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    ////////////////////
    //
    // Options menu
    //
    ////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reconnect:
                progress = ProgressDialog.show(context, null, "Reconnecting to robot", true);
                Thread routine = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        try {
                            RobotSession.getInstance().startServiceRoutine(null, false);
                            progress.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Reconnected to robot", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e){
                            e.printStackTrace();
                            progress.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Connection error, please try again", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }


                    }
                });
                routine.start();
                return true;
            case R.id.action_quit:
                leaveGame();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void setContentFragment(Fragment fragment){
        getFragmentManager().
                beginTransaction().
                replace(R.id.fragment_frame, fragment).
                commit();
   }


}
