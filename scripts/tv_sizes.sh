#!/bin/bash
# Every-size check for KS TV (specs/verification.md). Development tool only; nothing is shipped.
#
#   scripts/tv_sizes.sh OUT_DIR [tv-32 tv-43 ... tv-100]     (default: every size)
#   LANGS="en hi" scripts/tv_sizes.sh OUT_DIR ...             (Home and Settings also in Hindi)
#
# For each TV-size emulator (../scripts/emulators.sh): boot it if it is not running, run the
# connected tests, install KS TV Dev (never the real app), open Home, All apps, Subscriptions,
# Control Center and Settings, take screenshots, run the label audit (scripts/a11y_audit.py) and
# count crashes. An emulator this script booted is shut down again; one that was already running
# (maybe another session's) is left running.
#
# Note: `uiautomator dump` makes KS's window lose and regain focus, which KS treats like a reopen,
# so screens are reached from a fresh start each time and dumped last.
set -u
OUT="${1:?OUT_DIR}"; shift
SIZES=("$@"); [ ${#SIZES[@]} -eq 0 ] && SIZES=(tv-32 tv-43 tv-50 tv-55 tv-65 tv-75 tv-85 tv-100)
LANGS="${LANGS:-en}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"; cd "$ROOT" || exit 1
mkdir -p "$OUT"
PKG=com.ks.tvlauncher.dev; ACT=$PKG/com.ks.tvlauncher.MainActivity

k() { for x in "$@"; do adb shell input keyevent "KEYCODE_$x"; sleep 0.9; done; }
shot() { sleep 1.2; adb exec-out screencap -p > "$OUT/$1.png"; sips -Z 700 "$OUT/$1.png" --out "$OUT/s-$1.png" >/dev/null 2>&1; }
dump() { adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull -q /sdcard/ui.xml "$OUT/$1.xml" >/dev/null; }
dialog_open() { adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb shell cat /sdcard/ui.xml | grep -qE "Who's watching|कौन देख रहा है"; }
fresh() { adb shell am force-stop $PKG; adb shell am start -W -n $ACT >/dev/null; sleep 6; dialog_open && { k BACK; sleep 1; }; }
settings() { adb shell am force-stop $PKG; adb shell am start -n $ACT --ez open_updates true >/dev/null; sleep 5; }

for size in "${SIZES[@]}"; do
  echo "=== $size"; serial=""; started=0; avd="ks-${size}in"
  for s in $(adb devices | awk '/^emulator-/{print $1}'); do
    [ "$(adb -s "$s" emu avd name 2>/dev/null | head -1 | tr -d '\r')" = "$avd" ] && serial=$s
  done
  if [ -z "$serial" ]; then
    ../scripts/emulators.sh start "$size" --headless >/dev/null 2>&1 || { echo "$size: start FAILED"; continue; }
    serial=$(../scripts/emulators.sh serial "$size"); started=1
  fi
  export ANDROID_SERIAL=$serial
  adb shell 'while [ "$(getprop sys.boot_completed)" != 1 ]; do sleep 1; done'
  ./gradlew -q connectedDebugAndroidTest >"$OUT/$size-connected.log" 2>&1 \
    && echo "$size: connected tests PASS" || { echo "$size: connected tests FAIL"; tail -12 "$OUT/$size-connected.log"; }
  ./gradlew -q installDebug >"$OUT/$size-install.log" 2>&1 && echo "$size: install OK" || echo "$size: install FAIL"
  adb logcat -c
  for lang in $LANGS; do
    adb shell cmd locale set-app-locales $PKG --locales "$lang" >/dev/null 2>&1
    p="$size-$lang"
    fresh; shot "$p-home"; dump "$p-home"
    if [ "$lang" = en ]; then
      fresh; k DPAD_DOWN DPAD_CENTER; shot "$p-drawer"; dump "$p-drawer"
      fresh; k DPAD_DOWN DPAD_RIGHT DPAD_CENTER; shot "$p-subs"; dump "$p-subs"
      fresh; k DPAD_DOWN DPAD_RIGHT DPAD_RIGHT DPAD_RIGHT DPAD_CENTER; shot "$p-cc"; dump "$p-cc"
    fi
    settings; shot "$p-set-about"
    k DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP; shot "$p-set-mid"
    k DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP DPAD_UP; shot "$p-set-top"
  done
  adb shell cmd locale set-app-locales $PKG --locales en >/dev/null 2>&1
  dumps=$(ls "$OUT/$size"-*.xml 2>/dev/null)
  python3 scripts/a11y_audit.py $dumps >"$OUT/$size-audit.log" 2>&1 \
    && echo "$size: label audit OK" || { echo "$size: label audit FOUND GAPS"; grep unlabelled "$OUT/$size-audit.log" | head -5; }
  echo "$size: crashes=$(adb logcat -d -b crash | grep -c $PKG) fatal=$(adb logcat -d | grep -cE "FATAL EXCEPTION|ANR in $PKG")"
  [ $started = 1 ] && ../scripts/emulators.sh stop "$size" >/dev/null 2>&1
done
echo "=== done"
