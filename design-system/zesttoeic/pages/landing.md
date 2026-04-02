# Landing Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Landing / Marketing

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1440px
- **Layout:** Full-width hero, centered content blocks
- **Sections:** 1. Hero (headline + CTA + animated illustration), 2. Social proof (user count, rating), 3. Key Features (3 cards), 4. How it Works (steps), 5. Testimonials, 6. Pricing teaser, 7. Final CTA + Footer

### Spacing Overrides

- **Hero vertical padding:** `--space-3xl` (64px)
- **Section gaps:** `--space-2xl` (48px)

### Typography Overrides

- **Hero headline:** `display-lg` (3.5rem) weight 700 with gradient text effect (Primary → Tertiary)
- **Section titles:** `headline-lg` (2rem) weight 600

### Color Overrides

- **Hero background:** Gradient from `#F8FAFC` to `#EFF6FF` (subtle blue tint)
- **CTA section:** Primary blue background (`#2563EB`) with white text
- **Feature cards:** White surface with primary icon accent

### Component Overrides

- Hero CTA uses `btn-primary` (Orange gradient) — largest size
- Secondary CTA uses `btn-secondary` (Blue outline)

---

## Page-Specific Components

- **Animated Score Counter:** Count-up animation showing "900+ TOEIC Score" in hero
- **Feature Cards:** Lucide icons + title + description, hover lift effect
- **Social Proof Bar:** Logo strip of partner companies/universities

---

## Recommendations

- Effects: Scroll-triggered fade-in, hero parallax background, CTA glow pulse
- Performance: Use WebP images, lazy-load below-fold content
- Navigation: Sticky navbar with glassmorphism, highlight active section
