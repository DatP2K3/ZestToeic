# Placement Test Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Assessment / Placement Test

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 960px centered (narrow focus)
- **Layout:** Single column, distraction-free
- **Sections:** 1. Test intro (what to expect, estimated time), 2. Question view (same as Practice), 3. Progress indicator, 4. Results screen

### Spacing Overrides

- **Extra generous content padding:** `--space-xl` (32px) all sides

### Typography Overrides

- **Intro heading:** `headline-lg` in primary blue
- **Encouraging subtext:** `body-lg` in `--color-text-secondary`

### Color Overrides

- **Intro page:** Subtle gradient background (`#F8FAFC` → `#EFF6FF`)
- **During test:** Pure white background for reading clarity
- **Progress bar:** Primary blue with percentage label in Space Grotesk
- **Results score:** `display-lg` in primary blue with Progress Orb

### Component Overrides

- Start Test CTA: Extra large `btn-primary` with encouraging text
- Uses same question components as Practice page

---

## Page-Specific Components

- **Test Intro Card:** Glassmorphic card with icon list (30 questions, 20 min, instant result)
- **Results Breakdown:** Score + CEFR level + radar chart of skills

---

## Recommendations

- Effects: Countdown animation before test starts, results reveal animation
- UX: No back-navigation during test, clear question progress
- Results: Celebrate with confetti animation for high scores
