package tfg.jorgealcolea.naosports.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;

import tfg.jorgealcolea.naosports.R;
import tfg.jorgealcolea.naosports.adapters.ScoreAdapter;
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
        adapter.setScores(dbAdapter.getScores());
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }
}
