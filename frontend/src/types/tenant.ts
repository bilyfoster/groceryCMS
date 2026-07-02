export interface TenantSettings {
  primaryColor?: string;
  secondaryColor?: string;
  headingFont?: string;
  bodyFont?: string;
  borderRadius?: string;
  logoUrl?: string;
  contactEmail?: string;
  socialLinks?: Record<string, string>;
  [key: string]: unknown;
}

export interface TenantSettingsDto {
  slug: string;
  name: string;
  settings: TenantSettings;
}
