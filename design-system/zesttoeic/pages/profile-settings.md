# Profile & Settings Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** User Profile / Settings

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px
- **Layout:** Sidebar settings nav (240px) + Main content panel
- **Sections:** 1. Profile header (avatar + name + level + XP), 2. Account settings, 3. Notification preferences, 4. Study preferences (daily goal, difficulty), 5. Subscription status, 6. Privacy & Security, 7. Danger zone (delete account)

### Spacing Overrides

- **Settings group gaps:** `--space-xl` (32px)
- **Form field gaps:** `--space-md` (16px)

### Typography Overrides

- **Profile name:** `headline-md` weight 600
- **Section headers:** `title-md` weight 600 with `label-md` category tag
- **Setting descriptions:** `body-md` in `--color-text-secondary`

### Color Overrides

- **Settings nav:** `--color-surface-dim` (#F2F4F6) background
- **Active nav item:** `--color-primary-light` (#DBE1FF) bg + primary blue text
- **Avatar ring:** Primary blue (or level-based color)
- **Level badge:** `--color-xp` (#A78BFA) pill
- **Daily goal progress:** `--color-success` (#22C55E) fill bar
- **Subscription badge (Premium):** Gold `#F59E0B` with Lucide `crown` icon
- **Subscription badge (Free):** `--color-text-tertiary` neutral
- **Danger zone:** `--color-error` (#EF4444) light red background + red border
- **Save button:** `btn-primary` (Orange gradient)

### Component Overrides

- Toggle switches: Primary blue when on, grey when off
- Select dropdowns: Custom styled matching input spec

---

## Page-Specific Components

- **Avatar Upload:** Circular with camera icon overlay on hover
- **Daily Goal Selector:** Slider or pill group (15min / 30min / 60min / Custom)
- **Achievement Showcase:** 3 most recent achievements with icons

---

## Recommendations

- Effects: Save confirmation with green checkmark toast
- Validation: Real-time email/password validation
- Privacy: Toggle switches with smooth animated thumb
- Danger: Delete account requires typing confirmation
