# Table of contents

- [예제 - 간이쇼핑몰](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
    - [SAGA Pattern의 적용](#SAGA-Pattern-의-적용)
    - [CQRS Pattern의 적용](#CQRS-Pattern-의-적용)
    - [Correlation / Compensation(Unique Key)](#Correlation--CompensationUnique-Key)
    - [Request / Response (Feign Client / Sync.Async)](#request--response-feign-client--syncasync)
  - [구현/운영](#구현/운영)
    - [Gateway](#Gateway)
    - [Deploy / Pipeline](#Deploy--Pipeline)
    - [Circuit Breaker](#Circuit-Breaker)
    - [Autoscale(HPA)](#Autoscalehpa)
    - [Self-Healing(Liveness Probe)](#self-healingliveness-probe)
    - [Zero-Downtime Deploy(Readiness Probe)](#zero-downtime-deployreadiness-probe)
    - [Config Map / Persistence Volume](#config-map--persistence-volume)
    - [Polyglot](#Polyglot)


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
- 참여 : 윤정호, 김순호, 박원종
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
- 윤정호, 김순호, 박원종
- 결제시스템(pay)에 문제가 발생해도 주문은 받을 수 있도록 Pub/Sub 구조로 설계
- 
![image](https://user-images.githubusercontent.com/41348473/174700781-5b061161-2584-4abd-9174-a45f5c81daed.png)

- 배송시스템이 문제발생 시 상품시스템(product)에서 발생한 주문이 누락되지않도록 Pub/Sub 구조로 설계
- 
![image](https://user-images.githubusercontent.com/41348473/174713615-351b8a36-62dd-4d15-83b5-52c0d9b9e838.png)



# CQRS Pattern 의 적용
- 설계 : 윤정호
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
  
  
  
# Request / Response (Feign Client / Sync.Async)
- 설계 : 윤정호 / 구현 : 김순호
- 판매자가 결제확인 시도 시 재고가 0이면 주문이 취소되도록 구현이 필요해짐
- PayChecked <-> qty decrease 구간에 대해 이벤트스토밍 과정에서 설계했던 Pub/Sub구조에서 Req/Res 구조로 변경
  - AS-IS
  - 
![image](https://user-images.githubusercontent.com/41348473/174713672-9e72f5de-1d0c-474b-b610-b61175da0d1f.png)
  - TO-BE
  - 
![image](https://user-images.githubusercontent.com/41348473/174713808-eb6cf9a7-9236-46b9-bcb8-e85856463656.png)
- !!!Feign Client 구현에 대한 부분 추가필요
- !!!


# Correlation / Compensation(Unique Key)
- 설계 : 윤정호
- 모든 이벤트는 아래 Attribute를 가짐 : 
  - orderId : 주문번호 식별자
  - orderStatus : 주문상태 식별자


# 구현/운영

# Gateway
# Deploy / Pipeline
# Circuit Breaker

시나리오는 pay->product 시 연결 시 payPlaced 요청이 과도한 경우 서킷 브레이커를 통해 장애 격리
- 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함
- Hystrix 를 설정: 요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 서킷 브레이커로 닫히도록 (요청을 빠르게 실패처리, 차단) 설정

```
# application.yml
feign:
  hystrix:
    enabled: true
    
hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
```

- 피호출 서비스(결제:pay) 의 임의 부하 처리 - 400 밀리에서 증감 220 밀리 범위
```
# (pay) product.java (entity)
    @PostUpdate
    public void onPostUpdate() {
      
        ...

        QtyDecreased qtyDecreased = new QtyDecreased(this);
        
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        qtyDecreased.publishAfterCommit();
    }

```

- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인 (동시 100명, 60초 동안)
```
siege -c100 -t60S -v --content-type "application/json" 'http://localhost:8081/orders POST {"productId": 1, "productName":"TV", "address":"test", "qty":1}'

HTTP/1.1 201     1.07 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.07 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.07 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.07 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.07 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.07 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.07 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders

* 요청이 과도하여 CB를 동작함 요청을 차단

HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.07 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.02 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.81 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.81 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.81 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders

* 요청을 어느정도 돌려보내고나니, 기존에 밀린 일들이 처리되었고, 회로를 닫아 요청을 다시 받기 시작

HTTP/1.1 201     0.27 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.08 secs:     248 bytes ==> POST http://localhost:8081/orders

```

# Autoscale(HPA)

- cpu 15% 초과 시 최대 10개 레플리카 scail-out 설정한다.
```
kubectl autoscale deploy pay --min=1 --max=10 --cpu-percent=15
```

- siege로 1분 동안 동시 사용자 100명으로 걸어준다.
```
siege -c100 -t60S -v http://a4f11486e96b4480180cde891451e39b-355372236.us-east-1.elb.amazonaws.com:8080/pays
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy pay -w
```

- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:
```
NAME    DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
pay    1/1     1            1           46m

pay    1/1     1            1           54m
pay    0/1     0            0           0s
pay    0/1     0            0           1s
pay    0/1     0            0           1s
pay    0/1     1            0           1s
pay    1/1     1            1           3s
pay    1/1     1            1           4m51s
pay    0/1     0            0           0s
pay    0/1     0            0           0s
pay    0/1     0            0           0s
pay    0/1     1            0           0s
pay    1/1     1            1           1s
:
```
- siege 의 로그를 확인 결과 가용성 100%로 유실이 없음을 확인하였다.
```
Transactions:                  25068 hits
Availability:                 100.00 %
Elapsed time:                  59.85 secs
Data transferred:              10.16 MB
Response time:                  0.15 secs
Transaction rate:             418.85 trans/sec
Throughput:                     0.17 MB/sec
Concurrency:                   64.21
Successful transactions:       25068
Failed transactions:               1
Longest transaction:           15.47
Shortest transaction:           0.01
```

# Self-Healing(Liveness Probe)
# Zero-Downtime Deploy(Readiness Probe)
# Config Map / Persistence Volume
# Polyglot

- delivery started 이벤트를 mysql database에 적재
```
docker-compose.yml

mysql db pull and container up

version: '3' # docker compose 버전
services:
  local-db:
    image: library/mysql:8.0
    container_name: mysql-container # 컨테이너 이름
    restart: always
    ports:
      - 3306:3306 # 로컬의 3306 포트(좌항)를 컨테이너의 3306포트(우항)로 연결
    environment:
      MYSQL_USER: eads
      MYSQL_PASSWORD: eads
      MYSQL_ROOT_PASSWORD: root
      TZ: Asia/Seoul
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --lower_case_table_names=1
      - --sort_buffer_size=256000000
    volumes:
      - ./db/mysql/data:/var/lib/mysql
      - ./db/mysql/init:/docker-entrypoint-initdb.d
```

- delivery application.yml 수정
```
spring:
  profiles: default
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQL8Dialect
    generate-ddl: true
    show-sql: true
  datasource:
    url: jdbc:mysql://localhost:3306/delivery_test
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

- 설정 후 deliveryStarted json 데이터가 mysql db에 적재될 것으로 기대하였으나
- jdbc 등 dependency 문제가 발생하여 결과를 확인하진 못함