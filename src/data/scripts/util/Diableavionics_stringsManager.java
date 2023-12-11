/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;

public class Diableavionics_stringsManager {   
    private static final String ML="diableavionics";    
    
    public static String txt(String id){
        return Global.getSettings().getString(ML, id);
    }
}