# Pricing Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Pricing / Subscription

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1200px
- **Layout:** Single column centered
- **Sections:** 1. Pricing header (title + toggle Monthly/Annual), 2. Pricing cards (3 tiers), 3. Feature comparison table, 4. FAQ accordion, 5. Money-back guarantee + Final CTA

### Spacing Overrides

- **Pricing cards gap:** `--space-lg` (24px)
- **Content density:** Low — maximum clarity

### Typography Overrides

- **Price value:** `display-md` using Space Grotesk
- **Plan name:** `headline-md` weight 600
- **Feature list:** `body-md` with Lucide check icons

### Color Overrides

- **Free tier card:** White surface, `--color-text-secondary` header
- **Popular tier (Pro):** `--color-primary` (#2563EB) gradient header + "Most Popular" badge
- **Premium tier:** `--color-dark-bg` (#0F172A) header with gold accent `#F59E0B`
- **"Most Popular" badge:** `--color-cta` (#F97316) pill badge
- **Annual savings badge:** `--color-success` (#22C55E) small tag
- **Feature checkmark:** `--color-success` (#22C55E) ✓ / `--color-text-tertiary` ✗
- **CTA buttons:**
  - Free: `btn-secondary` (Blue outline)
  - Pro: `btn-primary` (Orange gradient) — prominent
  - Premium: White button on dark background

### Component Overrides

- Pricing toggle: Pill-shaped switch with smooth slide animation

---

## Page-Specific Components

- **Pricing Card:** Elevated card with plan name + price + feature list + CTA
- **Annual/Monthly Toggle:** Pill switch with savings label
- **FAQ Accordion:** Smooth expand/collapse with Lucide chevron rotation

---

## Recommendations

- Effects: Cards entrance animation (staggered fade-up), price number morphing on toggle
- Trust: Money-back guarantee section with shield icon
- CTA: Popular plan card slightly larger/elevated than others
