# Site imagery

All photos are downscaled for web (long edge ≤1600px). Content references them by exact filename;
swap a photo by replacing the file (no code change).

## Service / pricing photos (from brazentherapy.org)
| Filename | Used on |
| --- | --- |
| `service-individual.jpg` | Pricing card — Individual; Services page hero |
| `service-family.jpg` | Pricing card — Family |
| `service-couples.jpg` | Pricing card — Couples |
| `service-intern.jpg` | Pricing card — Intern (featured) |

## Lifestyle photos
| Filename | Used on |
| --- | --- |
| `consultation.jpg` | Home page hero background |
| `community.jpg` | Home page — community image band |
| `group-women.jpg` | available — suggested: Therapists directory header |
| `support-group.jpg` | available — suggested: a "groups / community" section (note: shows masks) |
| `seniors.jpg` | available — suggested: older-adults / in-home care accent |

## Brand
| Filename | Notes |
| --- | --- |
| `brazen-logo.png` | Brazen Therapy primary logo; available for header/footer (not yet wired) |

Notes:
- Referenced from Liquibase changeset `013-seed-services-pricing.sql` and rendered by
  `HeroBlock` / `ImageBlock` / `PricingBlock`.
- "Available" photos are resized and ready but not yet placed — wiring them onto the
  `/therapists` and `/match` pages needs small component edits (those routes aren't fully
  block-driven). Ask and they'll be added.
- Confirm licensing before production.
