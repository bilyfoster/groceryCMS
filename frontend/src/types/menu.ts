export interface MenuItemDto {
  id: string;
  label: string;
  href: string;
  target: string;
  children?: MenuItemDto[] | null;
}
