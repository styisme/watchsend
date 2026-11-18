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
import android.widget.TextView;
import java.util.Calendar;
public class UikitDatePickerActivity extends Activity {
    public static final String EXTRA_RESULT_YEAR = "res_year";
    public static final String EXTRA_RESULT_MONTH = "res_month";
    public static final String EXTRA_RESULT_DAY = "res_day";
    public static void show(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, UikitDatePickerActivity.class), requestCode);
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
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor);
        setContentView(root);
        UikitUi.applyRoundPadding(this, root);
        
        Calendar cal = Calendar.getInstance();
        int curYear = cal.get(Calendar.YEAR);
        
        // 支持过去 50 年，未来 50 年
        int startYear = curYear - 50;
        int endYear = curYear + 50;
        int yearCount = endYear - startYear + 1;
        String[] years = new String[yearCount];
        for (int i = 0; i < yearCount; i++) {
            years[i] = String.valueOf(startYear + i);
        }
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) months[i] = String.format("%02d", i + 1);
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);
        
        LinearLayout pickerRow = new LinearLayout(this);
        pickerRow.setOrientation(LinearLayout.HORIZONTAL);
        pickerRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        
        final UikitPicker yp = new UikitPicker(this); yp.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.2f)); yp.setItems(years);
        final UikitPicker mp = new UikitPicker(this); mp.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)); mp.setItems(months);
        final UikitPicker dp = new UikitPicker(this); dp.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)); dp.setItems(days);
        
        // 默认定位到当前日期
        yp.setSelectedIndex(50);
        mp.setSelectedIndex(cal.get(Calendar.MONTH));
        dp.setSelectedIndex(cal.get(Calendar.DAY_OF_MONTH) - 1);
        
        pickerRow.addView(yp); pickerRow.addView(mp); pickerRow.addView(dp);
        root.addView(pickerRow);
        
        TextView btnOk = new TextView(this);
        btnOk.setText("✓");
        btnOk.setTextSize(28);
        btnOk.setGravity(Gravity.CENTER);
        btnOk.setTextColor(UikitColor.getOnPrimaryColor(this));
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(UikitColor.getPrimaryColor(this));
        btnOk.setBackground(bg);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams((int)(64 * density), (int)(64 * density));
        btnLp.gravity = Gravity.CENTER_HORIZONTAL;
        btnLp.bottomMargin = (int)(16 * density);
        btnOk.setLayoutParams(btnLp);
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_RESULT_YEAR, yp.getSelectedItem());
                data.putExtra(EXTRA_RESULT_MONTH, mp.getSelectedItem());
                data.putExtra(EXTRA_RESULT_DAY, dp.getSelectedItem());
                setResult(RESULT_OK, data);
                finish();
                overridePendingTransition(0, 0);
            }
        });
        root.addView(btnOk);
    }
    @Override public boolean dispatchTouchEvent(android.view.MotionEvent ev) { if (UikitUi.handleSwipeBack(this, ev)) return true; return super.dispatchTouchEvent(ev); }
}
