# Auth Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Authentication (Login / Register)

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px
- **Layout:** Split-screen — Left: brand visual/illustration, Right: auth form
- **Sections (Login):** 1. Brand illustration panel, 2. Login form (email/password), 3. Social login (Google/Facebook), 4. Register link
- **Sections (Register):** 1. Brand panel, 2. Registration form, 3. Social signup, 4. Login link

### Spacing Overrides

- **Form padding:** `--space-xl` (32px)
- **Input gaps:** `--space-md` (16px)

### Typography Overrides

- **Welcome heading:** `headline-md` weight 600
- **Subtext:** `body-lg` in `--color-text-secondary`

### Color Overrides

- **Left panel:** Gradient from `#2563EB` to `#1D4ED8` with white text + subtle pattern overlay
- **Right panel (form):** White surface (`#FFFFFF`)
- **Social login buttons:** Surface dim (`#F2F4F6`) with hover to white
- **Input focus:** Primary blue ring (`--color-primary` at 10% opacity)

### Component Overrides

- Login button: `btn-primary` (Orange CTA gradient)
- Social buttons: Ghost border + icon + text
- "Forgot password" link: `btn-tertiary` style

---

## Page-Specific Components

- **Brand Illustration Panel:** Animated elements showing TOEIC score improvement
- **Password Strength Indicator:** Color bar (Red → Yellow → Green)

---

## Recommendations

- Effects: Gentle fade-in for form panel, focus ring animation on inputs
- Security: Show/hide password toggle with Lucide `eye` / `eye-off`
- Validation: Real-time validation on blur with `--color-success` / `--color-error`
