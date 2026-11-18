package org.watchsend.uikit;

import org.watchsend.R;
import android.content.Context;
import android.view.View;
public class UikitDivider extends View {
    public UikitDivider(Context context) {
        super(context);
        setBackgroundColor(UikitTheme.isDarkMode(context) ? 0xFF3E3E3E : 0xFFE0E0E0);
        setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
                UikitScreenUtils.dp2px(context, 1)));
        setPadding(UikitScreenUtils.dp2px(context, 16), 0, UikitScreenUtils.dp2px(context, 16), 0);
    }
}
