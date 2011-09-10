package org.measureit.androet;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * 
 * @author ezsi
 */
public class Preferences extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
