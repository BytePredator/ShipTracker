package it.univaq.byte_predator.shiptracker.Helper;

import java.util.ArrayList;

/**
 * Created by byte-predator on 21/02/18.
 */

public interface DataCallback<T>{
     void callback(ArrayList<T> data, boolean changed);
}
