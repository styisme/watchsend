package org.watchsend.uikit;

import org.watchsend.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
public class UikitFullConfirmActivity extends Activity {
    public static final String EXTRA_TITLE = "uikit_title";
    public static final String EXTRA_MESSAGE = "uikit_message";
    public static void show(Activity activity, String title, String message, int requestCode) {
        Intent intent = new Intent(activity, UikitFullConfirmActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_MESSAGE, message);
        activity.startActivityForResult(intent, requestCode);
    }
    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(UikitUi.wrapScale(newBase)); }
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        UikitTheme.applyTheme(this);
        UikitUi.applyEntryAnimation(this);
        boolean dark = UikitTheme.isDarkMode(this);
        int density = (int) getResources().getDisplayMetrics().density;
        int bgColor = dark ? 0xFF000000 : 0xFFF4F6F8;
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(bgColor));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor);
        root.setLayoutParams(new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        scroll.addView(root);
        setContentView(scroll);
        UikitUi.applyRoundPadding(this, root);
        UikitUi.setupWheelScroll(scroll, root);
        // 中间文字
        LinearLayout centerBox = new LinearLayout(this);
        centerBox.setOrientation(LinearLayout.VERTICAL);
        centerBox.setGravity(Gravity.CENTER);
        centerBox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        TextView tvTitle = new TextView(this);
        tvTitle.setText(getIntent().getStringExtra(EXTRA_TITLE));
        tvTitle.setTextSize(22);
        tvTitle.setTextColor(dark ? 0xFFFFFFFF : 0xFF000000);
        tvTitle.setGravity(Gravity.CENTER);
        centerBox.addView(tvTitle);
        String msg = getIntent().getStringExtra(EXTRA_MESSAGE);
        if (msg != null && !msg.isEmpty()) {
            TextView tvMsg = new TextView(this);
            tvMsg.setText(msg);
            tvMsg.setTextSize(14);
            tvMsg.setTextColor(dark ? 0xFFAAAAAA : 0xFF666666);
            tvMsg.setGravity(Gravity.CENTER);
            tvMsg.setPadding(0, (int)(8*density), 0, 0);
            centerBox.addView(tvMsg);
        }
        root.addView(centerBox);
        // 底部按钮
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        btnRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        btnRow.setPadding(0, 0, 0, (int)(32*density));
        TextView btnNo = new TextView(this);
        btnNo.setText("×");
        btnNo.setTextSize(28);
        btnNo.setGravity(Gravity.CENTER);
        btnNo.setTextColor(dark ? 0xFFFFFFFF : 0xFF000000);
        GradientDrawable noBg = new GradientDrawable();
        noBg.setShape(GradientDrawable.OVAL);
        noBg.setColor(dark ? 0xFF2C2C2E : 0xFFE0E0E0);
        btnNo.setBackground(noBg);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams((int)(64*density), (int)(64*density));
        lp1.rightMargin = (int)(24*density);
        btnNo.setLayoutParams(lp1);
        btnNo.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { setResult(RESULT_CANCELED); finish(); overridePendingTransition(0,0); } });
        TextView btnYes = new TextView(this);
        btnYes.setText("✓");
        btnYes.setTextSize(28);
        btnYes.setGravity(Gravity.CENTER);
        btnYes.setTextColor(UikitColor.getOnPrimaryColor(this));
        GradientDrawable yesBg = new GradientDrawable();
        yesBg.setShape(GradientDrawable.OVAL);
        yesBg.setColor(UikitColor.getPrimaryColor(this));
        btnYes.setBackground(yesBg);
        btnYes.setLayoutParams(new LinearLayout.LayoutParams((int)(64*density), (int)(64*density)));
        btnYes.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { setResult(RESULT_OK); finish(); overridePendingTransition(0,0); } });
        btnRow.addView(btnNo);
        btnRow.addView(btnYes);
        root.addView(btnRow);
    }
    @Override public boolean dispatchTouchEvent(android.view.MotionEvent ev) { if (UikitUi.handleSwipeBack(this, ev)) return true; return super.dispatchTouchEvent(ev); }

    @Override
    public void onBackPressed() {
        // 开启“软件内返回”时吞掉返回键，交给界面内横滑退出；关闭时交回系统
        if (org.watchsend.uikit.BackGate.handleBack(this)) return;
        super.onBackPressed();
    }
}
