---
type: Process
title: Verify on the TV, then independent review, then commit
stage: all
---

# Verification before every commit

Applies to every stage **and every fix**, however small. Order is fixed: TV test → owner confirms → Claude Cowork review → commit and push. Never commit or push earlier.

## 1. Test on the TV with sample data
- Install the build on the owner's TV: `adb connect 192.168.1.5:5555`, then `./gradlew installDebug`, then `adb shell am start -n com.ks.tvlauncher/.MainActivity`. Once the dev app exists ([dev-workflow](dev-workflow.md)) this installs and tests **KS TV Dev** (`com.ks.tvlauncher.dev`) and never touches the real app. If the TV is unreachable, stop and ask; an emulator is not a substitute unless the owner agrees.
- Exercise the changed feature with **sample test data** entered through the app itself, with names starting `TEST ` (for example a subscription "TEST Netflix", ₹499 monthly; "TEST Prime", ₹1,499 yearly; a profile name "TEST Goldi"). Cover the normal path, an edge case and the empty state.
- Drive the TV with `adb shell input keyevent` (DPAD, ENTER, BACK) and check results with `adb exec-out screencap -p`; look at the screenshot only as far as the check needs ([token-budget](token-budget.md)).
- Protect the owner's real data: never run `pm clear`, and never uninstall, without asking first. Remove every `TEST ` item when done. Sample data never ships in the code ([ground-rules](ground-rules.md)).
- Record what was tested and the result in the report, and add new checks to `docs/MANUAL_TEST.md`.

## 2. Owner confirms
Report the TV results and wait for the owner to confirm they are verified.

## 3. Claude Cowork review
- Leave the work **uncommitted** and stop. Give the owner this hand-off to paste into their Claude Cowork chat: the stage or fix name, the files changed, the build, unit-test and `kb.py check` results, and the TV test results.
- Cowork reviews the working tree against the spec. Fix whatever it finds, then repeat from step 1 for the changed parts.

## 4. Commit and push
Only after the owner says Cowork approved: `git commit` (one commit per stage or fix), then `git push`. Tags and releases stay the owner's decision. After a release, follow [dev-workflow](dev-workflow.md) steps 4 to 6: release onto the TV, verify the owner's data survived, remove KS TV Dev.
