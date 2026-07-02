export interface BlogCategory {
  id: string;
  parentId: string | null;
  name: string;
  slug: string;
  description: string | null;
}

export interface BlogSummary {
  id: string;
  slug: string;
  title: string;
  excerpt: string | null;
  featuredImage: string | null;
  publishedAt: string | null;
  sticky?: boolean;
  published?: boolean;
  viewCount: number;
  likeCount: number;
  readingMinutes: number;
}

export interface BlogPostDetail {
  id: string;
  slug: string;
  title: string;
  excerpt: string | null;
  bodyHtml: string;
  featuredImage: string | null;
  published: boolean;
  sticky?: boolean;
  allowComments?: boolean;
  publishedAt: string | null;
  viewCount: number;
  likeCount: number;
  readingMinutes: number;
  tags: string[];
  categories?: BlogCategory[];
  metaTitle: string | null;
  metaDescription: string | null;
}
