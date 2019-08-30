package it.univaq.byte_predator.shiptracker.Controllers.Main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Models.Point;
import it.univaq.byte_predator.shiptracker.Models.Race;
import it.univaq.byte_predator.shiptracker.Models.Trace;
import it.univaq.byte_predator.shiptracker.R;

/**
 * Created by byte-predator on 21/02/18.
 */

public class TracesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.traces_fragment, container, false);

        ArrayList<Trace> data_traces = new ArrayList<>();
        data_traces.add(new Trace(1,new Race(1, 0, new ArrayList<Point>())));
        data_traces.add(new Trace(2,new Race(2, 0, new ArrayList<Point>())));

        TracesAdapter traces = new TracesAdapter(data_traces);

        RecyclerView traces_list = view.findViewById(R.id.traces_list);
        traces_list.setLayoutManager(new LinearLayoutManager(getContext()));
        traces_list.setAdapter(traces);

        return view;
    }
}
