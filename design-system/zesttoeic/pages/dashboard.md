# Dashboard Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Student Dashboard / Home

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1400px
- **Layout:** Sidebar (240px fixed) + Main content area
- **Sections:** 1. Welcome header + Streak counter, 2. TOEIC Score Progress Orb, 3. Today's Goals / Daily quest cards, 4. Recent Practice sessions, 5. Weak Areas chart, 6. Quick Actions (Practice, Test, Squad)

### Spacing Overrides

- **Card grid gap:** `--space-lg` (24px)
- **Sidebar padding:** `--space-md` (16px)

### Typography Overrides

- **Score display:** `display-lg` using Space Grotesk for numeric impact
- **Welcome text:** `headline-md` + user's name

### Color Overrides

- **Sidebar:** `--color-surface-dim` (#F2F4F6) background
- **Active nav item:** Primary blue left border (3px) + `--color-primary-light` background
- **Streak display:** `--color-warning` (#F59E0B) with flame icon
- **XP counter:** `--color-xp` (#A78BFA) badge
- **Score Orb:** Primary blue conic-gradient fill

### Component Overrides

- Progress Orb: Uses Master's `.progress-orb` component
- Quick Action cards: Colored left accent bar (each action has its own accent)

---

## Page-Specific Components

- **Streak Counter:** Fire icon + day count + ring pulse animation
- **Daily Quest Cards:** Checkbox + title + XP reward badge
- **Score Trend Chart:** Line chart with primary blue line + gradient fill below

---

## Recommendations

- Effects: Count-up animations on load for XP/Score, progress bar fill on scroll
- Data: Show delta indicators (↑ +15 points this week) with green/red colors
- Quick Actions: Hover shows brief description + arrow-right icon
