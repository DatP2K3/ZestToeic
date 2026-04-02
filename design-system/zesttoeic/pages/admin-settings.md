# Admin Settings Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Admin / System Configuration

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1000px (Centered within the admin workspace for readability)
- **Layout:** Vertical tabs or Left-nav nested
- **Sections:** 1. Global Settings (Maintenance mode), 2. Feature Flags (Enable/Disable modules), 3. Pricing & Stripe Config, 4. Gamification Logic (XP balancing), 5. AI Integrations (API Keys)

### Spacing Overrides

- **Form group padding:** `--space-xl` (32px)
- **Divider gaps:** Prominent `--space-2xl` between major sections to prevent accidental misconfigurations.

### Typography Overrides

- **Config Label:** `label-md` weight 700
- **Warning text:** `body-sm` color `--color-error` below risky toggles (e.g., "Disabling this will break the mobile app").

### Color Overrides

- **Active Toggle (On):** `--color-success`
- **Inactive Toggle (Off):** `--color-surface-elevated`
- **Maintenance Banner preview:** `--color-warning` with dark text.
- **Danger Zone Panel:** Surrounded by a dashed `--color-error` border with a subtle red background.

### Component Overrides

- **Settings Group:** Wrapped in a standard `card` layout to create distinct configuration blocks.
- **Feature Flag Toggle:** Larger switch component for better tactile feedback.
- **Save Changes Button:** Floats at the bottom of the viewport when unsaved changes exist (`btn-primary`).

---

## Page-Specific Components

- **Variable Input Field:** Number inputs tailored for balancing game mechanics (e.g., XP per test).
- **Environment Badge:** Sticky badge in header showing if editing `Staging` vs `Production` configs.

---

## Recommendations

- State: "Unsaved changes" indicator must be highly visible.
- UX: Track config history (who changed what rule and when) in a mini-log beside each major setting group.
