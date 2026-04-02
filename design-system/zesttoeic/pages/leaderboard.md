# Leaderboard Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Ranking / Competition

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px
- **Layout:** Central content with filter nav
- **Sections:** 1. Leaderboard header (title + time filter: Daily/Weekly/All-time), 2. Top 3 podium display, 3. Rankings table (rank 4+), 4. My Position highlight, 5. Filter tabs (Global / Friends / Squad)

### Spacing Overrides

- **Podium card gaps:** `--space-lg` (24px)
- **Table row height:** 56px minimum

### Typography Overrides

- **Rank number (Top 3):** `display-md` using Space Grotesk
- **Score values:** `title-md` using Space Grotesk
- **Username:** `body-lg` weight 500

### Color Overrides

- **Top 3 Podium backgrounds:**
  - 🥇 1st: Gradient `#F59E0B` → `#D97706` (Gold)
  - 🥈 2nd: Gradient `#94A3B8` → `#64748B` (Silver)
  - 🥉 3rd: Gradient `#D97706` → `#92400E` (Bronze)
- **My rank row:** `--color-primary-light` (#DBE1FF) background highlight
- **Rank change indicator:** `--color-success` (↑ green) / `--color-error` (↓ red)
- **Table alternating rows:** White / `--color-surface-dim` (#F2F4F6)
- **XP badge:** `--color-xp` (#A78BFA) pill shape

### Component Overrides

- No overrides — use Master component specs

---

## Page-Specific Components

- **Podium Display:** 3 elevated cards with avatar, crown icon (1st), score, rank change
- **Rank Change Arrow:** Animated green up / red down arrow with +/- number
- **Score Badge:** Pill-shaped badge with XP value

---

## Recommendations

- Effects: Rank change animation (slide-up/down), podium entrance animation
- Responsive: Stack podium vertically on mobile, table becomes card list
- Interaction: Click user to see their profile mini-card
- Filter: Smooth tab transition when switching time periods
