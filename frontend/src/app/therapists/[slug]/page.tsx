import Image from "next/image";
import { notFound } from "next/navigation";
import { fetchTherapist } from "@/lib/api";
import { AnalyticsTracker } from "@/components/AnalyticsTracker";
import { TrackedLink } from "@/components/TrackedLink";
import {
  AVAILABILITY_BADGE_COLORS,
  AVAILABILITY_LABELS,
  SERVICE_DELIVERY_LABELS,
} from "@/types/therapist";
import type { Metadata } from "next";

interface Props {
  params: { slug: string };
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const therapist = await fetchTherapist(params.slug).catch(() => null);
  if (!therapist) return {};
  return {
    title: therapist.metaTitle || `${therapist.firstName} ${therapist.lastName}`,
    description: therapist.metaDescription || undefined,
    openGraph: therapist.ogImageUrl ? { images: [therapist.ogImageUrl] } : undefined,
    alternates: therapist.canonicalUrl ? { canonical: therapist.canonicalUrl } : undefined,
  };
}

export default async function TherapistProfilePage({ params }: Props) {
  const therapist = await fetchTherapist(params.slug).catch(() => null);
  if (!therapist) notFound();

  const fullName = `${therapist.firstName} ${therapist.lastName}`;

  return (
    <main className="mx-auto max-w-3xl px-4 py-8 md:py-12">
      <AnalyticsTracker
        eventType="profile_view"
        metadata={{ slug: params.slug, therapistName: fullName }}
      />
      <div className="rounded-[var(--border-radius)] border border-slate-200 bg-white p-6 md:p-10">
        <div className="flex flex-col gap-6 md:flex-row md:items-start">
          {therapist.photoUrl && (
            <Image
              src={therapist.photoUrl}
              alt={`Portrait of ${fullName}`}
              width={160}
              height={160}
              unoptimized
              className="h-40 w-40 rounded-[var(--border-radius)] object-cover"
            />
          )}
          <div className="flex-1">
            <h1 className="text-3xl font-bold">{fullName}</h1>
            {therapist.credentials && (
              <p className="text-lg text-slate-600">{therapist.credentials}</p>
            )}
            {therapist.pronouns && (
              <p className="text-sm text-slate-500">{therapist.pronouns}</p>
            )}
            <div className="mt-3 flex flex-wrap gap-2">
              <span
                className={`rounded-full px-3 py-1 text-sm font-medium ${AVAILABILITY_BADGE_COLORS[therapist.availabilityStatus]}`}
              >
                {AVAILABILITY_LABELS[therapist.availabilityStatus]}
              </span>
              <span className="rounded-full bg-slate-100 px-3 py-1 text-sm font-medium text-slate-800">
                {SERVICE_DELIVERY_LABELS[therapist.serviceDelivery]}
              </span>
            </div>
          </div>
        </div>

        {therapist.bio && (
          <div className="prose mt-8 max-w-none">
            <h2 className="text-xl font-semibold">About</h2>
            <p className="whitespace-pre-line">{therapist.bio}</p>
          </div>
        )}

        {(therapist.yearsOfExperience || therapist.education || therapist.licensure) && (
          <div className="mt-8 grid gap-4 sm:grid-cols-3">
            {therapist.yearsOfExperience !== null && (
              <div>
                <h3 className="text-sm font-semibold text-slate-700">Experience</h3>
                <p className="text-slate-600">{therapist.yearsOfExperience} years</p>
              </div>
            )}
            {therapist.education && (
              <div>
                <h3 className="text-sm font-semibold text-slate-700">Education</h3>
                <p className="text-slate-600">{therapist.education}</p>
              </div>
            )}
            {therapist.licensure && (
              <div>
                <h3 className="text-sm font-semibold text-slate-700">Licensure</h3>
                <p className="text-slate-600">{therapist.licensure}</p>
              </div>
            )}
          </div>
        )}

        {therapist.focusAreas.length > 0 && (
          <div className="mt-8">
            <h3 className="mb-2 text-sm font-semibold text-slate-700">Focus areas</h3>
            <div className="flex flex-wrap gap-2">
              {therapist.focusAreas.map((term) => (
                <span
                  key={term.id}
                  className="rounded-full bg-amber-50 px-3 py-1 text-sm text-amber-800"
                >
                  {term.label}
                </span>
              ))}
            </div>
          </div>
        )}

        {therapist.modalities.length > 0 && (
          <div className="mt-4">
            <h3 className="mb-2 text-sm font-semibold text-slate-700">Modalities</h3>
            <div className="flex flex-wrap gap-2">
              {therapist.modalities.map((term) => (
                <span
                  key={term.id}
                  className="rounded-full bg-blue-50 px-3 py-1 text-sm text-blue-800"
                >
                  {term.label}
                </span>
              ))}
            </div>
          </div>
        )}

        {therapist.demographics.length > 0 && (
          <div className="mt-4">
            <h3 className="mb-2 text-sm font-semibold text-slate-700">Demographics served</h3>
            <div className="flex flex-wrap gap-2">
              {therapist.demographics.map((term) => (
                <span
                  key={term.id}
                  className="rounded-full bg-purple-50 px-3 py-1 text-sm text-purple-800"
                >
                  {term.label}
                </span>
              ))}
            </div>
          </div>
        )}

        {therapist.schedulingUrl && (
          <div className="mt-10">
            <TrackedLink
              href={therapist.schedulingUrl}
              eventType="scheduling_click"
              metadata={{ slug: params.slug, therapistName: fullName, source: "profile" }}
              className="inline-flex min-h-[44px] items-center rounded-[var(--border-radius)] bg-[var(--color-primary)] px-6 py-2 font-medium text-white hover:opacity-90"
            >
              Schedule with {therapist.firstName}
            </TrackedLink>
          </div>
        )}
      </div>
    </main>
  );
}
