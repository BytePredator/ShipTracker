package it.univaq.byte_predator.shiptracker.Controllers.Main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import it.univaq.byte_predator.shiptracker.Models.Trace;
import it.univaq.byte_predator.shiptracker.R;

/**
 * Created by byte-predator on 21/02/18.
 */

public class TracesAdapter extends RecyclerView.Adapter<TracesAdapter.ViewHolder> {

    private ArrayList<Trace> data;

    public TracesAdapter(ArrayList<Trace> data){
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trace_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Double iDistance = data.get(position).getRace().getDistance();
        holder.distance.setText(iDistance.toString()+" Km");
        Integer iTime = data.get(position).getRace().getTime();
        Integer H = iTime/3600;
        Integer M = iTime%3600/60;
        Integer S = iTime%60;
        holder.time.setText(H.toString()+":"+M.toString()+":"+S.toString());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView distance;
        TextView time;
        ViewHolder(View itemView) {
            super(itemView);

            distance = itemView.findViewById(R.id.distance);
            time = itemView.findViewById(R.id.time);
        }
    }
}
