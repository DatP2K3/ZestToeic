# Admin Moderation Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Admin / Community Safety

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** Full width (1600px max)
- **Layout:** Triage queue layout (Left nav, heavy task list)
- **Sections:** 1. Queue summary (Reported, Needs Review, Auto-flagged), 2. Report Tickets List, 3. Action panel (Approve, Delete, Warning Strike)

### Spacing Overrides

- **Ticket spacing:** `--space-md` gap between reported items to clearly separate contexts.

### Typography Overrides

- **Reported content:** `body-md` italic if flagged as inappropriate.
- **Reporter Info:** `body-sm` in `--color-text-secondary`.

### Color Overrides

- **Offensive Content Highlight:** `--color-error` background at 10% opacity.
- **Spam Flag:** `--color-warning` background at 10% opacity.
- **Safe/Resolved Action:** `--color-success` text button.
- **Ban/Delete Action:** `--color-error` filled button.

### Component Overrides

- **Ticket Card:** Distinct border `--color-error` (if high severity). Includes context (original post vs reported comment).
- **Triage Action Group:** Attached to the bottom right of ticket cards for mass clearing.

---

## Page-Specific Components

- **Context Viewer:** Mini-modal that fetches the parent thread so moderators can see what led to the reported comment.
- **Strike System Graphic:** 3 strike indicators (dots) next to the offender's profile name, turning red as strikes accumulate.

---

## Recommendations

- UX: Keyboard shortcuts (e.g., 'E' to approve, 'D' to delete, 'J/K' to navigate) to dramatically speed up the moderation workflow.
- Visual: Blur potentially explicit images by default, require click to view.
