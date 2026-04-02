# Notifications Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Utility / Engagement

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 720px (Can also be a Dropdown Popover, but this ruleset applies to the full page)
- **Layout:** Simple list view
- **Sections:** 1. Header (Filters: All / Unread, Mark all as read), 2. Notification List, 3. Settings shortcut

### Spacing Overrides

- **List Item Padding:** `--space-md` (16px)
- **Icon / Text gap:** `--space-sm` (8px)

### Typography Overrides

- **Notification Text:** `body-md` - bold for unread, normal for read.
- **Timestamp:** `label-sm` in `--color-text-tertiary`

### Color Overrides

- **Unread Indicator:** A subtle `--color-primary-light` background tint or a pure `--color-primary` dot.
- **System Alert Icon:** `--color-warning` (Amber)
- **Social Alert Icon:** `--color-xp` (Violet)

### Component Overrides

- **Notification Row:** Must have `cursor-pointer`. Hover states use surface-dim (`#F2F4F6`).

---

## Page-Specific Components

- **Empty State:** Illustration of a sleeping mascot or empty mailbox with text "You're all caught up!".
- **Action Buttons Inline:** E.g., "Accept Squad Invite" right inside the notification row.

---

## Recommendations

- UX: Immediately clear the "Unread" dot ping globally when the notification page/dropdown is opened.
- Performance: Use pagination or infinite scrolling (Load more on scroll) if notifications exceed 50.
