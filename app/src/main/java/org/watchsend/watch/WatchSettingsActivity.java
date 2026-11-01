package org.watchsend.watch;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_watch_settings);
        ThemeUtils.applyTheme(this);
        WatchUiUtils.applyEntryAnimation(this);

        boolean isDark = ThemeUtils.isDarkMode(this);
        View root = findViewById(R.id.settings_root);
        if (root != null) {
            root.setBackgroundColor(isDark ? 0xFF000000 : 0xFFF4F6F8);
            WatchUiUtils.applyRoundPadding(this, root);
            
            // 在圆屏模式下隐藏顶部返回栏，依赖滑动返回
            View header = findViewById(R.id.settings_header);
            if (header != null && org.watchsend.watch.WatchHardwareUtils.isRoundScreen(root)) {
                header.setVisibility(View.GONE);
            }
        }

        container = (LinearLayout) findViewById(R.id.settings_container);
        ScrollView scrollView = (ScrollView) findViewById(R.id.settings_scroll);
        if (scrollView != null && container != null) {
            WatchUiUtils.setupWheelScroll(scrollView, container);
            org.watchsend.watch.WatchHardwareUtils.bindRotaryEncoder(scrollView);
        }
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

        addSection(I18nManager.get("general"));

        // 屏幕形状选择 (自动/强制圆/强制方)
        addItem(I18nManager.get("screen_shape") + ": " + currentShapeLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickShape(); }
        });
// 底部下巴避让 (针对 Moto 360 等有黑边的圆表)
        addSwitch(I18nManager.get("chin_padding"), I18nManager.get("chin_padding_desc"), prefs().getBoolean("chin_padding", false), saveTo("chin_padding"));

        // 动态取色 (仅在 Android 12+ 显示)
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            addSwitch(I18nManager.get("dynamic_color"), I18nManager.get("dynamic_color_desc"), prefs().getBoolean("dynamic_color", true), new SwitchHandler() {
                public void onChanged(boolean v) { prefs().edit().putBoolean("dynamic_color", v).commit(); recreate(); }
            });
        }

        // 低性能模式
        addSwitch(I18nManager.get("low_perf_mode"), I18nManager.get("low_perf_desc"), prefs().getBoolean("low_perf_mode", false), saveTo("low_perf_mode"));
        
        // 深色模式
        addSwitch(I18nManager.get("dark_mode"), I18nManager.get("dark_mode_desc"), prefs().getBoolean("dark_mode", false), new SwitchHandler() {
            public void onChanged(boolean v) { prefs().edit().putBoolean("dark_mode", v).commit(); recreate(); }
        });
        
        // 语言
        addItem(I18nManager.get("language") + ": " + currentLangLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickLang(); }
        });
        
        // UI 大小
        addItem(I18nManager.get("ui_size") + ": " + getUiSizeLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickUiSize(); }
        });

        // 拦截系统返回
        addSwitch(I18nManager.get("block_system_back"), I18nManager.get("block_system_back_desc"), prefs().getBoolean("block_system_back", true), saveTo("block_system_back"));


        addSection(I18nManager.get("transfer_settings"));
        addSwitch(I18nManager.get("auto_save"), I18nManager.get("auto_save_desc"), OldSendService.getPrefBooleanGlobal("auto_save", false), saveTo("auto_save"));
        addSwitch(I18nManager.get("auto_whitelist"), I18nManager.get("auto_whitelist_desc"), OldSendService.getPrefBooleanGlobal("auto_accept_whitelist", false), saveTo("auto_accept_whitelist"));
        addSwitch(I18nManager.get("save_album"), I18nManager.get("save_album_desc"), OldSendService.getPrefBooleanGlobal("save_album", true), saveTo("save_album"));
        addSwitch(I18nManager.get("save_history"), I18nManager.get("save_history_desc"), OldSendService.getPrefBooleanGlobal("save_history", true), saveTo("save_history"));
        addSwitch(I18nManager.get("use_pin"), I18nManager.get("use_pin_desc"), OldSendService.getPrefBooleanGlobal("use_pin", false), saveTo("use_pin"));
        addItem(I18nManager.get("modify_pin") + " (" + prefs().getString("pin_code", "0000") + ")", new View.OnClickListener() {
            public void onClick(View v) { editPin(); }
        });


        addSection(I18nManager.get("network"));
        addSwitch(I18nManager.get("https_enable"), I18nManager.get("https_enable_desc"), prefs().getBoolean("use_https", false), new SwitchHandler() {
            public void onChanged(boolean v) { prefs().edit().putBoolean("use_https", v).commit(); }
        });
        addSwitch(I18nManager.get("web_enable"), I18nManager.get("web_enable_desc"), OldSendService.getPrefBooleanGlobal("web_enable", true), saveTo("web_enable"));
        addSwitch(I18nManager.get("auto_start"), I18nManager.get("auto_start_desc"), OldSendService.getPrefBooleanGlobal("auto_start", false), saveTo("auto_start"));
        addItem(I18nManager.get("port") + ": " + prefs().getInt("port", 53317), new View.OnClickListener() {
            public void onClick(View v) { editPort(); }
        });


        addSection(I18nManager.get("device_security"));
        addItem(I18nManager.get("set_name") + ": " + OldSendService.getAlias(this), new View.OnClickListener() {
            public void onClick(View v) { editName(); }
        });
        addItem(I18nManager.get("filter_ext_title") + ": " + OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh"), new View.OnClickListener() {
            public void onClick(View v) { editFilter(); }
        });
        addItem(I18nManager.get("manage_acl"), new View.OnClickListener() {
            public void onClick(View v) { manageAcl(); }
        });


        // 极客模式 (Root/Shizuku)
        if (org.watchsend.util.ShellUtils.isRootAvailable() || org.watchsend.util.ShellUtils.isShizukuAvailable()) {
            addSection(I18nManager.get("geek_mode"));
            addSwitch(I18nManager.get("silent_install") + " (Beta)", I18nManager.get("silent_install_desc"), prefs().getBoolean("silent_install", false), saveTo("silent_install"));
        }

        addSection(I18nManager.get("other"));
        addItem(I18nManager.get("backup"), new View.OnClickListener() {
            public void onClick(View v) { backupDialog(); }
        });
        addItem(I18nManager.get("about"), new View.OnClickListener() {
            public void onClick(View v) { aboutDialog(); }
        });
    }

    private String currentShapeLabel() {
        int shape = prefs().getInt("screen_shape", 0);
        if (shape == 1) return I18nManager.get("shape_round");
        if (shape == 2) return I18nManager.get("shape_square");
        return I18nManager.get("shape_auto");
    }

    private String getUiSizeLabel() {
        float scale = prefs().getFloat("ui_scale", 1.0f);
        if (scale <= 0.85f) return I18nManager.get("ui_size_small");
        if (scale >= 1.15f) return I18nManager.get("ui_size_large");
        return I18nManager.get("ui_size_medium");
    }

    private String currentLangLabel() {
        String lang = prefs().getString("app_lang", "system");
        String[] codes = {"system","zh","zh_TW","en","ja","ko","de","fr","es","ru","pt"};
        String[] names = {I18nManager.get("follow_system"),"简体中文","繁體中文","English","日本語","한국어","Deutsch","Français","Español","Русский","Português"};
        for (int i = 0; i < codes.length; i++) if (codes[i].equals(lang)) return names[i];
        return lang;
    }

    private void addSection(String title) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        TextView tv = new TextView(this);
        tv.setText(title); tv.setTextSize(12);
        tv.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(dp(12), dp(10), dp(12), dp(4));
        container.addView(tv);
    }

        private void addSwitch(String label, boolean value, final SwitchHandler handler) {
        addSwitch(label, null, value, handler);
    }

private void addSwitch(String label, String subLabel, boolean value, final SwitchHandler handler) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setBackground(WatchUiUtils.getPillBackground(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(label); tvTitle.setTextSize(16);
        tvTitle.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        textCol.addView(tvTitle);

        if (subLabel != null) {
            TextView tvSub = new TextView(this);
            tvSub.setText(subLabel); tvSub.setTextSize(12);
            tvSub.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);
            tvSub.setPadding(0, dp(2), 0, 0);
            textCol.addView(tvSub);
        }
        row.addView(textCol);

        org.watchsend.ui.Md3Switch cb = new org.watchsend.ui.Md3Switch(this);
        cb.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(26)));
        cb.setChecked(value);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean c) { handler.onChanged(c); }
        });
        row.addView(cb);
        container.addView(row);
    }

    private void addItem(String label, View.OnClickListener click) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setBackground(WatchUiUtils.getPillBackground(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(label); tvTitle.setTextSize(16);
        tvTitle.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvTitle);

        TextView arrow = new TextView(this);
        arrow.setText(">"); arrow.setTextSize(18);
        arrow.setTextColor(isDark ? 0xFF81C784 : 0xFF999999);
        row.addView(arrow);

        row.setOnClickListener(click);
        container.addView(row);
    }

    private void pickShape() {
        final String[] items = {I18nManager.get("shape_auto"), I18nManager.get("shape_round"), I18nManager.get("shape_square")};
        WatchUiUtils.showList(this, I18nManager.get("screen_shape"), items, new WatchUiUtils.OnItemSelectListener() {
            public void onItemSelect(int pos) {
                prefs().edit().putInt("screen_shape", pos).commit();
                recreate();
            }
        }).show();
    }

    private void pickUiSize() {
        final float currentScale = prefs().getFloat("ui_scale", 1.0f);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));

        final TextView tvValue = new TextView(this);
        tvValue.setText(String.format(Locale.US, "%.2f", currentScale) + "x");
        tvValue.setTextSize(18); tvValue.setGravity(Gravity.CENTER);
        tvValue.setTextColor(org.watchsend.util.ThemeUtils.getPrimaryColor(this));
        layout.addView(tvValue);

        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        // 把当前比例映射到 0-100 的进度 (范围: 0.3f ~ 2.5f)
        int currentProgress = (int) (((currentScale - 0.3f) / 2.2f) * 100f);
        sb.setProgress(Math.max(0, Math.min(100, currentProgress)));
        
        int thumbSize = dp(24);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(3)); bg.setColor(0xFF3E3E3E); bg.setSize(0, dp(6));
        GradientDrawable progress = new GradientDrawable();
        progress.setCornerRadius(dp(3)); progress.setColor(org.watchsend.util.ThemeUtils.getPrimaryColor(this)); progress.setSize(0, dp(6));
        ClipDrawable clip = new ClipDrawable(progress, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{bg, clip});
        ld.setId(0, android.R.id.background); ld.setId(1, android.R.id.progress);
        sb.setProgressDrawable(ld);

        GradientDrawable thumb = new GradientDrawable();
        thumb.setShape(GradientDrawable.OVAL); thumb.setColor(0xFFFFFFFF); thumb.setStroke(dp(3), org.watchsend.util.ThemeUtils.getPrimaryColor(this)); thumb.setSize(thumbSize, thumbSize);
        sb.setThumb(thumb);
        sb.setSplitTrack(false);
        sb.setPadding(thumbSize/2, 0, thumbSize/2, 0);
        
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int p, boolean f) {
                float newScale = 0.3f + (p / 100f) * 2.2f;
                tvValue.setText(String.format(Locale.US, "%.2f", newScale) + "x");
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {
                float newScale = 0.3f + (seekBar.getProgress() / 100f) * 2.2f;
                prefs().edit().putFloat("ui_scale", newScale).commit();
                recreate();
            }
        });
        layout.addView(sb);

        WatchUiUtils.createBaseDialog(this, I18nManager.get("ui_size"), layout, I18nManager.get("confirm"), null, null, null).show();
    }

    private void pickLang() {
        final String[] items = {I18nManager.get("follow_system"),"简体中文","繁體中文","English","日本語","한국어","Deutsch","Français","Español","Русский","Português"};
        WatchUiUtils.showList(this, I18nManager.get("language"), items, new WatchUiUtils.OnItemSelectListener() {
            public void onItemSelect(int pos) {
                String[] codes = {"system","zh","zh_TW","en","ja","ko","de","fr","es","ru","pt"};
                prefs().edit().putString("app_lang", codes[pos]).commit();
                Toast.makeText(WatchSettingsActivity.this, I18nManager.get("lang_switched"), Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(WatchSettingsActivity.this, MainActivity.class));
            }
        }).show();
    }

    private void editPin() {
        WatchUiUtils.showInput(this, I18nManager.get("modify_pin"), prefs().getString("pin_code", "0000"), new WatchUiUtils.OnInputListener() {
            public void onInput(String text) {
                if (text.length() > 0) { prefs().edit().putString("pin_code", text).commit(); recreate(); }
            }
        });
    }

    private void editPort() {
        WatchUiUtils.showInput(this, I18nManager.get("port"), String.valueOf(prefs().getInt("port", 53317)), new WatchUiUtils.OnInputListener() {
            public void onInput(String text) {
                try { prefs().edit().putInt("port", Integer.parseInt(text)).commit(); recreate(); } catch (Exception ignored) {}
            }
        });
    }

    private void editFilter() {
        WatchUiUtils.showInput(this, I18nManager.get("filter_ext_title"), OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh"), new WatchUiUtils.OnInputListener() {
            public void onInput(String text) { prefs().edit().putString("filter_ext", text.replace(" ","").toLowerCase()).commit(); recreate(); }
        });
    }

    private void editName() {
        WatchUiUtils.showInput(this, I18nManager.get("set_name"), OldSendService.getAlias(this), new WatchUiUtils.OnInputListener() {
            public void onInput(String text) {
                if (text.length() > 0) { OldSendService.setAlias(WatchSettingsActivity.this, text); recreate(); }
            }
        });
    }

    private void manageAcl() {
        String[] items = {I18nManager.get("view_whitelist"), I18nManager.get("view_blacklist"), I18nManager.get("clear_all")};
        WatchUiUtils.showList(this, I18nManager.get("manage_acl"), items, new WatchUiUtils.OnItemSelectListener() {
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

    private void backupDialog() {
        String[] items = {I18nManager.get("backup_export"), I18nManager.get("backup_import")};
        WatchUiUtils.showList(this, I18nManager.get("backup_title"), items, new WatchUiUtils.OnItemSelectListener() {
            public void onItemSelect(int pos) {
                android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (pos == 0) {
                    try {
                        org.json.JSONObject o = new org.json.JSONObject();
                        for (java.util.Map.Entry<String, ?> e : prefs().getAll().entrySet()) o.put(e.getKey(), e.getValue());
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("WatchSendBackup", o.toString()));
                        Toast.makeText(WatchSettingsActivity.this, I18nManager.get("export_ok"), Toast.LENGTH_SHORT).show();
                    } catch (Exception ignored) {}
                } else {
                    try {
                        android.content.ClipData clip = cm.getPrimaryClip();
                        if (clip == null || clip.getItemCount() == 0) return;
                        String text = clip.getItemAt(0).coerceToText(WatchSettingsActivity.this).toString();
                        org.json.JSONObject o = new org.json.JSONObject(text);
                        android.content.SharedPreferences.Editor ed = prefs().edit();
                        java.util.Iterator<String> keys = o.keys();
                        while (keys.hasNext()) {
                            String k = keys.next(); Object v = o.get(k);
                            if (v instanceof Boolean) ed.putBoolean(k, (Boolean) v);
                            else if (v instanceof Integer) ed.putInt(k, (Integer) v);
                            else if (v instanceof String) ed.putString(k, (String) v);
                        }
                        ed.commit();
                        Toast.makeText(WatchSettingsActivity.this, I18nManager.get("import_ok"), Toast.LENGTH_SHORT).show();
                        recreate();
                    } catch (Exception ignored) {}
                }
            }
        }).show();
    }

    private void aboutDialog() {
        TextView tv = new TextView(this);
        tv.setText("WatchSend · Watch Edition\nv1.1.0\n\n" + I18nManager.get("author") + ": styisme\nAI " + I18nManager.get("contributors") + "\nMIT License\n\n" + I18nManager.get("about_desc"));
        tv.setTextSize(12);
        tv.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFE0E0E0 : 0xFF333333);
        tv.setLineSpacing(0, 1.3f);
        WatchUiUtils.createBaseDialog(this, I18nManager.get("about"), tv, I18nManager.get("confirm"), null, null, null).show();
    }

    private int dp(int val) { return (int)(val * getResources().getDisplayMetrics().density); }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (WatchUiUtils.handleSwipeBack(this, ev)) return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (WatchUiUtils.isSystemBackBlocked(this)) {
            Toast.makeText(this, I18nManager.get("swipe_back_toast"), Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}
