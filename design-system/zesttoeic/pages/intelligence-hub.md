# Intelligence Hub Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** AI Analytics Dashboard

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1400px
- **Layout:** Sidebar + main content with 12-column grid
- **Sections:** 1. Current Proficiency score + trend, 2. AI Recommendations (personalized study plan), 3. Skill Breakdown (Listening/Reading/Grammar/Vocab radar chart), 4. Weak Areas detail table, 5. Practice History timeline, 6. Predicted Score projection

### Spacing Overrides

- **Content Density:** Medium-high — balance data richness with breathing room
- **Chart card padding:** `--space-xl` (32px)
- **Metric card gaps:** `--space-lg` (24px)

### Typography Overrides

- **Proficiency score:** `display-lg` using Space Grotesk (monospace feel)
- **AI insight labels:** `label-md` in `--color-text-secondary`
- **Chart legends:** `label-sm` using Space Grotesk

### Color Overrides

- **Background:** `--color-surface-dim` (#F2F4F6) for data-rich sections
- **Metric cards:** White surface with colored top accent bar
  - Listening: `--color-primary` (#2563EB)
  - Reading: `--color-tertiary` (#38BDF8)
  - Grammar: `--color-xp` (#A78BFA)
  - Vocabulary: `--color-success` (#22C55E)
- **AI Recommendation cards:** Subtle gradient border (Primary → Tertiary)
- **Score trend chart:** Primary blue line with 10% opacity area fill
- **Predicted score:** Dashed line in `--color-xp` (#A78BFA)

### Component Overrides

- No overrides — use Master component specs

---

## Page-Specific Components

- **Radar Chart:** 5-axis skill visualization with primary blue fill at 20% opacity
- **AI Insight Cards:** Glass card with Lucide `sparkles` icon + recommendation text
- **Score Prediction Bar:** Dual bar showing current vs. predicted score

---

## Recommendations

- Effects: Radar chart draw animation on scroll, counter animations for metrics
- Interaction: Hover on chart segments shows detailed tooltip
- Data: Auto-refresh recommendations based on latest practice sessions
- AI badges: Use `--color-xp` violet for AI-driven content markers
