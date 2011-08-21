package org.measureit.androet.ui;

import android.content.Context;
import android.widget.TextView;

/**
 *
 * @author ezsi
 */
public class TextViewBuilder {
    private TextView textView;

    public TextViewBuilder(Context context, String text) {
        textView = new TextView(context);
        textView.setText(text);
    }

    public TextView build(){
        return textView;
    }
    
    public static TextViewBuilder text(Context context, String text){
        return new TextViewBuilder(context, text);
    }
    
    public TextViewBuilder size(int size){
        textView.setTextSize(size);
        return this;
    }
    
    public TextViewBuilder color(int color){
        textView.setTextColor(color);
        return this;
    }
    
    public TextViewBuilder gravity(int gravity){
        textView.setGravity(gravity);
        return this;
    }
}
