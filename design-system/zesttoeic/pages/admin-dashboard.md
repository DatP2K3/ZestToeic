# Admin Dashboard Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Admin / Data Management

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** Full width (1600px max)
- **Layout:** Fixed sidebar (280px dark) + main content
- **Sections:** 1. KPI summary cards (Users, Revenue, Active, Conversion), 2. User growth chart, 3. Recent registrations table, 4. Content management (Questions, Tests), 5. System alerts

### Spacing Overrides

- **Content Density:** High — optimize for data visibility
- **KPI card grid:** `--space-md` (16px) gap
- **Table row height:** 48px

### Typography Overrides

- **KPI values:** `display-md` using Space Grotesk
- **KPI labels:** `label-md` in `--color-text-secondary`
- **Table data:** `body-md` using Space Grotesk for numbers

### Color Overrides

- **Admin sidebar:** `--color-dark-bg` (#0F172A) with white text
- **Active nav:** White text + 3px left border `--color-primary` + subtle bg highlight
- **KPI cards accent bars:**
  - Total Users: `--color-primary` (#2563EB)
  - Revenue: `--color-success` (#22C55E)
  - Active Users: `--color-tertiary` (#38BDF8)  
  - Conversion: `--color-xp` (#A78BFA)
- **Trend indicators:** Green up / Red down arrows
- **Status badges:** Active=Green, Suspended=Yellow, Banned=Red
- **Alert badges:** Red dot with count number

### Component Overrides

- Data tables: Use Surface Dim alternating rows, no 1px row borders
- Admin buttons: More subdued — `btn-secondary` for most actions

---

## Page-Specific Components

- **KPI Card:** Large number + trend arrow + sparkline mini-chart
- **Data Table:** Sortable columns + search + bulk actions + pagination
- **System Alert:** Toast notification with severity color coding

---

## Recommendations

- Effects: Number count-up on KPI load, chart draw animation
- Interaction: Row hover highlights, bulk select with checkbox
- Data: Real-time updates via WebSocket for critical metrics
- Search: Debounced search with loading spinner
