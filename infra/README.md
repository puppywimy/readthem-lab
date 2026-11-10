# Infra

OCI(도쿄, `ap-tokyo-1`)에 운영 환경을 만드는 Terraform 코드입니다. 환경별로 폴더를 나누며 지금은 `prod`만 있습니다.

## 미리 준비할 것

Terraform이 만들지 않고 조회만 하는 자원입니다. `apply` 전에 같은 compartment에 있어야 합니다.

| 자원 | 이름 | 용도 |
| --- | --- | --- |
| Reserved Public IP | `readthem-public-ip-1` | 인스턴스를 다시 만들어도 IP가 바뀌지 않도록 Terraform 밖에서 관리 |
| Object Storage 버킷 | `readthem-bucket-1` | 부트스트랩이 내려받는 `compose.yaml`, `.env` 보관 |

로컬에는 Terraform과 인증을 마친 OCI CLI가 필요합니다. Reserved Public IP 연결을 `local-exec`에서 OCI CLI로 실행합니다.

## 적용

1. `prod/secrets.tf.example`을 `prod/secrets.tf`로 복사해 값을 채웁니다. `secrets.tf`와 state 파일은 커밋하지 않습니다.
2. `prod/compose.yaml`과 운영용 `.env`를 버킷 최상위에 올립니다.
3. `prod`에서 실행합니다.

```bash
terraform init
terraform apply
```

## 부트스트랩

인스턴스는 처음 뜰 때 `user_data` 스크립트로 Docker와 OCI CLI를 설치하고, Vault에서 GHCR 계정을 읽어 로그인한 뒤 버킷의 `compose.yaml`로 컨테이너를 올립니다. 로그는 인스턴스의 `/var/log/bootstrap.log`에 남습니다.

## 백엔드 배포

`dev`에 `back` 변경이 push되면 `.github/workflows/back-cd.yml`이 다음 태그 계산 → `linux/arm64` 이미지 빌드(GHCR) → SSH 접속 후 Blue/Green 배포 → 태그·릴리즈 생성 순으로 실행합니다. 새 컨테이너(`readthem-back-1` ↔ `readthem-back-2`)가 `/actuator/health`를 통과하면 NPMplus API로 `application_domain` 프록시 호스트의 업스트림을 바꾸고, 이전 컨테이너를 30초 유예로 멈춘 뒤 지웁니다. 실패하면 새 컨테이너만 지우고 기존 것은 그대로 두며, 태그와 릴리즈도 만들지 않습니다.

빌드와 배포 잡은 시작할 때 자기 커밋이 `dev`의 최신 커밋인지 확인하고, 아니면 실패합니다. 예전 실행을 재시도해 옛 코드가 최신 배포를 덮어쓰는 것을 막기 위해서입니다. 그래서 예전 버전으로 되돌리려면 `dev`에 revert 커밋을 올립니다.

저장소 Actions 시크릿이 필요합니다.

| 시크릿 | 값 |
| --- | --- |
| `INSTANCE_HOST` | Reserved Public IP |
| `INSTANCE_USER` | `ubuntu` |
| `INSTANCE_SSH_KEY` | `ssh_public_key`와 짝인 개인 키 |

첫 배포 전에 NPMplus에 `application_domain` 프록시 호스트가 만들어져 있어야 합니다. GitHub 러너 IP가 고정이 아니라 22번 포트는 러너에서 닿을 수 있어야 합니다.

부트스트랩 스크립트를 고치고 `apply`하면 인스턴스가 교체됩니다. 스크립트에 들어가는 변수(`password`, `application_domain` 등)나 `ssh_public_key`를 바꿔도 마찬가지입니다. MySQL, Redis, NPMplus 데이터는 부트 볼륨에 있어 교체와 함께 사라지므로, `plan`에서 교체 여부를 먼저 확인합니다.
