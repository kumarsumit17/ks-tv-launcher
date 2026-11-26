---
type: Specification
title: Languages (English and Hindi)
description: How KS speaks English and Hindi through ks-utility's language files, how the language is chosen and remembered, and how text in another script is laid out.
tags: [languages, hindi, english, ks-utility, settings]
---

# Languages (English and Hindi)

KS speaks English and Hindi, as every KS-Suite app does ([ks-utility languages](https://github.com/kumasumit/ks-utility/blob/main/specs/languages.md)).

## Language files
- All interface text is in `tv/google-tv/src/main/lang/en.json` and `hi.json`, in the suite's [translation file format](https://github.com/kumasumit/ks-utility/blob/main/specs/translation-format.md). English is the source; both files have the same keys.
- The build (`languageResources`, before `preBuild`) runs `node ks-utility/typescript/tools/lang.mjs android` and writes `res/values/lang_strings.xml` and `res/values-hi/lang_strings.xml` from KS's files plus ks-utility's shared catalogue (`common.*`, for example `common_language`). The generated files are git-ignored; never edit them. The build needs Node.
- Keys are chosen so the resource names stay as they were (`settings.title` → `R.string.settings_title`). Placeholders are `{arg1}`, `{arg2}`, … and `{count}` in plurals; the tool turns them into `%1$s`, `%2$s`, numbered by first appearance in the English text, so a translation may reorder them freely and the calling code keeps its argument order.
- `res/values/strings.xml` keeps only text that is never translated (`brand_badge`). KS TV Dev's names are in `src/debug/res/values` and `values-hi`.
- `checkLanguageFiles` (`lang.mjs check`) runs with every unit-test task and with `check`: same keys in both languages, the same placeholders, only the plural forms each language uses, Latin digits, and the Hindi term list.
- Hindi follows the [Hindi style guide](https://github.com/kumasumit/ks-utility/blob/main/specs/hindi-style.md). It is written by Claude and reviewed by the owner; a review never blocks a release. Terms KS uses that are not yet in the suite's term list (सब्सक्रिप्शन, पिन करें, अनपिन करें, वॉलपेपर, मौसम, रिन्यूअल, रिमाइंडर, स्टोरेज, अनइंस्टॉल करें, स्क्रीनसेवर, इनपुट, होम, नोटिफ़िकेशन, डिस्प्ले, कंट्रोल सेंटर) are proposed to ks-utility for its list, marked owner review.

## Choosing the language
The suite's rule: follow the system, allow a choice, remember it (`data.language.AppLanguage`).
- **Settings › Language**: "Same as the system", "English", "हिंदी" (each language by its own name, from the shared catalogue). Choosing one shows the screen again in that language.
- **Android 13 and later**: the choice is the system's per-app language (`LocaleManager.applicationLocales`, the app's languages in `res/xml/locales_config.xml`, `android:localeConfig` in the manifest), so it also shows in the TV's own app-language settings and survives updates.
- **Android 8 to 12**: KS saves the choice in its own preferences (`ks_language`) and wraps the context of `MainActivity` and `KsApplication` with it (`attachBaseContext`), so notifications use it too. No AndroidX AppCompat ([dependencies](dependencies.md)).
- A saved value KS does not know counts as "Same as the system". With no choice, Android picks: Hindi when the TV's language is Hindi, else English.

## Numbers, dates and text in other scripts
- Numbers, sizes, dates, times and money use the platform's formatters with Latin digits.
- Inter covers Latin only ([design-system](design-system.md)); Devanagari is drawn by the system's fallback font. Tight negative tracking (the hero title, the greeting, screen titles) clips fallback-font text, so `TextStyle.forText(text)` sets the tracking back to 0 when the text has letters of another script. Use it for every style with negative letter spacing.
- Every screen is checked in both languages on the TV-size emulators (Hindi words are often longer, and Devanagari needs more height).
