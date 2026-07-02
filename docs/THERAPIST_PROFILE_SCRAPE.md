# Therapist Profile Scrape — data collection spec

**Goal:** collect each therapist's real profile details from the live site
(`brazentherapy.org/<name>`) so we can (1) replace the stub bios, (2) tag each therapist with
specialties/modalities/demographics (this is what powers the "find a therapist" matching quiz), and
(3) publish them. Photos are already handled — **do not** collect photos.

**How to use this doc:**
1. For each therapist in the roster (bottom), open their profile page.
2. Copy the **per-therapist template** and fill one block per person.
3. Tag using the **controlled vocabulary** below. If the site mentions something not in a list, put
   it under the matching `— other (verbatim)` line — don't force it. Don't invent anything; if a
   field isn't on the page, write `N/A`.
4. Paste the bio **verbatim** (exact wording).
5. Send the finished markdown back — that's all I need.

---

## Controlled vocabulary (tag using these EXACT terms)

- **Focus areas:** Anxiety · Depression · Trauma · PTSD · LGBTQ+ · Gender Identity ·
  Neurodivergence · Relationship Issues · Grief · Addiction · Life Transitions
- **Modalities:** CBT · DBT · EMDR · ACT · Somatic Therapy · Narrative Therapy
- **Demographics served:** Children · Adolescents · Adults · Seniors · Couples · Families
- **Session delivery:** `VIRTUAL` · `IN_PERSON` · `HYBRID` (pick one; if unclear, `N/A`)
- **Accepting new clients:** `ACCEPTING` · `LIMITED` · `WAITLIST` · `NOT_ACCEPTING`
  (if the page doesn't say, put `N/A` — we default these separately)

> Mapping hints: "trauma-informed" → **Trauma**; "couples/marriage" → **Relationship Issues**
> (focus) **and** **Couples** (demographic); "kids/teens" → **Children**/**Adolescents**;
> "substance use" → **Addiction**; "identity / coming out" → **LGBTQ+** / **Gender Identity**.
> If a modality like *IFS, Mindfulness, Motivational Interviewing, ACT-adjacent* appears and isn't in
> the 6 above, list it under `Modalities — other (verbatim)` — we'll decide whether to add it.

---

## Per-therapist template (copy one block per person)

```
### <Full name as shown, e.g. Alexis Welsh, LCSW>
- Profile URL: <https://www.brazentherapy.org/...>
- Pronouns: <e.g. she/her | N/A>
- Credentials: <e.g. LCSW | N/A>
- Role/Title: <e.g. Therapist | Intern Therapist | Clinical Supervisor | Practice Manager>
- Bio (verbatim): <paste the full bio text exactly>
- Focus areas: <comma-separated from controlled list>
- Focus areas — other (verbatim): <anything not in the list | N/A>
- Modalities: <comma-separated from controlled list>
- Modalities — other (verbatim): <e.g. IFS, Mindfulness | N/A>
- Demographics served: <comma-separated from controlled list | N/A>
- Session delivery: <VIRTUAL | IN_PERSON | HYBRID | N/A>
- Accepting new clients: <ACCEPTING | LIMITED | WAITLIST | NOT_ACCEPTING | N/A>
- Scheduling / "Book Now" URL: <the exact link the booking button points to | N/A>
- Years of experience: <number if stated | N/A>
- Education / school: <if stated | N/A>
- Other notes (verbatim): <anything else useful, e.g. languages, faith-based, populations | N/A>
```

### Filled example (for reference — do not submit this one)
```
### Alexis Welsh, LCSW
- Profile URL: https://www.brazentherapy.org/alexis
- Pronouns: N/A
- Credentials: LCSW
- Role/Title: Therapist
- Bio (verbatim): I'm very proud of you for taking the first step of your healing journey! My approach is supportive and collaborative, using a trauma informed and strength based perspective in hopes of creating a safe environment that allows you to be transparent and work towards restoration.
- Focus areas: Anxiety, Depression, Grief, Trauma
- Focus areas — other (verbatim): N/A
- Modalities: CBT, DBT, EMDR
- Modalities — other (verbatim): IFS, Mindfulness, Motivational Interviewing
- Demographics served: Adults
- Session delivery: N/A
- Accepting new clients: N/A
- Scheduling / "Book Now" URL: https://brazentherapy.clientsecure.me/
- Years of experience: N/A
- Education / school: N/A
- Other notes (verbatim): N/A
```

---

## Roster (28 people) — profile URLs are `brazentherapy.org/<slug>`

The slug is usually the first name (some are shortened, e.g. Shuheng Hu → `/shu`). **Confirm each URL
by clicking the person on the team page** (`brazentherapy.org/ourteam`) — the ones marked *confirm*
are best-guesses.

| # | Name | Role | Profile URL |
| --- | --- | --- | --- |
| 1 | Clarke Scott, LPC | Founder & Clinical Director | https://www.brazentherapy.org/clarke |
| 2 | Alexis Welsh, LCSW | Therapist | https://www.brazentherapy.org/alexis |
| 3 | Amber Block-Zambrano, LCSW | Therapist & Clinical Supervisor | https://www.brazentherapy.org/amber |
| 4 | Shuheng Hu, LPC | Therapist & Clinical Supervisor | https://www.brazentherapy.org/shu |
| 5 | Raven Taylor-Aduwak, LAC | Therapist | *(confirm)* https://www.brazentherapy.org/raven |
| 6 | Ana Franco, LAC | Therapist | *(confirm)* https://www.brazentherapy.org/ana |
| 7 | Aundrea Austin, LMSW | Therapist | *(confirm)* https://www.brazentherapy.org/aundrea |
| 8 | Elise Pinkowski, LMSW | Therapist | *(confirm)* https://www.brazentherapy.org/elise |
| 9 | Makya Kirchner, LMSW | Therapist | *(confirm)* https://www.brazentherapy.org/makya |
| 10 | Riana Burnett, LCSW | Therapist | *(confirm)* https://www.brazentherapy.org/riana |
| 11 | Alex Righi, LMSW | Therapist | *(confirm)* https://www.brazentherapy.org/alex |
| 12 | Renz Narciso, LMSW | Therapist | *(confirm)* https://www.brazentherapy.org/renz |
| 13 | Cailin Payson, LAMFT | Therapist | *(confirm)* https://www.brazentherapy.org/cailin |
| 14 | Erica Harris, LPC | Therapist | *(confirm)* https://www.brazentherapy.org/erica |
| 15 | Vonyee Soulfire, LAC | Therapist | *(confirm)* https://www.brazentherapy.org/vonyee |
| 16 | Teena Miller, LAMFT | Therapist | *(confirm)* https://www.brazentherapy.org/teena |
| 17 | Chelsea Honea, LPC, LIAC | Therapist | *(confirm)* https://www.brazentherapy.org/chelsea |
| 18 | Shae Moreau, LAC | Therapist | *(confirm)* https://www.brazentherapy.org/shae |
| 19 | Carissa Fenceroy, MSW | Therapist | *(confirm)* https://www.brazentherapy.org/carissa |
| 20 | Sybil Nwulu, LAC | Therapist | *(confirm)* https://www.brazentherapy.org/sybil |
| 21 | Jeanine Whitehead, LCSW | Therapist | *(confirm)* https://www.brazentherapy.org/jeanine |
| 22 | Maggie Reichler | Intern Therapist | *(confirm)* https://www.brazentherapy.org/maggie |
| 23 | Sukhmani Khalsa | Intern Therapist | *(confirm)* https://www.brazentherapy.org/sukhmani |
| 24 | Kira McSherry | Intern Therapist | *(confirm)* https://www.brazentherapy.org/kira |
| 25 | Robin Burnam | Intern Therapist | *(confirm)* https://www.brazentherapy.org/robin |
| 26 | Mimi (Xiaojun) Jiang | Intern Therapist | *(confirm)* https://www.brazentherapy.org/mimi |
| 27 | Angelinah Honea | Practice Manager | *(confirm)* https://www.brazentherapy.org/angelinah |
| 28 | Stella Behnke | Administrative Assistant | *(confirm)* https://www.brazentherapy.org/stella |

Notes:
- **#27 Angelinah Honea (Practice Manager)** and **#28 Stella Behnke (Administrative Assistant)** are
  non-clinical — capture bio if present, but they won't get focus areas/modalities and won't appear in
  the matching quiz. Mark clinical fields `N/A`.
- **Colleen Casson, LPC** is in our system but not on the current public team page — skip unless the
  team member has her details.
- Match the **Name** exactly as written above so records line up (credentials included).

---

## What happens when you send it back
I'll: normalize the tags → populate each therapist's specialties/modalities/demographics → replace
the stub bios → set delivery/availability/scheduling link → publish them → then verify the matching
quiz returns sensible, ranked results before we add it to the menu.
