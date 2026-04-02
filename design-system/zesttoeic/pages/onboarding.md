# Onboarding Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** User Activation / Flow

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 800px (Centered, focus-driven layout)
- **Layout:** Wizard / Step-by-step flow
- **Sections:** 1. Top progress bar, 2. Question/Prompt area, 3. Visual/Avatar selection, 4. Bottom action bar (Next/Skip)

### Spacing Overrides

- **Step Content padding:** `--space-3xl` (64px) top/bottom to keep it airy.
- **Choices gap:** `--space-md` (16px)

### Typography Overrides

- **Welcome/Question text:** `display-sm` (2.5rem) weight 600, centered
- **Choice Option text:** `headline-sm` weight 500

### Color Overrides

- **Background:** Shift away from standard gray to `--color-surface` (White) to feel cleaner.
- **Progress Bar:** `--color-primary`
- **Selected Choice:** Highlight border with `--color-primary` and soft background `--color-primary-light`.

### Component Overrides

- **Choice Card:** Large clickable area with hover lift (`--shadow-md`), becomes `--shadow-glow` when selected.
- **Bottom Navigation Bar:** Fixed to bottom of screen on mobile, absolute at bottom of container on desktop.

---

## Page-Specific Components

- **Wizard Progress Indicator:** Dots or continuous bar indicating "Step 1 of 4".
- **Avatar Builder:** Interactive grid of avatar illustrations with bounding ring when selected.
- **Confetti Celebration:** Canvas-based Confetti effect on the final "You're all set!" screen.

---

## Recommendations

- Interaction: Auto-advance to next step when a single-choice option is clicked (do not force the user to click "Next").
- UX: Keep copy extremely concise. Provide a "Skip" option where ethically/technically possible.
