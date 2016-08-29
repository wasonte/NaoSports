package tfg.jorgealcolea.naosports.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import tfg.jorgealcolea.naosports.ConnectActivity;
import tfg.jorgealcolea.naosports.R;
import tfg.jorgealcolea.naosports.RobotSession;

/**
 * Created by george on 29/08/16.
 */
public class ResultFragment extends Fragment {

    private ImageView winImageView;
    private ImageView loseImageView;
    private TextView resultSoloTextView;
    private TextView resultVersusTextView;
    private Button backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmen_result, container, false);

        winImageView = (ImageView)view.findViewById(R.id.win_imageview);
        loseImageView = (ImageView)view.findViewById(R.id.lose_imageview);
        resultSoloTextView = (TextView)view.findViewById(R.id.result_solo_textview);
        resultVersusTextView = (TextView)view.findViewById(R.id.result_versus_textview);
        backButton = (Button)view.findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMenu();
            }
        });

        configureResultScreen();
        return view;
    }

    private void configureResultScreen(){
        if (RobotSession.getInstance().getMode().equals("solo")){
            resultSoloTextView.setText(RobotSession.getInstance().getPlayerScore() + " points");
            resultSoloTextView.setVisibility(View.VISIBLE);
        } else {
            if (RobotSession.getInstance().getPlayerScore() > RobotSession.getInstance().getRivalScore()){
                winImageView.setVisibility(View.VISIBLE);
            } else if (RobotSession.getInstance().getPlayerScore() < RobotSession.getInstance().getRivalScore()){
                loseImageView.setVisibility(View.VISIBLE);
            }
            resultVersusTextView.setText(
                    RobotSession.getInstance().getPlayerScore()
                            + " - "
                            + RobotSession.getInstance().getRivalScore());
            resultVersusTextView.setVisibility(View.VISIBLE);
        }
    }

    private void backToMenu(){
        getActivity().finish();
    }
}
