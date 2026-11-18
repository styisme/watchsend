package org.watchsend.uikit;

import org.watchsend.R;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
public class UikitEmptyView extends LinearLayout {
    public UikitEmptyView(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setPadding(16, 32, 16, 32);
        
        TextView icon = new TextView(ctx);
        icon.setText("👻");
        icon.setTextSize(48);
        icon.setGravity(Gravity.CENTER);
        addView(icon);
        
        TextView title = new TextView(ctx);
        title.setText("No Data");
        title.setTextSize(18);
        title.setTextColor(UikitTheme.isDarkMode(ctx) ? 0xFFE0E0E0 : 0xFF1C1B1F);
        title.setPadding(0, 16, 0, 8);
        title.setGravity(Gravity.CENTER);
        addView(title);
        
        TextView sub = new TextView(ctx);
        sub.setText("Please check back later");
        sub.setTextSize(14);
        sub.setTextColor(UikitTheme.isDarkMode(ctx) ? 0xFFAAAAAA : 0xFF666666);
        sub.setGravity(Gravity.CENTER);
        addView(sub);
    }
}
