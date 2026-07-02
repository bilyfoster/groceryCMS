import { test, expect } from "@playwright/test";

test.describe("contact", () => {
  test("contact page shows form when published", async ({ page }) => {
    await page.goto("/contact");
    const heading = page.getByRole("heading", { level: 1 });
    await expect(heading).toBeVisible({ timeout: 10_000 });
    await expect(page.getByLabel("Name")).toBeVisible();
    await expect(page.getByLabel("Email")).toBeVisible();
    await expect(page.getByLabel("Message")).toBeVisible();
  });
});
