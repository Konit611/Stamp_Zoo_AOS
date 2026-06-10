# QR 페이로드 사양 (StampZoo — iOS / AOS 공유)

스킴: `stamp_zoo://`

## 형식 일람

| 형식 | Type | 의미 |
|---|---|---|
| `stamp_zoo://animal/{animalId}` | ANIMAL (REAL) | 동물 스탬프 수집 |
| `stamp_zoo://facility/{facilityId}` | FACILITY (REAL) | 시설 상세 이동 |
| `stamp_zoo://bingo` | BINGO (REAL) | 빙고 홈으로 |
| `stamp_zoo://event` | EVENT (REAL) | "이벤트 준비 중" 안내 |
| `stamp_zoo://test/animal/{animalId}` | ANIMAL (TEST) | 테스트 수집 (위치 검증 없음) |
| `stamp_zoo://facility/{facilityId}/animal/{animalIndex}` | FacilityAnimal (REAL) | 시설별 동물 인덱스 방식 |
| `stamp_zoo://test/facility/{facilityId}/animal/{animalIndex}` | FacilityAnimal (TEST) | 위와 동일하나 TEST |

## 시즌 파라미터 (2026 시즌 도입)

QR URL 끝에 시즌 쿼리를 붙일 수 있다:

```
stamp_zoo://animal/{animalId}?season=2026
stamp_zoo://facility/{facilityId}/animal/{animalIndex}?season=2026
```

규칙:
- **쿼리는 경로 파싱 전에 제거**한다. `?` 이후를 떼어내고 `season` 값만 추출 → 나머지 경로(UUID/인덱스 등)는 기존과 동일하게 파싱.
- `season`이 **없으면** 기존 동작 그대로 (구형 QR 호환).
- QR `season`이 **현재 시즌**(JSON `metadata.season`)보다 과거이면 수집을 막고 "이전 시즌(작년) QR입니다" 안내.
  - 비교: 둘 다 숫자면 숫자 비교, 아니면 문자열 비교 (iOS와 동일).
- 현재 시즌은 `assets/zoo_data_YYYY_MM_DD.json`의 `metadata.season`(String). 없으면 폴백 `"2025"`.

## 구현 위치

| | iOS | AOS |
|---|---|---|
| 파싱/시즌 추출 | `Core/Scanner/QRValidationService.swift` (`extractSeason`/`stripQuery`) | `core/qr/QRParser.kt` |
| 검증 결과 | `QRValidationResult.expiredSeason` | `ScannerViewModel.isExpiredSeason()` + `onExpiredSeason` 콜백 |
| 현재 시즌 | `JSONDataService.currentSeason` | `ZooRepository.getCurrentSeason()` |

> DB 스키마 변경 없음. AOS는 `StampCollection`에 season을 저장하지 않는다 (시즌 검증은 JSON metadata.season만으로 충분).
