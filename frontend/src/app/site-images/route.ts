import { readdir } from "fs/promises";
import path from "path";

export const dynamic = "force-dynamic";

/**
 * Lists the bundled site images under public/images so the admin media library
 * can show them (these are shipped as static assets, not uploaded media).
 */
export async function GET() {
  const root = path.join(process.cwd(), "public", "images");

  async function walk(dir: string, base: string): Promise<string[]> {
    let out: string[] = [];
    let entries;
    try {
      entries = await readdir(dir, { withFileTypes: true });
    } catch {
      return out;
    }
    for (const e of entries) {
      if (e.isDirectory()) {
        out = out.concat(await walk(path.join(dir, e.name), `${base}/${e.name}`));
      } else if (/\.(jpe?g|png|webp|gif|svg)$/i.test(e.name)) {
        out.push(`${base}/${e.name}`);
      }
    }
    return out;
  }

  const files = await walk(root, "/images");
  return Response.json(files.sort());
}
