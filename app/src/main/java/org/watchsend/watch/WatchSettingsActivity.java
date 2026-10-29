package org.watchsend.watch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.watchsend.MainActivity;
import org.watchsend.OldSendService;
import org.watchsend.R;
import org.watchsend.util.I18nManager;
import org.watchsend.util.ThemeUtils;

public class WatchSettingsActivity extends Activity {
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WatchUiUtils.applyScale(this); // 应用 UI 缩放
        super.onCreate(savedInstanceState);
        I18nManager.init(this);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_watch_settings);
        ThemeUtils.applyTheme(this);
        
        // 设置根背景色
        boolean isDark = ThemeUtils.isDarkMode(this);
        findViewById(R.id.settings_root).setBackgroundColor(isDark ? 0xFF121212 : 0xFFF4F6F8);

        container = (LinearLayout) findViewById(R.id.settings_container);
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
        boolean isDark = ThemeUtils.isDarkMode(this);

        addSection(I18nManager.get("general"));

        addSwitch(I18nManager.get("dark_mode"), prefs().getBoolean("dark_mode", false), new SwitchHandler() {
            public void onChanged(boolean v) { prefs().edit().putBoolean("dark_mode", v).commit(); recreate(); }
        });
        addSwitch(I18nManager.get("auto_save"), OldSendService.getPrefBooleanGlobal("auto_save", false), saveTo("auto_save"));
        addSwitch(I18nManager.get("auto_whitelist"), OldSendService.getPrefBooleanGlobal("auto_accept_whitelist", false), saveTo("auto_accept_whitelist"));
        addSwitch(I18nManager.get("save_album"), OldSendService.getPrefBooleanGlobal("save_album", true), saveTo("save_album"));
        addSwitch(I18nManager.get("save_history"), OldSendService.getPrefBooleanGlobal("save_history", true), saveTo("save_history"));
        addSwitch(I18nManager.get("use_pin"), OldSendService.getPrefBooleanGlobal("use_pin", false), saveTo("use_pin"));

        addItem("PIN: " + prefs().getString("pin_code", "0000"), new View.OnClickListener() {
            public void onClick(View v) { editPin(); }
        });

        addSection(I18nManager.get("network"));

        addSwitch(I18nManager.get("https_enable"), prefs().getBoolean("use_https", false), new SwitchHandler() {
            public void onChanged(boolean v) { prefs().edit().putBoolean("use_https", v).commit(); }
        });
        addSwitch(I18nManager.get("web_enable"), OldSendService.getPrefBooleanGlobal("web_enable", true), saveTo("web_enable"));
        addSwitch(I18nManager.get("auto_start"), OldSendService.getPrefBooleanGlobal("auto_start", false), saveTo("auto_start"));
        addSwitch(I18nManager.get("receive_notif"), OldSendService.getPrefBooleanGlobal("receive_notif", false), saveTo("receive_notif"));

        addItem(I18nManager.get("port") + ": " + prefs().getInt("port", 53317), new View.OnClickListener() {
            public void onClick(View v) { editPort(); }
        });
        addItem(I18nManager.get("timeout") + ": " + prefs().getInt("timeout", 500), new View.OnClickListener() {
            public void onClick(View v) { editTimeout(); }
        });

        addSection(I18nManager.get("other"));
        
        // 新增：UI 大小调节
        addItem(I18nManager.get("ui_size") + ": " + getUiSizeLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickUiSize(); }
        });

        addItem(I18nManager.get("language") + ": " + currentLangLabel(), new View.OnClickListener() {
            public void onClick(View v) { pickLang(); }
        });
        addSwitch(I18nManager.get("anim"), prefs().getBoolean("use_anim", true), saveTo("use_anim"));
        addItem(I18nManager.get("filter_ext_title") + ": " + OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh"),
            new View.OnClickListener() {
                public void onClick(View v) { editFilter(); }
            });
        addItem(I18nManager.get("set_name") + ": " + OldSendService.getAlias(this), new View.OnClickListener() {
            public void onClick(View v) { editName(); }
        });
        addItem(I18nManager.get("manage_acl"), new View.OnClickListener() {
            public void onClick(View v) { manageAcl(); }
        });
        addItem(I18nManager.get("backup"), new View.OnClickListener() {
            public void onClick(View v) { backupDialog(); }
        });
        addItem(I18nManager.get("about"), new View.OnClickListener() {
            public void onClick(View v) { aboutDialog(); }
        });
    }

    private void addSection(String title) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        TextView tv = new TextView(this);
        tv.setText(title); tv.setTextSize(12);
        tv.setTextColor(isDark ? 0xFFAAAAAA : 0xFF666666);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(12, 10, 12, 4);
        container.addView(tv);
    }

    private void addSwitch(String label, boolean value, final SwitchHandler handler) {
        boolean isDark = ThemeUtils.isDarkMode(this);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(12, 6, 12, 6);
        row.setBackgroundResource(isDark ? R.drawable.watch_card_dark : R.drawable.watch_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = 4;
        row.setLayoutParams(lp);

        TextView tv = new TextView(this);
        tv.setText(label); tv.setTextSize(13);
        tv.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tv);

        org.watchsend.ui.Md3Switch cb = new org.watchsend.ui.Md3Switch(this);
        cb.setLayoutParams(new LinearLayout.LayoutParams(44, 26));
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
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(12, 12, 12, 12);
        row.setBackgroundResource(isDark ? R.drawable.watch_card_dark : R.drawable.watch_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = 4;
        row.setLayoutParams(lp);

        TextView tv = new TextView(this);
        tv.setText(label); tv.setTextSize(13);
        tv.setTextColor(isDark ? 0xFFE0E0E0 : 0xFF1C1B1F);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tv);

        TextView arrow = new TextView(this);
        arrow.setText(">"); arrow.setTextSize(20); arrow.setTextColor(isDark ? 0xFF81C784 : 0xFF999999);
        row.addView(arrow);

        row.setOnClickListener(click);
        container.addView(row);
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

    private void editPin() {
        WatchUiUtils.showInput(this, "PIN", prefs().getString("pin_code", "0000"),
            new WatchUiUtils.OnInputListener() {
                public void onInput(String text) {
                    if (text != null && text.length() > 0) {
                        prefs().edit().putString("pin_code", text).commit();
                        Toast.makeText(WatchSettingsActivity.this, I18nManager.get("update_success"), Toast.LENGTH_SHORT).show();
                        buildSettings();
                    }
                }
            });
    }

    private void editPort() {
        WatchUiUtils.showInput(this, I18nManager.get("port"), String.valueOf(prefs().getInt("port", 53317)),
            new WatchUiUtils.OnInputListener() {
                public void onInput(String text) {
                    try { int v = Integer.parseInt(text); prefs().edit().putInt("port", v).commit(); buildSettings(); }
                    catch (Exception ignored) {}
                }
            });
    }

    private void editTimeout() {
        WatchUiUtils.showInput(this, I18nManager.get("timeout"), String.valueOf(prefs().getInt("timeout", 500)),
            new WatchUiUtils.OnInputListener() {
                public void onInput(String text) {
                    try { int v = Integer.parseInt(text); prefs().edit().putInt("timeout", v).commit(); buildSettings(); }
                    catch (Exception ignored) {}
                }
            });
    }

    private void editFilter() {
        WatchUiUtils.showInput(this, I18nManager.get("filter_ext_title"),
            OldSendService.getPrefStringGlobal("filter_ext", "exe,bat,cmd,vbs,sh"),
            new WatchUiUtils.OnInputListener() {
                public void onInput(String text) {
                    prefs().edit().putString("filter_ext", text.replace(" ","").toLowerCase()).commit();
                    buildSettings();
                }
            });
    }

    private void editName() {
        WatchUiUtils.showInput(this, I18nManager.get("set_name"), OldSendService.getAlias(this),
            new WatchUiUtils.OnInputListener() {
                public void onInput(String text) {
                    if (text != null && text.length() > 0) {
                        OldSendService.setAlias(WatchSettingsActivity.this, text);
                        buildSettings();
                    }
                }
            });
    }
    
    private void pickUiSize() {
        String[] items = {I18nManager.get("ui_size_small"), I18nManager.get("ui_size_medium"), I18nManager.get("ui_size_large")};
        WatchUiUtils.showList(this, I18nManager.get("ui_size"), items,
            new WatchUiUtils.OnItemSelectListener() {
                public void onItemSelect(int pos) {
                    float scale = 1.0f;
                    if (pos == 0) scale = 0.85f;
                    else if (pos == 2) scale = 1.15f;
                    prefs().edit().putFloat("ui_scale", scale).commit();
                    recreate();
                }
            }).show();
    }

    private void pickLang() {
        final String[] items = {I18nManager.get("follow_system"),"简体中文","繁體中文","English","日本語","한국어","Deutsch","Français","Español","Русский","Português"};
        WatchUiUtils.showList(this, I18nManager.get("select_lang"), items,
            new WatchUiUtils.OnItemSelectListener() {
                public void onItemSelect(int pos) {
                    String[] codes = {"system","zh","zh_TW","en","ja","ko","de","fr","es","ru","pt"};
                    prefs().edit().putString("app_lang", codes[pos]).commit();
                    Toast.makeText(WatchSettingsActivity.this, I18nManager.get("restart_tip"), Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(WatchSettingsActivity.this, MainActivity.class));
                }
            }).show();
    }

    private void manageAcl() {
        String[] items = {I18nManager.get("view_whitelist"), I18nManager.get("view_blacklist"), I18nManager.get("clear_all")};
        WatchUiUtils.showList(this, I18nManager.get("manage_acl"), items,
            new WatchUiUtils.OnItemSelectListener() {
                public void onItemSelect(int pos) {
                    if (pos == 2) {
                        prefs().edit().remove("whitelist").remove("blacklist").commit();
                        Toast.makeText(WatchSettingsActivity.this, I18nManager.get("all_deleted"), Toast.LENGTH_SHORT).show();
                    } else showSpecificList(pos == 0 ? "whitelist" : "blacklist");
                }
            }).show();
    }

    private void showSpecificList(final String key) {
        String s = OldSendService.getPrefStringGlobal(key, "");
        if (s.trim().isEmpty()) { Toast.makeText(this, I18nManager.get("list_empty"), Toast.LENGTH_SHORT).show(); return; }
        final String[] arr = s.split(",");
        String[] display = new String[arr.length];
        for (int i = 0; i < arr.length; i++)
            display[i] = arr[i].length() > 16 ? arr[i].substring(0, 16) + "..." : arr[i];
        WatchUiUtils.showList(this, key.equals("whitelist") ? I18nManager.get("view_whitelist") : I18nManager.get("view_blacklist"), display,
            new WatchUiUtils.OnItemSelectListener() {
                public void onItemSelect(int pos) {
                    String t = arr[pos];
                    String cur = OldSendService.getPrefStringGlobal(key, "");
                    cur = cur.replace(t + ",", "").replace(t, "");
                    prefs().edit().putString(key, cur).commit();
                    showSpecificList(key);
                }
            }).show();
    }

    private void backupDialog() {
        String[] items = {I18nManager.get("backup_export"), I18nManager.get("backup_import")};
        WatchUiUtils.showList(this, I18nManager.get("backup_title"), items,
            new WatchUiUtils.OnItemSelectListener() {
                public void onItemSelect(int pos) {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (pos == 0) {
                        try {
                            org.json.JSONObject o = new org.json.JSONObject();
                            for (java.util.Map.Entry<String, ?> e : prefs().getAll().entrySet())
                                o.put(e.getKey(), e.getValue());
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
                            buildSettings();
                        } catch (Exception ignored) {}
                    }
                }
            }).show();
    }

    private void aboutDialog() {
        TextView tv = new TextView(this);
        tv.setText("WatchSend · Watch Edition\nv2.0.0\n\n" + I18nManager.get("author") + ": styisme\nAI " + I18nManager.get("contributors") + "\nMIT License\n\n" + I18nManager.get("about_desc"));
        tv.setTextSize(12);
        tv.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFE0E0E0 : 0xFF333333);
        tv.setLineSpacing(0, 1.3f);
        WatchUiUtils.createBaseDialog(this, I18nManager.get("about"), tv,
            I18nManager.get("confirm"), null, null, null).show();
    }
}
