"use client";

import { useEffect, useState } from "react";

import { apiFetch } from "@/lib/backend/client";

import type { components } from "@/lib/backend/apiV1/schema";
import { goToErrorPage } from "@/lib/error/goToErrorPage";
import { formatDate } from "@/lib/formatDate";
import { useToast } from "@/lib/toast/ToastProvider";

import Pager from "@/app/admin/Pager";

type PageAdminMemberDto = components["schemas"]["PageAdminMemberDto"];

export default function MemberAdmin() {
  const { showToast, showErrorToast, confirmAction } = useToast();
  const [pageData, setPageData] = useState<PageAdminMemberDto | null>(null);
  const [pageNumber, setPageNumber] = useState(0);

  const loadMembers = (page: number) => {
    apiFetch(`/api/v1/members/admin?page=${page}&size=10`)
      .then(setPageData)
      .catch(goToErrorPage);
  };

  useEffect(() => {
    loadMembers(pageNumber);
  }, [pageNumber]);

  const handleForceDelete = async (id: number) => {
    if (!(await confirmAction("이 회원을 강제 탈퇴시키겠습니까?"))) return;

    apiFetch(`/api/v1/members/admin/${id}`, { method: "DELETE" })
      .then((data) => {
        showToast(data?.message ?? "회원을 강제 탈퇴시켰습니다.");
        loadMembers(pageNumber);
      })
      .catch(showErrorToast);
  };

  if (pageData == null)
    return (
      <div aria-busy="true" style={{ marginTop: 28 }}>
        <span className="sk line" style={{ height: 44 }} />
      </div>
    );

  const members = pageData.content ?? [];

  return (
    <>
      {members.length === 0 ? (
        <div className="state">
          <strong>회원이 없습니다.</strong>
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th className="num">ID</th>
                <th>아이디</th>
                <th>닉네임</th>
                <th>GitHub</th>
                <th>권한</th>
                <th>상태</th>
                <th>가입일</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {members.map((member) => (
                <tr key={member.id}>
                  <td className="num">{member.id}</td>
                  <td className="nowrap">{member.username}</td>
                  <td className="nowrap">{member.nickname}</td>
                  <td className="nowrap">{member.githubId}</td>
                  <td>
                    <span className={`badge ${member.admin ? "on" : ""}`}>
                      {member.admin ? "관리자" : "일반"}
                    </span>
                  </td>
                  <td className="nowrap">{member.deleted ? "탈퇴" : "정상"}</td>
                  <td className="nowrap">{formatDate(member.createdDate)}</td>
                  <td className="fit">
                    {!member.admin && !member.deleted && (
                      <button
                        type="button"
                        className="text-btn"
                        onClick={() => handleForceDelete(member.id)}
                      >
                        강제 탈퇴
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <Pager
        page={pageData.number ?? 0}
        totalPages={pageData.totalPages ?? 1}
        onChange={setPageNumber}
      />
    </>
  );
}
