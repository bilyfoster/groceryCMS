#!/usr/bin/env python3
"""One-time migration of Brazen Therapy content into the CMS.

Reads the collected content in docs/content/ and creates:
- Pages (Home, About, Services, Team/Staff, Contact, Career, Blog)
- Blog posts
- Staff members (inserted directly into the DB since no admin API exists yet)
- Reading settings (front page, posts page)

Run from the project root on the server (needs access to the Docker DB):
    python3 scripts/migrate_brazen_content.py
"""

import json
import os
import re
import subprocess
import sys
from pathlib import Path
from typing import Any

import requests

PROJECT_ROOT = Path(__file__).resolve().parent.parent
ENV_PATH = PROJECT_ROOT / ".env"
CONTENT_DIR = PROJECT_ROOT / "docs" / "content"

BASE_URL = os.environ.get("APP_BASE_URL", "https://brazen.1lpro.com")
API_URL = f"{BASE_URL}/api"
ADMIN_EMAIL = "admin@demo.local"
ADMIN_PASSWORD = "password"


def load_env() -> dict[str, str]:
    """Parse the project's .env file into a dict."""
    env: dict[str, str] = {}
    if ENV_PATH.exists():
        with open(ENV_PATH) as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                if "=" in line:
                    key, value = line.split("=", 1)
                    env[key.strip()] = value.strip()
    return env


def login(session: requests.Session) -> None:
    resp = session.post(
        f"{API_URL}/auth/login",
        headers={"X-Tenant-Slug": "demo", "Content-Type": "application/json"},
        json={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD},
    )
    resp.raise_for_status()
    data = resp.json()
    if not data.get("success"):
        raise RuntimeError(f"Login failed: {data}")


def api_post(session: requests.Session, path: str, json_data: dict[str, Any]) -> dict[str, Any]:
    resp = session.post(
        f"{API_URL}{path}",
        headers={"X-Tenant-Slug": "demo", "Content-Type": "application/json"},
        json=json_data,
    )
    try:
        resp.raise_for_status()
    except requests.HTTPError:
        print(f"POST {path} failed: {resp.status_code} {resp.text[:500]}")
        raise
    data = resp.json()
    if not data.get("success"):
        raise RuntimeError(f"API error: {data}")
    return data["data"]


def api_put(session: requests.Session, path: str, json_data: dict[str, Any]) -> dict[str, Any]:
    resp = session.put(
        f"{API_URL}{path}",
        headers={"X-Tenant-Slug": "demo", "Content-Type": "application/json"},
        json=json_data,
    )
    try:
        resp.raise_for_status()
    except requests.HTTPError:
        print(f"PUT {path} failed: {resp.status_code} {resp.text[:500]}")
        raise
    data = resp.json()
    if not data.get("success"):
        raise RuntimeError(f"API error: {data}")
    return data["data"]


def get_pages(session: requests.Session) -> list[dict[str, Any]]:
    resp = session.get(
        f"{API_URL}/admin/pages",
        headers={"X-Tenant-Slug": "demo"},
    )
    resp.raise_for_status()
    data = resp.json()
    return data["data"]


def find_page(pages: list[dict[str, Any]], slug: str) -> dict[str, Any] | None:
    return next((p for p in pages if p["slug"] == slug), None)


def create_page(
    session: requests.Session,
    slug: str,
    title: str,
    page_type: str,
    layout: str,
    nav_order: int | None,
    meta_title: str | None = None,
    meta_description: str | None = None,
) -> dict[str, Any]:
    return api_post(
        session,
        "/admin/pages",
        {
            "slug": slug,
            "title": title,
            "pageType": page_type,
            "layout": layout,
            "navOrder": nav_order,
            "metaTitle": meta_title,
            "metaDescription": meta_description,
            "config": {},
        },
    )


def add_block(
    session: requests.Session,
    page_id: str,
    block_type: str,
    content: dict[str, Any],
    sort_order: int,
) -> dict[str, Any]:
    return api_post(
        session,
        f"/admin/pages/{page_id}/blocks",
        {
            "blockType": block_type,
            "content": content,
            "sortOrder": sort_order,
            "published": True,
        },
    )


def publish_page(session: requests.Session, page_id: str) -> dict[str, Any]:
    resp = session.patch(
        f"{API_URL}/admin/pages/{page_id}/publish",
        headers={"X-Tenant-Slug": "demo", "Content-Type": "application/json"},
        json={"published": True},
    )
    try:
        resp.raise_for_status()
    except requests.HTTPError:
        print(f"PATCH /admin/pages/{page_id}/publish failed: {resp.status_code} {resp.text[:500]}")
        raise
    data = resp.json()
    if not data.get("success"):
        raise RuntimeError(f"API error: {data}")
    return data["data"]


def markdown_to_html(text: str) -> str:
    """Very small markdown-to-HTML converter for the content we have."""
    # Convert headers
    text = re.sub(r"^### (.+)$", r"<h3>\1</h3>", text, flags=re.MULTILINE)
    text = re.sub(r"^## (.+)$", r"<h2>\1</h2>", text, flags=re.MULTILINE)
    text = re.sub(r"^# (.+)$", r"<h1>\1</h1>", text, flags=re.MULTILINE)
    # Convert bold
    text = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", text)

    # Split into paragraphs and lists
    paragraphs = re.split(r"\n\n+", text.strip())
    result: list[str] = []
    for para in paragraphs:
        para = para.strip()
        if not para:
            continue
        lines = para.split("\n")
        if all(line.strip().startswith("- ") for line in lines if line.strip()):
            items = [f"<li>{line.strip()[2:]}</li>" for line in lines if line.strip()]
            result.append("<ul>" + "".join(items) + "</ul>")
        elif len(lines) == 1 and para.startswith("<h"):
            result.append(para)
        else:
            inner = "<br />\n".join(lines)
            result.append(f"<p>{inner}</p>")
    return "\n".join(result)


def clear_page_blocks(session: requests.Session, page_id: str) -> None:
    resp = session.get(
        f"{API_URL}/admin/pages/{page_id}",
        headers={"X-Tenant-Slug": "demo"},
    )
    resp.raise_for_status()
    detail = resp.json()["data"]
    for block in detail.get("blocks", []):
        bresp = session.delete(
            f"{API_URL}/admin/pages/{page_id}/blocks/{block['id']}",
            headers={"X-Tenant-Slug": "demo"},
        )
        bresp.raise_for_status()


def create_home_page(session: requests.Session, pages: list[dict[str, Any]]) -> dict[str, Any]:
    page = find_page(pages, "home")
    existing = page is not None
    if not page:
        page = create_page(
            session,
            slug="home",
            title="Brazen Therapy",
            page_type="HOME",
            layout="hero-centered",
            nav_order=0,
            meta_title="Brazen Therapy | Empowering You To Be Bold Without Shame",
            meta_description="Compassionate, affirming therapy for adults, children, couples, and families in Phoenix, AZ.",
        )
    else:
        print("Home page exists; replacing blocks and updating metadata.")
        clear_page_blocks(session, page["id"])
        api_put(
            session,
            f"/admin/pages/{page['id']}",
            {
                "title": "Brazen Therapy",
                "metaTitle": "Brazen Therapy | Empowering You To Be Bold Without Shame",
                "metaDescription": "Compassionate, affirming therapy for adults, children, couples, and families in Phoenix, AZ.",
            },
        )

    add_block(
        session,
        page["id"],
        "HERO",
        {
            "heading": "Empowering You To Be Bold Without Shame",
            "subheading": "We are dedicated to creating a safe space for our clients to feel secure enough to speak openly and show the more vulnerable parts of themselves.",
            "buttonText": "Book a free consult",
            "buttonUrl": "https://brazentherapy.clientsecure.me/",
            "overlay": True,
        },
        sort_order=0,
    )

    clients_html = markdown_to_html(
        "- Adults (18+)\n- Children & Adolescents\n- Couples & Families\n- LGBTQ+ & QTBIPOC Affirming"
    )
    focus_html = markdown_to_html(
        "- Anxiety & Depression\n- Trauma & PTSD\n- Grief & Loss\n- Identity & Relationship Concerns\n- Life Transitions"
    )
    approach_html = markdown_to_html(
        "- Individual & Group Therapy\n- Couples & Family Therapy\n- Supervision & Consultation\n- Evidence-Based Modalities (CBT, EMDR, Narrative, DBT)"
    )

    add_block(session, page["id"], "TEXT", {"heading": "Clients We Work With", "body": clients_html, "alignment": "left"}, 1)
    add_block(session, page["id"], "TEXT", {"heading": "Areas of Focus", "body": focus_html, "alignment": "left"}, 2)
    add_block(session, page["id"], "TEXT", {"heading": "Our Approach", "body": approach_html, "alignment": "left"}, 3)
    add_block(
        session,
        page["id"],
        "CTA",
        {
            "heading": "Ready to get started?",
            "body": "Schedule a free 15-minute consultation with one of our therapists.",
            "primaryButton": {"text": "Book Appointment", "url": "https://brazentherapy.clientsecure.me/"},
            "secondaryButton": {"text": "Meet Our Team", "url": "/our-team"},
        },
        4,
    )

    publish_page(session, page["id"])
    print("Created home page." if not existing else "Updated home page.")
    return page


def create_about_page(session: requests.Session, pages: list[dict[str, Any]]) -> dict[str, Any]:
    page = find_page(pages, "about")
    if page:
        print("About page already exists, skipping.")
        return page

    page = create_page(
        session,
        slug="about",
        title="About",
        page_type="CUSTOM",
        layout="contained",
        nav_order=10,
        meta_title="About Brazen Therapy",
        meta_description="Founded in 2021 by Clarke Scott, LPC. Affirming, evidence-based therapy in Phoenix, AZ.",
    )

    about_html = markdown_to_html(
        'Brazen\'s team of compassionate and skilled therapists are committed to showing up not just as mental health professionals, but as advocates and safe harbors for those navigating life\'s challenges. We use evidence-based practices within a framework that centers client autonomy, prioritizes emotional safety, and honors each person\'s lived experience.'
    )
    mission_html = markdown_to_html(
        '"We believe connection is strongest when we embrace difference. At Brazen, we meet you where you are, affirm who you are, and walk beside you as you create change, on your terms."'
    )
    focus_html = markdown_to_html(
        "- Anxiety\n- Depression\n- Trauma/PTSD\n- Life Transitions\n- QTPOC/LGBTQ+ Affirming Care\n- Family of Origin Issues\n- Grief/Loss\n- Identity & Relationships (including non-monogamy, sex-positive/kink, body positivity, and internalized oppression)"
    )
    techniques_html = markdown_to_html(
        "- Solution Focused Based Therapy (SFBT)\n- Narrative Therapy\n- Eye Movement Desensitization and Reprocessing (EMDR)\n- Internal Family Systems (IFS)\n- Acceptance and Commitment Therapy (ACT)\n- Dialectical Behavioral Therapy (DBT)\n- Somatic/Body Oriented Approaches\n- Mindfulness Based Interventions"
    )

    add_block(session, page["id"], "TEXT", {"heading": "Our Mission", "body": mission_html, "alignment": "center"}, 0)
    add_block(session, page["id"], "TEXT", {"heading": "About Brazen Therapy", "body": about_html, "alignment": "left"}, 1)
    add_block(session, page["id"], "TEXT", {"heading": "Areas of Focus", "body": focus_html, "alignment": "left"}, 2)
    add_block(session, page["id"], "TEXT", {"heading": "Therapeutic Techniques", "body": techniques_html, "alignment": "left"}, 3)
    add_block(
        session,
        page["id"],
        "CTA",
        {
            "heading": "Book a free consult",
            "body": "We'd love to learn more about how we can support you.",
            "primaryButton": {"text": "Book Now", "url": "https://brazentherapy.clientsecure.me/"},
            "secondaryButton": {"text": "Meet our therapists", "url": "/our-team"},
        },
        4,
    )

    publish_page(session, page["id"])
    print("Created about page.")
    return page


def create_services_page(session: requests.Session, pages: list[dict[str, Any]]) -> dict[str, Any]:
    page = find_page(pages, "services")
    if page:
        print("Services page already exists, skipping.")
        return page

    page = create_page(
        session,
        slug="services",
        title="Services & Pricing",
        page_type="CUSTOM",
        layout="contained",
        nav_order=20,
        meta_title="Services & Pricing | Brazen Therapy",
        meta_description="Transparent pricing for individual, family, couples, and intern therapy in Phoenix, AZ.",
    )

    intro_html = markdown_to_html(
        "At Brazen Therapy, we strive to provide affordable and accessible therapy services to all of our clients. We believe that everyone should have access to quality therapy and we are committed to making that a reality. Our transparent pricing per service helps our clients to plan and budget for their sessions with ease."
    )
    note_html = markdown_to_html("**Note:** Prices shown are for out-of-pocket payment (no insurance).")
    services_html = markdown_to_html(
        "### Individual Therapy\n- Intake: $235\n- Session: $180\n\n"
        "### Family Therapy\n- Intake: $235\n- Session: $200\n\n"
        "### Couples Therapy\n- Intake: $235\n- Session: $200\n\n"
        "### Intern Therapy (supervised)\n- Intake: $75\n- Individual Session: $30\n- Couple/Family Session: $60"
    )
    insurance_html = markdown_to_html(
        "### Accepted Insurances (In-Network)\n- Blue Cross Blue Shield\n- Aetna\n- Cigna\n- United Healthcare / UMR / Optum\n- Tricare / Triwest\n\n"
        "**Out-of-Network:** We provide a superbill for reimbursement.\n\n"
        "### Payment Methods\n- Credit cards via Stripe (Visa, Mastercard, Discover, Amex)\n- HSA/FSA cards accepted"
    )

    add_block(session, page["id"], "TEXT", {"heading": "Services & Pricing", "body": intro_html, "alignment": "left"}, 0)
    add_block(session, page["id"], "TEXT", {"body": note_html, "alignment": "left"}, 1)
    add_block(session, page["id"], "TEXT", {"heading": "Pricing", "body": services_html, "alignment": "left"}, 2)
    add_block(session, page["id"], "TEXT", {"heading": "Insurance & Payment", "body": insurance_html, "alignment": "left"}, 3)
    add_block(
        session,
        page["id"],
        "BUTTON",
        {"text": "Book Now", "url": "https://brazentherapy.clientsecure.me/", "style": "primary"},
        4,
    )

    publish_page(session, page["id"])
    print("Created services page.")
    return page


def create_team_page(session: requests.Session, pages: list[dict[str, Any]]) -> dict[str, Any]:
    page = find_page(pages, "our-team")
    if page:
        print("Team page already exists, skipping.")
        return page

    page = create_page(
        session,
        slug="our-team",
        title="Our Team",
        page_type="STAFF",
        layout="grid",
        nav_order=30,
        meta_title="Our Team | Brazen Therapy",
        meta_description="Meet our diverse team of licensed therapists, interns, and clinical supervisors in Phoenix, AZ.",
    )

    intro_html = markdown_to_html(
        "We offer free 15-minute consultations with all therapists. Email us at contact@brazentherapy.org with any questions."
    )
    add_block(session, page["id"], "TEXT", {"heading": "Our Team", "body": intro_html, "alignment": "center"}, 0)

    publish_page(session, page["id"])
    print("Created team page.")
    return page


def create_contact_page(session: requests.Session, pages: list[dict[str, Any]]) -> dict[str, Any]:
    page = find_page(pages, "contact")
    if page:
        print("Contact page already exists, skipping.")
        return page

    page = create_page(
        session,
        slug="contact",
        title="Contact",
        page_type="CONTACT",
        layout="centered",
        nav_order=40,
        meta_title="Contact Brazen Therapy",
        meta_description="Contact Brazen Therapy in Phoenix, AZ. 340 E. Palm Lane, Suite 255.",
    )

    contact_html = markdown_to_html(
        "**Address:**\n340 E. Palm Lane, Suite 255\nPhoenix, AZ 85004\n\n"
        "**Phone:** 602.918.3664\n\n"
        "**Fax:** 480.681.1916\n\n"
        "**Hours:**\nMon–Friday: 8am-6pm\n\n"
        "**Insurance Accepted:**\nBlue Cross Blue Shield | Aetna | Optum | UMR | United | Oscar Health | Cigna | Out-of-Network"
    )
    add_block(session, page["id"], "TEXT", {"heading": "Get in Touch", "body": contact_html, "alignment": "left"}, 0)

    publish_page(session, page["id"])
    print("Created contact page.")
    return page


def create_career_page(session: requests.Session, pages: list[dict[str, Any]]) -> dict[str, Any]:
    page = find_page(pages, "career")
    if page:
        print("Career page already exists, skipping.")
        return page

    page = create_page(
        session,
        slug="career",
        title="Join Our Team",
        page_type="CUSTOM",
        layout="contained",
        nav_order=None,
        meta_title="Careers | Brazen Therapy",
        meta_description="Join the Brazen Therapy team. We value diversity and authentic connection.",
    )

    body_html = markdown_to_html(
        "At Brazen Therapy, we believe in the transformative power of authentic connection and individualized counseling. If you're a dedicated and empathetic therapist looking to join a team that values diversity and embraces the journey of self-discovery, explore the opportunities at Brazen Therapy.\n\n"
        "### Openings\nWe currently have no openings but check back soon!\n\n"
        "**Email resume and cover letter to:** contact@brazentherapy.org"
    )
    add_block(session, page["id"], "TEXT", {"heading": "Join Our Team", "body": body_html, "alignment": "left"}, 0)

    publish_page(session, page["id"])
    print("Created career page.")
    return page


def create_blog_page(session: requests.Session, pages: list[dict[str, Any]]) -> dict[str, Any]:
    page = find_page(pages, "blog")
    if page:
        print("Blog page already exists, skipping.")
        return page

    page = create_page(
        session,
        slug="blog",
        title="Blog",
        page_type="BLOG",
        layout="grid",
        nav_order=50,
        meta_title="Blog | Brazen Therapy",
        meta_description="Insights and resources from the Brazen Therapy team.",
    )

    publish_page(session, page["id"])
    print("Created blog page.")
    return page


BLOG_POSTS = [
    {
        "slug": "navigating-insurance-for-mental-health-care",
        "title": "Navigating Insurance for Mental Health Care: A Comprehensive Guide",
        "excerpt": "A guide to understanding premiums, deductibles, copays, coinsurance, and how to use your mental health benefits.",
        "author": "Clarke Scott, LPC",
        "date": "2026-05-15T00:00:00Z",
        "body": """<p>Understanding insurance can feel overwhelming. This guide breaks down the key terms and steps you need to take to use your mental health benefits.</p>

<h2>Key Terms & Definitions</h2>
<ul>
<li><strong>Premium</strong> — The monthly amount paid to keep insurance active, regardless of usage.</li>
<li><strong>Deductible</strong> — The out-of-pocket amount paid before insurance shares costs.</li>
<li><strong>Copay</strong> — A flat fee per session paid after meeting the deductible.</li>
<li><strong>Coinsurance</strong> — A percentage of session cost shared with insurance after the deductible is met.</li>
<li><strong>Out-of-Pocket Max</strong> — The maximum paid in a plan year; insurance covers 100% after this limit.</li>
<li><strong>In-Network</strong> — Therapists with a contract to the insurer.</li>
<li><strong>Out-of-Network</strong> — Therapists without a contract; partial reimbursement may apply.</li>
<li><strong>Superbill</strong> — An itemized receipt for out-of-network reimbursement.</li>
<li><strong>Authorization</strong> — Pre-approval required by some plans before therapy begins.</li>
</ul>

<h2>Step-by-Step Insurance Process</h2>
<ol>
<li>Check your benefits by calling member services or logging into your insurer's portal.</li>
<li>Find an in-network therapist using the insurer's provider directory.</li>
<li>Confirm coverage with the therapist's office before starting.</li>
<li>Attend your session.</li>
<li>Pay and track your Explanation of Benefits (EOB).</li>
</ol>

<p><strong>Heads Up:</strong> Insurance directories are often outdated. Always call a therapist directly to confirm they're accepting new clients and are still in-network.</p>

<h2>Brazen Therapy Insurance Coverage</h2>
<p>Brazen Therapy is in-network with Aetna, Blue Cross Blue Shield, Cigna/Evernorth, United/UMR, Tricare, Triwest, ChampVA, and Optum. We check your benefits for you before the first session.</p>

<h2>Financial Alternatives</h2>
<ul>
<li><strong>Out-of-Network Reimbursement:</strong> Submit a superbill to your insurer.</li>
<li><strong>HSA / FSA Accounts:</strong> Therapy is a qualified medical expense.</li>
<li><strong>Sliding Scale & Intern Rates:</strong> Reduced-cost care pathways offered by Brazen Therapy.</li>
</ul>

<p>Figuring out insurance shouldn't stand between you and the support you deserve. Our team is happy to verify your benefits and answer questions before your first session.</p>""",
    },
    {
        "slug": "what-is-autistic-burnout",
        "title": "What is Autistic Burnout? A Guide to Recovery and Prevention",
        "excerpt": "Autistic burnout is a distinct physiological event. Learn the symptoms, causes, and how to recover.",
        "author": "Erica Marisella Harris, LPC",
        "date": "2026-04-08T00:00:00Z",
        "body": """<p>Autistic burnout is a distinct physiological event, differing significantly from neurotypical "professional burnout." It is often described as a "total system shutdown" rather than simple tiredness.</p>

<blockquote>"Burnout is not a character flaw. It is a physiological event, no different than catching a cold."</blockquote>

<figure><img src="/images/support-group.jpg" alt="A calm, supportive group setting" /><figcaption>Rest and connection are core to recovering from autistic burnout.</figcaption></figure>

<h2>Symptoms & Signs</h2>
<ul>
<li><strong>Chronic Exhaustion:</strong> Deep, systemic fatigue requiring rest beyond typical relaxation.</li>
<li><strong>Loss of Skills:</strong> Impacts cognition, executive functioning, memory, and speech.</li>
<li><strong>Increased Sensory Sensitivity:</strong> Sounds, textures, or lights feel overwhelming.</li>
<li><strong>Increased Stimming:</strong> Heightened need for vocalizing, hand movements, or muscle tensing.</li>
</ul>

<h2>Primary Causes</h2>
<ul>
<li><strong>Masking:</strong> Repressing natural behaviors to "pass" as non-autistic.</li>
<li><strong>Major Life Transitions:</strong> New jobs, relocation, school changes.</li>
<li><strong>Cumulative Stress:</strong> Multiple small changes happening simultaneously.</li>
<li><strong>External Expectations:</strong> Pressure to succeed by neurotypical standards.</li>
</ul>

<h2>Path to Recovery & Prevention</h2>
<h3>1. Protect Your Energy</h3>
<p>Use a traffic light analogy:</p>
<ul>
<li>🔴 Red Days: Basic survival and rest.</li>
<li>🟡 Yellow Days: Go slow and take care of basics.</li>
<li>🟢 Green Days: Tackle bigger optional tasks when energy permits.</li>
</ul>

<h3>2. Validation & Radical Self-Talk</h3>
<p>Find people who understand, acknowledge small accomplishments, and separate identity from output.</p>

<h3>3. Build a Daily Sensory Diet</h3>
<p>Maintain preventative maintenance for your nervous system with weighted blankets, fidgets, heavy work, and other regulating tools.</p>""",
    },
    {
        "slug": "therapy-isnt-just-for-crisis",
        "title": "Therapy Isn't Just for Crisis: Why People Start Therapy Even When Life Feels Fine",
        "excerpt": "You don't need a diagnosis or a crisis to deserve support. Therapy can be preventative care.",
        "author": "Kira McSherry, Master of Counseling Intern",
        "date": "2026-03-23T00:00:00Z",
        "body": """<blockquote>"Therapy isn't a competition about who is struggling the most. You don't need a diagnosis or a crisis to deserve support."</blockquote>

<h2>The Myth of Crisis-Only Therapy</h2>
<p>Many assume therapy is reserved for major life crises. Common barriers include:</p>
<ul>
<li>"I don't think my problems are big enough."</li>
<li>"Other people have it worse."</li>
<li>"Nothing bad happened, so I don't know if I need therapy."</li>
</ul>

<h2>Therapy as Preventative Care</h2>
<p>Starting therapy early helps you recognize stress before it becomes burnout, notice recurring patterns, learn coping mechanisms, improve communication, and build resilience.</p>

<h2>Understanding Patterns vs. Self-Judgment</h2>
<p>Therapy shifts the internal narrative from self-blame to curiosity:</p>
<ul>
<li><strong>Old Focus:</strong> "Why do I overthink?"</li>
<li><strong>New Focus:</strong> "Where did this pattern come from, and is it still helping me?"</li>
</ul>

<h2>Therapy for Life Transitions</h2>
<p>Transitions are common reasons to start therapy, even when they feel positive: starting or ending relationships, becoming a parent, career shifts, moving, or receiving a major diagnosis.</p>

<h2>Actionable Indicators</h2>
<p>Therapy may be helpful if you feel stuck, notice repeating patterns, feel more anxious lately, feel disconnected, want better boundaries, or want support navigating change.</p>

<p>If you are wondering if starting therapy makes sense even when life feels mostly okay, that curiosity alone is a meaningful place to begin.</p>""",
    },
]


def create_blog_posts(session: requests.Session, blog_page_id: str) -> None:
    resp = session.get(
        f"{API_URL}/admin/blog",
        headers={"X-Tenant-Slug": "demo"},
    )
    resp.raise_for_status()
    existing = {p["slug"]: p for p in resp.json()["data"]}

    for post in BLOG_POSTS:
        if post["slug"] in existing:
            print(f"Blog post '{post['slug']}' already exists, skipping.")
            continue
        featured_images = {
            "navigating-insurance-for-mental-health-care": "/images/consultation.jpg",
            "what-is-autistic-burnout": "/images/community.jpg",
            "therapy-isnt-just-for-crisis": "/images/support-group.jpg",
        }
        payload = {
            "pageId": blog_page_id,
            "slug": post["slug"],
            "title": post["title"],
            "excerpt": post["excerpt"],
            "body": post["body"],
            "featuredImage": featured_images.get(post["slug"]),
            "tags": [],
            "categoryIds": [],
            "sticky": False,
            "allowComments": False,
            "publishedAt": post["date"],
            "metaTitle": post["title"],
            "metaDescription": post["excerpt"],
        }
        created = api_post(session, "/admin/blog", payload)
        resp = session.patch(
            f"{API_URL}/admin/blog/{created['id']}/publish",
            headers={"X-Tenant-Slug": "demo", "Content-Type": "application/json"},
            json={"published": True},
        )
        try:
            resp.raise_for_status()
        except requests.HTTPError:
            print(f"PATCH /admin/blog/{created['id']}/publish failed: {resp.status_code} {resp.text[:500]}")
            raise
        data = resp.json()
        if not data.get("success"):
            raise RuntimeError(f"API error: {data}")
        print(f"Created blog post: {post['title']}")


STAFF = [
    # Licensed Therapists
    {"name": "Alexis Welsh, LCSW", "title": "Licensed Clinical Social Worker", "bio": "Specialties: Anxiety, depression, grief, trauma. Approach: Trauma-informed, strength-based, CBT, DBT, EMDR, IFS, mindfulness.", "sort_order": 1},
    {"name": "Raven Taylor-Aduwak, LAC", "title": "Licensed Associate Counselor", "bio": "Specialties: Teens (15+), adults, depression, anxiety, self-esteem, life transitions. Approach: CBT, DBT, ACT, motivational interviewing. Gender affirming care letters (18+).", "sort_order": 2},
    {"name": "Ana Franco, LAC", "title": "Licensed Associate Counselor", "bio": "Specialties: Adults (18+), anxiety, depression, trauma, grief, relationship conflicts. Approach: Person-centered, CBT, DBT, mindfulness-based, trauma-informed. Spanish-speaking sessions available.", "sort_order": 3},
    {"name": "Aundrea Austin, LMSW", "title": "Licensed Master Social Worker", "bio": "Specialties: Children (8+), teens, adults, families. Approach: CBT, SFBT, mindfulness, Expressive Arts Therapy (EAT). Chicago native with AmeriCorps and Boys & Girls Clubs experience.", "sort_order": 4},
    {"name": "Colleen Casson, LPC", "title": "Licensed Professional Counselor", "bio": "Specialties: Complex/developmental/relational/attachment trauma, PTSD, anxiety, aging. Approach: Relational, person-centered, consent-based, NARM certified, EMDR trained. LGBTQ+ affirming care, gender affirming letters (18+).", "sort_order": 5},
    {"name": "Elise Pinkowski, LMSW", "title": "Licensed Master Social Worker", "bio": "Specialties: Adults (20+), anxiety, depression, family dysfunction, insecure attachment. Approach: Person-centered, CBT, somatic, mindfulness-based.", "sort_order": 6},
    {"name": "Makya Kirchner, LMSW", "title": "Licensed Master Social Worker", "bio": "Specialties: Children (10+), teens, adults, trauma, foster care, homelessness, justice system. Approach: Client-centered, strength-based, CBT, DBT, EMDR, expressive arts.", "sort_order": 7},
    {"name": "Riana Burnett, LCSW", "title": "Licensed Clinical Social Worker", "bio": "Specialties: Teens (16+), adults, emotional regulation, childhood trauma. Approach: DBT grounding skills, present-focused therapy.", "sort_order": 8},
    {"name": "Alex Righi, LMSW", "title": "Licensed Master Social Worker", "bio": "Specialties: Children (8+), teens, adults (18+), elders (65+). Approach: Mindfulness-based, CBT, DBT, EMDR. Men's issues specialist, emotion regulation for teens.", "sort_order": 9},
    {"name": "Renz Narciso, LMSW", "title": "Licensed Master Social Worker", "bio": "Specialties: Children (7+), teens, adults, anxiety, depression, emotional regulation. Approach: CBT, expressive arts, play therapy. Filipina first-gen immigrant, Tagalog-speaking.", "sort_order": 10},
    {"name": "Cailin Payson, LAMFT", "title": "Licensed Associate Marriage and Family Therapist", "bio": "Specialties: Couples, teens (14+), adults (18+), elderly (65+). Approach: Relational work, EFT, EMDR, parts work. Gender affirming care letters (18+).", "sort_order": 11},
    {"name": "Erica Harris, LPC", "title": "Licensed Professional Counselor", "bio": "Specialties: Children, teens, adults (18+), complex trauma, PTSD, neurodivergence (Autism/ADHD). Approach: TF-CBT, DBT, CBT, expressive (gameplay, sand tray, storytelling). Neurodivergent therapist, LGBTQ+ ally.", "sort_order": 12},
    {"name": "Vonyee Soulfire, LAC", "title": "Licensed Associate Counselor", "bio": "Specialties: Adults, families, trauma, anxiety, emotional disconnection. Approach: Jungian depth psychology, DBT, somatic therapy. Liberian-born African-American, holistic/yoga practitioner.", "sort_order": 13},
    {"name": "Teena Miller, LAMFT", "title": "Licensed Associate Marriage and Family Therapist", "bio": "Specialties: Adults, couples/families, infidelity, intimacy, communication. Approach: Queer and non-monogamous affirming.", "sort_order": 14},
    {"name": "Chelsea Honea, LPC, LIAC", "title": "Licensed Professional Counselor", "bio": "Specialties: Adults, relationship concerns, depression, anxiety, substance use. Approach: Systemic, multicultural, strengths-based, CBT, parts work, attachment/systems.", "sort_order": 15},
    {"name": "Shae Moreau, LAC", "title": "Licensed Associate Counselor", "bio": "Specialties: Teens (14+), adults (18+), mood shifts, intense emotions. Approach: EMDR, DBT, parts work (IFS), neurodivergent-affirming. Gender affirming care letters (14+).", "sort_order": 16},
    {"name": "Carissa Fenceroy, MSW", "title": "Master of Social Work", "bio": "Specialties: Children, families, all ages (intern therapist). Approach: Client-centered, strengths-based, solution-focused, systems lens. 8 years social work experience.", "sort_order": 17},
    {"name": "Sybil Nwulu, LAC", "title": "Licensed Associate Counselor", "bio": "Specialties: Adults (18+), anxiety, depression, relationship conflicts, cultural stress. Approach: Person-centered, multicultural, Intersectional Feminism, Liberation Psychology, DBT. Born in Lagos, Nigeria.", "sort_order": 18},
    {"name": "Jeanine Whitehead, LCSW", "title": "Licensed Clinical Social Worker", "bio": "Specialties: Adults (18+), multicultural/biracial identity, transracial adoption. Approach: Empathetic, supportive, silly; trauma-informed.", "sort_order": 19},
    # Intern Therapists
    {"name": "Maggie Reichler", "title": "Counseling Intern", "bio": "Specialties: Adults (18+), adolescents (14-17). Approach: Holistic, mindfulness-based, ACT, CBT, trauma-informed, strengths-based.", "sort_order": 20},
    {"name": "Sukhmani Khalsa", "title": "Social Work Intern", "bio": "Specialties: Adults, teens (supervised). Approach: DBT-influenced, trauma-informed, strengths-based.", "sort_order": 21},
    {"name": "Kira McSherry", "title": "Counseling Intern", "bio": "Specialties: Children, teens, adolescents, adults, couples (supervised). Approach: Trauma-informed, relational, attachment-based. Adoptee with lived experience.", "sort_order": 22},
    {"name": "Robin Burnam", "title": "Social Work Intern", "bio": "Specialties: Adolescents, adults (supervised). Approach: Compassionate, client-centered, strengths-based, solution-focused, mindfulness. 20+ years campus/young adult experience.", "sort_order": 23},
    {"name": "Mimi (Xiaojun) Jiang", "title": "Counseling Intern", "bio": "Specialties: Individuals, teens (14+), adults (18+). Approach: Integrative, trauma-informed, strengths-based, person-centered, CBT, ACT. English/Mandarin bilingual.", "sort_order": 24},
    # Staff & Leadership
    {"name": "Clarke Scott, LPC", "title": "Founder & Clinical Director", "bio": "Specializes in LGBTQ+, anxiety/depression, neurodivergent, family-of-origin, polyamorous/consensually non-monogamous. Kink-affirming, sex-positive, systemic lens. Licensed in Arizona, Colorado, Minnesota, Missouri. Board-Approved Clinical Supervisor (Arizona).", "sort_order": 25},
    {"name": "Amber Block-Zambrano, LCSW", "title": "Therapist & Clinical Supervisor", "bio": "Specializes in Black, brown, queer folks (all ages birth to end of life). Approach: EMDR, MI, parts work/inner child, DBT, SFT, mindfulness. 10+ years social work experience.", "sort_order": 26},
    {"name": "Shuheng Hu, LPC", "title": "Therapist & Clinical Supervisor", "bio": "Specializes in complex trauma, depression, anxiety, work stress, life transitions, identity development. Approach: Eclectic (trauma-informed, DBT, CBT, psychodynamic, ACT, person-centered). English/Mandarin bilingual.", "sort_order": 27},
    {"name": "Angelinah Honea", "title": "Practice Manager", "bio": "Music lover (Classical/Jazz, trumpet player).", "sort_order": 28},
    {"name": "Stella Behnke", "title": "Administrative Assistant", "bio": "Gaming (Infinity Nikki, Sims 4), Stray Kids fan.", "sort_order": 29},
]


def migrate_staff(team_page_id: str, env: dict[str, str]) -> None:
    """Insert staff members directly into the DB since no admin API exists."""
    db = env["POSTGRES_DB"]
    user = env["POSTGRES_USER"]
    password = env["POSTGRES_PASSWORD"]

    # Get tenant id
    tenant_sql = "SELECT id FROM tenants WHERE slug = 'demo';"
    result = subprocess.run(
        [
            "docker", "exec", "-i", "brazen-cms-db-1",
            "psql", "-U", user, "-d", db, "-t", "-c", tenant_sql,
        ],
        capture_output=True,
        text=True,
        check=True,
    )
    tenant_id = result.stdout.strip()
    if not tenant_id:
        raise RuntimeError("Could not find demo tenant")

    # Check existing staff
    result = subprocess.run(
        [
            "docker", "exec", "-i", "brazen-cms-db-1",
            "psql", "-U", user, "-d", db, "-t", "-c",
            f"SELECT COUNT(*) FROM staff_members WHERE tenant_id = '{tenant_id}' AND page_id = '{team_page_id}';",
        ],
        capture_output=True,
        text=True,
        check=True,
    )
    existing_count = int(result.stdout.strip() or 0)
    if existing_count > 0:
        print(f"Team page already has {existing_count} staff members, skipping staff migration.")
        return

    def pg_escape(value: str) -> str:
        return value.replace("'", "''")

    for member in STAFF:
        sql = (
            "INSERT INTO staff_members (tenant_id, page_id, name, title, bio, photo_url, email, sort_order, published, social_links) "
            f"VALUES ('{tenant_id}', '{team_page_id}', "
            f"'{pg_escape(member['name'])}', "
            f"'{pg_escape(member['title'])}', "
            f"'{pg_escape(member['bio'])}', "
            f"NULL, NULL, {member['sort_order']}, true, '{{}}');"
        )
        subprocess.run(
            [
                "docker", "exec", "-i", "brazen-cms-db-1",
                "psql", "-U", user, "-d", db, "-c", sql,
            ],
            text=True,
            check=True,
        )
    print(f"Inserted {len(STAFF)} staff members.")


def set_reading_settings(session: requests.Session, home_page_id: str, blog_page_id: str) -> None:
    api_put(session, "/admin/pages/reading", {"frontPageId": home_page_id, "postsPageId": blog_page_id})
    print("Set reading settings (front page and posts page).")


def main() -> int:
    env = load_env()
    session = requests.Session()
    print(f"Logging in to {API_URL} ...")
    login(session)

    print("Fetching existing pages...")
    pages = get_pages(session)

    home = create_home_page(session, pages)
    about = create_about_page(session, pages)
    services = create_services_page(session, pages)
    team = create_team_page(session, pages)
    contact = create_contact_page(session, pages)
    career = create_career_page(session, pages)
    blog = create_blog_page(session, pages)

    print("Creating blog posts...")
    create_blog_posts(session, blog["id"])

    print("Migrating staff...")
    migrate_staff(team["id"], env)

    print("Setting reading settings...")
    set_reading_settings(session, home["id"], blog["id"])

    print("Migration complete.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
