package it.univaq.byte_predator.shiptracker.Controllers.Main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import it.univaq.byte_predator.shiptracker.Controllers.Track.NewTrackActivity;
import it.univaq.byte_predator.shiptracker.R;

/**
 * Created by byte-predator on 22/02/18.
 */

public class AddTrackDialog extends DialogFragment {

    private EditText ed;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.add_track_dialog, null);

        ed = view.findViewById(R.id.new_track_name);
        InputFilter[] f = {new InputFilter.LengthFilter(20)};
        ed.setFilters(f);

        builder.setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddTrackDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary, null));
                d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary, null));
            }
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!ed.getText().toString().isEmpty()) {
                        Intent intent = new Intent(getActivity(), NewTrackActivity.class);
                        intent.putExtra("Name", ed.getText().toString());
                        dismiss();
                        getActivity().startActivityForResult(intent , MainActivity.ADD_TRACK_REQUEST);
                    }
                }
            });
        }
    }
}
