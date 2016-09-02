package tfg.jorgealcolea.naosports;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import tfg.jorgealcolea.naosports.fragments.ScoreListFragment;

/**
 * Created by george on 21/08/16.
 */
public class ConnectActivity extends AppCompatActivity {

    private static final String TAG = "ConnectActivity";

    private static final int THIRTYMINMATCH = 1800*1000;
    private static final int TWENTYMINMATCH = 1200*1000;
    private static final int FIFTEENMINMATCH = 900*1000;
    private static final int TENMINMATCH = 600*1000;
    private static final int FIVEMINMATCH = 300*1000;

    private Context context;

    private ProgressDialog progress;
    private EditText editTextPlayerName;
    private EditText editTextIp;
    private EditText editTextRivalName;
    private LinearLayout layoutRivalName;
    private ToggleButton toggleMode;
    private Button buttonPlay;
    private Button buttonScore;
    private Button buttonVsScore;
    private Spinner timeSpinner;
    private FrameLayout scoreTable;

    private BroadcastReceiver mStartBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        context = this;

        toggleMode = (ToggleButton)findViewById(R.id.toggle_mode);
        layoutRivalName = (LinearLayout)findViewById(R.id.layout_rival_name);
        buttonPlay = (Button)findViewById(R.id.button_play);
        buttonScore = (Button)findViewById(R.id.button_score);
        buttonVsScore  = (Button)findViewById(R.id.button_vs_score);
        editTextPlayerName = (EditText)findViewById(R.id.edittext_player_name);
        editTextIp = (EditText)findViewById(R.id.edittext_ip);
        editTextRivalName = (EditText)findViewById(R.id.edittext_rival_name);
        timeSpinner = (Spinner)findViewById(R.id.time_spinner);
        scoreTable = (FrameLayout)findViewById(R.id.score_table_connect);

        toggleMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    layoutRivalName.setVisibility(View.VISIBLE);
                } else {
                    layoutRivalName.setVisibility(View.GONE);
                }
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    // TODO manejar el modo versus

                    if (MyGcmManager.getInstance().getRegisteredInGCM()) {
                        if (toggleMode.isChecked()){
                            progress = ProgressDialog.show(context, null, "Connecting to robot and waiting for rival to start", true);
                        } else {
                            progress = ProgressDialog.show(context, null, "Connecting to robot", true);
                        }
                        connectToRobot();
                    } else {
                        Toast.makeText(context, "Not registered in GCM, restart application", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    return;
                }
            }
        });

        buttonScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonPlay.setVisibility(View.GONE);
                buttonScore.setVisibility(View.GONE);
                buttonVsScore.setVisibility(View.GONE);
                setContentFragment();
                scoreTable.setVisibility(View.VISIBLE);
            }
        });
        initializeReceiver();

        // Register in GCM
        MyGcmManager.getInstance().registerGCM(this, true);
    }

    public void onBackPressed() {
        if (scoreTable.getVisibility() == View.VISIBLE){
            scoreTable.setVisibility(View.GONE);
            buttonPlay.setVisibility(View.VISIBLE);
            buttonScore.setVisibility(View.VISIBLE);
            buttonVsScore.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    public void connectToRobot() {
        Thread routine = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                if (editTextIp.getText() != null && !editTextIp.getText().toString().equals("")) {

                    try {
                        // Initialize robot
                        RobotSession.getInstance().startServiceRoutine(
                                editTextIp.getText().toString(),
                                toggleMode.isChecked());
                        goToGame();
                    } catch (Exception e){
                        e.printStackTrace();
                        Log.e(TAG, "Connection Error", e);
                        progress.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Connection error, please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

        });
        routine.start();
    }

    public void goToGame(){

        RobotSession.getInstance().setPlayerName(editTextPlayerName.getText().toString());

        if (toggleMode.isChecked()){
            // Versus mode
            RobotSession.getInstance().setRivalName(editTextRivalName.getText().toString());
            LocalBroadcastManager.getInstance(this).registerReceiver(mStartBroadcastReceiver,
                    new IntentFilter(MyGcmManager.GCM_START));
        } else {
           createIntent();
        }
    }

    public void createIntent(){
        progress.dismiss();
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("gameDuration", getGameDuration(timeSpinner.getSelectedItemPosition()));
        startActivity(intent);
    }

    public boolean validateFields(){
        if (editTextPlayerName.getText().toString().equals("")) {
            Toast.makeText(context, "Wrong player name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (editTextIp.getText().toString().equals("")) {
            Toast.makeText(context, "Wrong IP", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (toggleMode.isChecked()){
            if (editTextRivalName.getText().toString().equals("")) {
                Toast.makeText(context, "Wrong rival name", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public int getGameDuration(int spinnerPosition){
        int gameDuration = 0;
        switch (spinnerPosition){
            case 0:
                gameDuration = FIVEMINMATCH;
                break;
            case 1:
                gameDuration = TENMINMATCH;
                break;
            case 2:
                gameDuration = FIFTEENMINMATCH;
                break;
            case 3:
                gameDuration = TWENTYMINMATCH;
                break;
            case 4:
                gameDuration = THIRTYMINMATCH;
                break;
        }
        return gameDuration;
    }

    public void setContentFragment(){
            getFragmentManager().
                    beginTransaction().
                    replace(R.id.score_table_connect, new ScoreListFragment()).
                    commit();
    }

    public void initializeReceiver(){
        mStartBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(mStartBroadcastReceiver);
                createIntent();
            }
        };
    }

    //
}
