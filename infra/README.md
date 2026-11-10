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

부트스트랩 스크립트를 고치고 `apply`하면 인스턴스가 교체됩니다. 스크립트에 들어가는 변수(`password`, `application_domain` 등)나 `ssh_public_key`를 바꿔도 마찬가지입니다. MySQL, Redis, NPMplus 데이터는 부트 볼륨에 있어 교체와 함께 사라지므로, `plan`에서 교체 여부를 먼저 확인합니다.
