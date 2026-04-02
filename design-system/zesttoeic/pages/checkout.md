# Checkout Page Overrides

> **PROJECT:** ZestTOEIC
> **Updated:** 2026-04-02
> **Page Type:** Billing / Revenue

> ⚠️ **IMPORTANT:** Rules in this file **override** the Master file (`design-system/MASTER.md`).
> Only deviations from the Master are documented here. For all other rules, refer to the Master.

---

## Page-Specific Rules

### Layout Overrides

- **Max Width:** 1000px
- **Layout:** Split layout (Left: Payment Form, Right: Order Summary/Trust signals)
- **Sections:** 1. Plan selected, 2. Billing interval toggle (Monthly/Yearly), 3. Secure Payment widget (Stripe), 4. Trust badges / Testimonials

### Spacing Overrides

- **Input gap:** `--space-md` (16px) inside the payment form.

### Typography Overrides

- **Total Price:** `display-sm` (2.5rem) weight 700 with a monospace touch for numbers.
- **Discount text:** `label-md` uppercase, strikethrough for old prices.

### Color Overrides

- **Discount/Savings Tag:** `--color-success` (#22C55E) background with white text.
- **Secure Icon/Lock:** `--color-success` or neutral `--color-text-secondary` to convey safety.
- **Pay Button:** Absolute massive `btn-primary`, potentially pulsing or showing a locked icon.

### Component Overrides

- **Order Summary Box:** Stronger contrast, usually set against `--color-surface-dim` to stand out from the white payment form.
- **Payment Inputs:** Must look 100% native but adopt our border-radius (`--radius-md`) and focus ring (`--color-primary`).

---

## Page-Specific Components

- **Stripe Elements Container:** Integrated seamlessly without iframe-looking borders.
- **Trust Badges:** Grayed out icons for Visa, Mastercard, SSL Secure, Money-back guarantee.
- **Success Overlay:** A full-screen or modal component triggering a success animation when payment clears.

---

## Recommendations

- Security: Emphasize security visually (padlock icons, "encrypted" text).
- Avoid Distraction: Hide global top navigation links during the checkout phase to prevent users from abandoning the cart. Provide only a "Back" button.
