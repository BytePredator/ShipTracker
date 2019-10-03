package it.univaq.byte_predator.shiptracker.Helper;

public class HelperTime {
    public static String unix2str(int time){
        Integer H = time/3600;
        Integer M = time%3600/60;
        Integer S = time%60;
        return String.format("%02d:%02d:%02d", H, M, S);
    }
}
