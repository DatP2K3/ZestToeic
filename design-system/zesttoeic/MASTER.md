# Design System Master File

> **LOGIC:** When building a specific page, first check `design-system/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

---

**Project:** ZestTOEIC
**Updated:** 2026-04-02
**Category:** EdTech / Gamified Learning Platform
**North Star:** "The Luminous Scholar" — Premium editorial learning meets gamification energy

---

## Global Rules

### Color Palette

| Role | Hex | CSS Variable | Usage |
|------|-----|--------------|-------|
| Primary | `#2563EB` | `--color-primary` | Navigation, key actions, trust |
| Primary Dark | `#004AC6` | `--color-primary-dark` | Hover states, emphasis |
| Primary Light | `#DBE1FF` | `--color-primary-light` | Backgrounds, selected states |
| Secondary / CTA | `#F97316` | `--color-cta` | CTA buttons, "Zest" accent |
| Secondary Dark | `#9D4300` | `--color-cta-dark` | CTA hover, gradient end |
| Tertiary | `#38BDF8` | `--color-tertiary` | Info badges, links, progress |
| Success | `#22C55E` | `--color-success` | Correct answers, streaks, XP gain |
| Warning | `#F59E0B` | `--color-warning` | Time alerts, streak at risk |
| Error | `#EF4444` | `--color-error` | Wrong answers, destructive actions |
| XP / Reward | `#A78BFA` | `--color-xp` | XP indicators, achievements |
| Background | `#F8FAFC` | `--color-background` | Light mode base canvas |
| Surface | `#FFFFFF` | `--color-surface` | Cards, interactive panels |
| Surface Dim | `#F2F4F6` | `--color-surface-dim` | Secondary areas, sidebar bg |
| Text Primary | `#1E293B` | `--color-text` | Headings, primary body text |
| Text Secondary | `#64748B` | `--color-text-secondary` | Labels, captions, metadata |
| Text Tertiary | `#94A3B8` | `--color-text-tertiary` | Placeholders, disabled text |
| Border | `#E2E8F0` | `--color-border` | Subtle borders (use sparingly) |
| Dark BG | `#0F172A` | `--color-dark-bg` | Battle Royale, dark mode base |
| Dark Surface | `#1E293B` | `--color-dark-surface` | Cards on dark backgrounds |

### Dark Mode Palette (Battle Royale, Focused Practice)

| Role | Hex | CSS Variable |
|------|-----|--------------|
| Background | `#0F172A` | `--dark-bg` |
| Surface | `#1E293B` | `--dark-surface` |
| Surface Elevated | `#334155` | `--dark-surface-elevated` |
| Text | `#F1F5F9` | `--dark-text` |
| Text Secondary | `#94A3B8` | `--dark-text-secondary` |
| Accent Glow (Cyan) | `#14B8A6` | `--dark-accent` |
| Accent Glow (Blue) | `#38BDF8` | `--dark-accent-blue` |

### Typography

- **Heading Font:** Plus Jakarta Sans (Google Fonts)
- **Body Font:** Plus Jakarta Sans
- **Monospace / Data Font:** Space Grotesk (for scores, timers, stats)
- **Mood:** Editorial Authority + Playful Energy
- **Display:** 3.5rem, weight 700, letter-spacing -0.02em
- **Headline:** 2rem, weight 600
- **Title:** 1.25rem, weight 500
- **Body:** 1rem, weight 400, line-height 1.6
- **Label:** 0.75rem, weight 600, uppercase, letter-spacing +0.05em

### Spacing Variables

| Token | Value | Usage |
|-------|-------|-------|
| `--space-xs` | `4px` / `0.25rem` | Tight gaps |
| `--space-sm` | `8px` / `0.5rem` | Icon gaps, inline spacing |
| `--space-md` | `16px` / `1rem` | Standard padding |
| `--space-lg` | `24px` / `1.5rem` | Section padding |
| `--space-xl` | `32px` / `2rem` | Large gaps, card padding |
| `--space-2xl` | `48px` / `3rem` | Section margins |
| `--space-3xl` | `64px` / `4rem` | Hero padding |

### Roundness

| Token | Value | Usage |
|-------|-------|-------|
| `--radius-sm` | `4px` | Chips, small tags |
| `--radius-md` | `8px` | Buttons, inputs, cards |
| `--radius-lg` | `12px` | Large cards, modals |
| `--radius-xl` | `16px` | Hero sections, featured cards |
| `--radius-full` | `9999px` | Avatars, pill buttons |

### Shadow Depths

| Level | Value | Usage |
|-------|-------|-------|
| `--shadow-sm` | `0 1px 3px rgba(30,41,59,0.04)` | Subtle lift |
| `--shadow-md` | `0 4px 12px rgba(30,41,59,0.08)` | Cards, buttons |
| `--shadow-lg` | `0 12px 32px rgba(30,41,59,0.10)` | Modals, dropdowns |
| `--shadow-xl` | `0 20px 40px rgba(30,41,59,0.12)` | Hero images, featured |
| `--shadow-glow` | `0 0 20px rgba(37,99,235,0.15)` | Focus glow, interactive |

---

## Surface & Depth Philosophy

### The "No-Line" Rule
Strictly prohibit 1px solid borders for sectioning. Boundaries must be defined through:
- **Tonal Shifts:** `surface-dim` against `background`
- **Glass Definition:** backdrop-blur + "Ghost Border"
- **Spacing:** Generous whitespace between sections

### Surface Hierarchy
- **Base Layer:** `--color-background` (#F8FAFC) — Canvas
- **Sectional Layer:** `--color-surface-dim` (#F2F4F6) — Secondary zones
- **Interactive Layer:** `--color-surface` (#FFFFFF) — Cards, panels
- **Nesting:** Inner cards on sectional layers create natural depth

### Glassmorphism
For floating elements (nav, modals, scorecards):
- Background: `rgba(255, 255, 255, 0.7)`
- Backdrop Filter: `blur(20px)`
- Border: `1.5px solid rgba(255, 255, 255, 0.4)`

### Ghost Border Fallback
When accessibility requires an edge:
- Color: `--color-border` at 20% opacity
- Weight: 1.5px
- Never use 100% opaque borders

---

## Component Specs

### Buttons

```css
/* Primary CTA — "Zest" Orange Gradient */
.btn-primary {
  background: linear-gradient(135deg, #F97316, #EA580C);
  color: white;
  padding: 12px 28px;
  border-radius: 8px;
  font-weight: 600;
  font-family: 'Plus Jakarta Sans', sans-serif;
  transition: all 250ms ease;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(249,115,22,0.25);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(249,115,22,0.35);
}

/* Secondary Button — Blue Outline */
.btn-secondary {
  background: transparent;
  color: #2563EB;
  border: 2px solid #2563EB;
  padding: 12px 28px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 250ms ease;
  cursor: pointer;
}

.btn-secondary:hover {
  background: #2563EB;
  color: white;
}

/* Tertiary — Text Only */
.btn-tertiary {
  background: none;
  border: none;
  color: #2563EB;
  font-weight: 600;
  cursor: pointer;
  transition: color 200ms ease;
}
```

### Cards

```css
.card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-md);
  transition: all 250ms ease;
  cursor: pointer;
}

.card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-3px);
}
```

### Inputs

```css
.input {
  padding: 12px 16px;
  background: #F8FAFC;
  border: 1.5px solid rgba(226,232,240,0.5);
  border-radius: 8px;
  font-size: 16px;
  font-family: 'Plus Jakarta Sans', sans-serif;
  transition: all 200ms ease;
}

.input:focus {
  border-color: #2563EB;
  outline: none;
  box-shadow: 0 0 0 4px rgba(37,99,235,0.1);
}
```

### Modals

```css
.modal-overlay {
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(8px);
}

.modal {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: var(--shadow-xl);
  max-width: 500px;
  width: 90%;
}
```

### Progress Orb (Signature Component)

```css
/* TOEIC Score tracking — conic-gradient orb */
.progress-orb {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: conic-gradient(
    #2563EB var(--progress), 
    rgba(37,99,235,0.1) var(--progress)
  );
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.progress-orb::before {
  content: '';
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: white;
  position: absolute;
}
```

---

## Gamification Design Tokens

| Element | Color | Icon Style |
|---------|-------|------------|
| XP Points | `#A78BFA` (Violet) | Lucide `zap` |
| Streak Fire | `#FB923C` (Orange) | Lucide `flame` |
| Achievement | `#F59E0B` (Amber) | Lucide `trophy` |
| Level Up | `#22C55E` (Green) | Lucide `arrow-up-circle` |
| Correct Answer | `#22C55E` (Green) + pulse anim | Lucide `check-circle` |
| Wrong Answer | `#EF4444` (Red) + shake anim | Lucide `x-circle` |
| Time Warning | `#F59E0B` (Amber) + blink | Lucide `clock` |

---

## Style Guidelines

**Style:** Motion-Driven Glassmorphism

**Keywords:** Frosted glass, ambient light, editorial typography, micro-interactions, tonal depth, organized depth

**Key Effects:**
- Scroll animations (Intersection Observer, fade-in + slide-up)
- Hover transitions (250ms ease)
- Number count-up animations for scores/stats
- Progress bar fill animations
- Glassmorphism for floating elements
- `prefers-reduced-motion` respected

---

## Anti-Patterns (Do NOT Use)

- ❌ **Emojis as icons** — Use SVG icons (Lucide, Heroicons)
- ❌ **Missing cursor:pointer** — All clickable elements must have cursor:pointer
- ❌ **Layout-shifting hovers** — Avoid scale transforms that push content
- ❌ **Low contrast text** — Maintain 4.5:1 minimum contrast ratio
- ❌ **Instant state changes** — Always use transitions (150-300ms)
- ❌ **Invisible focus states** — Focus states must be visible for a11y
- ❌ **Pure black (#000000)** — Use `--color-text` (#1E293B) instead
- ❌ **1px solid borders for sections** — Use tonal shifts or spacing
- ❌ **Random colors per page** — ALL pages must use this palette

---

## Pre-Delivery Checklist

Before delivering any UI code, verify:

- [ ] All colors match this design system (no random hex values)
- [ ] No emojis used as icons (use Lucide SVG instead)
- [ ] All icons from consistent icon set (Lucide)
- [ ] `cursor-pointer` on all clickable elements
- [ ] Hover states with smooth transitions (150-300ms)
- [ ] Light mode: text contrast 4.5:1 minimum
- [ ] Focus states visible for keyboard navigation
- [ ] `prefers-reduced-motion` respected
- [ ] Responsive: 375px, 768px, 1024px, 1440px
- [ ] No content hidden behind fixed navbars
- [ ] No horizontal scroll on mobile
- [ ] Font loaded: Plus Jakarta Sans from Google Fonts
- [ ] Consistent border-radius using design tokens
