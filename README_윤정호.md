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



Gateway의 resources/application.yaml에 각 서비스 uri 지정하여 pod에 service명으로 접근할 수 있도록 설정 
```
spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: order
          uri: http://order:8080
          predicates:
            - Path=/orders/** 
        - id: pay
          uri: http://pay:8080
          predicates:
            - Path=/pays/** 
        - id: delivery
          uri: http://delivery:8080
          predicates:
            - Path=/deliveries/** 
        - id: product
          uri: http://product:8080
          predicates:
            - Path=/products/** 
        - id: frontend
          uri: http://frontend:8080
          predicates:
            - Path=/**

```

게이트웨이는 Service Type LoadBalancer로 외부에 노출하고 나머지는 일반 Service로 오픈
```
kubectl expose deploy delivery --port=8080
kubectl expose deploy order --port=8080
kubectl expose deploy pay --port=8080
kubectl expose deploy product --port=8080
kubectl expose deploy delivery --port=8080
kubectl expose deploy gateway --type=LoadBalancer --port=8080
```

서비스 expose 결과
```
gitpod /workspace/hw3-msa-capstone-project (main) $ kubectl get service
NAME         TYPE           CLUSTER-IP      EXTERNAL-IP                                                              PORT(S)          AGE
gateway      LoadBalancer   10.100.67.110   a4f11486e96b4480180cde891451e39b-355372236.us-east-1.elb.amazonaws.com   8080:30618/TCP   4h18m
kubernetes   ClusterIP      10.100.0.1      <none>                                                                   443/TCP          4h27m
order        ClusterIP      10.100.225.68   <none>                                                                   8080/TCP         3h52m
pay          ClusterIP      10.100.152.65   <none>                                                                   8080/TCP         3h52m
product      ClusterIP      10.100.95.161   <none>                                                                   8080/TCP         3h56m
```

브라우저에서 Gateway의 External-ip로 procuct 정보 확인결과
- 
![image](https://user-images.githubusercontent.com/41348473/174948774-e9abad5a-75bc-49c7-a51a-3d6679f741a4.png)


# Deploy / Pipeline
# Circuit Breaker
- 담당 : 김순호
# Autoscale(HPA)
- 담당 : 김순호

# Self-Healing(Liveness Probe)
- 담당 : 윤정호
- 주문 마이크로시스템의 Self-Healing을 위한 Liveness Probe 적용
  - 기본값 대신 명시적 Liveness Probe를 사용
  - Memory Leak 테스트 코드 실행을 용이하게 하기 위해 resource를 300MiB로 낮춤

Liveness Probe 적용 후 order의 deployment.yaml
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order
  labels:
    app: order
spec:
  replicas: 4
  selector:
    matchLabels:
      app: order
  template:
    metadata:
      labels:
        app: order
    spec:
      containers:
        - name: order
          image: 004814395703.dkr.ecr.us-east-1.amazonaws.com/order:memleak
          resources:
            limits:
              memory: "300Mi"
            requests:
              memory: "300Mi"
          ports:
            - containerPort: 8080
          livenessProbe:
            tcpSocket:
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 20
```

OrderController.java에 어설픈 memleak 유발 코드 삽입
```
@RestController
@RequestMapping(value = "/orders")
public class OrderController {

    @GetMapping(value = "/memleak")
    public String Memleak() {
        String[] S = new String[100000];
        int i = 0 , j = 0;

        for (i = 0; i < 1000000; i++) {
            S[i] = new String();
            for (j = 0; j < 1000000000; j++) {
                S[i] += "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            }
        }
        return "Memleak Test";
    }
}
```

siege로 동접자 100명 시나리오로 접속시도로 memleak 유발
```
root@siege2:/# siege -c100 -t60S -v http://order:8080/orders/memleak
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: read error Connection reset by peer sock.c:539: Connection reset by peer
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: read error Connection reset by peer sock.c:539: Connection reset by peer
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
siege aborted due to excessive socket failure; you
can change the failure threshold in $HOME/.siegerc

Transactions:                      0 hits
Availability:                   0.00 %
Elapsed time:                  23.68 secs
Data transferred:               0.00 MB
Response time:                  0.00 secs
Transaction rate:               0.00 trans/sec
Throughput:                     0.00 MB/sec
Concurrency:                    0.00
Successful transactions:           0
Failed transactions:            1123
Longest transaction:            0.00
Shortest transaction:           0.00
```

watch kubectl get pods 결과 OOM발생으로 인한 RESTART COUNT 증가를 관찰할 수 있음
```
Every 2.0s: kubectl get pods                          ohgnuz-hw3msacapstonepr-p696w009c6n: Wed Jun 22 04:55:39 2022

NAME                       READY   STATUS      RESTARTS   AGE
frontend-6b66bc86d-zs4r7   1/1     Running     0          3h37m
gateway-86456789b-rtl7f    1/1     Running     0          4h11m
order-5648f5bd68-2bll9     1/1     Running     2          5m37s
order-5648f5bd68-c86j9     1/1     Running     2          5m39s
order-5648f5bd68-kt47b     0/1     OOMKilled   1          5m39s
order-5648f5bd68-w6jws     0/1     OOMKilled   1          5m36s
pay-59b788c64-4pjr5        1/1     Running     0          3h5m
product-6bbf69557-2sgtr    1/1     Running     0          3h43m
product-6bbf69557-gp24c    1/1     Running     0          3h43m
product-6bbf69557-l87pn    1/1     Running     0          3h43m
siege1                     1/1     Running     0          3h24m
siege2                     1/1     Running     0          3h24m

Every 2.0s: kubectl get pods                          ohgnuz-hw3msacapstonepr-p696w009c6n: Wed Jun 22 04:59:26 2022

NAME                       READY   STATUS    RESTARTS   AGE
frontend-6b66bc86d-zs4r7   1/1     Running   0          3h41m
gateway-86456789b-rtl7f    1/1     Running   0          4h15m
order-5648f5bd68-2bll9     1/1     Running   3          9m24s
order-5648f5bd68-c86j9     1/1     Running   3          9m26s
order-5648f5bd68-kt47b     1/1     Running   2          9m26s
order-5648f5bd68-w6jws     1/1     Running   2          9m23s
pay-59b788c64-4pjr5        1/1     Running   0          3h9m
product-6bbf69557-2sgtr    1/1     Running   0          3h46m
product-6bbf69557-gp24c    1/1     Running   0          3h47m
product-6bbf69557-l87pn    1/1     Running   0          3h46m
siege1                     1/1     Running   0          3h28m
siege2                     1/1     Running   0          3h28m
```


# Zero-Downtime Deploy(Readiness Probe)
- 담당 : 윤정호
- 주문 마이크로시스템의 무정지배포를 위한 Readiness Probe 적용


Readiniss Probe 적용 전 order의 deployment.yaml
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order
  labels:
    app: order
spec:
  replicas: 2
  selector:
    matchLabels:
      app: order
  template:
    metadata:
      labels:
        app: order
    spec:
      containers:
        - name: order
          image: 004814395703.dkr.ecr.us-east-1.amazonaws.com/order:latest
          ports:
            - containerPort: 8080
```

Readiness Probe 적용 전 siege 실행 중 deploy 시 결과
```
kubectl exec -it siege2 -- /bin/bash
root@siege2:/# siege -c1 -t20S -v http://order:8080/orders --delay=1S
** SIEGE 4.0.4
** Preparing 1 concurrent users for battle.
The server is now under siege...
HTTP/1.1 200     0.00 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.00 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.00 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.00 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
[error] socket: unable to connect sock.c:249: Connection refused
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
[error] socket: unable to connect sock.c:249: Connection refused
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.00 secs:     301 bytes ==> GET  /orders
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
[error] socket: unable to connect sock.c:249: Connection refused
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders

Lifting the server siege...
Transactions:                     23 hits
Availability:                  74.19 %
Elapsed time:                  19.51 secs
Data transferred:               0.01 MB
Response time:                  0.01 secs
Transaction rate:               1.18 trans/sec
Throughput:                     0.00 MB/sec
Concurrency:                    0.01
Successful transactions:          23
Failed transactions:               8
Longest transaction:            0.02
Shortest transaction:           0.00
```

Readiniss Probe 적용 후 order의 deployment.yaml
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order
  labels:
    app: order
spec:
  replicas: 2
  selector:
    matchLabels:
      app: order
  template:
    metadata:
      labels:
        app: order
    spec:
      containers:
        - name: order
          image: 004814395703.dkr.ecr.us-east-1.amazonaws.com/order:latest
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/orders'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
```

Readiness Probe 적용 후 siege를 걸고 order의 replica를 2개 늘림
initial Delay 후에도 서비스가 살 때 까지 더 기다리는 모습
```
gitpod /workspace/hw3-msa-capstone-project/capstone-domain-02/order (main) $ kubectl get pods
NAME                       READY   STATUS    RESTARTS   AGE
order-766d76c6b5-h6x79     0/1     Running   0          13s
order-766d76c6b5-k82cf     1/1     Running   0          2m50s
order-766d76c6b5-lzhlt     1/1     Running   0          2m50s
order-766d76c6b5-rzzpr     0/1     Running   0          13s
siege2                     1/1     Running   0          41m
```

Readiness Probe 적용 후 siege 실행 중 deploy 시 결과
```
kubectl exec -it siege2 -- /bin/bash
root@siege2:/# siege -c1 -t20S -v http://order:8080/orders --delay=1S
** SIEGE 4.0.4
** Preparing 1 concurrent users for battle.
The server is now under siege...
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.01 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.06 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.03 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.05 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.04 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.04 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.06 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.03 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.03 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.04 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.03 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.04 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.05 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.06 secs:     301 bytes ==> GET  /orders
HTTP/1.1 200     0.02 secs:     301 bytes ==> GET  /orders

Lifting the server siege...
Transactions:                     30 hits
Availability:                 100.00 %
Elapsed time:                  19.51 secs
Data transferred:               0.01 MB
Response time:                  0.03 secs
Transaction rate:               1.54 trans/sec
Throughput:                     0.00 MB/sec
Concurrency:                    0.04
Successful transactions:          30
Failed transactions:               0
Longest transaction:            0.06
Shortest transaction:           0.01
```



# Config Map / Persistence Volume
# Polyglot
