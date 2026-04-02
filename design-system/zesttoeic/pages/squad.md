# Squad Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Social / Team Management

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px
- **Layout:** Sidebar + Main content
- **Sections:** 1. My Squad header (name, members, streak), 2. Squad Activity feed, 3. Squad Leaderboard (internal), 4. Squad Challenges, 5. Members grid, 6. Join/Create Squad (if no squad)

### Spacing Overrides

- **Activity feed gaps:** `--space-md` (16px)
- **Member card grid:** `--space-lg` (24px) gap

### Typography Overrides

- **Squad name:** `headline-lg` weight 700
- **Activity text:** `body-md` with timestamp in `label-sm`

### Color Overrides

- **Squad streak counter:** `--color-warning` (#F59E0B) with flame icon
- **Squad XP total:** `--color-xp` (#A78BFA) badge
- **Active now indicator:** `--color-success` (#22C55E) pulsing dot
- **Challenge cards:** Left accent bar using `--color-cta` (#F97316)
- **Member online:** Green dot, offline: grey dot
- **Join Squad CTA:** `btn-primary` (Orange gradient)

### Component Overrides

- No overrides — use Master component specs

---

## Page-Specific Components

- **Activity Feed:** Timeline-style list (avatar + action text + timestamp)
- **Challenge Card:** Title + progress bar + XP reward + deadline
- **Member Card:** Avatar + name + level badge + online status

---

## Recommendations

- Effects: Activity feed items fade-in on load, online status pulse
- Social: Show "X members studying now" with animated dots
- Gamification: Daily squad challenges with shared XP rewards
