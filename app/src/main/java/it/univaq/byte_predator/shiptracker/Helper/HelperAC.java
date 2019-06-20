package it.univaq.byte_predator.shiptracker.Helper;

import android.app.Application;
import android.content.Context;

public class HelperAC extends Application {
    private static Context context;

    public static Context getContext(){
        return HelperAC.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HelperAC.context = getApplicationContext();
    }
}
