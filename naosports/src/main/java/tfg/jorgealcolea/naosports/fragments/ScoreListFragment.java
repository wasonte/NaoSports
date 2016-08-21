package tfg.jorgealcolea.naosports.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;

import tfg.jorgealcolea.naosports.R;
import tfg.jorgealcolea.naosports.adapters.ScoreAdapter;
import tfg.jorgealcolea.naosports.beans.Score;
import tfg.jorgealcolea.naosports.sqlite.DatabaseAdapter;

/**
 * Created by george on 17/08/16.
 */
public class ScoreListFragment extends ListFragment {

    ScoreAdapter adapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DatabaseAdapter dbAdapter = new DatabaseAdapter(getActivity());
        adapter = new ScoreAdapter(getActivity(), R.layout.item_scorelist);

        if (dbAdapter.getScores().size() == 0){
            for (int i = 0; i < 50; i++){
                Score score = new Score("Jorge" + i, 23);
                dbAdapter.insertScore(score);
            }
        }

        adapter.setScores(dbAdapter.getScores());
        setListAdapter(adapter);
    }
}
