---
type: Specification
title: Renewal reminders
description: "Renewal notifications (Stage 5)."
tags: [reminders, feature]
stage: 5
---

# Renewal reminders

- WorkManager daily check; local notification N days before a renewal (N from [settings](settings.md)).
- Add `POST_NOTIFICATIONS`; request it on API 33+.
- Reschedule when a [subscription](subscriptions.md) date changes; cancel when it is paused or deleted.
- Never promise exact timing; WorkManager may defer work.
- While reminders are on, the daily work is re-registered about 5 s after Home is first shown (unique work, KEEP; no immediate check), so it comes back after a force-stop ([performance](performance.md)). Turning reminders off cancels it as before.
