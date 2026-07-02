import { test, expect } from "@playwright/test";

test.describe("admin blog crash", () => {
  test("no client exception on /admin/blog after login", async ({ page }) => {
    const errors: string[] = [];
    const failedRequests: string[] = [];

    page.on("console", (msg) => {
      const text = msg.text();
      if (msg.type() === "error") {
        errors.push(text);
      }
    });
    page.on("pageerror", (err) => {
      errors.push(String(err));
    });
    page.on("requestfinished", async (req) => {
      const resp = await req.response();
      if (resp && resp.status() >= 500) {
        failedRequests.push(`${req.method()} ${req.url()} -> ${resp.status()}`);
      }
    });

    await page.goto("/auth/login");
    await page.getByLabel("Email").fill("admin@demo.local");
    await page.getByLabel("Password").fill("password");
    await page.getByRole("button", { name: /sign in/i }).click();

    await page.waitForURL("/admin");
    await page.goto("/admin/blog");
    await page.waitForLoadState("networkidle");

    console.log("Failed requests:", failedRequests.join("\n"));
    console.log("Console errors:", errors.join("\n"));

    // Only assert that the BlockNote initialContent crash is gone.
    expect(errors).not.toContainEqual(
      expect.stringContaining("Error creating document from blocks passed as `initialContent`")
    );
  });
});
