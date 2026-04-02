# Community Forum Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Social Learning / Q&A

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px
- **Layout:** Standard two-column (Main feed + Sidebar with Popular tags/Top contributors)
- **Sections:** 1. Filter/Search bar, 2. Global feed (Learn & Teach), 3. Pinned announcements, 4. Question details (threaded comments).

### Spacing Overrides

- **Thread gap:** `--space-lg` (24px)
- **Comment indent:** `--space-xl` (32px) for nested replies
- **Tag spacing:** `--space-xs` (4px)

### Typography Overrides

- **Question Title:** `headline-sm` (1.5rem) weight 600
- **Teacher Badge:** `label-sm` weight 700 with uppercase
- **Upvote Count:** `body-lg` weight 600, monospace for numbers

### Color Overrides

- **Upvote Button:** Default `--color-text-tertiary`, active `--color-primary`
- **Teacher/Verified Badge:** `--color-success` (#22C55E) background with white text
- **Answered/Solved Check:** `--color-success` icon
- **Tag Background:** `--color-primary-light` (#DBE1FF) with `--color-primary-dark` text

### Component Overrides

- **Search input:** Expanded size, floating search icon inside `input`.
- **"New Question" button:** `btn-primary` fixed at bottom right on mobile, top right on desktop.

---

## Page-Specific Components

- **Thread Card:** Title, preview of content, tag chips, upvote counter, reply counter, time elapsed.
- **Verified Answer Block:** Highlighted background (`--color-success` at 5% opacity) and green border.
- **Upvote Widget:** Vertical stacking of Up arrow, Number, Down arrow.

---

## Recommendations

- Effects: Smooth expand/collapse for nested replies.
- Interaction: Upvote action should give immediate visual feedback.
- Moderation: Implement a discrete "Report" flag icon with hover state (`--color-error`).
