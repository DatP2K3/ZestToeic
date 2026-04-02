# Clan Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Social / Massive Multiplayer (Clan Management)

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1440px
- **Layout:** Three-column layout for dashboard (Sidebar nav, Main Feed/Stats, Right Widget rail)
- **Sections:** 1. Clan Header (Hero banner, logo, total XP, rank), 2. Clan Activity feed, 3. Internal Leaderboard, 4. Clan Wars Status/Events, 5. Clan Chat/Announcements, 6. Members list and roles.

### Spacing Overrides

- **Header padding:** `--space-2xl` (48px)
- **Widget gaps:** `--space-xl` (32px)
- **Activity item padding:** `--space-sm` (8px)

### Typography Overrides

- **Clan Name:** `display-sm` (2.5rem) weight 700 with text-shadow for readability over banner
- **Rank display:** `headline-md` weight 600, colored based on tier (Gold/Silver/Bronze)
- **Announcements:** `title-lg` weight 600

### Color Overrides

- **Clan Banner overlay:** Linear gradient to dark `--color-dark-bg` to ensure text is visible.
- **Clan XP Bar:** Gradient from `--color-xp` to `--color-primary`
- **Clan Wars indicator:** `--color-error` (Red) pulse if war is active
- **Admin/Leader tag:** `--color-warning` (Gold)
- **Member tag:** `--color-tertiary` (Teal)

### Component Overrides

- **Join Clan CTA:** Massive `btn-primary` with subtle scale animation.
- **Rank Badge:** 3D styled icon or SVG with glow effect (`--shadow-glow`).

---

## Page-Specific Components

- **Clan Hero Banner:** Full width image with dark overlay, centered Clan Avatar, Title, and Description.
- **War Status Widget:** Progress bar comparing Clan A vs Clan B with animated numbers.
- **Role Badge:** Small pill-shaped tag next to user names (Leader, Co-leader, Elder, Member).

---

## Recommendations

- Effects: Use parallax scroll on Clan Banner, pulse animation on active wars.
- Social: Emphasize "Clan total XP" with an odometer roll-up animation.
- Engagement: Show top 3 weekly contributors prominently to drive internal competition.
