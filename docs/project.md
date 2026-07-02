# Product Requirements Document (PRD)

## Project Name

Pickles Bodega Gluten-Free & Allergy-Friendly Grocery CMS

## Version

1.0

## Overview

Pickles Bodega requires a modern website and content management platform for its gluten-free and allergy-friendly grocery store in Flagstaff, Arizona.

The new platform will serve two primary functions:

1. Public-facing marketing website
2. Product catalog with allergy/diet filtering and an allergy-awareness intake tool

The platform will not provide medical diagnosis or store Protected Health Information (PHI). The intake tool is informational only and encourages shoppers to discuss symptoms with a healthcare provider.

The goal is to improve the shopping experience by helping customers find products that fit their dietary restrictions and possible allergy concerns, while providing Pickles Bodega staff with a centralized content and product management system.

---

# Business Goals

## Primary Goals

- Modernize website design and user experience
- Improve therapist discovery
- Increase therapist-client match quality
- Reduce manual therapist routing
- Allow Brazen staff to manage website content without developer involvement
- Improve SEO performance and content discoverability

## Non-Goals

The following are explicitly out of scope:

- Electronic Health Records (EHR)
- Electronic Medical Records (EMR)
- Client portals
- Clinical documentation
- Session notes
- Appointment scheduling
- Insurance claims processing
- Billing systems
- Storage of PHI

---

# User Types

## Public Visitor

Prospective client seeking information or therapist recommendations.

Permissions:

- Browse website
- Search therapists
- Complete matching questionnaire
- Submit contact/intake requests
- Access scheduling links

---

## Content Administrator

Brazen staff member responsible for website content.

Permissions:

- Create and edit pages
- Manage services
- Manage resources/blog content
- Manage FAQs
- Manage therapist profiles
- Publish and unpublish content

---

## Practice Administrator

Brazen management staff.

Permissions:

- All Content Administrator permissions
- Manage therapist availability status
- Manage therapist specialties
- View intake submissions
- View matching recommendations
- Configure matching rules

---

## Therapist

Individual provider.

Permissions:

- Edit personal profile
- Update biography
- Update profile photo
- Update specialties (optional approval workflow)
- Update accepting-new-clients status
- Manage scheduling link destination

---

# Technology Stack

## Frontend

React

Responsibilities:

- Public website
- Therapist directory
- Therapist matching workflow
- Responsive mobile experience

---

## Backend

Java

Responsibilities:

- CMS APIs
- Authentication
- Authorization
- Therapist matching engine
- Content delivery

---

## Database

Relational Database

Examples:

- PostgreSQL
- MySQL

---

# Public Website Requirements

## Home Page

Must support:

- Hero section
- Introductory content
- Featured services
- Featured therapists
- Call-to-action buttons
- Contact information

All content editable via CMS.

---

## About Pages

Editable content pages including:

- Company overview
- Mission
- Values
- Team information

---

## Services Pages

Dynamic service records.

Examples:

- Individual Therapy
- Couples Therapy
- Family Therapy
- Trauma Therapy

Each service must support:

- Title
- Slug
- Description
- Featured image
- Related therapists

---

## Resources / Blog

Must support:

- Categories
- Tags
- Featured images
- SEO metadata
- Publish scheduling

---

## FAQ Management

Administrators can:

- Create FAQs
- Edit FAQs
- Reorder FAQs
- Categorize FAQs

---

# Therapist Management

Therapists are managed as structured CMS records.

## Therapist Fields

### Identity

- First Name
- Last Name
- Credentials
- Pronouns
- Profile Photo

### Professional Information

- Professional Bio
- Years of Experience
- Education
- Licensure Information

### Client Focus Areas

Multi-select fields including:

- Anxiety
- Depression
- Trauma
- PTSD
- LGBTQ+
- Gender Identity
- Neurodivergence
- Relationship Issues
- Grief
- Addiction

Administrative users must be able to add new focus areas without developer involvement.

---

### Therapy Modalities

Examples:

- CBT
- DBT
- EMDR
- ACT
- Somatic Therapy
- Narrative Therapy

Administrative users can manage modality lists.

---

### Demographics Served

- Children
- Adolescents
- Adults
- Seniors
- Couples
- Families

---

### Service Delivery

- Virtual
- In-Person
- Hybrid

---

### Status

- Accepting New Clients
- Limited Availability
- Waitlist Only
- Not Accepting Clients

---

### Scheduling

- External Scheduling URL
- External Booking Platform Reference

No appointment data will be stored.

---

# Therapist Directory

## Directory Features

Users must be able to:

- Search therapists
- Filter therapists
- Sort therapists

### Filters

- Specialty
- Therapy modality
- Demographic served
- Session type
- Availability status

---

## Therapist Profile Page

Displays:

- Biography
- Credentials
- Specialties
- Modalities
- Demographics served
- Availability status
- Scheduling CTA

---

# Therapist Matching Tool

## Objective

Guide prospective clients toward the most appropriate therapist.

---

## Intake Questionnaire

The questionnaire should collect:

### Areas of Concern

Examples:

- Anxiety
- Depression
- Trauma
- Relationships
- LGBTQ+ Support
- Gender Identity
- Life Transitions

### Preferences

Optional preferences:

- Therapist gender
- Therapist identity
- Virtual or in-person

### Client Demographic

Examples:

- Adult
- Teen
- Couple
- Family

---

## Matching Engine

The system shall score therapists based on:

- Specialty alignment
- Modality alignment
- Demographic alignment
- Service delivery alignment
- Availability status

The system shall return a ranked list of recommended therapists.

---

## Match Results

Display:

- Top therapist recommendations
- Match explanation
- Profile links
- Scheduling links

---

# CMS Requirements

## Content Types

The CMS must support:

- Pages
- Therapists
- Services
- Resources/Blog Posts
- FAQs
- Focus Areas
- Therapy Modalities

---

## SEO Management

Each content type must support:

- SEO Title
- Meta Description
- Open Graph Image
- Canonical URL

---

## Media Library

Support:

- Image uploads
- Image replacement
- Asset organization

---

# Analytics

Track:

- Page views
- Therapist profile views
- Therapist search activity
- Matching tool usage
- Match-to-booking click-throughs

No clinical information shall be stored.

---

# Security

Requirements:

- Role-based access control
- Secure authentication
- Audit logging for administrative actions
- HTTPS-only communication

---

# Future Enhancements (Out of Scope for MVP)

- AI-assisted therapist recommendations
- Multi-location support
- Resource recommendation engine
- Marketing automation integration
- CRM integration
- Waitlist management
- Client portal integration

---

# Success Metrics

- Increase therapist profile engagement
- Increase matching tool completion rate
- Increase scheduling link click-through rate
- Reduce manual therapist assignment effort
- Improve organic search traffic
- Improve website conversion rates

End of Document