package tfg.jorgealcolea.naosports;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

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

    Context context;

    ProgressDialog progress;
    EditText editTextPlayerName;
    EditText editTextIp;
    EditText editTextRivalIp;
    LinearLayout layoutRivalRobotIp;
    ToggleButton toggleMode;
    Button buttonPlay;
    Spinner timeSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        context = this;

        toggleMode = (ToggleButton)findViewById(R.id.toggle_mode);
        layoutRivalRobotIp = (LinearLayout)findViewById(R.id.layout_rival_robot_ip);
        buttonPlay = (Button)findViewById(R.id.button_play);
        editTextPlayerName = (EditText)findViewById(R.id.edittext_player_name);
        editTextIp = (EditText)findViewById(R.id.edittext_ip);
        editTextRivalIp = (EditText)findViewById(R.id.edittext_rival_ip);
        timeSpinner = (Spinner)findViewById(R.id.time_spinner);

        toggleMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    layoutRivalRobotIp.setVisibility(View.VISIBLE);
                } else {
                    layoutRivalRobotIp.setVisibility(View.GONE);
                }
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editTextPlayerName.getText().toString().equals("")){
                    Toast.makeText(context, "Wrong player name", Toast.LENGTH_SHORT).show();
                    return;
                } else if (editTextIp.getText().toString().equals("")){
                    Toast.makeText(context, "Wrong IP", Toast.LENGTH_SHORT).show();
                    return;
                }
                progress = ProgressDialog.show(context, null, "Connecting to robot", true);
                connectToRobot();
            }
        });
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
                                editTextPlayerName.getText().toString());
                        goToGame();
                    } catch (Exception e){
                        e.printStackTrace();
                        Log.e(TAG, "Connection Error", e);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Connection error, please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    progress.dismiss();
                }
            }

        });
        routine.start();
    }

    public void goToGame(){

        getGameDuration(timeSpinner.getSelectedItemPosition());

        Intent intent = new Intent(context, MainActivity.class);
        if (toggleMode.isChecked()){
            intent.putExtra("mode", "versus");
            intent.putExtra("rivalIp", editTextRivalIp.getText().toString());
        } else {
            intent.putExtra("mode", "solo");
        }
        intent.putExtra("gameDuration", getGameDuration(timeSpinner.getSelectedItemPosition()));
        startActivity(intent);
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
}
