package tfg.jorgealcolea.naosports;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import tfg.jorgealcolea.naosports.fragments.ScoreListFragment;

import com.aldebaran.qi.EmbeddedTools;

import java.io.File;

public class MainActivity extends AppCompatActivity implements
        SensorEventListener {

    private static final String TIMEFORMAT = "%02d:%02d";

    private static final String TAG = "MainActivity";

    private Context context;

    private Switch headMovement;
    private Switch trackBallSwitch;
    private FrameLayout scoreTable;

    private LinearLayout timerLayout;
    private TextView timerTextView;

    private TextView textViewPlayerName;
    private LinearLayout linearLayoutPlayer2Score;
    private ImageView imageViewVS;

    private SensorManager mgr=null;
    private float movementX;
    private float movementY;

    private Handler handler;
    private ImageView image;

    private CountDownTimer timer;

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
        headMovement = (Switch) findViewById(R.id.headMovementSwitch);
        trackBallSwitch = (Switch) findViewById(R.id.trackBallSwitch);
        image = (ImageView) findViewById(R.id.image_view);

        scoreTable = (FrameLayout)findViewById(R.id.score_frame);

        textViewPlayerName = (TextView)findViewById(R.id.textview_playername);
        textViewPlayerName.setText(RobotSession.getInstance().getPlayerName());

        imageViewVS = (ImageView)findViewById(R.id.vs_image);
        linearLayoutPlayer2Score = (LinearLayout)findViewById(R.id.player2_score);

        if (getIntent().getStringExtra("mode").equals("versus")){
            imageViewVS.setVisibility(View.VISIBLE);
            linearLayoutPlayer2Score.setVisibility(View.VISIBLE);
        }

        // Robot controllers
        handler = new Handler();
        mgr=(SensorManager)getSystemService(Context.SENSOR_SERVICE);

        headMovement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    registerHeadMovementManager();
                } else {
                    unRegisterHeadMovementManager();
                }
            }
        });

        trackBallSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

        initiazeVideo();
        initiazeTimer();
    }

    @Override
    protected void onPause(){
        unRegisterHeadMovementManager();
        RobotSession.getInstance().unRegisterBallTracker();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (RobotSession.getInstance().isAlMotion()){
            if (headMovement.isChecked())
                registerHeadMovementManager();
            if(trackBallSwitch.isChecked()){
                RobotSession.getInstance().registerBallTracker();
            }
        }
    }

    @Override
    protected void onDestroy() {
        RobotSession.getInstance().unsuscribeToVideo();
        //application.stop();
        super.onDestroy();
    }

    public void onBackPressed() {

        if (scoreTable.getVisibility() == View.VISIBLE){
            scoreTable.setVisibility(View.GONE);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();

            // TODO Desconectar sesion y guardar puntuacion si sale
        }
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        headMovement.setEnabled(true);
                        trackBallSwitch.setEnabled(true);
                        unRegisterHeadMovementManager();
                    }
                });
            }
        });
        routine.start();
    }

    public void initiazeTimer() {
        timer = new CountDownTimer(getIntent().getIntExtra("gameDuration", 185*1000),1000) {
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
                Toast.makeText(context, "Final", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }


    ////////////////////
    //
    // Head Movement manager
    //
    ////////////////////
    public void registerHeadMovementManager() {
        mgr.registerListener(this, mgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unRegisterHeadMovementManager(){
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
                RobotSession.getInstance().moveHead(RobotSession.getInstance().HeadYaw, movementX);
                RobotSession.getInstance().moveHead(RobotSession.getInstance().HeadPitch, movementY);
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onStop(View view) {
        try {
            RobotSession.getInstance().onStop();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void onGoToLeft(View view) {
        try {
            RobotSession.getInstance().onGoToLeft();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onGoToRight(View view) {
        try {
            RobotSession.getInstance().onGoToRight();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onGoToBack(View view) {
        try {
            RobotSession.getInstance().onGoToBack();
        } catch (Exception e){
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
            case R.id.action_score:
                setContentFragment();
                scoreTable.setVisibility(View.VISIBLE);
                return true;
            case R.id.action_settings:
                Toast toast = Toast.makeText(this, "Hola puta", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void setContentFragment(){
        if (getFragmentManager().findFragmentById(R.id.score_frame) == null) {
            getFragmentManager().
                    beginTransaction().
                    add(R.id.score_frame, new ScoreListFragment()).
                    commit();
        }
    }
}
