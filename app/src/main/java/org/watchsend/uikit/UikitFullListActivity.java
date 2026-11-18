package org.watchsend.uikit;

import org.watchsend.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;

public class UikitFullListActivity extends Activity {
    public static final String EXTRA_TITLE = "uikit_title";
    public static final String EXTRA_ITEMS = "uikit_items";
    public static final String EXTRA_RESULT_INDEX = "uikit_result_index";
    public static final String EXTRA_RESULT_TEXT = "uikit_result_text";

    public static void show(Activity activity, String title, String[] items, int requestCode) {
        ArrayList<UikitListItemModel> models = new ArrayList<UikitListItemModel>();
        for (String s : items) models.add(new UikitListItemModel(s));
        show(activity, title, models, requestCode);
    }

    public static void show(Activity activity, String title, ArrayList<UikitListItemModel> items, int requestCode) {
        Intent intent = new Intent(activity, UikitFullListActivity.class);
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
        ArrayList<UikitListItemModel> itemsExtra = (ArrayList<UikitListItemModel>) getIntent().getSerializableExtra(EXTRA_ITEMS);
        final ArrayList<UikitListItemModel> items = itemsExtra == null ? new ArrayList<UikitListItemModel>() : itemsExtra;

        boolean dark = UikitTheme.isDarkMode(this);
        int density = (int) getResources().getDisplayMetrics().density;
        int bgColor = dark ? 0xFF000000 : 0xFFF4F6F8;

        // 设置 Window 背景，防止漏出系统灰色
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(bgColor));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        root.setBackgroundColor(bgColor);
        
        scroll.addView(root);
        setContentView(scroll);

        UikitUi.applyRoundPadding(this, root);
        UikitUi.setupWheelScroll(scroll, root);

        // 标题
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(20);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(UikitColor.getAccentColor(this, dark));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLp.topMargin = UikitScreenUtils.dp2px(this, 16);
        titleLp.bottomMargin = UikitScreenUtils.dp2px(this, 16);
        tvTitle.setLayoutParams(titleLp);
        root.addView(tvTitle);

        // 列表
        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            final UikitListItemModel model = items.get(i);

            LinearLayout itemCard = new LinearLayout(this);
            itemCard.setOrientation(LinearLayout.VERTICAL);
            itemCard.setPadding(UikitScreenUtils.dp2px(this, 16), UikitScreenUtils.dp2px(this, 12),
                    UikitScreenUtils.dp2px(this, 16), UikitScreenUtils.dp2px(this, 12));

            if (model.cardColor != 0) {
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setCornerRadius(32 * density);
                bg.setColor(model.cardColor);
                itemCard.setBackground(bg);
            } else {
                itemCard.setBackground(UikitUi.getPillBackground(this));
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = UikitScreenUtils.dp2px(this, 16);
            lp.rightMargin = UikitScreenUtils.dp2px(this, 16);
            lp.bottomMargin = UikitScreenUtils.dp2px(this, 12);
            itemCard.setLayoutParams(lp);

            // 主标题
            TextView tvMain = new TextView(this);
            tvMain.setText(model.title);
            tvMain.setTextSize(16);
            if (model.accentColor != 0) {
                tvMain.setTextColor(model.accentColor);
            } else if (model.cardColor != 0) {
                int r = (model.cardColor >> 16) & 0xFF;
                int g = (model.cardColor >> 8) & 0xFF;
                int b = model.cardColor & 0xFF;
                int y = (r * 299 + g * 587 + b * 114) / 1000;
                tvMain.setTextColor(y > 160 ? 0xFF1C1B1F : 0xFFFFFFFF);
            } else {
                tvMain.setTextColor(dark ? 0xFFE0E0E0 : 0xFF1C1B1F);
            }
            tvMain.setGravity(model.subtitle != null ? Gravity.START : Gravity.CENTER);
            itemCard.addView(tvMain);

            // 副标题
            if (model.subtitle != null) {
                TextView tvSub = new TextView(this);
                tvSub.setText(model.subtitle);
                tvSub.setTextSize(12);
                if (model.accentColor == 0 && model.cardColor != 0) {
                    int r = (model.cardColor >> 16) & 0xFF;
                    int g = (model.cardColor >> 8) & 0xFF;
                    int b = model.cardColor & 0xFF;
                    int y = (r * 299 + g * 587 + b * 114) / 1000;
                    tvSub.setTextColor(y > 160 ? 0x99000000 : 0x99FFFFFF);
                } else {
                    tvSub.setTextColor(dark ? 0xFFAAAAAA : 0xFF666666);
                }
                tvSub.setPadding(0, UikitScreenUtils.dp2px(this, 4), 0, 0);
                itemCard.addView(tvSub);
            }

            itemCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_RESULT_INDEX, index);
                    data.putExtra(EXTRA_RESULT_TEXT, model.title);
                    setResult(RESULT_OK, data);
                    finish();
                    overridePendingTransition(0, 0);
                }
            });
            root.addView(itemCard);
        }
        
        View bottomSpace = new View(this);
        bottomSpace.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, UikitScreenUtils.dp2px(this, 32)));
        root.addView(bottomSpace);
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (UikitUi.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }
}
