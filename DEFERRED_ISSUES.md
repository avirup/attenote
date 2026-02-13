# Deferred Issues

## List Continuation in Rich Notes Editor
- Reported on: 2026-02-14
- Scope: Prompt 12 rich-text editor (`Add Note` screen)
- Current behavior: Ordered and unordered lists may stop continuing after several points on Enter.
- Status: Addressed in Prompt 14 hardening pass (editor field now grows instead of fixed-height clipping; route/error handling and editor change normalization also hardened).
- Follow-up plan: Re-verify on-device during Prompt 14 smoke/regression checklist and reopen only if list continuation still reproduces.
