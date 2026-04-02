# Focus Mode Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Productivity / Immersive Tool

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 100%" (Full bleed)
- **Layout:** Zen Mode (Minimalist, distracting elements hidden)
- **Sections:** 1. Massive Timer / Progress Ring center, 2. Current Task text, 3. Play/Pause/Stop controls, 4. Passive Stats (session time).

### Spacing Overrides

- **Timer margin:** Extreme whitespace, `--space-3xl` above and below

### Typography Overrides

- **Timer Text:** `display-xl` (6rem or larger), strictly monospace (`Space Grotesk`) weight 700
- **Task Focus Text:** `headline-md` weight 500

### Color Overrides

- **Background:** Shift to dark mode for focus `--color-dark-bg` (#0F172A) by default
- **Timer text:** `--dark-text` (#F1F5F9)
- **Focus Progress Ring:** `--color-primary` (Blue)
- **Break Progress Ring:** `--color-success` (Green)
- **Warning State (Under 1 min):** `--color-warning` (Amber)

### Component Overrides

- **Controls (Play/Pause):** Massive circular buttons. Ghost style border with solid center icon.

---

## Page-Specific Components

- **Pomodoro Ring:** SVG circular progress bar tracking minutes elapsed over total time circle. Smooth `stroke-dashoffset` animation.
- **Zen Mode Toggle:** Hides all navigation, headers, footers for full-screen mode.
- **Lofi Player (Optional):** Tiny floating widget at bottom right for ambient music.

---

## Recommendations

- Effects: Progress ring updates must be smooth (CSS transition on stroke-dashoffset).
- State: Confirm dialog if user tries to exit/close tab while focus mode is active.
- Experience: Auto full-screen mode request upon clicking "Start Focus".
