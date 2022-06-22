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
- 설계 / 구현 : 윤정호
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

- 각 단계에 맞게 orderStatus가 변경되는지에 관한 테스트 수행
- orderPlaced 이벤트의 바로 다음인 payPlaced 상태로 넘어가기 전 상태와 비교

order 프로세스만 띄우고 주문 발생
```
gitpod /workspace/hw3-msa-capstone-project (main) $ http http://localhost:8081/orders orderId=1 productId=1 productName="TV" price=100 qty=3
HTTP/1.1 201 
Connection: keep-alive
Content-Type: application/json
Date: Wed, 22 Jun 2022 06:26:33 GMT
Keep-Alive: timeout=60
Location: http://localhost:8081/orders/1
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/1"
        },
        "self": {
            "href": "http://localhost:8081/orders/1"
        }
    },
    "address": null,
    "productId": 1,
    "productName": "TV",
    "qty": 3
}
```

주문상태 조회 시 "주문접수" 상태로 확인됨
```
gitpod /workspace/hw3-msa-capstone-project (main) $ http http://localhost:8085/orderStatuses
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/hal+json
Date: Wed, 22 Jun 2022 06:27:12 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{
    "_embedded": {
        "orderStatuses": [
            {
                "_links": {
                    "orderStatus": {
                        "href": "http://localhost:8085/orderStatuses/1"
                    },
                    "self": {
                        "href": "http://localhost:8085/orderStatuses/1"
                    }
                },
                "address": null,
                "orderId": "1",
                "orderStatus": "주문접수",
                "price": "null",
                "productId": "1",
                "productName": "TV",
                "qty": 3
            }
        ]
    }
```

pay 서비스 기동 전 kafka consumer
```
gitpod /workspace/hw3-msa-capstone-project/kafka (main) $ docker exec -it kafka-kafka-1 /bin/kafka-console-consumer --bootstrap-server http://localhost:9092 --topic hwmsacapstoneteam --from-beginning
{"eventType":"OrderPlaced","timestamp":1655879193504,"id":1,"address":null,"qty":3,"productName":"TV","productId":1}
```


pay 서비스 기동 후 kafka consumer
```
gitpod /workspace/hw3-msa-capstone-project/kafka (main) $ docker exec -it kafka-kafka-1 /bin/kafka-console-consumer --bootstrap-server http://localhost:9092 --topic hwmsacapstoneteam --from-beginning
{"eventType":"OrderPlaced","timestamp":1655879193504,"id":1,"address":null,"qty":3,"productName":"TV","productId":1}
{"eventType":"PayPlaced","timestamp":1655879526701,"id":1,"orderId":1,"productId":1,"productName":null,"address":null,"qty":3,"price":null}
```

pay 서비스 기동 후 orderStatus 변화 "주문접수" -> "입금확인대기"
```
gitpod /workspace/hw3-msa-capstone-project (main) $ http http://localhost:8085/orderStatuses
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/hal+json
Date: Wed, 22 Jun 2022 06:37:33 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{
    "_embedded": {
        "orderStatuses": [
            {
                "_links": {
                    "orderStatus": {
                        "href": "http://localhost:8085/orderStatuses/1"
                    },
                    "self": {
                        "href": "http://localhost:8085/orderStatuses/1"
                    }
                },
                "address": null,
                "orderId": "1",
                "orderStatus": "입금확인대기",
                "price": "null",
                "productId": "1",
                "productName": "TV",
                "qty": 3
            }
        ]
    },
```

  
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

product에 상품추가
```
gitpod /workspace/hw3-msa-capstone-project (main) $ http http://localhost:8084/products productId=1 name="TV" price=100 qty=10000
HTTP/1.1 201 
Connection: keep-alive
Content-Type: application/json
Date: Wed, 22 Jun 2022 06:45:37 GMT
Keep-Alive: timeout=60
Location: http://localhost:8084/products/1
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{
    "_links": {
        "product": {
            "href": "http://localhost:8084/products/1"
        },
        "self": {
            "href": "http://localhost:8084/products/1"
        }
    },
    "name": "TV",
    "orderId": null,
    "price": 100,
    "qty": 10000
}
```

order 호출 시 pay를 거쳐 재고감소 확인
```
gitpod /workspace/hw3-msa-capstone-project (main) $ http http://localhost:8081/orders orderId=2 productId=1 productName="TV" price=100 qty=3
HTTP/1.1 201 
Connection: keep-alive
Content-Type: application/json
Date: Wed, 22 Jun 2022 06:47:11 GMT
Keep-Alive: timeout=60
Location: http://localhost:8081/orders/2
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/2"
        },
        "self": {
            "href": "http://localhost:8081/orders/2"
        }
    },
    "address": null,
    "productId": 1,
    "productName": "TV",
    "qty": 3
}
```

kafka event
```
gitpod /workspace/hw3-msa-capstone-project/kafka (main) $ docker exec -it kafka-kafka-1 /bin/kafka-console-consumer --bootstrap-server http://localhost:9092 --topic hwmsacapstoneteam --from-beginning
{"eventType":"OrderPlaced","timestamp":1655880431281,"id":2,"address":null,"qty":3,"productName":"TV","productId":1}
{"eventType":"PayPlaced","timestamp":1655880431423,"id":1,"orderId":2,"productId":1,"productName":null,"address":null,"qty":3,"price":null}
{"eventType":"PayChecked","timestamp":1655880431424,"id":1,"orderId":2,"productId":1,"productName":null,"address":null,"qty":3,"price":null}
{"eventType":"QtyDecreased","timestamp":1655880431487,"id":1,"orderId":2,"name":"TV","qty":9997,"price":100}
```

product 감소
```
gitpod /workspace/hw3-msa-capstone-project (main) $ http http://localhost:8084/products
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/hal+json
Date: Wed, 22 Jun 2022 06:47:16 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{
    "_embedded": {
        "products": [
            {
                "_links": {
                    "product": {
                        "href": "http://localhost:8084/products/1"
                    },
                    "self": {
                        "href": "http://localhost:8084/products/1"
                    }
                },
                "name": "TV",
                "orderId": 2,
                "price": 100,
                "qty": 9997
            }
        ]
    },
    "_links": {
        "profile": {
            "href": "http://localhost:8084/profile/products"
        },
        "self": {
            "href": "http://localhost:8084/products"
        }
    },
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1
    }
}
```

# 구현/운영

# Gateway

- 서비스의 진입점 통일을 위한 Gateway구현

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
![image](https://user-images.githubusercontent.com/41348473/174949994-a93a5211-e6d1-4611-8337-2adc3ad1b11d.png)

# Deploy / Pipeline
# Circuit Breaker
- 담당 : 김순호

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
- 담당 : 김순호

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
