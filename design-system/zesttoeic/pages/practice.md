# Practice Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** TOEIC Practice / Question View

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px centered
- **Layout:** Single column, focused content
- **Sections:** 1. Practice header (Part type, progress bar), 2. Question area (audio player for Listening / passage for Reading), 3. Answer options (A/B/C/D), 4. Navigation (Previous / Next / Submit), 5. Question palette sidebar (optional)

### Spacing Overrides

- **Question content padding:** `--space-xl` (32px)
- **Answer option gaps:** `--space-sm` (8px)

### Typography Overrides

- **Question number:** `label-md` in `--color-text-secondary`
- **Question text:** `title-md` (1.25rem) weight 500
- **Reading passages:** `body-lg` with extra line-height (1.8) for readability

### Color Overrides

- **Background:** Clean white (`#FFFFFF`) for maximum reading focus
- **Selected answer:** `--color-primary-light` (#DBE1FF) background + primary border
- **Correct answer (review):** `--color-success` (#22C55E) left border + light green bg
- **Wrong answer (review):** `--color-error` (#EF4444) left border + light red bg
- **Progress bar track:** `--color-surface-dim` with `--color-primary` fill
- **Timer (if timed):** `--color-text` default, `--color-warning` when < 5min, `--color-error` when < 1min

### Component Overrides

- Audio player: Custom styled with primary blue accent
- Answer options: Radio-card style, not plain radio buttons

---

## Page-Specific Components

- **Audio Player:** Waveform visualization + play/pause + speed control
- **Question Palette:** Grid of numbered circles showing answered/unanswered/flagged
- **Part Selector:** Tab-style navigation for TOEIC Parts 1-7

---

## Recommendations

- Effects: Smooth transition between questions (slide-left), answer selection pulse
- Interaction: Keyboard shortcuts (1-4 for answers, Enter to submit)
- Focus: Minimize distractions — hide sidebar nav during practice
- Timer: Red pulse animation when time is critically low
