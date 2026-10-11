"use client";

import { useState } from "react";

import RoughButton from "@/app/_components/RoughButton";
import RoughFrame from "@/app/_components/RoughFrame";
import { RoughInput } from "@/app/_components/RoughInput";

type WidgetGuideModalProps = {
  copiedWidgetLink: boolean;
  onCancel: () => void;
  onCopyWidgetLink: (code: string) => void;
  widgetLink: string;
};

export default function WidgetGuideModal({
  copiedWidgetLink,
  onCancel,
  onCopyWidgetLink,
  widgetLink,
}: WidgetGuideModalProps) {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const codeSnippet = `<img src="${widgetLink}" alt="내 서재 위젯" />`;

  return (
    <div
      className="rough-modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="widget-guide-title"
    >
      <div className="rough-modal-card">
        <RoughFrame className="rough-overlay" variant="card" />
        <h2 id="widget-guide-title" className="text-xl font-bold">
          README에 위젯 넣는 방법
        </h2>
        <div className="rough-panel-border mt-2 bg-[var(--surface-muted)] px-3 py-2 text-sm theme-muted">
          <RoughFrame className="rough-overlay" variant="card" />
          <p>버튼을 눌러 단계를 바꿔가며 확인할 수 있어요.</p>
        </div>

        <div className="mt-4 flex flex-wrap gap-2">
          <RoughButton
            roughSize="sm"
            tone={step === 1 ? "history" : "neutral"}
            type="button"
            onClick={() => setStep(1)}
          >
            1단계
          </RoughButton>
          <RoughButton
            roughSize="sm"
            tone={step === 2 ? "history" : "neutral"}
            type="button"
            onClick={() => setStep(2)}
          >
            2단계
          </RoughButton>
          <RoughButton
            roughSize="sm"
            tone={step === 3 ? "history" : "neutral"}
            type="button"
            onClick={() => setStep(3)}
          >
            3단계
          </RoughButton>
        </div>

        <div className="rough-panel-border mt-4 bg-[var(--surface-muted)] p-3 text-sm">
          <RoughFrame className="rough-overlay" variant="card" />
          {step === 1 && (
            <div>
              <div className="font-semibold">
                1. GitHub 프로필 README 레포지토리 생성
              </div>
              <div className="mt-1 theme-muted">
                GitHub에서 사용자 이름과 같은 이름의 공개 레포지토리를 만들고,
                `README.md` 파일을 준비해 주세요.
              </div>
            </div>
          )}

          {step === 2 && (
            <div>
              <div className="font-semibold">2. 위젯 코드 복사</div>
              <div className="mt-1 theme-muted">
                README에서 이미지를 표시할 수 있도록 준비된 `img` 태그를
                복사합니다.
              </div>
            </div>
          )}

          {step === 3 && (
            <div>
              <div className="font-semibold">3. README.md에 붙여넣기</div>
              <div className="mt-1 theme-muted">
                복사한 `img` 태그를 README.md에 붙여 넣으면 프로필에서 바로
                위젯이 보입니다.
              </div>
            </div>
          )}
        </div>

        <div className="rough-panel-border mt-4 flex flex-col gap-2 bg-[var(--surface-muted)] p-3">
          <RoughFrame className="rough-overlay" variant="card" />
          <div className="text-sm font-semibold">복사할 코드</div>
          <RoughInput
            inputClassName="font-mono text-xs"
            roughSize="sm"
            readOnly
            value={codeSnippet}
            onFocus={(e) => e.currentTarget.select()}
          />
          <div className="flex flex-wrap gap-2">
            <RoughButton
              roughSize="sm"
              tone="history"
              type="button"
              onClick={() => onCopyWidgetLink(codeSnippet)}
            >
              {copiedWidgetLink ? "복사 완료" : "코드 복사"}
            </RoughButton>
            <span className="self-center text-xs theme-muted">
              GitHub 프로필 README.md에 그대로 붙여 넣으면 됩니다.
            </span>
          </div>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <RoughButton
            roughSize="sm"
            tone="cancel"
            type="button"
            onClick={onCancel}
          >
            취소
          </RoughButton>
        </div>
      </div>
    </div>
  );
}
