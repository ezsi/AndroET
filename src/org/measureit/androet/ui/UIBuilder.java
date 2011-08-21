package org.measureit.androet.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 *
 * @author ezsi
 */
public class UIBuilder {

    public static LinearLayout createViewWithLabel(Context context, String label, View view){
        TextView textLabel = new TextView(context);
        textLabel.setText(label);
        return createVerticalView(context, textLabel, view);
    }

    public static TableLayout createHorizontalView(Context context, View ... views){
        TableLayout layout = new TableLayout(context);
        TableRow tableRow = new TableRow(context);
        for(int i=0, n = views.length; i<n; i++){
            layout.setColumnStretchable(i, true);
            tableRow.addView(views[i]);
        }
        layout.addView(tableRow);
        return layout;
    }
    
    public static LinearLayout createVerticalView(Context context, View ... views){
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        for(int i=0, n = views.length; i<n; i++)
            layout.addView(views[i]);
        return layout;
    }
    
    public static Button createButton(Context context, String label, OnClickListener onClickListener){
        Button button = new Button(context);
        button.setText(label);
        button.setOnClickListener(onClickListener);
        return button;
    }
    
}
