# Kubernetes-rest-ex

쿠버네티스 내에서 REST API 통신을 실험하는 예제

- 실습 환경
    - Kubernetes v1.26.3
    - Calico CNI
    - Spring boot v3.0.6
    - jdk 17
    - Docker
    - Docker Hub
    - 우리집

## Docker Hub URL

미리 만들어둔 Spring Boot Image를 사용합니다.

- https://hub.docker.com/repository/docker/ghdcksgml1/spring-hello/general
- https://hub.docker.com/repository/docker/ghdcksgml1/spring-rest/general

## Application 구조

앱 동작은 간단합니다. Rest Application이 Hello Application을 rest 요청을 한 값을 그대로 출력하는 Application입니다.

<img width="706" alt="스크린샷 2023-05-08 오후 8 52 36" src="https://user-images.githubusercontent.com/79779676/236816967-264eccec-2d3b-4cbe-ad08-ba0a1bc5d4bb.png">

---

## Hello Application 소스 코드 (8080포트)

```kotlin
@RestController
class HelloController {

    @GetMapping("/")
    fun getHello(): String {
        return "Hello Server"
    }
}
```

## Rest Application 소스 코드 (8081포트)

``` kotlin
@RestController
class HelloRestController(
    @Value("\${rest.url}") val url: String // rest.url="http://localhost:8080/"

) {

    @GetMapping("/")
    fun getRest(): String {
        val restTemplate = RestTemplate()

        return restTemplate?.getForObject(url, String::class.java) ?: "error"
    }
}

```

### 실행결과

당연히 Local에서는 잘 돈다.

<img width="247" alt="스크린샷 2023-05-08 오후 8 58 40" src="https://user-images.githubusercontent.com/79779676/236818138-230b834b-6747-4ee7-a555-825e0e90e99b.png">

---

### 이제 쿠버네티스에 배포를 해보자.

```yaml
apiVersion: apps/v1 
kind: Deployment # hello Application Deployment
metadata:
  labels:
    app: webserver
  name: webserver-hello
spec:
  replicas: 3
  selector:
    matchLabels:
      app: webserver
  template:
    metadata:
      labels:
        app: webserver
    spec:
      containers:
      - image: ghdcksgml1/spring-hello
        name: webserver-hello
        ports:
        - containerPort: 8080
---
apiVersion: apps/v1
kind: Deployment # Rest Application Deployment
metadata:
  labels:
    app: webserver
  name: webserver-rest
spec:
  replicas: 3
  selector:
    matchLabels:
      app: webserver
  template:
    metadata:
      labels:
        app: webserver
    spec:
      containers:
      - image: ghdcksgml1/spring-rest
        name: webserver-rest
        ports:
        - containerPort: 8081
---
apiVersion: v1
kind: Service # LoadBalancer
metadata:
  labels:
    app: webserver
  name: webserver-svc
spec:
  ports:
  - port: 8080
    protocol: TCP
    targetPort: 8080
    name: webserver-hello
  - port: 8081
    protocol: TCP
    targetPort: 8081
    name: webserver-rest
  selector:
    app: webserver
  type: LoadBalancer
```

<br/><br/>

### 쿠버네티스 아키텍처는 아래와 같다.

<img width="877" alt="스크린샷 2023-05-08 오후 8 38 54" src="https://user-images.githubusercontent.com/79779676/236818668-67bbe8a5-7bf3-4795-843c-b642d83543b6.png">


---

### 배포해보면...

![image](https://user-images.githubusercontent.com/79779676/236820511-e17e0e7e-d2aa-4489-a592-608f0b1009c3.png)

<img width="249" alt="스크린샷 2023-05-08 오후 9 10 25" src="https://user-images.githubusercontent.com/79779676/236820618-5659ac7b-af87-4bbe-b9dc-ad7e5e3e070c.png">

![image](https://user-images.githubusercontent.com/79779676/236821193-cf074ae5-afe8-41cc-86f1-e69ffe270ef7.png)

### 이유는?

현재 'http://localhost:8080/'을 호출하고 있습니다. 
하지만, 아키텍처를 보면 각각의 deployment는 분리된 환경이기 때문에 Hello Deployment의 8080포트로 접근할 수 없는 것이지요.

### 그럼 접근하는 방법은?

- Service IP로 접근하기

```bash
$kubectl get svc
```

위 명령어를 치면 service의 cluster-ip가 존재합니다. 이걸 localhost 대신 적어주는 것이죠.

<img width="417" alt="스크린샷 2023-05-08 오후 9 19 13" src="https://user-images.githubusercontent.com/79779676/236822146-5a9f2a22-f4c0-4681-aaf0-7c74b821ec53.png">

URI를 'http://10.100.36.230:8080'으로 요청하면 됩니다.

하지만, service가 삭제되면 cluster-IP가 바뀔텐데 어떻게 적용하지? 라는 의문이 생깁니다.

맞습니다. service ip대신 service-name을 입력할 수 있습니다.

'http://webserver-svc:8080/'로 요청하면 되겠죠? 이렇게되면, 서비스가 삭제되고 다시 생성되어도 rest uri를 바꾸지 않아도 됩니다.

<img width="247" alt="스크린샷 2023-05-08 오후 8 58 40" src="https://user-images.githubusercontent.com/79779676/236823144-bb3e67cb-5535-4317-8d41-181d935f8ba4.png">

![image](https://user-images.githubusercontent.com/79779676/236823212-ceea414a-5d3a-4f70-8933-d0c27355595d.png)


