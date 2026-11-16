---
type: Feature
title: Renewal reminders
stage: 5
---

# Renewal reminders

- WorkManager daily check; local notification N days before a renewal (N from [settings](settings.md)).
- Add `POST_NOTIFICATIONS`; request it on API 33+.
- Reschedule when a [subscription](subscriptions.md) date changes; cancel when it is paused or deleted.
- Never promise exact timing; WorkManager may defer work.
