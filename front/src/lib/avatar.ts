type AvatarSource =
  | {
      imgUrl?: string | null;
      avatarUrl?: string | null;
      profileImgUrl?: string | null;
      iconUrl?: string | null;
    }
  | null
  | undefined;

export function resolveAvatarUrl(source: AvatarSource) {
  return (
    source?.imgUrl?.trim() ||
    source?.avatarUrl?.trim() ||
    source?.profileImgUrl?.trim() ||
    source?.iconUrl?.trim() ||
    null
  );
}
