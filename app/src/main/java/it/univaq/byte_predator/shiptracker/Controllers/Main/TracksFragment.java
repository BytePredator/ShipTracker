package it.univaq.byte_predator.shiptracker.Controllers.Main;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.R;
import it.univaq.byte_predator.shiptracker.Tables.tracksTable;

/**
 * Created by byte-predator on 21/02/18.
 */

public class TracksFragment extends Fragment {
    private TracksAdapter tracks;
    private AlertDialog dialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tracks_fragment,container,false);

        this.tracks = new TracksAdapter(tracksTable.getTracks(), AMcallback);

        RecyclerView tracks_list = view.findViewById(R.id.tracks_list);
        tracks_list.setLayoutManager(new LinearLayoutManager(getContext()));

        tracks_list.setAdapter(tracks);

        view.findViewById(R.id.add_track).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTrackDialog dialog = new AddTrackDialog();
                dialog.show(getActivity().getFragmentManager(), "AddTrackDialog");
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_tracks_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                for(Long Id: tracks.getSelected()){
                    tracksTable.Delete(Id);
                }
                tracks.setData(tracksTable.getTracks());
                tracks.getActionMode().finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                tracks.getActionMode().finish();
            }
        });
        this.dialog = builder.create();

        ArrayList<Track> tracks_l = tracksTable.getTracks();
        if(tracks_l.size()>0)
            this.tracks.setData(tracks_l);
        for(Track t : tracks_l)
            Log.w("tracks fragment", "id: "+t.getId());
        Log.w("tracks fragment", "created fragment "+tracks_l.size());

        return view;
    }

    public void RefreshTracks(){
        this.tracks.setData(tracksTable.getTracks());
        this.tracks.notifyDataSetChanged();
        Log.w("tracks fragment","tracks updated "+tracksTable.getTracks().size());
    }

    @Override
    public void onResume() {
        super.onResume();
        this.RefreshTracks();
    }

    private ActionMode.Callback AMcallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu){
            mode.getMenuInflater().inflate(R.menu.selection_menu,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu){return false;}

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item){
            if(item.getItemId() == R.id.menu_delete)
                dialog.show();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode){
            tracks.selectionStop();
        }
    };
}
