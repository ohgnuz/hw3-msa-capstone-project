# Table of contents

- [예제 - 쇼핑몰](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
    - [SAGA Pattern의 적용](#SAGA-Pattern-의-적용)
    - [CQRS Pattern의 적용](#CQRS-Pattern-의-)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

간단 쇼핑몰 커버

기능적 요구사항
1. 고객(Client)이 상품과 수량을 선택하여 주문한다.
2. 주문과 동시에 결제할 수 있는 상태가 된다. (결제는 무통장 입금만 가능)
3. 판매자가 입금확인을 하면 product에서 재고수량이 감소한다 (이 때, 재고수량에 문제가 있으면 주문취소를 할 수 있다.)
4. 재고에 문제가 없으면 delivery process에서 배송이 시작된다
5. 고객이 물품수령 후 수령확인을 하면 주문 프로세스가 종료된다
6. 고객은 주문상태를 한 화면에서 확인할 수 있다
7. 고객은 판매자가 입금확인완료 전에만 주문취소를 할 수 있다.

비기능적 요구사항
1. 트랜잭션
    1. 재고가 없으면 판매자의 입금확인 후 배송이 발생하지 않아야한다. (Sync 호출)
1. 장애격리
    1. 결제기능이 수행되지 않더라도 예약은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
    1. 주문시스템이 과중되면 사용자를 잠시동안 받지 않고 잠시 후에 하도록 유도한다  Circuit breaker, fallback
1. 성능
    1. 주문상태를 한번에 확인할 수 있어야 한다  (CQRS)


# 체크포인트
- 분석설계
- SAGA Pattern
- CQRS Pattern
- Correlation / Compensation(Unique Key)
- Request / Response (Feign Client / Sync.Async)
- Gateway
- Deploy / Pipeline
- Circuit Breaker
- Autoscale(HPA)
- Self-Healing(Liveness Probe)
- Zero-Downtime Deploy(Readiness Probe)
- Config Map / Persistence Volume
- Polyglot

# 분석설계
- 이벤트스토밍 결과
- MSAEz 로 모델링한 이벤트스토밍 결과(최초): https://labs.msaez.io/#/storming/IT74UK0hTgVAQL194nSu2xdq2tw1/6f6e576e230b867104636b3d859bbfcf

![DDD](https://user-images.githubusercontent.com/41348473/174694494-6185835e-e492-4eb5-be59-b21806efa1a1.png)
- 주문상태
  - 주문접수 - OrderPlaced Create
  - 입금확인중 - OrderPlaced Update
  - 입금확인완료 - QtyDecreased Update
  - 배송시작 - DeliveryStarted Update
  - 배송완료확인 - OrderClosed Update

# SAGA Pattern 의 적용
- 결제시스템(pay)에 문제가 발생해도 주문은 받을 수 있도록 Pub/Sub 구조로 설계
- 
![image](https://user-images.githubusercontent.com/41348473/174700781-5b061161-2584-4abd-9174-a45f5c81daed.png)

- 배송시스템이 문제발생 시 상품시스템(product)에서 발생한 주문이 누락되지않도록 Pub/Sub 구조로 설계
- 
![image](https://user-images.githubusercontent.com/41348473/174713615-351b8a36-62dd-4d15-83b5-52c0d9b9e838.png)

- 판매자가 결제확인 시도 시 재고가 0이면 주문이 취소되도록 Pub/Sub구조에서 Req/Res 구조로 변경
  - AS-IS
  - 
![image](https://user-images.githubusercontent.com/41348473/174713672-9e72f5de-1d0c-474b-b610-b61175da0d1f.png)
  - TO-BE
  - 
![image](https://user-images.githubusercontent.com/41348473/174713808-eb6cf9a7-9236-46b9-bcb8-e85856463656.png)


# CQRS Pattern 의 적용
- 고객이 주문상태를 한눈에 확인할 수 있도록 CQRS 패턴을 적용
- 이벤트 별 주문상태 정의 
  - OrderPlaced : 주문접수
  - OrderCanceled : 주문취소
    - (PayChecked)입금확인완료 시 주문취소 불가
  - PayPlaced : 입금확인대기
  - PayChecked : 입금확인완료
    - 재고가 0이면 결제취소
  - PayCanceled : 결제취소
  - QtyDecreased : 상품준비완료
  - DeliveryStarted : 배송중
  - OrderClosed : 수령확인
    - 고객이 수령확인 버튼을 누르면 주문프로세스 종료
  
