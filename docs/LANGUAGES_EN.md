# WatchSend Localisation Reference

For anyone changing strings, adding a language, or asking "why is this screen in English?". Three parts: how the language data is organised, what is covered today, and where to touch when adding or fixing something.

> Short version: the app ships **52 languages / 18,819 strings** (365-366 for the 10 base languages, 361 for the other 42); the web page carries a separate **10-language** dictionary; **ar / fa / he / ur are right-to-left**, and since 1.9.0 the whole UI mirrors for them.

---

## 1. Two language systems, not one number

| Surface | Languages | Strings | Implementation | Switching |
| --- | --- | --- | --- | --- |
| App UI (watch) | 52 | 361-366 per language, 18,819 total | `I18nManager` (Java string tables, lazy-loaded) | Settings - General - Language, or follow system |
| Web page (WebShare) | 10 | about 30 | JS dictionary inside `assets/index.html` | Browser language detection, dropdown on the page |

The web page only carries 10 languages (zh / zh_TW / en / ja / ko / de / fr / es / ru / pt) because it is a temporary browser page with a small string count. The app is where the full 52 live. Do not mix the two numbers.

---

## 2. Switching language

1. Open **Settings - General - Language**.
2. With "Follow system", the system language is matched: `zh` splits into `zh` / `zh_TW` by region (TW / HK), and legacy codes are normalised (`iw -> he`, `in -> id`, `tl -> fil`, `no -> nb`).
3. Picking a language rebuilds every page immediately; no process restart is needed.
4. The last entry, **`debug_keys`**, renders the whole UI as raw keys - for finding untranslated strings, not for daily use.
5. The choice lives in the `app_lang` key of the `oldsend` SharedPreferences; the default is `system`.

---

## 3. All 52 languages

| Language | Code | Strings | Direction | Translation source |
| --- | --- | --- | --- | --- |
| 简体中文 | `zh` | 365 | Left-to-right | Human-maintained |
| 繁體中文 | `zh_TW` | 365 | Left-to-right | Human-maintained |
| English | `en` | 365 | Left-to-right | Human-maintained |
| 日本語 | `ja` | 366 | Left-to-right | Human-maintained |
| 한국어 | `ko` | 366 | Left-to-right | Human-maintained |
| Deutsch | `de` | 366 | Left-to-right | Human-maintained |
| Français | `fr` | 366 | Left-to-right | Human-maintained |
| Español | `es` | 366 | Left-to-right | Human-maintained |
| Русский | `ru` | 366 | Left-to-right | Human-maintained |
| Português | `pt` | 366 | Left-to-right | Human-maintained |
| Italiano | `it` | 361 | Left-to-right | Human-maintained |
| العربية | `ar` | 361 | Right-to-left | Human + MT |
| Azərbaycan | `az` | 361 | Left-to-right | Human + MT |
| Български | `bg` | 361 | Left-to-right | Human + MT |
| বাংলা | `bn` | 361 | Left-to-right | Human + MT |
| Bosanski | `bs` | 361 | Left-to-right | Human + MT |
| Català | `ca` | 361 | Left-to-right | Human + MT |
| Čeština | `cs` | 361 | Left-to-right | Human + MT |
| Dansk | `da` | 361 | Left-to-right | Human + MT |
| Ελληνικά | `el` | 361 | Left-to-right | Human + MT |
| Eesti | `et` | 361 | Left-to-right | Human + MT |
| Euskara | `eu` | 361 | Left-to-right | Human + MT |
| فارسی | `fa` | 361 | Right-to-left | Human + MT |
| Suomi | `fi` | 361 | Left-to-right | Human + MT |
| Filipino | `fil` | 361 | Left-to-right | Human + MT |
| עברית | `he` | 361 | Right-to-left | Human + MT |
| हिन्दी | `hi` | 361 | Left-to-right | Human + MT |
| Hrvatski | `hr` | 361 | Left-to-right | Human + MT |
| Magyar | `hu` | 361 | Left-to-right | Human + MT |
| Bahasa Indonesia | `id` | 361 | Left-to-right | Human-maintained |
| Íslenska | `is` | 361 | Left-to-right | Human + MT |
| Lietuvių | `lt` | 361 | Left-to-right | Human + MT |
| Latviešu | `lv` | 361 | Left-to-right | Human + MT |
| Македонски | `mk` | 361 | Left-to-right | Human + MT |
| Bahasa Melayu | `ms` | 361 | Left-to-right | Human + MT |
| Norsk bokmål | `nb` | 361 | Left-to-right | Human + MT |
| नेपाली | `ne` | 361 | Left-to-right | Human + MT |
| Nederlands | `nl` | 361 | Left-to-right | Human-maintained |
| Polski | `pl` | 361 | Left-to-right | Human-maintained |
| Română | `ro` | 361 | Left-to-right | Human + MT |
| Slovenčina | `sk` | 361 | Left-to-right | Human + MT |
| Slovenščina | `sl` | 361 | Left-to-right | Human + MT |
| Shqip | `sq` | 361 | Left-to-right | Human + MT |
| Српски | `sr` | 361 | Left-to-right | Human + MT |
| Svenska | `sv` | 361 | Left-to-right | Human + MT |
| Kiswahili | `sw` | 361 | Left-to-right | Human + MT |
| ไทย | `th` | 361 | Left-to-right | Human + MT |
| Türkçe | `tr` | 361 | Left-to-right | Human-maintained |
| Українська | `uk` | 361 | Left-to-right | Human + MT |
| اردو | `ur` | 361 | Right-to-left | Human + MT |
| Oʻzbekcha | `uz` | 361 | Left-to-right | Human + MT |
| Tiếng Việt | `vi` | 361 | Left-to-right | Human-maintained |

The string counts above are measured, not estimated: reflect `I18nManager` on a desktop JVM, force every package to load, count each map. Why they differ is in the next section.

---

## 4. How the totals add up

**3 x 365 + 7 x 366 + 42 x 361 = 18,819.**

- **42 languages have 361 strings:** 353 plus the 8 strings added in 1.11.0. All of them still miss the same four keys: `resend`, `choose_device_to_send`, `idle_stop_server`, `idle_stop_server_desc`. Those were added in 1.7.12 and only translated into the 10 base languages, so in the other 42 they fall back to English.
- **ja / ko / de / fr / es / ru / pt have 366.** That is 353 + those four + one legacy key `save_to_dir` that only ever existed in these seven packages (`en` does not have it) + the 8 strings added in 1.11.0.
- **zh / zh_TW / en have 365.** That is 353 + those four + the 8 strings added in 1.11.0 (`auto_wifi`, `auto_wifi_desc`, `auto_wifi_list`, `battery_stop`, `battery_stop_desc`, `compress_image`, `compress_image_desc`, `send_all_devices`).
- A previous revision of this document claimed "7 packages with 354 entries, 18,363 total". That miscounted both the legacy key and the four base-language strings and is superseded by this section and the 1.7.13 CHANGELOG entry.

To verify: compile `I18nManager`, reflect the private `ensureLoaded(code)`, and count. Static initialisation holds 10 packages; after loading all codes you get 52 packages and 18,819 strings.

---

## 5. Right-to-left and long text

### Four RTL languages

`ar` (العربية), `fa` (فارسی), `he` (עברית), `ur` (اردو). Since 1.9.0 this is handled by `uikit/RtlSupport`:

- switching language inside the app does not change the system Locale, so the root view is set to RTL based on the selected language, and children inherit it;
- list order, `START/END` alignment and `marginStart` / `paddingStart` all mirror; physical `left/right` attributes stay for older versions;
- direction-specific details are handled separately: the settings chevron points left, the transfer progress bar fills right to left;
- swipe back already accepted both edges and both directions, so in RTL the gesture simply starts from the opposite edge;
- below Android 4.2 (API 17) the layout stays LTR, because `Gravity.START` is not available there.

Known gap: the percentage text on the progress ring is still drawn LTR - readable, but not mirrored.

### Long text

The same sentence can grow 60-120% in German or Russian. `uikit/TextFit` centralises two behaviours:

- `wrap(tv, maxLines)`: titles, list items and descriptions get a line cap plus ellipsis, with high-quality line breaking and hyphenation so German compounds break inside the word when needed;
- `fitSingleLine(tv, maxSp, minSp)`: buttons keep one line and shrink instead - system autosize on Android 8.0+, otherwise a width estimate (CJK counts as 1em, Latin and symbols as 0.55em);
- switch descriptions in Settings are capped at three lines and can be tapped to open in full, so one option no longer fills the screen in long languages.

---

## 6. Fallback order

`I18nManager.get(key)` always resolves in four steps:

1. the current language package;
2. **English** (`en`);
3. **Simplified Chinese** (`zh`);
4. the raw key (should never happen).

Notes:

- `zh` and `zh_TW` do not fall back to each other; both go to `en`;
- the four keys missing from the 42 languages show English by design, not by mistake;
- `debug_keys` bypasses fallback entirely.

---

## 7. Lazy loading

Building all 52 maps up front costs about 1MB of heap and tens of milliseconds of cold start. Since 1.7.11:

1. static initialisation keeps **10 base packages** resident (en / zh as fallbacks, plus zh_TW / ja / ko / de / fr / es / ru / pt);
2. once the active language is known, `I18nManager.ensureLoaded(lang)` builds that package - idempotent and locked;
3. `get()` has one more fallback load for calls made before initialisation.

This saves runtime heap and cold start, not APK size - the string constants stay in the dex.

---

## 8. Language-specific user guides

Ten end-user manuals, matching the ten README languages:

| Language | File |
| --- | --- |
| 简体中文 | [USER_GUIDE_zh.md](USER_GUIDE_zh.md) |
| 繁體中文 | [USER_GUIDE_zh_TW.md](USER_GUIDE_zh_TW.md) |
| English | [USER_GUIDE_en.md](USER_GUIDE_en.md) |
| 日本語 | [USER_GUIDE_ja.md](USER_GUIDE_ja.md) |
| 한국어 | [USER_GUIDE_ko.md](USER_GUIDE_ko.md) |
| Deutsch | [USER_GUIDE_de.md](USER_GUIDE_de.md) |
| Français | [USER_GUIDE_fr.md](USER_GUIDE_fr.md) |
| Español | [USER_GUIDE_es.md](USER_GUIDE_es.md) |
| Русский | [USER_GUIDE_ru.md](USER_GUIDE_ru.md) |
| Português | [USER_GUIDE_pt.md](USER_GUIDE_pt.md) |

The other 42 languages have no separate manual; the English guide plus the in-app UI is the reference.

---

## 9. Adding or changing strings

### A new string

1. Add `put("your_key", "...")` to the 10 base packages in `I18nManager`;
2. to cover all 52, also add it to the matching `installFullXx()`;
3. always give `en` a value, otherwise it falls back to Chinese;
4. walk the UI with `debug_keys` and confirm no raw key shows up;
5. if the string sits in a title or a button, wire it through `TextFit.wrap` / `fitSingleLine` so long languages do not break the layout.

### A new language

1. Write an `installFullXx()` with 361 strings (copy the `en` map as a template);
2. add the code to `CODES` and `LABELS` (that order is the order in Settings; `system` is always first);
3. **register a branch in `ensureLoaded()`**, otherwise the package never loads and the UI stays English;
4. for an RTL language, add the code to `RtlSupport.RTL_LANGS`;
5. add legacy system codes to `normalizeCode()`;
6. verify: 10 packages after static init, 52 after full load, 361 strings each; for RTL also check that the chevron, progress bar and swipe edge mirror.

### Before committing

- walk every page in the target language: home, devices, send, transfer, settings, history, about;
- check 0.3x and 2.5x interface scale on a round screen - no clipping, no one-letter lines;
- for RTL, also confirm arrow direction, progress direction and that swipe back works from the opposite edge;
- confirm no raw keys.

---

## 10. FAQ

**The UI did not change after switching.** The current Activity is rebuilt on save; if you are on a sub-page, go back once.

**Why is some text in English?** Fallback step 2 in section 6 - typically the four keys from section 4, or a string the package never covered.

**Why does the web page only have 10 languages?** Its dictionary lives in `assets/index.html` and covers 10; more languages would mean extending the front-end dictionary separately, which is not done yet.

**Are Traditional and Simplified Chinese the same file?** No. `zh_TW` is its own string set and does not fall back to `zh`; both fall back to `en`.

**Why is the Arabic UI mirrored?** That is RTL support from 1.9.0, not a broken layout. Pick any LTR language to go back to left-to-right.

---

## 11. Related documents

- [README_EN.md](../README_EN.md) - project overview
- [CHANGELOG.md](../CHANGELOG.md) - language and UI changes per release
- [CONTRIBUTING_EN.md](../CONTRIBUTING_EN.md) - development and commit conventions
- [DEVELOPMENT_CONSTRAINTS.md](DEVELOPMENT_CONSTRAINTS.md) - verification scope and untested areas
- [LANGUAGES.md](LANGUAGES.md) - Chinese version of this file
