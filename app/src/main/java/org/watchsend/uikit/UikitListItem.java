package org.watchsend.uikit;

import org.watchsend.R;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton;

public class UikitListItem extends LinearLayout {
    private TextView tvTitle;
    private TextView tvSubtitle;
    private UikitSwitch switchView;
    
    public UikitListItem(Context ctx, String title, String subtitle) {
        super(ctx);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(UikitScreenUtils.dp2px(ctx, 16), UikitScreenUtils.dp2px(ctx, 12),
                   UikitScreenUtils.dp2px(ctx, 16), UikitScreenUtils.dp2px(ctx, 12));
        setBackground(UikitUi.getPillBackground(ctx));
        
        LinearLayout textCol = new LinearLayout(ctx);
        textCol.setOrientation(VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        
        tvTitle = new TextView(ctx);
        tvTitle.setText(title);
        tvTitle.setTextSize(16);
        tvTitle.setTextColor(UikitTheme.isDarkMode(ctx) ? 0xFFE0E0E0 : 0xFF1C1B1F);
        textCol.addView(tvTitle);
        
        if (subtitle != null) {
            tvSubtitle = new TextView(ctx);
            tvSubtitle.setText(subtitle);
            tvSubtitle.setTextSize(12);
            tvSubtitle.setTextColor(UikitTheme.isDarkMode(ctx) ? 0xFFAAAAAA : 0xFF666666);
            textCol.addView(tvSubtitle);
        }
        addView(textCol);
    }
    
    public void setSwitch(boolean checked, CompoundButton.OnCheckedChangeListener listener) {
        switchView = new UikitSwitch(getContext());
        switchView.setLayoutParams(new LinearLayout.LayoutParams(
                UikitScreenUtils.dp2px(getContext(), 44), UikitScreenUtils.dp2px(getContext(), 26)));
        switchView.setChecked(checked);
        switchView.setOnCheckedChangeListener(listener);
        addView(switchView);
    }
    
    public void setArrow() {
        TextView arrow = new TextView(getContext());
        arrow.setText(">");
        arrow.setTextSize(18);
        arrow.setTextColor(UikitColor.getPrimaryColor(getContext()));
        addView(arrow);
    }
}
