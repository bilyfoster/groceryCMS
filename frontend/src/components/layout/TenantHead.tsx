import type { TenantSettingsDto } from "@/types/tenant";

export function TenantHead({ tenant }: { tenant: TenantSettingsDto }) {
  const faviconUrl = tenant.settings.faviconUrl as string | undefined;
  const customCss = tenant.settings.customCss as string | undefined;
  const analyticsId = tenant.settings.analyticsId as string | undefined;
  const googleSiteVerification = tenant.settings.googleSiteVerification as string | undefined;
  const bingVerification = tenant.settings.bingVerification as string | undefined;

  return (
    <>
      {faviconUrl && <link rel="icon" href={faviconUrl} />}
      {googleSiteVerification && (
        <meta name="google-site-verification" content={googleSiteVerification} />
      )}
      {bingVerification && <meta name="msvalidate.01" content={bingVerification} />}
      {customCss && (
        <style
          dangerouslySetInnerHTML={{
            __html: customCss.replace(/<\/style/gi, ""),
          }}
        />
      )}
      {analyticsId?.startsWith("G-") && (
        <>
          <script async src={`https://www.googletagmanager.com/gtag/js?id=${analyticsId}`} />
          <script
            dangerouslySetInnerHTML={{
              __html: `window.dataLayer=window.dataLayer||[];function gtag(){dataLayer.push(arguments);}gtag('js',new Date());gtag('config','${analyticsId}');`,
            }}
          />
        </>
      )}
    </>
  );
}
