export function SpacerBlock({ content }: { content: Record<string, unknown> }) {
  const height = typeof content.height === "number" ? content.height : 48;
  return <div style={{ height }} aria-hidden />;
}
