# Vericerti 블록체인 통합 문서

## 아키텍처

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Docker Compose                               │
├─────────────┬─────────────┬─────────────┬───────────────────────────┤
│ Spring Boot │   MySQL     │    Redis    │    Hardhat Node           │
│  :8080      │   :3306     │   :6379     │   :8545                   │
└─────────────┴─────────────┴─────────────┴───────────────────────────┘
```

---

## 동기화 전략

### 1. 등록 (PENDING → RECORDED)

```
1분마다 스케줄러 → registerHash() → txHash 저장 → 5분 후 검증 예약
```

### 2. 지연 검증 (5분 후)

```
Redis Sorted Set에서 만료 항목 → verifyHash() → 블록체인 검증
```

### 3. 하루 2번 전체 검증

```
06:00, 18:00 → 모든 RECORDED 엔트리 → 블록체인 검증 → 불일치 보정
```

### 4. Admin 수동 동기화

```
POST /api/ledger/sync          → 전체 동기화 (ADMIN 전용)
POST /api/ledger/sync/{entryId} → 개별 동기화 (ADMIN 전용)
```

---

## 컴포넌트

| 클래스 | 역할 |
|--------|------|
| `Web3jService` | Web3j 연결 관리 |
| `LedgerRegistryService` | 컨트랙트 호출 (registerHash, verifyHash) |
| `BlockchainContractService` | Redis에서 컨트랙트 주소 읽기 |
| `BlockchainSyncScheduler` | 등록 배치 (1분) |
| `BlockchainVerificationService` | 검증 배치 (5분 지연, 하루 2회, Admin) |

---

## API

| 엔드포인트 | 권한 | 설명 |
|-----------|------|------|
| `GET /api/organizations/{orgId}/ledger` | Public | 조직 Ledger 조회 |
| `GET /api/ledger/verify/{txHash}` | Public | 트랜잭션 검증 |
| `POST /api/ledger/sync` | ADMIN | 전체 동기화 |
| `POST /api/ledger/sync/{entryId}` | ADMIN | 개별 동기화 |

---

## 실행

```bash
docker-compose up -d --build
```

## LedgerEntry 상태

| 상태 | 설명 |
|------|------|
| `PENDING` | 등록 대기 |
| `RECORDED` | 기록 완료 |
| `FAILED` | 등록 실패 |
