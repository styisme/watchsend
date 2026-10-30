package org.watchsend.watch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.Locale;

import org.watchsend.MainActivity;
import org.watchsend.OldSendService;
import org.watchsend.R;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;

public class WatchSettingsActivity extends Activity {
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WatchUiUtils.applyScale(this);
        super.onCreate(savedInstanceState);
        I18nManager.init(this);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_watch_settings);
        org.watchsend.watch.WatchUiUtils.applyEntryAnimation(this);
        
        // 如果开启了圆屏优化，隐藏顶部标题和返回按钮
        boolean isRoundOpt = getSharedPreferences("oldsend", MODE_PRIVATE).getBoolean("round_screen_opt", false);
        View headerBar = findViewById(R.id.settings_header);
        if (headerBar != null) {
            headerBar.setVisibility(isRoundOpt ? View.GONE : View.VISIBLE);
        }

        ThemeUtils.applyTheme(this);
        
        boolean isDark = ThemeUtils.isDarkMode(this);
        View root = findViewById(R.id.settings_root);
        root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
        WatchUiUtils.applyRoundPadding(this, root);

        container = (LinearLayout) findViewById(R.id.settings_container);
        ScrollView scrollView = (ScrollView) findViewById(R.id.settings_scroll);
        WatchUiUtils.setupWheelScroll(scrollView, container);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        buildSettings();
    }

    private android.content.SharedPreferences prefs() {
        return getSharedPreferences("oldsend", MODE_PRIVATE);
    }

    private interface SwitchHandler { void onChanged(boolean v); }

    private SwitchHandler saveTo(final String key) {
        return new SwitchHandler() {
            public void onChanged(boolean v) { prefs().edit().putBoolean(key, v).commit(); }
        };
    }

    private void buildSettings() {
        container.removeAllViews();
        
        addMainTitle(I18nManager.get("settings_title"));
        addSection(I18nManager.get("general"));

        addSwitch(I18nManager.get("low_perf_mode"), I18nManager.get("low_perf_desc"), prefs().getBoolean("low_perf_mode", false), saveTo("low_perf_mode"));
        addSwitch(I18nManager.get("round_screen_opt"), I18nManager.get("round_screen_opt_desc"), prefs().getBoolean("round_screen_opt", false), new SwitchHandler() {
            public void onChanged(boolean v) { prefs().edit().putBoolean("round_screen_opt", v).commit(); recreate(); }
        });

        addSwitch(I18nManager.get("block_system_back"), I18nManager.get("block_system_back_desc"), prefs().getBoolean("block_system_back", true), saveTo("block_system_back"));
        addSwitch(I18nManager.get("dark_mode"), I18nManager.get("dark_mode_desc"), prefs().getBoolean("dark_mode", false), new SwitchHandler() {
            public void onChanged(boolean v) { prefs().edit().putBoolean("dark_mode", v).commit(); recreate(); }
        });
        
        addItem(I18nManager.get("lang_title"), currentLangLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickLang(); }
        });
        
        addItem(I18nManager.get("ui_size"), String.format(Locale.US, "%.2f", prefs().getFloat("ui_scale", 1.0f)) + "x", new View.OnClickListener() {
            public void onClick(View v) { showUiScaleDialog(); }
        });

        addSection(I18nManager.get("transfer_settings"));
        addSwitch(I18nManager.get("auto_save"), I18nManager.get("auto_save_desc"), OldSendService.getPrefBooleanGlobal("auto_save", false), saveTo("auto_save"));
        addSwitch(I18nManager.get("auto_whitelist"), I18nManager.get("auto_whitelist_desc"), OldSendService.getPrefBooleanGlobal("auto_accept_whitelist", false), saveTo("auto_accept_whitelist"));
        addSwitch(I18nManager.get("save_album"), I18nManager.get("save_album_desc"), OldSendService.getPrefBooleanGlobal("save_album", true), saveTo("save_album"));
        addSwitch(I18nManager.get("save_history"), I18nManager.get("save_history_desc"), OldSendService.getPrefBooleanGlobal("save_history", true), saveTo("save_history"));
        addSwitch(I18nManager.get("use_pin"), I18nManager.get("use_pin_desc"), OldSendService.getPrefBooleanGlobal("use_pin", false), saveTo("use_pin"));
        
        addItem(I18nManager.get("modify_pin"), prefs().getString("pin_code", "0000"), new View.OnClickListener() {
            public void onClick(View v) { editPin(); }
        });

        addSection(I18nManager.get("network"));
        addSwitch(I18nManager.get("https_enable"), I18nManager.get("https_enable_desc"), prefs().getBoolean("use_https", false), new SwitchHandler() {
            public void onChanged(boolean v) { prefs().edit().putBoolean("use_https", v).commit(); }
        });
        addSwitch(I18nManager.get("web_enable"), I18nManager.get("web_enable_desc"), OldSendService.getPrefBooleanGlobal("web_enable", true), saveTo("web_enable"));
        addSwitch(I18nManager.get("auto_start"), I18nManager.get("auto_start_desc"), OldSendService.getPrefBooleanGlobal("auto_start", false), saveTo("auto_start"));
        addSwitch(I18nManager.get("receive_notif"), I18nManager.get("receive_notif_desc"), OldSendService.getPrefBooleanGlobal("receive_notif", false), saveTo("receive_notif"));

        addItem(I18nManager.get("port_settings"), String.valueOf(prefs().getInt("port", 53317)), new View.OnClickListener() {
            public void onClick(View v) { editPort(); }
        });
        addItem(I18nManager.get("timeout_settings"), String.valueOf(prefs().getInt("timeout", 500)) + " ms", new View.OnClickListener() {
            public void onClick(View v) { editTimeout(); }
        });

        addSection(I18nManager.get("device_security"));
        addItem(I18nManager.get("device_name_title"), OldSendService.getAlias(this), new View.OnClickListener() {
            public void onClick(View v) { editName(); }
        });
        addItem(I18nManager.get("filter_ext_title"), OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh"), new View.OnClickListener() {
            public void onClick(View v) { editFilter(); }
        });
        addItem(I18nManager.get("manage_acl"), I18nManager.get("manage_acl_desc"), new View.OnClickListener() {
            public void onClick(View v) { manageAcl(); }
        });

        addSection(I18nManager.get("other"));
        addItem(I18nManager.get("about"), "v2.0.0", new View.OnClickListener() {
            public void onClick(View v) { aboutDialog(); }
        });
    }

    private void addMainTitle(String title) {
        TextView tv = new TextView(this);
        tv.setText(title); tv.setTextSize(24);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFE0E0E0 : 0xFF1C1B1F);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        tv.setLayoutParams(lp);
        container.addView(tv);
    }

    private void addSection(String title) {
        TextView tv = new TextView(this);
        tv.setText(title); tv.setTextSize(13);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFAAAAAA : 0xFF666666);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = dp(16); lp.topMargin = dp(16); lp.bottomMargin = dp(8);
        tv.setLayoutParams(lp);
        container.addView(tv);
    }

    private void addSwitch(String title, String subtitle, boolean value, final SwitchHandler handler) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(WatchUiUtils.getPillBackground(this));
        row.setPadding(dp(20), dp(12), dp(20), dp(12));
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title); tvTitle.setTextSize(16);
        tvTitle.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFE0E0E0 : 0xFF1C1B1F);
        textContainer.addView(tvTitle);

        if (subtitle != null) {
            TextView tvSub = new TextView(this);
            tvSub.setText(subtitle); tvSub.setTextSize(12);
            tvSub.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFAAAAAA : 0xFF666666);
            tvSub.setPadding(0, dp(2), 0, 0);
            textContainer.addView(tvSub);
        }
        row.addView(textContainer);

        org.watchsend.ui.Md3Switch cb = new org.watchsend.ui.Md3Switch(this);
        cb.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(26)));
        cb.setChecked(value);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean c) { handler.onChanged(c); }
        });
        row.addView(cb);
        container.addView(row);
    }

    private void addItem(String title, String subtitle, View.OnClickListener click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(WatchUiUtils.getPillBackground(this));
        row.setPadding(dp(20), dp(12), dp(20), dp(12));
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title); tvTitle.setTextSize(16);
        tvTitle.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFE0E0E0 : 0xFF1C1B1F);
        textContainer.addView(tvTitle);

        if (subtitle != null) {
            TextView tvSub = new TextView(this);
            tvSub.setText(subtitle); tvSub.setTextSize(12);
            tvSub.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFAAAAAA : 0xFF666666);
            tvSub.setPadding(0, dp(2), 0, 0);
            textContainer.addView(tvSub);
        }
        row.addView(textContainer);

        TextView arrow = new TextView(this);
        arrow.setText(">"); arrow.setTextSize(18); 
        arrow.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFF81C784 : 0xFF999999);
        row.addView(arrow);

        row.setOnClickListener(click);
        container.addView(row);
    }

    private void showUiScaleDialog() {
        final float currentScale = prefs().getFloat("ui_scale", 1.0f);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));

        final TextView tvValue = new TextView(this);
        tvValue.setText(String.format(Locale.US, "%.2f", currentScale) + "x");
        tvValue.setTextSize(18); tvValue.setGravity(Gravity.CENTER);
        tvValue.setTextColor(0xFF388E3C);
        layout.addView(tvValue);

        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        sb.setProgress(Math.max(0, Math.min(100, (int)((currentScale - 0.5f) * 100))));
        
        int thumbSize = dp(24);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(3)); bg.setColor(0xFF3E3E3E); bg.setSize(0, dp(6));
        GradientDrawable progress = new GradientDrawable();
        progress.setCornerRadius(dp(3)); progress.setColor(0xFF388E3C); progress.setSize(0, dp(6));
        ClipDrawable clip = new ClipDrawable(progress, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{bg, clip});
        ld.setId(0, android.R.id.background); ld.setId(1, android.R.id.progress);
        sb.setProgressDrawable(ld);

        GradientDrawable thumb = new GradientDrawable();
        thumb.setShape(GradientDrawable.OVAL); thumb.setColor(0xFFFFFFFF); thumb.setStroke(dp(3), 0xFF388E3C); thumb.setSize(thumbSize, thumbSize);
        sb.setThumb(thumb);
        sb.setSplitTrack(false);
        sb.setPadding(thumbSize/2, 0, thumbSize/2, 0);
        
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int p, boolean f) {
                tvValue.setText(String.format(Locale.US, "%.2f", 0.5f + (p/100f)) + "x");
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {
                prefs().edit().putFloat("ui_scale", 0.5f + (seekBar.getProgress()/100f)).commit();
                recreate();
            }
        });
        layout.addView(sb);

        WatchUiUtils.createBaseDialog(this, I18nManager.get("ui_scale_dialog_title"), layout, I18nManager.get("confirm"), null, null, null).show();
    }

    private int dp(int val) { return (int)(val * getResources().getDisplayMetrics().density); }

    private String currentLangLabel() {
        String lang = prefs().getString("app_lang", "system");
        String[] codes = {"system","zh","zh_TW","en","ja","ko","de","fr","es","ru","pt"};
        String[] names = {"跟随系统","简体中文","繁體中文","English","日本語","한국어","Deutsch","Français","Español","Русский","Português"};
        for (int i = 0; i < codes.length; i++) if (codes[i].equals(lang)) return names[i];
        return lang;
    }

    private void editPin() {
        WatchUiUtils.showInput(this, I18nManager.get("modify_pin"), prefs().getString("pin_code", "0000"), new WatchUiUtils.OnInputListener() {
            public void onInput(String text) {
                if (text.length() > 0) { prefs().edit().putString("pin_code", text).commit(); recreate(); }
            }
        });
    }

    private void editPort() {
        WatchUiUtils.showInput(this, I18nManager.get("port_dialog_title"), String.valueOf(prefs().getInt("port", 53317)), new WatchUiUtils.OnInputListener() {
            public void onInput(String text) {
                try { prefs().edit().putInt("port", Integer.parseInt(text)).commit(); recreate(); } catch (Exception ignored) {}
            }
        });
    }

    private void editTimeout() {
        WatchUiUtils.showInput(this, I18nManager.get("timeout_dialog_title"), String.valueOf(prefs().getInt("timeout", 500)), new WatchUiUtils.OnInputListener() {
            public void onInput(String text) {
                try { prefs().edit().putInt("timeout", Integer.parseInt(text)).commit(); recreate(); } catch (Exception ignored) {}
            }
        });
    }

    private void editFilter() {
        WatchUiUtils.showInput(this, I18nManager.get("filter_dialog_title"), OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh"), new WatchUiUtils.OnInputListener() {
            public void onInput(String text) { prefs().edit().putString("filter_ext", text.replace(" ","").toLowerCase()).commit(); recreate(); }
        });
    }

    private void editName() {
        WatchUiUtils.showInput(this, I18nManager.get("device_name_title"), OldSendService.getAlias(this), new WatchUiUtils.OnInputListener() {
            public void onInput(String text) {
                if (text.length() > 0) { OldSendService.setAlias(WatchSettingsActivity.this, text); recreate(); }
            }
        });
    }

    private void pickLang() {
        final String[] items = {"跟随系统","简体中文","繁體中文","English","日本語","한국어","Deutsch","Français","Español","Русский","Português"};
        WatchUiUtils.showList(this, I18nManager.get("lang_title"), items, new WatchUiUtils.OnItemSelectListener() {
            public void onItemSelect(int pos) {
                String[] codes = {"system","zh","zh_TW","en","ja","ko","de","fr","es","ru","pt"};
                prefs().edit().putString("app_lang", codes[pos]).commit();
                Toast.makeText(WatchSettingsActivity.this, "语言已切换", Toast.LENGTH_SHORT).show();
                finish(); // 直接 finish 退回主页，主页面 onResume 会检测到并自动重建
            }
        }).show();
    }

    private void manageAcl() {
        String[] items = {I18nManager.get("view_whitelist"), I18nManager.get("view_blacklist"), I18nManager.get("clear_all")};
        WatchUiUtils.showList(this, I18nManager.get("acl_dialog_title"), items, new WatchUiUtils.OnItemSelectListener() {
            public void onItemSelect(int pos) {
                if (pos == 2) { prefs().edit().remove("whitelist").remove("blacklist").commit(); recreate(); }
                else showSpecificList(pos == 0 ? "whitelist" : "blacklist");
            }
        }).show();
    }

    private void showSpecificList(final String key) {
        String s = OldSendService.getPrefStringGlobal(key, "");
        if (s.trim().isEmpty()) { Toast.makeText(this, I18nManager.get("list_empty"), Toast.LENGTH_SHORT).show(); return; }
        final String[] arr = s.split(",");
        WatchUiUtils.showList(this, key.equals("whitelist") ? I18nManager.get("whitelist_title") : I18nManager.get("blacklist_title"), arr, new WatchUiUtils.OnItemSelectListener() {
            public void onItemSelect(int pos) {
                String cur = OldSendService.getPrefStringGlobal(key, "");
                cur = cur.replace(arr[pos] + ",", "").replace(arr[pos], "");
                prefs().edit().putString(key, cur).commit();
                showSpecificList(key);
            }
        }).show();
    }

    private void aboutDialog() {
        TextView tv = new TextView(this);
        tv.setText("WatchSend · Watch Edition\nv2.0.0\n\n作者: styisme\nAI 辅助生成\nMIT License\n\n兼容 LocalSend v2.2");
        tv.setTextSize(13); tv.setTextColor(0xFFE0E0E0); tv.setLineSpacing(0, 1.3f);
        WatchUiUtils.createBaseDialog(this, I18nManager.get("about_title"), tv, I18nManager.get("confirm"), null, null, null).show();
    }


    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (org.watchsend.watch.WatchUiUtils.handleSwipeBack(this, ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (org.watchsend.watch.WatchUiUtils.isSystemBackBlocked(this)) {
            android.widget.Toast.makeText(this, "请滑动屏幕返回", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}
