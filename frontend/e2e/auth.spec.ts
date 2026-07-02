import { test, expect } from "@playwright/test";

test.describe("auth", () => {
  test("login page renders", async ({ page }) => {
    await page.goto("/auth/login");
    await expect(page.getByRole("heading", { name: "Sign in" })).toBeVisible();
    await expect(page.getByLabel("Email")).toBeVisible();
  });

  test("magic link page renders", async ({ page }) => {
    await page.goto("/auth/magic-link");
    await expect(page.getByRole("heading", { name: "Magic link sign in" })).toBeVisible();
  });
});
