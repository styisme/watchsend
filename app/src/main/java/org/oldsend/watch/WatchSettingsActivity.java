package org.oldsend.watch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.oldsend.MainActivity;
import org.oldsend.OldSendService;
import org.oldsend.R;
import org.oldsend.util.I18nManager;
import org.oldsend.util.ThemeUtils;

public class WatchSettingsActivity extends Activity {
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        I18nManager.init(this);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_watch_settings);
        ThemeUtils.applyTheme(this);
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
        boolean dark = ThemeUtils.isDarkMode(this);

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
        TextView tv = new TextView(this);
        tv.setText(title); tv.setTextSize(12);
        tv.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFAAAAAA : 0xFF666666);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(12, 10, 12, 4);
        container.addView(tv);
    }

    private void addSwitch(String label, boolean value, final SwitchHandler handler) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(12, 6, 12, 6);
        row.setBackgroundResource(R.drawable.watch_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = 4;
        row.setLayoutParams(lp);

        TextView tv = new TextView(this);
        tv.setText(label); tv.setTextSize(13);
        tv.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFE0E0E0 : 0xFF1C1B1F);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tv);

        org.oldsend.ui.Md3Switch cb = new org.oldsend.ui.Md3Switch(this);
        cb.setLayoutParams(new LinearLayout.LayoutParams(44, 26));
        cb.setChecked(value);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean c) { handler.onChanged(c); }
        });
        row.addView(cb);
        container.addView(row);
    }

    private void addItem(String label, View.OnClickListener click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(12, 12, 12, 12);
        row.setBackgroundResource(R.drawable.watch_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = 4;
        row.setLayoutParams(lp);

        TextView tv = new TextView(this);
        tv.setText(label); tv.setTextSize(13);
        tv.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFE0E0E0 : 0xFF1C1B1F);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tv);

        TextView arrow = new TextView(this);
        arrow.setText("›"); arrow.setTextSize(20); arrow.setTextColor(0xFF999999);
        row.addView(arrow);

        row.setOnClickListener(click);
        container.addView(row);
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
                        Toast.makeText(WatchSettingsActivity.this, "PIN 已更新", Toast.LENGTH_SHORT).show();
                        buildSettings();
                    }
                }
            });
    }

    private void editPort() {
        WatchUiUtils.showInput(this, "端口", String.valueOf(prefs().getInt("port", 53317)),
            new WatchUiUtils.OnInputListener() {
                public void onInput(String text) {
                    try { int v = Integer.parseInt(text); prefs().edit().putInt("port", v).commit(); buildSettings(); }
                    catch (Exception ignored) {}
                }
            });
    }

    private void editTimeout() {
        WatchUiUtils.showInput(this, "超时 (ms)", String.valueOf(prefs().getInt("timeout", 500)),
            new WatchUiUtils.OnInputListener() {
                public void onInput(String text) {
                    try { int v = Integer.parseInt(text); prefs().edit().putInt("timeout", v).commit(); buildSettings(); }
                    catch (Exception ignored) {}
                }
            });
    }

    private void editFilter() {
        WatchUiUtils.showInput(this, "拦截后缀",
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
        String[] items = {"查看白名单", "查看黑名单", "清空所有"};
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
        if (s.trim().isEmpty()) { Toast.makeText(this, "名单为空", Toast.LENGTH_SHORT).show(); return; }
        final String[] arr = s.split(",");
        String[] display = new String[arr.length];
        for (int i = 0; i < arr.length; i++)
            display[i] = arr[i].length() > 16 ? arr[i].substring(0, 16) + "..." : arr[i];
        WatchUiUtils.showList(this, key.equals("whitelist") ? "白名单" : "黑名单", display,
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
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("OldSendBackup", o.toString()));
                            Toast.makeText(WatchSettingsActivity.this, "已导出到剪贴板", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(WatchSettingsActivity.this, "已导入", Toast.LENGTH_SHORT).show();
                            buildSettings();
                        } catch (Exception ignored) {}
                    }
                }
            }).show();
    }

    private void aboutDialog() {
        TextView tv = new TextView(this);
        tv.setText("OldSend · Watch Edition\nv2.0.0\n\n作者: styisme\nAI 辅助生成\nMIT License\n\n兼容 LocalSend v2.2\n端口 53317\n组播 224.0.0.167");
        tv.setTextSize(12);
        tv.setTextColor(ThemeUtils.isDarkMode(this) ? 0xFFE0E0E0 : 0xFF333333);
        tv.setLineSpacing(0, 1.3f);
        WatchUiUtils.createBaseDialog(this, I18nManager.get("about"), tv,
            I18nManager.get("confirm"), null, null, null).show();
    }
}
