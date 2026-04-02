# Admin Analytics Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Admin / Data & Analytics

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** Full width (1600px max)
- **Layout:** Masonry grid / Dashboard grid layout
- **Sections:** 1. Date Range Picker (Top right), 2. High-level Revenue/Activation charts, 3. Engagement funnel, 4. Question Difficulty metrics, 5. Cohort retention matrix

### Spacing Overrides

- **Chart Container padding:** `--space-xl` (32px) to let graphs breathe.

### Typography Overrides

- **Chart Labels:** `label-sm` weight 600 in `--color-text-tertiary`
- **Data Callouts:** `display-sm` monospace

### Color Overrides

- **Data Visualization Palette:**
  - Base: Primary blue `--color-primary`
  - Positive trend: `--color-success`
  - Negative trend: `--color-error`
  - Cohort matrix scale: Light violet to Deep violet `--color-xp`
- **Grid Lines:** `--color-border` at 30% opacity to minimize noise.

### Component Overrides

- **Date Picker:** Advanced date range selector widget with styling similar to Google Analytics.
- **Export Button:** Subdued outline button in top right of every chart container.

---

## Page-Specific Components

- **Funnel Chart:** Tiered horizontal bars representing Acquisition -> Activation -> Retention -> Revenue.
- **Cohort Retention Table:** Matrix grid with colored background intensity correlating to percentage retained.

---

## Recommendations

- Rendering: Use a canvas-based charting library (like Chart.js or Recharts) for high performance.
- UX: Hover over data points invokes a rich tooltip with exact numbers and date.
- Export: Ensure high-contrast mode applies before generating PDF/PNG exports.
