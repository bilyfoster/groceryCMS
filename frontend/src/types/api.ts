export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string | null;
  errors: Record<string, string[]> | null;
}

export interface PagedResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface UserDto {
  id: string;
  email: string;
  displayName: string | null;
  role: string;
  tenantId: string;
}
