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

public class UikitPickerActivity extends Activity {
    public static final String EXTRA_TITLE = "uikit_title";
    public static final String EXTRA_ITEMS = "uikit_items";
    public static final String EXTRA_RESULT_INDEX = "uikit_result_index";
    public static final String EXTRA_RESULT_TEXT = "uikit_result_text";

    public static void show(Activity activity, String title, String[] items, int requestCode) {
        Intent intent = new Intent(activity, UikitPickerActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_ITEMS, items);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(UikitUi.wrapScale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        UikitTheme.applyTheme(this);
        UikitUi.applyEntryAnimation(this);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        final String[] items = getIntent().getStringArrayExtra(EXTRA_ITEMS);
        
        boolean dark = UikitTheme.isDarkMode(this);
        int density = (int) getResources().getDisplayMetrics().density;
        int bgColor = dark ? 0xFF000000 : 0xFFF4F6F8;

        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(bgColor));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor);
        setContentView(root);
        
        UikitUi.applyRoundPadding(this, root);

        // 标题
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(20);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(UikitColor.getAccentColor(this, dark));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLp.topMargin = 8 * density;
        tvTitle.setLayoutParams(titleLp);
        root.addView(tvTitle);

        // 3D 滑动选择器
        UikitPicker picker = new UikitPicker(this);
        LinearLayout.LayoutParams pickerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        picker.setLayoutParams(pickerLp);
        
        final int[] selectedIndex = {0};
        picker.setOnItemSelectedListener(new UikitPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index, String item) {
                selectedIndex[0] = index;
            }
        });
        picker.setItems(items);
        root.addView(picker);

        // 底部确认按钮
        TextView btnOk = new TextView(this);
        btnOk.setText("✓");
        btnOk.setTextSize(28);
        btnOk.setGravity(Gravity.CENTER);
        btnOk.setTextColor(UikitColor.getOnPrimaryColor(this));
        
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(UikitColor.getPrimaryColor(this));
        btnOk.setBackground(bg);

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                64 * density, 64 * density);
        btnLp.gravity = Gravity.CENTER_HORIZONTAL;
        btnLp.bottomMargin = 16 * density;
        btnOk.setLayoutParams(btnLp);
        
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_RESULT_INDEX, selectedIndex[0]);
                data.putExtra(EXTRA_RESULT_TEXT, items[selectedIndex[0]]);
                setResult(RESULT_OK, data);
                finish();
                overridePendingTransition(0, 0);
            }
        });
        root.addView(btnOk);
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (UikitUi.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }
}
