# Error Pages Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Fallback / Utility (404, 500, Offline, Maintenance)

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 600px
- **Layout:** Single column, vertically and horizontally centered in the viewport.
- **Sections:** 1. Massive Illustration/Graphic, 2. Error Code, 3. Friendly message, 4. CTA to return to safety.

### Spacing Overrides

- **Graphic to text gap:** `--space-2xl` (48px)
- **Text to button gap:** `--space-xl` (32px)

### Typography Overrides

- **Error Code (e.g. 404):** `display-xl` (6rem+), weight 800, faded opacity or gradient text.
- **Friendly Title:** `headline-lg` weight 600
- **Subtitle:** `body-lg` in `--color-text-secondary`

### Color Overrides

- **404 (Not Found):** Use neutral `--color-text-tertiary` for the giant number.
- **500 (Internal Error):** Incorporate a subtle `--color-error` (Red) touch, maybe via graphics.
- **Maintenance:** `--color-warning` (Amber) accents.
- **Background:** Solid `--color-background` (#F8FAFC), avoid complex layers.

### Component Overrides

- **Back Home Button:** `btn-primary`

---

## Page-Specific Components

- **Playful Animations:** SVG animation of a lost astronaut, disconnected plug, or a compass spinning for 404.
- **Auto-Refresh Countdown (Maintenance):** Shows "Retrying in 10... 9..." for network errors.

---

## Recommendations

- UX: Never just say "Error". Always explain what happened in human terms ("Looks like you're lost", "Our servers are taking a brief nap").
- Helpful Links: On 404 pages, provide quick links to "Practice", "Dashboard", and "Support" below the main CTA.
