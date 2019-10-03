package it.univaq.byte_predator.shiptracker.Controllers.Main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.R;
import it.univaq.byte_predator.shiptracker.Controllers.Track.ViewTrackActivity;

/**
 * Created by byte-predator on 21/02/18.
 */

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> {

    private ArrayList<Track> data;
    private boolean selection;
    private ArrayList<Long> selected = new ArrayList<>();
    private ActionMode actionMode;
    private ActionMode.Callback AMcallback;

    public TracksAdapter(ArrayList<Track> data, ActionMode.Callback callback){
        this.selection = false;
        this.data = data;
        this.AMcallback = callback;
    }

    public void selectionStop(){
        this.selection = false;
        this.selected.clear();
        this.notifyDataSetChanged();
    }

    public ArrayList<Long> getSelected(){
        return this.selected;
    }

    public ActionMode getActionMode(){
        return this.actionMode;
    }

    public void setData(ArrayList<Track> data){
        this.data = data;
    }

    private boolean toggleItem(long id){
        if(this.selection) {
            if(this.selected.contains(id)) {
                this.selected.remove(id);
                if(this.selected.size()==0)
                    actionMode.finish();
                return false;
            }else {
                this.selected.add(id);
                return true;
            }
        }
        return false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.Id = data.get(position).getId();
        String iName = data.get(position).getName();
        holder.name.setText(iName);
        Integer iWaypoints = data.get(position).waypointsNumber();
        holder.waypoints.setText(iWaypoints.toString());
        Double iDistance = data.get(position).getDistance();
        holder.distance.setText(String.format("%.3f Km",iDistance));
        Integer iTime = data.get(position).getBestTime();
        if(iTime != 0) {
            Integer H = iTime / 3600;
            Integer M = iTime % 3600 / 60;
            Integer S = iTime % 60;
            holder.time.setText(H.toString() + ":" + M.toString() + ":" + S.toString());
        }else
            holder.time.setText("~");
        if(this.selected.contains(new Long(holder.Id)))
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.colorSelected));
        else
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        long Id;
        TextView name;
        TextView waypoints;
        TextView distance;
        TextView time;
        ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            waypoints = itemView.findViewById(R.id.waypoints);
            distance = itemView.findViewById(R.id.distance);
            time = itemView.findViewById(R.id.time);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(selection){
                if (toggleItem(this.Id))
                    v.setBackgroundColor(v.getContext().getResources().getColor(R.color.colorSelected));
                else
                    v.setBackgroundColor(Color.TRANSPARENT);
            }else{
                Context context = v.getContext();
                Intent intent = new Intent(context, ViewTrackActivity.class);
                intent.putExtra("Id",this.Id);
                context.startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(!selection) {
                selection = true;
                if (toggleItem(this.Id))
                    v.setBackgroundColor(v.getContext().getResources().getColor(R.color.colorSelected));
                else
                    v.setBackgroundColor(Color.TRANSPARENT);
                actionMode = ((AppCompatActivity) v.getContext()).startSupportActionMode(AMcallback);
            }
            return true;
        }
    }
}
