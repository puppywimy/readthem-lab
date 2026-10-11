"use client";

import type { ReactNode } from "react";

import { ratingColor } from "@/lib/ratingColor";

import Avatar from "@/app/_components/Avatar";
import RatingHistogram from "@/app/_components/RatingHistogram";
import RatingValue from "@/app/_components/RatingValue";

type LibraryProfilePanelProps = {
  avatarLabel: string | null | undefined;
  avatarUrl?: string | null;
  username: string | null | undefined;
  githubId: string | null | undefined;
  githubLink?: string | null;
  averageRating: number | null;
  averageLabel: string;
  rating?: Record<string, unknown>;
  mobileWidget?: ReactNode;
  actions?: ReactNode;
};

export default function LibraryProfilePanel({
  avatarLabel,
  avatarUrl,
  username,
  githubId,
  githubLink,
  averageRating,
  averageLabel,
  rating,
  mobileWidget,
  actions,
}: LibraryProfilePanelProps) {
  return (
    <aside className="flex w-48 shrink-0 flex-col items-center gap-1 pt-6 max-sm:w-full max-sm:items-stretch">
      <div className="flex w-full flex-col items-center gap-3 max-sm:flex-row max-sm:items-center max-sm:justify-start">
        <Avatar
          label={avatarLabel ?? null}
          imageUrl={avatarUrl ?? null}
          size="lg"
        />
        <div className="min-w-0 text-center max-sm:min-w-0 max-sm:flex-1 max-sm:text-left">
          <div className="text-sm">아이디 : {username}</div>
          <div className="text-sm">닉네임 : {githubId}</div>
          {githubLink && (
            <a
              className="theme-link text-sm underline"
              href={githubLink}
              target="_blank"
              rel="noreferrer"
            >
              github 주소
            </a>
          )}
          <div className="mt-3 flex w-full justify-start max-sm:w-auto">
            {actions}
          </div>
        </div>
      </div>

      {mobileWidget && (
        <div className="mt-8 hidden w-full max-sm:block">{mobileWidget}</div>
      )}

      <div className="mt-2 flex w-full flex-col gap-3 max-sm:flex-row max-sm:items-start">
        <div className="flex min-w-0 flex-1 flex-col gap-1">
          <div className="text-xs theme-muted">{averageLabel}</div>
          {averageRating != null && (
            <div className={`text-lg font-bold ${ratingColor(averageRating)}`}>
              <RatingValue rating={averageRating} />
            </div>
          )}
        </div>
        <div className="min-w-0 flex-1">
          <RatingHistogram rating={rating ?? {}} className="mt-0 w-full" />
        </div>
      </div>

    </aside>
  );
}
