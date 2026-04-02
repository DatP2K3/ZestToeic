# Help Center & FAQ Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Support / Information

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1000px
- **Layout:** Hero block with Search, followed by Category Grid, followed by FAQ Accordions.
- **Sections:** 1. Search Hero, 2. Quick Links/Categories (Billing, Tech, Content), 3. Top Articles, 4. Contact Support Block.

### Spacing Overrides

- **Hero padding:** `--space-3xl`
- **Accordion item padding:** `--space-md` (16px)

### Typography Overrides

- **Hero Question:** `display-sm` (2.5rem)
- **Category Titles:** `title-lg` weight 600
- **Accordion Text:** `body-md` line-height 1.7 for readability

### Color Overrides

- **Hero Background:** Dark mode `--color-dark-bg` or primary blue gradient to make search bar pop.
- **Category Card:** White surface with `--shadow-md`.
- **Contact Support Bar:** `--color-primary-light` (#DBE1FF) background.

### Component Overrides

- **Search Bar (Hero):** Oversized `input`, white background, deeply rounded `--radius-full`, with magnifying glass icon.
- **Accordion:** Subtle bottom border `--color-border`. Chevron icon rotates 180deg on open.

---

## Page-Specific Components

- **Category Icon Card:** Centered icon, title, short description. Hover elevates the card.
- **Helpful Rating Widget:** "Was this article helpful?" with simple Thumbs Up / Thumbs Down icons at the bottom of specific articles.

---

## Recommendations

- Interaction: Accordions must slide open/closed elegantly (no jarring jumps).
- SEO: Ensure FAQ questions use appropriate `<h2>` and `<h3>` tags and potentially Schema.org structured data.
