# Admin Users Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Admin / User Management

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** Full width (1600px max)
- **Layout:** Fixed admin sidebar + List / Detail split view
- **Sections:** 1. Advanced Search (Email, ID, Clan), 2. User Data Table, 3. Slide-out Panel for User Details (Profile, Activity logs, Subscription status, Moderation actions)

### Spacing Overrides

- **Slide-out Panel Width:** 40vw minimum for detailed logs

### Typography Overrides

- **User ID / Email:** `body-md` monospace for easy copy-pasting
- **Action Logs:** `body-sm` in `--color-text-secondary`

### Color Overrides

- **Ban/Suspend Action:** Critical action using pure `--color-error` (Red)
- **Warning Action:** `--color-warning` (Amber)
- **Subscription Tags:** 
  - Free: Neutral gray
  - Premium: `--color-xp` (Violet)

### Component Overrides

- **Action Dropdown (kebab menu):** Appears on hover inside table rows for quick actions (Reset password, Suspend).
- **Slide-out Drawer:** Glides in from the right edge with a dark overlay `rgba(15, 23, 42, 0.5)` on main content.

---

## Page-Specific Components

- **User Activity Heatmap:** GitHub-style contribution graph showing the user's daily study frequency.
- **Permissions Toggles:** iOS-style switch components for granting admin/moderator roles.

---

## Recommendations

- Security: Destructive actions (Ban, Delete) require typing the user's email to confirm.
- State: Ensure the user detail panel has a sticky header with the user's name and status badge so context is never lost when scrolling logs.
