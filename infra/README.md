# Infra

OCI(도쿄, `ap-tokyo-1`)에 운영 환경을 만드는 Terraform 코드입니다. 환경별로 폴더를 나누며 지금은 `prod`만 있습니다. `prod/terraform`에 Terraform 코드가 있고, `prod/compose.yaml`은 버킷에 올려 부트스트랩이 내려받는 파일입니다.

## 미리 준비할 것

Terraform이 만들지 않고 조회만 하는 자원입니다. `apply` 전에 같은 compartment에 있어야 합니다.

| 자원 | 이름 | 용도 |
| --- | --- | --- |
| Reserved Public IP | `readthem-public-ip-1` | 인스턴스를 다시 만들어도 IP가 바뀌지 않도록 Terraform 밖에서 관리 |
| Object Storage 버킷 | `readthem-bucket-1` | 부트스트랩이 내려받는 `compose.yaml`, `.env` 보관 |

로컬에는 Terraform과 인증을 마친 OCI CLI가 필요합니다. Reserved Public IP 연결을 `local-exec`에서 OCI CLI로 실행합니다.

## 적용

1. `prod/terraform/secrets.tf.example`을 `prod/terraform/secrets.tf`로 복사해 값을 채웁니다. `secrets.tf`와 state 파일은 커밋하지 않습니다.
2. `prod/compose.yaml`과 운영용 `.env`를 버킷 최상위에 올립니다.
3. `prod/terraform`에서 실행합니다.

```bash
terraform init
terraform apply
```

## 부트스트랩

인스턴스는 처음 뜰 때 `user_data` 스크립트로 Docker와 OCI CLI를 설치하고, Vault에서 GHCR 계정을 읽어 로그인한 뒤 Caddy 설정을 쓰고 버킷의 `compose.yaml`로 컨테이너를 올립니다. 로그는 인스턴스의 `/var/log/bootstrap.log`에 남습니다.

## 리버스 프록시

Caddy가 80, 443 포트를 받고 인증서 발급, HTTPS 리다이렉트, HTTP/3을 기본 동작으로 처리합니다. 설정은 부트스트랩이 `/opt/readthem/readthem-caddy/volumes/etc/caddy/`에 쓰는 파일 두 개입니다.

| 파일 | 내용 |
| --- | --- |
| `Caddyfile` | `application_domain` 블록에서 `upstream`을 `import` |
| `upstream` | 첫 배포 전에는 `respond 503`, 배포 뒤에는 `reverse_proxy {컨테이너}:{포트}` 한 줄. CD가 덮어씁니다 |

부트스트랩이 끝나면 `application_domain`은 HTTPS로 503을 응답하고, 첫 배포가 성공하면 백엔드가 응답합니다.

Caddyfile을 저장소에서 고치면 부트스트랩이 바뀌어 `apply` 때 인스턴스가 교체됩니다. 운영 중에는 서버에서 파일을 고치고 reload합니다.

```bash
sudo docker exec readthem-caddy caddy reload --config /etc/caddy/Caddyfile
```

인증서는 볼륨 없이 컨테이너 안에 있어, 인스턴스를 교체하거나 Caddy 컨테이너를 다시 만들면 다시 발급됩니다.

## 백엔드 배포

`dev`에 `back` 변경이 push되면 `.github/workflows/back-cd.yml`이 다음 태그 계산 → `linux/arm64` 이미지 빌드(GHCR) → SSH 접속 후 Blue/Green 배포 → 태그·릴리즈 생성 순으로 실행합니다. 새 컨테이너(`readthem-back-1` ↔ `readthem-back-2`)가 `/actuator/health`를 통과하면 `upstream` 파일을 새 컨테이너로 덮어쓰고 `caddy reload`한 뒤, 이전 컨테이너를 30초 유예로 멈춘 뒤 지웁니다. 실패하면 새 컨테이너만 지우고 기존 것은 그대로 두며, 태그와 릴리즈도 만들지 않습니다. 현재 업스트림도 `upstream` 파일에서 읽으며, 파일이 두 컨테이너 중 어느 것도 가리키지 않으면 초기 배포로 처리합니다.

DB 마이그레이션은 새 컨테이너가 기동할 때 Flyway가 실행합니다. 아직 적용되지 않은 `db/migration/` 파일을 순서대로 적용한 뒤 `validate`로 엔티티와 스키마를 대조하고, 어긋나면 기동이 실패해 헬스 체크를 통과하지 못합니다. 이때 이전 컨테이너는 이전 코드로 트래픽을 계속 받고 있고, 새 컨테이너가 지워져도 이미 적용된 마이그레이션은 되돌려지지 않습니다. 그래서 마이그레이션은 이전 버전 코드와 호환되게 씁니다(`docs/guardrails.md`).

빌드와 배포 잡은 시작할 때 자기가 이 워크플로의 최신 실행인지 확인하고, 아니면 실패합니다. 예전 실행을 재시도해 옛 코드가 최신 배포를 덮어쓰는 것을 막기 위해서입니다. `dev`의 최신 커밋이 아니라 실행 ID로 비교하므로, `back`과 무관한 커밋이 뒤에 올라와도 배포는 그대로 진행됩니다. 그래서 예전 버전으로 되돌리려면 `dev`에 revert 커밋을 올립니다.

도서 수집 스케줄(`BookScheduler.upsertBooks`)은 지금 꺼져 있어 새 컨테이너가 떠도 수집을 시작하지 않습니다.

저장소 Actions 시크릿이 필요합니다.

| 시크릿 | 값 |
| --- | --- |
| `INSTANCE_HOST` | Reserved Public IP |
| `INSTANCE_USER` | `ubuntu` |
| `INSTANCE_SSH_KEY` | `ssh_public_key`와 짝인 개인 키 |

GitHub 러너 IP가 고정이 아니라 22번 포트는 러너에서 닿을 수 있어야 합니다.

부트스트랩 스크립트를 고치고 `apply`하면 인스턴스가 교체됩니다. 스크립트에 들어가는 변수(`application_domain` 등)나 `ssh_public_key`를 바꿔도 마찬가지입니다. MySQL, Redis 데이터는 부트 볼륨에 있어 교체와 함께 사라지므로, `plan`에서 교체 여부를 먼저 확인합니다.
