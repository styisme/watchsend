# WatchSend Multilingual Guide

This document explains the language coverage of WatchSend, how to switch languages, how missing strings fall back, and where to change things when adding a language or a single string.

> Short version: **the app supports 52 languages with 353 entries each (18,363 strings in total); the web frontend (WebShare) supports 10 languages. They are two different sets, not one number.**

---

## 1. Two separate language systems

| Surface | Languages | Entries | Implementation | How to switch |
| --- | --- | --- | --- | --- |
| App UI (watch) | 52 | 353 per language, 18,363 total | `I18nManager` (Java string table, lazily loaded) | Settings → Language, or follow system |
| Web frontend (WebShare) | 10 | about 30 strings | JS dictionary inside `assets/index.html` | Detects browser language; a dropdown is also available |

The web frontend intentionally ships 10 languages (zh / zh_TW / en / ja / ko / de / fr / es / ru / pt) because it is a small throwaway browser page; the 52-language set belongs to the app. Older READMEs merged these two numbers into one claim, which was corrected during the 1.7.12 documentation review.

---

## 2. Switching languages

1. Open **Settings → Language**.
2. With "Follow system", the language is matched automatically: `zh` distinguishes Simplified and Traditional (TW / HK → Traditional), and legacy codes `iw → he`, `in → id`, `tl → fil`, `no → nb` are normalized.
3. After selecting a language, activities are recreated and the change applies immediately — no process restart needed.
4. The bottom of the list has a **`debug_keys`** entry: the whole UI then shows raw string keys instead of translations. It is a debugging aid, not for daily use.

---

## 3. The 52 languages

| Language | Code | Entries | Origin |
| --- | --- | --- | --- |
| 简体中文 | `zh` | 353 | Hand-maintained, full |
| 繁體中文 | `zh_TW` | 353 | Hand-maintained, full |
| English | `en` | 353 | Hand-maintained, full (fallback language) |
| 日本語 | `ja` | 354 | Hand-maintained, full |
| 한국어 | `ko` | 354 | Hand-maintained, full |
| Deutsch | `de` | 354 | Hand-maintained, full |
| Français | `fr` | 354 | Hand-maintained, full |
| Español | `es` | 354 | Hand-maintained, full |
| Русский | `ru` | 354 | Hand-maintained, full |
| Português | `pt` | 354 | Hand-maintained, full |
| Italiano | `it` | 353 | Hand-maintained, full |
| Nederlands | `nl` | 353 | Hand-maintained, full |
| Polski | `pl` | 353 | Hand-maintained, full |
| Türkçe | `tr` | 353 | Hand-maintained, full |
| Bahasa Indonesia | `id` | 353 | Hand-maintained, full |
| Tiếng Việt | `vi` | 353 | Hand-maintained, full |
| العربية | `ar` | 353 | Official human translations + MT merge |
| Azərbaycan | `az` | 353 | Official human translations + MT merge |
| Български | `bg` | 353 | Official human translations + MT merge |
| বাংলা | `bn` | 353 | Official human translations + MT merge |
| Bosanski | `bs` | 353 | Official human translations + MT merge |
| Català | `ca` | 353 | Official human translations + MT merge |
| Čeština | `cs` | 353 | Official human translations + MT merge |
| Dansk | `da` | 353 | Official human translations + MT merge |
| Ελληνικά | `el` | 353 | Official human translations + MT merge |
| Eesti | `et` | 353 | Official human translations + MT merge |
| Euskara | `eu` | 353 | Official human translations + MT merge |
| فارسی | `fa` | 353 | Official human translations + MT merge |
| Suomi | `fi` | 353 | Official human translations + MT merge |
| Filipino | `fil` | 353 | Official human translations + MT merge (Latin script) |
| עברית | `he` | 353 | Official human translations + MT merge |
| हिन्दी | `hi` | 353 | Official human translations + MT merge |
| Hrvatski | `hr` | 353 | Official human translations + MT merge (Latin script) |
| Magyar | `hu` | 353 | Official human translations + MT merge |
| Íslenska | `is` | 353 | Official human translations + MT merge |
| Lietuvių | `lt` | 353 | Official human translations + MT merge |
| Latviešu | `lv` | 353 | Official human translations + MT merge |
| Македонски | `mk` | 353 | Official human translations + MT merge |
| Bahasa Melayu | `ms` | 353 | Official human translations + MT merge (Latin script) |
| Norsk bokmål | `nb` | 353 | Official human translations + MT merge |
| नेपाली | `ne` | 353 | Official human translations + MT merge |
| Română | `ro` | 353 | Official human translations + MT merge |
| Slovenčina | `sk` | 353 | Official human translations + MT merge |
| Slovenščina | `sl` | 353 | Official human translations + MT merge |
| Shqip | `sq` | 353 | Official human translations + MT merge |
| Српски | `sr` | 353 | Official human translations + MT merge (Cyrillic) |
| Svenska | `sv` | 353 | Official human translations + MT merge |
| Kiswahili | `sw` | 353 | Official human translations + MT merge |
| ไทย | `th` | 353 | Official human translations + MT merge |
| Українська | `uk` | 353 | Official human translations + MT merge |
| اردو | `ur` | 353 | Official human translations + MT merge |
| Oʻzbekcha | `uz` | 353 | Official human translations + MT merge |

About the 354s: `ja / ko / de / fr / es / ru / pt` each carry one legacy debug entry, which is why the total is 18,363 rather than 52 × 353 = 18,356 (7 × 354 + 45 × 353).

### Translation quality

- 16 languages (`zh`, `zh_TW`, `en`, `ja`, `ko`, `de`, `fr`, `es`, `ru`, `pt`, `it`, `nl`, `pl`, `tr`, `id`, `vi`) are fully hand-maintained.
- The remaining 36 were produced by a three-layer merge: **official LocalSend human translations (27 languages usable, 52 exact keys matched) → the 22 existing core strings → offline NLLB machine translation for the rest of the 353**. Roughly 140 obviously wrong machine translations were then fixed by hand.
- So those 36 are "usable, not line-by-line polished". If a sentence reads badly, a PR following section 7 is welcome.

---

## 4. Fallback rules

`I18nManager.get(key)` always resolves in this order:

1. the active language pack;
2. otherwise **English** (`en`);
3. otherwise **Simplified Chinese** (`zh`);
4. otherwise the raw key (which should never happen in practice).

Notes:

- The 4 strings added in 1.7.12 (Resend, "tap a device to send", idle-pause title and description) were translated into the **10 base languages only**. The other 42 fall back to English — a deliberate trade-off rather than running a full MT pass for 4 short strings.
- `debug_keys` bypasses this chain and prints the key itself.

---

## 5. Languages are loaded on demand

Building all 52 packs up front costs roughly 1MB of heap and tens of milliseconds of cold start. Since 1.7.11:

1. only the **10 base packs** live at startup (en / zh as fallbacks, plus ja / ko / de / fr / es / ru / pt / zh_TW);
2. once the active language is known, `I18nManager.ensureLoaded(lang)` builds that pack exactly once (idempotent, thread-safe);
3. `get()` keeps a last-resort load for the static-initialization phase.

Measured on desktop JVM via reflection: 10 packs after static init; all 52 codes loaded → still 52 packs and 18,363 entries; repeated calls do not rebuild.

Note that lazy loading saves **runtime heap and cold-start time**, not APK size — the string constants still live in the dex.

---

## 6. FAQ

**The UI did not change after switching language.**
Activities recreate after saving; if you were on a second-level page, go back once. `app_lang` lives in the `oldsend` SharedPreferences.

**Why is some English mixed into a language?**
That is rule 2 of section 4. Typical cases: the 4 strings added in 1.7.12, or long-tail keys the pack does not cover yet.

**Why does the web frontend only have 10 languages?**
Its dictionary is independent, inside `assets/index.html`, and covers 10 languages. More would require extending the frontend dictionary separately (not done).

**Is Traditional Chinese the same pack as Simplified?**
No. `zh_TW` is its own 353-entry pack and neither falls back to the other; both fall back to `en`.

---

## 7. Adding a language or a string

### Adding one string

1. Add `put("your_key", "...")` to the 10 base packs in `I18nManager`;
2. to cover all 52 languages, add it to the matching `installFullXx()` as well;
3. always provide a value for `en`, otherwise it falls back to Chinese;
4. verify with `debug_keys` for missing or overflowing text.

### Adding a language

1. Write an `installFullXx()` with all 353 entries (copying the `en` pack as a template works);
2. add the new code to `CODES` and `LABELS` (order is the order shown in Settings, with `system` always first);
3. **register it in `ensureLoaded()`** — otherwise the pack is never loaded and the UI silently stays English;
4. if the system reports a legacy code (`iw / in / tl / no`), extend `normalizeCode()`;
5. verify: 10 packs after static init, 52 after loading everything (one more after adding a language), 353 entries per pack.

### Self-check before submitting

- Switch to the target language and walk every screen: home, device list, transfer, settings, history, about;
- check long strings are not clipped on a round display (use the 0.3x–2.5x UI scaling to test extremes);
- make sure no raw keys appear.

---

## 8. Related documents

- [README_EN.md](../README_EN.md) — project overview and features
- [CHANGELOG.md](../CHANGELOG.md) — per-version language and feature changes
- [CONTRIBUTING_EN.md](../CONTRIBUTING_EN.md) — development and submission rules
- [DEVELOPMENT_CONSTRAINTS.md](DEVELOPMENT_CONSTRAINTS.md) — verification scope and untested areas
- [LANGUAGES.md](LANGUAGES.md) — the Chinese version of this guide
