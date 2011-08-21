package org.measureit.androet.ui;

import android.content.Context;
import android.content.Intent;
import java.io.Serializable;

/**
 *
 * @author ezsi
 */
public class ActivitySwitch {
    private Intent intent;
    private Context context;

    protected ActivitySwitch(Context context, Class clazz) {
        intent = new Intent(context, clazz);
        this.context = context;
    }

    public static ActivitySwitch to(Context context, Class clazz){
        return new ActivitySwitch(context, clazz);
    }
    
    public ActivitySwitch add(String parameterName, Serializable parameterValue){
        intent.putExtra(parameterName, parameterValue);
        return this;
    }
    
    public void execute(){
        context.startActivity(intent);
    }
    
    
}
