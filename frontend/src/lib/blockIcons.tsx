import type { ComponentType } from "react";
import {
  Sparkles,
  Users,
  MessageCircle,
  HandHeart,
  ShieldCheck,
  Heart,
  HeartHandshake,
  Scale,
  Leaf,
  Compass,
  Star,
  Lock,
} from "lucide-react";

/** Curated icon set selectable on content blocks (e.g. value/feature cards). */
export const BLOCK_ICONS: Record<string, ComponentType<{ className?: string }>> = {
  sparkles: Sparkles,
  users: Users,
  message: MessageCircle,
  "hand-heart": HandHeart,
  shield: ShieldCheck,
  heart: Heart,
  handshake: HeartHandshake,
  scale: Scale,
  leaf: Leaf,
  compass: Compass,
  star: Star,
  lock: Lock,
};

export const BLOCK_ICON_NAMES = Object.keys(BLOCK_ICONS);
