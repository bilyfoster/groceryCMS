import Image from "next/image";
import Link from "next/link";
export function HeroBlock({ content }: { content: Record<string, unknown> }) {
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
    <section className="relative overflow-hidden bg-slate-900 text-white">
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
      {overlay && <div className="absolute inset-0 bg-black/50" />}
      <div className="relative mx-auto flex max-w-6xl flex-col gap-4 px-4 py-16 md:py-24">
        <h1
          className="text-3xl font-bold md:text-5xl"
          style={{ fontFamily: "var(--font-heading)" }}
        >
          {heading}
        </h1>
        {subheading && <p className="max-w-2xl text-lg text-slate-200">{subheading}</p>}
        {buttonText && buttonUrl && (
          <Link
            href={buttonUrl}
            className="inline-flex min-h-[44px] w-fit items-center justify-center rounded-[var(--border-radius)] bg-white px-4 py-2 text-sm font-medium text-slate-900 hover:bg-slate-100"
          >
            {buttonText}
          </Link>
        )}
      </div>
    </section>
  );
}
