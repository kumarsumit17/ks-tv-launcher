#!/usr/bin/env bash
# Smoothness scenarios (docs/spec/smoothness.md). Read-only: it opens KS, presses keys and reads
# `dumpsys gfxinfo`. It never installs, uninstalls or clears anything.
#
#   scripts/perf.sh [-p package] [-n runs] [scenario...]
#
#   package   com.ks.tvlauncher.dev (default, the perf/debug build) or com.ks.tvlauncher (real app)
#   scenario  1 row (dynamic wallpaper)  2 row (set wallpaper to Plain first)  3 control center
#             4 all apps drawer  5 settings          default: 1 3 4 5
#
# Each run: force-stop, cold start, 5 s settle, gfxinfo reset, the key presses, gfxinfo read.
# Prints one line per run and the median of each column per scenario.
set -euo pipefail

PKG=com.ks.tvlauncher.dev
RUNS=3
while getopts "p:n:" opt; do
  case $opt in
    p) PKG=$OPTARG ;;
    n) RUNS=$OPTARG ;;
    *) sed -n '2,12p' "$0"; exit 2 ;;
  esac
done
shift $((OPTIND - 1))
SCENARIOS=("${@:-1 3 4 5}")
# shellcheck disable=SC2206
SCENARIOS=(${SCENARIOS[*]})

GAP=${GAP:-0.45}   # seconds between key presses

key() { adb shell input keyevent "$1" >/dev/null; sleep "$GAP"; }
keys() { local k=$1 n=$2; for ((i = 0; i < n; i++)); do key "$k"; done; }

cold_start() {
  adb shell am force-stop "$PKG"
  adb shell am start -W -n "$PKG/com.ks.tvlauncher.MainActivity" >/dev/null
  sleep 5
}

# Focus starts on the first pinned tile after a cold start.
run_scenario() {
  case $1 in
    1|2) keys KEYCODE_DPAD_RIGHT 6; keys KEYCODE_DPAD_LEFT 6 ;;
    3) for _ in 1 2 3; do key KEYCODE_MENU; sleep 0.6; key KEYCODE_BACK; sleep 0.6; done ;;
    4) key KEYCODE_DPAD_DOWN; key KEYCODE_DPAD_CENTER; sleep 1
       keys KEYCODE_DPAD_RIGHT 4; key KEYCODE_DPAD_DOWN; keys KEYCODE_DPAD_LEFT 4
       key KEYCODE_DPAD_DOWN; keys KEYCODE_DPAD_RIGHT 2; sleep 0.5; key KEYCODE_BACK ;;
    5) # Gear: Up to the hero, Up to the top bar (lands on the weather pill or gear), Right to the gear.
       keys KEYCODE_DPAD_UP 3; key KEYCODE_DPAD_RIGHT; key KEYCODE_DPAD_CENTER; sleep 1
       keys KEYCODE_DPAD_DOWN 25; keys KEYCODE_DPAD_UP 25; key KEYCODE_BACK ;;
    *) echo "unknown scenario $1" >&2; exit 2 ;;
  esac
  sleep 1.5
}

# total janky% p50 p90 p95 from the app's gfxinfo
read_gfx() {
  adb shell dumpsys gfxinfo "$PKG" | awk '
    /^Total frames rendered:/ {t=$4}
    /^Janky frames:/          {gsub(/[()%]/, "", $4); j=$4}
    /^50th percentile:/       {gsub(/ms/, "", $3); p50=$3}
    /^90th percentile:/       {gsub(/ms/, "", $3); p90=$3}
    /^95th percentile:/       {gsub(/ms/, "", $3); p95=$3}
    END {printf "%s %s %s %s %s\n", t, j, p50, p90, p95}'
}

median() { sort -n | awk '{a[NR]=$1} END {print (NR % 2) ? a[(NR + 1) / 2] : (a[NR / 2] + a[NR / 2 + 1]) / 2}'; }

adb get-state >/dev/null
echo "package $PKG  version $(adb shell dumpsys package "$PKG" | awk -F= '/versionName/ {print $2; exit}')  runs $RUNS"
printf '%-3s %-4s %7s %7s %6s %6s %6s\n' sc run frames janky% p50 p90 p95
for sc in "${SCENARIOS[@]}"; do
  results=()
  for ((r = 1; r <= RUNS; r++)); do
    cold_start
    adb shell dumpsys gfxinfo "$PKG" reset >/dev/null
    run_scenario "$sc"
    line=$(read_gfx)
    results+=("$line")
    # shellcheck disable=SC2086
    printf '%-3s %-4s %7s %7s %6s %6s %6s\n' "$sc" "$r" $line
  done
  med=()
  for col in 1 2 3 4 5; do
    med+=("$(printf '%s\n' "${results[@]}" | awk -v c=$col '{print $c}' | median)")
  done
  printf '%-3s %-4s %7s %7s %6s %6s %6s\n' "$sc" med "${med[@]}"
done
