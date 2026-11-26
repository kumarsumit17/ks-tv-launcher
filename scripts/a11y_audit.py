#!/usr/bin/env python3
"""Accessibility label audit for KS TV (specs/accessibility.md). Development tool only.

Reads a `uiautomator dump` XML and lists every focusable element that TalkBack would have no
words for: no text or content description on the element itself or on anything inside it
(TalkBack reads a focusable element's children as its label).

    adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml
    python3 scripts/a11y_audit.py ui.xml [more.xml ...]

Exit code 1 when an unlabelled focusable element is found.

Known false positive: a control clipped at the edge of a scrolling list (under the Settings
header, for example) has its text outside the visible part, so the dump leaves the text out.
TalkBack scrolls it into view when it moves there, and the label is read. Check the screenshot
before fixing such a hit.
"""
import sys
import xml.etree.ElementTree as ET

PACKAGE_PREFIX = "com.ks.tvlauncher"


def words(node):
    own = (node.get("text") or "").strip() or (node.get("content-desc") or "").strip()
    if own:
        return own
    for child in node:
        found = words(child)
        if found:
            return found
    return ""


def audit(path):
    root = ET.parse(path).getroot()
    problems, focusables = [], 0
    for node in root.iter("node"):
        if not (node.get("package") or "").startswith(PACKAGE_PREFIX):
            continue
        if node.get("focusable") != "true" and node.get("clickable") != "true":
            continue
        # The window's own root views are focusable containers, not controls.
        if node.get("class") in ("android.widget.FrameLayout", "android.widget.LinearLayout", "android.view.ViewGroup", "androidx.compose.ui.platform.ComposeView"):
            continue
        focusables += 1
        if not words(node):
            problems.append("%s: unlabelled %s at %s" % (path, node.get("class"), node.get("bounds")))
    return focusables, problems


def main(paths):
    failed = False
    for path in paths:
        count, problems = audit(path)
        print("%s: %d focusable, %d unlabelled" % (path, count, len(problems)))
        for p in problems:
            print("  " + p)
        failed = failed or bool(problems)
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
