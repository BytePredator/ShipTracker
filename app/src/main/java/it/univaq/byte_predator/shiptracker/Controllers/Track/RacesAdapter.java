package it.univaq.byte_predator.shiptracker.Controllers.Track;

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

import it.univaq.byte_predator.shiptracker.Controllers.Race.ViewRaceActivity;
import it.univaq.byte_predator.shiptracker.R;
import it.univaq.byte_predator.shiptracker.Models.Race;

import java.util.ArrayList;

public class RacesAdapter extends RecyclerView.Adapter<RacesAdapter.ViewHolder> {

    private ArrayList<Race> data;
    private boolean selection;
    private ArrayList<Long> selected = new ArrayList<>();
    private ActionMode actionMode;
    private ActionMode.Callback AMcallback;

    public RacesAdapter(ArrayList<Race> data, ActionMode.Callback callback){
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

    public void setData(ArrayList<Race> data){
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.race_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.Id.setText(String.valueOf(data.get(position).getId()));
        Integer iTime = data.get(position).getTime();
        Integer H = iTime/3600;
        Integer M = iTime%3600/60;
        Integer S = iTime%60;
        holder.time.setText(H.toString()+":"+M.toString()+":"+S.toString());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView Id;
        TextView time;

        public ViewHolder(View itemView) {
            super(itemView);

            Id = itemView.findViewById(R.id.id);
            time = itemView.findViewById(R.id.time);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            long id = Long.parseLong(this.Id.getText().toString());
            if(selection){
                if (toggleItem(id))
                    v.setBackgroundColor(v.getContext().getResources().getColor(R.color.colorSelected));
                else
                    v.setBackgroundColor(Color.TRANSPARENT);
            }else{
                Context context = v.getContext();
                Intent intent = new Intent(context, ViewRaceActivity.class);
                intent.putExtra("Id",id);
                context.startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(!selection) {
                selection = true;
                if (toggleItem(Long.parseLong(this.Id.getText().toString())))
                    v.setBackgroundColor(v.getContext().getResources().getColor(R.color.colorSelected));
                else
                    v.setBackgroundColor(Color.TRANSPARENT);
                actionMode = ((AppCompatActivity) v.getContext()).startSupportActionMode(AMcallback);
            }
            return true;
        }
    }
}
