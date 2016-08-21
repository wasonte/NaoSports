package tfg.jorgealcolea.naosports.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import tfg.jorgealcolea.naosports.R;
import tfg.jorgealcolea.naosports.beans.Score;

import java.util.List;

/**
 * Created by george on 17/08/16.
 */
public class ScoreAdapter extends ArrayAdapter<Score> {

    private LayoutInflater inflater;

    public ScoreAdapter(Context context, int itemResourceId){
        super(context, itemResourceId);
        this.inflater = LayoutInflater.from(context);
    }

    public void setScores (List<Score> scores) {
        clear();
        for (Score score : scores) {
            add(score);
        }
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null){
            v = inflater.inflate(R.layout.item_scorelist, null);
        }
        Score score = getItem(position);

        if (score != null){
            TextView textViewPosition = (TextView)v.findViewById(R.id.scorelist_position);
            TextView textViewUser = (TextView)v.findViewById(R.id.scorelist_user);
            TextView textViewScore = (TextView)v.findViewById(R.id.scorelist_score);

            textViewPosition.setText(Integer.toString(position+1)+".");
            textViewUser.setText(score.getUserName());
            textViewScore.setText(Integer.toString(score.getScore()));
        }
        return v;
    }
}
