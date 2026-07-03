import Image from "next/image";
import Link from "next/link";

export function HeroBlock({ content }: { content: Record<string, unknown> }) {
  const eyebrow = content.eyebrow ? String(content.eyebrow) : null;
  const heading = String(content.heading ?? "");
  const subheading = String(content.subheading ?? "");
  const buttonText = content.buttonText ? String(content.buttonText) : null;
  const buttonUrl = content.buttonUrl ? String(content.buttonUrl) : null;
  const backgroundImage = content.backgroundImage
    ? String(content.backgroundImage)
    : null;
  const overlay = Boolean(content.overlay);
  const objectPosition = content.objectPosition
    ? String(content.objectPosition)
    : "center";

  return (
    <section className="relative isolate overflow-hidden bg-slate-950 text-white">
      {backgroundImage && (
        <Image
          src={backgroundImage}
          alt=""
          fill
          className="object-cover"
          style={{ objectPosition }}
          priority
          sizes="100vw"
        />
      )}
      {overlay && (
        <div className="absolute inset-0 bg-gradient-to-r from-black/75 via-black/45 to-black/20" />
      )}
      <div className="absolute inset-x-0 bottom-0 h-24 bg-gradient-to-t from-black/35 to-transparent" />
      <div className="relative mx-auto flex min-h-[430px] max-w-6xl flex-col justify-center gap-5 px-4 py-14 md:min-h-[520px] md:py-20">
        {eyebrow && (
          <p className="w-fit rounded-[var(--border-radius)] bg-white/15 px-3 py-1 text-xs font-semibold uppercase tracking-[0.14em] text-white/90 backdrop-blur">
            {eyebrow}
          </p>
        )}
        <h1
          className="max-w-4xl text-3xl font-bold leading-tight md:text-6xl"
          style={{ fontFamily: "var(--font-heading)" }}
        >
          {heading}
        </h1>
        {subheading && (
          <p className="max-w-3xl text-base leading-8 text-slate-100 md:text-xl">
            {subheading}
          </p>
        )}
        {buttonText && buttonUrl && (
          <Link
            href={buttonUrl}
            className="inline-flex min-h-[46px] w-fit items-center justify-center rounded-[var(--border-radius)] bg-white px-5 py-3 text-sm font-semibold text-slate-950 shadow-lg shadow-black/20 transition hover:bg-slate-100"
          >
            {buttonText}
          </Link>
        )}
      </div>
    </section>
  );
}
