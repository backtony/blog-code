# AWS - nGrinder 구축하기


# 1. nGrinder 란?
---
nGrinder는 스크립트 생성, 테스트 실행, 모니터링 및 결과 보고서 생성기를 동시에 실행할 수 있는 스트레스 테스트용 플랫폼입니다.  
오픈 소스 nGrinder는 불편함을 없애고 통합 환경을 제공하여 스트레스 테스트를 수행할 수 있는 쉬운 방법을 제공합니다.  
<br>

# 2. 시스템 아키텍처
---
![그림1](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-1.PNG?raw=true)  

nGrinder는 Controller, Agent, Monitor로 구성되어 있습니다.  
+ Controller 
    - 성능테스트를 위한 웹 인터페이스를 제공합니다.
    - 테스트 프로세스를 조정합니다.
    - 테스트 통계를 수집하고 표시하여 줍니다. 테스트 결과를 저장하고 보여줍니다.
    - 사용자가 테스트를 위한 Script를 작성하고 수정할 수가 있습니다.
+ Agent 
    - 성능 대상 시스템에 부하를 가하는 프로세스 및 스레드를 실행합니다.
+ Monitor 
    - 성능테스트의 대상이 되는 서버에 설치하여서 테스트가 수행하는 동안 대상 서버의 상황 정보를 Controller로 전달하여서 Controller에서 웹 인터페이스로 모니터링 할수 있도록 하여줍니다.

간단하게 설명하자면, Controller가 각각의 Agent에게 Target 서버로 요청을 보내라는 명령을 보내면 Agent에서는 그 동작을 수행하게 되는 구조입니다.  
<br>

![그림2](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-2.PNG?raw=true)  
nGrinder는 일반적으로 Controller 1개에 Agent를 3~6개로 구성합니다.  
Controller는 최소 t2.large. Agent는 개인적으로 최소 t2.2xlarge 를 권장사양으로 합니다.(테스트 하면서 필요하다면 언제든지 변경 가능)


# 3. EC2 세팅
---
nGrinder 네트워크 통신 포트 정보를 정리하면 다음과 같습니다.  
>에이전트:모든 포트 ==> 컨트롤러 : 16001  
>에이전트:모든 포트 ==> 컨트롤러 : 12000 ~ 12000 + 동시 테스트 허용할 테스트 개수  
>컨트롤러:모든 포트 ==> 모니터:13243  
>컨트롤러 ==> 일반 유저 : 웹 서버 설정 방식에 따르나, 디폴트는 8080 입니다.  

==> 는 단방향 통신을 의미합니다.  
16001 포트는 테스트를 하지 않는 에이전트가 컨트롤러에게 할일이 없으니 테스트 가능이란 메시지를 알려주는 포트입니다.  
1200x는 테스트 실행, 종료와 같은 컨트롤러명령어와 에이전트별 테스트 실행 통계를 수집하는 포트입니다.  
<br>

즉, EC2 보안그룹에서 위 포트를 전부 열어주어야 합니다.  
docker을 이용해서 Controller을 설치할 예정이므로 8080을 제외하고 80번을 열도록 하겠습니다.  
Agent에서는 Controller의 IP를 명시해주어야 하기 때문에 nGrinder Controller EC2에는 탄력적 IP를 하나 할당해주는 것이 좋습니다.  
위 과정은 기본적인 내용이므로 생략하고 바로 설치과정으로 들어가겠습니다.  

<br>

## Controller EC2
사용하는 EC2는 모두 Amazon linux2를 사용하도록 하겠습니다.

### 도커 설치
```
도커 설치
sudo yum -y install docker

도커 설치 작업이 잘 되었는지 버전 확인
docker -v

도커 시작
sudo service docker start

도커 그룹에 사용자 추가 -> docker가 그룹명, ec2-user가 사용자명
sudo usermod -aG docker ec2-user 

// 참고 // 
docker ps 시 Permission Denied가 발생할 경우 아래 명령어 입력할 것
sudo chmod 666 /var/run/docker.sock
```

### Controller 실행
```sh
docker pull ngrinder/controller

docker run -d -v ~/ngrinder-controller:/opt/ngrinder-controller --name controller -p 80:80 -p 16001:16001 -p 12000-12009:12000-12009 ngrinder/controller
```

### 로그인
![그림3](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-3.PNG?raw=true)  
Controller EC2 IP 로 접속하시면 로그인 화면이 나옵니다.  
기본 아이디 패스워드는 모두 admin 입니다.  

<br>

## Agent EC2
Agent EC2로 넘어와서 작업합니다.  
도커는 위에서와 같이 설치하면 됩니다.
```sh
# 이미지 다운
docker pull ngrinder/agent

# 실행
# agent는 controller_ip:controller_webport 부분을 옵션 argument로 전달해야 합니다.
docker run -v ~/ngrinder-agent:/opt/ngrinder-agent -d ngrinder/agent controller_ip:controller_port

# 예시
# 컨트롤러에서 웹포트를 80번으로 열었으므로 80으로 적어줍니다.
docker run -v ~/ngrinder-agent:/opt/ngrinder-agent -d --name agent ngrinder/agent 3.34.50.222:80

### 만약 에러가 발생할 경우 ###
sudo vi  ~/ngrinder-agent/.ngrinder-agent/agent.conf
# 아래 내용 수정
agent.controller_host=controller IP 입력
agent.controller_port=16001
```

![그림5](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-5.PNG?raw=true)  
docker logs -f agent 입력시 위와 같다면 정상입니다.

<br>

## 적용 확인
![그림6](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-6.PNG?raw=true)  
다시 nGrinder 화면으로 돌아와서 상단에 Agent Managment를 클릭합니다.
<br><br>

![그림7](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-7.PNG?raw=true)  
이렇게 Agent가 잘 나타나면 성공입니다.  
이와 같은 방법으로 필요한 Agent 숫자만큼 반복해서 Agent를 생성하고 확인하면 됩니다.

## 많은 스레드의 실행이 필요한 경우
Linux에서 에이전트를 실행하는 경우 많은 스레드를 실행하도록 ulimit를 구성해야 할 수 있습니다. 다음을 확인해야 합니다.
```
> ulimit -a

core file size          (blocks, -c) 0

data seg size           (kbytes, -d) unlimited

scheduling priority             (-e) 0

file size               (blocks, -f) unlimited

pending signals                 (-i) 30676

max locked memory       (kbytes, -l) 64

max memory size         (kbytes, -m) unlimited


open files                      (-n) 16000

pipe size            (512 bytes, -p) 8

POSIX message queues     (bytes, -q) 819200

real-time priority              (-r) 0

stack size              (kbytes, -s) 10240

cpu time               (seconds, -t) unlimited

max user processes              (-u) 32768

virtual memory          (kbytes, -v) unlimited

file locks                      (-x) unlimited
```
"ulimit -a"를 실행할 때 컴퓨터에 작은 "max user processes" 및 "open files" 항목이 있는 경우 최소 10000개 이상으로 만들어야 합니다. 루트 계정에서 /etc/security/limits.conf 파일을 열고 다음을 추가해줘야 합니다.
```sh
# conf 수정
sudo vi /etc/security/limits.conf

ec2-user        soft    nproc           32768
ec2-user        hard    nproc           32768
root            soft    nproc           32768
root            hard    nproc           32768
ec2-user        soft    nofile          16000
ec2-user        hard    nofile          16000
root            soft    nofile          16000
root            hard    nofile          16000
```
sudo reboot 를 이용해서 재부팅 해주고 재실행해주면 됩니다.

<br>

# 4. 스크립트 작성
---
![그림8](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-8.PNG?raw=true)  
Controller 페이지에 가서 상단에 Script를 클릭합니다. 그리고 Create를 클릭합니다.  
<br><br>

![그림9](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-9.PNG?raw=true)  
스크립트 이름과 테스트할 URL을 입력해줍니다. Show Advanced Configuration을 클릭하면 추가적으로 세팅이 가능합니다.  
<Br><br>

create하면 아래와 같이 기본적인 스크립트가 생성됩니다.
```groovy
import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse
import org.ngrinder.http.cookie.Cookie
import org.ngrinder.http.cookie.CookieManager

/**
* A simple example using the HTTP plugin that shows the retrieval of a single page via HTTP.
*
* This script is automatically generated by ngrinder.
*
* @author admin
*/
@RunWith(GrinderRunner)
class TestRunner {

	public static GTest test
	public static HTTPRequest request
	public static Map<String, String> headers = [:]
	public static Map<String, Object> params = [:]
	public static List<Cookie> cookies = []

	@BeforeProcess
	public static void beforeProcess() {
		HTTPRequestControl.setConnectionTimeout(300000)
		test = new GTest(1, "gjgs-test.com")
		request = new HTTPRequest()
		grinder.logger.info("before process.")
	}

	@BeforeThread
	public void beforeThread() {
		test.record(this, "test")
		grinder.statistics.delayReports = true
		grinder.logger.info("before thread.")
	}

	@Before
	public void before() {
		request.setHeaders(headers)
		CookieManager.addCookies(cookies)
		grinder.logger.info("before. init headers and cookies")
	}

	@Test
	public void test() {
		HTTPResponse response = request.GET("https://gjgs-test.com/health", params)

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}
}

```
하나하나씩 살펴보겠습니다.

+ @RunWith(GrinderRunner)
    - @Runwith는 스프링의 테스트 컨텍스트 프레임워크의 JUnit 확장기능을 지정하는 애노테이션입니다.
    - Groovy 스크립트 내의 클래스에는 JUnit을 nGrinder에 맞게 처리할 수 있도록 GrinderRunner을 사용해줍니다.
    - JUnit은 각각의 테스트가 서로 영향을 주지 않고 독릭접으로 실행하는 것을 기본으로 하기에 각 테스트 클래스마다 매번 오브젝트를 생성합니다. 그러므로 각 테스트 클래스를 지정한 ApplicationContext도 매번 새로 생성되는 상황이 발생합니다. 이를 방지하기 위해 @RunWith 애노테이션은 각 테스트 별로 오브젝트가 생성되더라도 싱글톤의 ApplicationContext를 보장하는 역할을 합니다.

Annotation|Description|Applied to|Usage
---|---|---|---
@BeforeProcess|프로세스가 호출되기 전에 실행되어야 하는 동작 정의|static method|- 스레드가 공유하는 리소스 파일을 로드합니다.<Br> - GTest를 정의하고 기록 방법을 사용하여 테스트 대상을 계측합니다.
@AfterProcess|프로세스가 종료된 후 실행되어야 하는 동작 정의|static method|리소스파일 닫기
@BeforeThread|각 스레드가 실행되기 전에 실행되어야 하는 동작을 정의합니다.|member method|- 테스트 대상 시스템 로그인<br> - 쓰레드 별 쿠키 핸들러 설정
@Before|각각의 @Test 메서드가 실행되기 전에 실행해야 하는 동작 정의|member method|- 테스트 대상 시스템 로그아웃
@After|각각의 @Test 메서드가 종료된 이후 실행해야 하는 동작|member method|- 거의 사용하지 않음
@Test|테스트 동작 정의|member method|Test body

<Br>

## @BeforeProcess
```groovy
public static GTest test
public static HTTPRequest request

@BeforeProcess
public static void beforeProcess() {
    HTTPRequestControl.setConnectionTimeout(300000)
    test = new GTest(1, "gjgs-test.com")
    request = new HTTPRequest()
    grinder.logger.info("before process.")
}
```
프로세스가 생성 전에 호출되어 모든 쓰레드가 공유할 데이터를 정의하기 좋은 곳입니다.  
+ test = new GTest(1, "test.com")
    - 테스트 수집을 위한 GTest 인스턴스 생성합니다.
    - 파라미터는 해당 인스턴스를 구분하기 위한 것이라고 보면 됩니다.
    - 나중에 결과 로그를 보면 어디에 해당하는지 확인할 수 있습니다.
+ request = new HTTPRequest()
    - HTTPRequest 객체인 request 인스턴스를 생성합니다.

## @BeforeThread
```groovy
@BeforeThread
public void beforeThread() {
    test.record(this, "test")
    grinder.statistics.delayReports = true
    grinder.logger.info("before thread.")
}
```
쓰레드가 시작되기 전 작업을 정의하는 곳입니다. 주로 타겟 시스템에 로그인 하거나 쿠키 핸들링 등을 지정합니다.  
+ test.record(this, "test")
    - 앞서 생성한 GTest 인스턴스에게 test 메서드를 호출할 때마다 TPS를 증가시킵니다.
    - 테스트하는 대상은 전체 코드의 하단에 있는 @Test 애노테이션이 붙은 test 메서드이므로 메서드명을 명시해줍니다.
+ grinder.statistics.delayReports = true
    - 일반적으로 테스트 결과는 자동으로 보고됩니다.
    - 테스트를 반환할 때, 통계를 변경하려면 true로 설정하고 테스트를 수행하기 전에 보고를 지연하도록 세팅해줍니다.
    - 현재 작업자 스레드에게 영향을 줍니다.

## @Before
```groovy
@Before
public void before() {
    request.setHeaders(headers)
    CookieManager.addCookies(cookies)
    grinder.logger.info("before. init headers and cookies")
}
```
각각의 @Test 메서드가 수행된 후 처리할 동작을 정의하는 곳입니다.  
헤더와 쿠키를 세팅해줍니다.

## @Test
```groovy
@Test
public void test() {
    HTTPResponse response = request.GET("https://gjgs-test.com/health", params)

    if (response.statusCode == 301 || response.statusCode == 302) {
        grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
    } else {
        assertThat(response.statusCode, is(200))
    }
}
```
테스트 동작을 정의하는 곳입니다.  
assert를 이용해서 정의해줍니다.  

## 스크립트 실행 플로우
![그림10](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-10.PNG?raw=true)  

<Br>

# 5. 테스트 진행
---
![그림11](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-11.PNG?raw=true)  
상단의 Performance Test를 클릭하고 우측에 Create Test를 선택합니다.  
<Br><br>

![그림12](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-12.PNG?raw=true)  
1. 테스트 이름을 적어줍니다.
2. 사용할 Agent 개수를 명시합니다.
3. Agent 당 가상 user 정의합니다. 되도록이면 Thread를 많이 지정하는 것이 좋습니다.
4. 테스트에 사용할 script 선택합니다.
5. 테스트 시간 명시합니다.
6. 천천히 부하를 늘려가면서 진행하는 방식입니다.
7. Thread를 선택해줍니다.

<br><br>

![그림13](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-13.PNG?raw=true)  
테스트 결과 화면입니다.  
+ Total Vusers
    - 총 가상 유저 수
+ TPS
    - 시간당 처리량
    - 1초에 처리하는 단위 작업수로 1초에 처리하는 HTTP 요청 수로 해석할 수 있습니다.
+ Peak TPS
    - 최고 TPS
+ Mean Test Time(MTT)
    - 평균 Test 시간
+ Executed Tests
    - 총 테스트 횟수
+ Successful Tests
    - 성공한 테스트
+ Errors
    - 실패한 테스트
+ Runtime
    - 테스트 실행 시간

아래 로그를 다운로드해서 확인해 봅시다.
<Br>

![그림14](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-14.PNG?raw=true)  
앞서 GTest 인스턴스를 생성했을때, 1과 gjgs-test.com를 인자로 넣었습니다.  
이렇게 로그에서 식별자로 확인할 수 있습니다.  
<br>

# 6. IOException 에러 해결
---
![그림15](https://github.com/backtony/blog-code/blob/master/aws/img/aws/2/10-15.PNG?raw=true)  
Spring boot에서 간단한 health Check의 경우 별 문제없이 테스트가 성공했지만, 시간이 조금 걸리는 요청의 경우 IOException이 발생했습니다.  
이는 클라이언트에서 서버로 요청을 한 뒤 응답을 받기전에 요청이 중단되었을 경우 발생되는 예외였습니다.  
즉, 서버의 문제가 아니고 클라이언트에서 요청을 보내고 응답을 받기 전에 요청을 중단한 것입니다.  
이를 해결하기 위해서는 Script를 수정해야 합니다.
```groovy
import net.grinder.plugin.http.HTTPPluginControl

@BeforeProcess
public static void beforeProcess() {		
    HTTPPluginControl.getConnectionDefaults().timeout = 6000
    
    test = new GTest(1, "gjgs-test.com")
    request = new HTTPRequest()
}
```
기존 코드에 있던 HTTPRequestControl.setConnectionTimeout(300000)를 제거하고 HTTPPluginControl.getConnectionDefaults().timeout = 6000 를 추가하여 connectionTimeout 시간을 조정해주면 해결할 수 있습니다.  
<Br>

# 7. 추가적으로 작성한 스크립트
---
## 매번 다른 요청
프로젝트에서 회원가입을 테스트해야 했는데, 회원가입 과정에서는 nickname, id, phone 이 중복되면 에러를 뱉도록 설계했습니다.
```groovy
import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith


import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse
import org.ngrinder.http.cookie.Cookie
import org.ngrinder.http.cookie.CookieManager

import org.json.JSONObject;


/**
* A simple example using the HTTP plugin that shows the retrieval of a single page via HTTP.
*
* This script is automatically generated by ngrinder.
*
* @author admin
*/
@RunWith(GrinderRunner)
class TestRunner {

	public static GTest test
	public static HTTPRequest request
	public static Map<String, String> headers = [:]
	public static List<Cookie> cookies = []
	
	public static String imageFileUrl = "http://k.kakaocdn.net"
	public static String name = "최준성"
	public static int age = 25
	public static String sex = "M"
	public static Long zoneId = 1L
	public static Long[] categoryIdList = [1,2,3]
	public static String fcmToken = "123"

	@BeforeProcess
	public static void beforeProcess() {
		HTTPRequestControl.setConnectionTimeout(300000)
		test = new GTest(1, "gjgs-test.com")
		request = new HTTPRequest()

		// Set header data
		headers.put("Content-Type", "application/json")
		grinder.logger.info("before process.")
	}

	@BeforeThread
	public void beforeThread() {
		test.record(this, "test")
		grinder.statistics.delayReports = true
		grinder.logger.info("before thread.")
	}

	@Before
	public void before() {
		request.setHeaders(headers)
		CookieManager.addCookies(cookies)
	
		grinder.logger.info("before. init headers and cookies")
	}

	@Test
	public void test() {
		Long min = 1000000000L
		Long max = 99999999999L
		Long phone = ((Math.random() * (max - min)) + min);
		
		min = 10L
		max = 9223372036854775800L
		Long nickname = ((Math.random() * (max - min)) + min)
		
		Long id = ((Math.random() * (max - min)) + min)
		
		JSONObject testJson= new JSONObject()
		testJson.put("id",id)
		testJson.put("imageFileUrl",imageFileUrl)
		testJson.put("name",name)
		testJson.put("phone",phone)
		testJson.put("nickname",nickname)
		testJson.put("age",age)
		testJson.put("sex",sex)
		testJson.put("zoneId",zoneId)
		testJson.put("categoryIdList",categoryIdList)
		testJson.put("fcmToken",fcmToken)
		String jsonDataStr = testJson.toString()
		
		HTTPResponse response = request.POST("https://gjgs-test.com/api/v1/login/first", jsonDataStr.getBytes())
		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}
}
```
import org.json.JSONObject 를 이용해 json을 만들어 주었고, 중복되면 안되는 값에는 Math.random을 이용했습니다.  
아직 제 지식에서는 이정도 였지만, 분명 더 좋은 다른 방법이 있을 것 같습니다.

## 로그인이 필요한 테스트
어떤 요청을 보낼 때, 로그인이 필요한 경우가 있습니다. JWT 토큰을 이용해서 인증을 하고 있을 때의 스크립트 입니다.

```groovy
import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse
import org.ngrinder.http.cookie.Cookie
import org.ngrinder.http.cookie.CookieManager

import groovy.json.JsonSlurper

/**
* A simple example using the HTTP plugin that shows the retrieval of a single page via HTTP.
*
* This script is automatically generated by ngrinder.
*
* @author admin
*/
@RunWith(GrinderRunner)
class TestRunner {

	def toJSON = { new JsonSlurper().parseText(it) }

	public static GTest test
	public static HTTPRequest request
	public static Map<String, String> headers = [:]
	public static Map<String, Object> params = [:]
	public static List<Cookie> cookies = []
	
	// login
	public static HTTPRequest loginRequest
	public static Map<String, String> loginHeaders = [:]
	public static String fcmTokenBody = "{\n\"fcmToken\" : \"test\"\n}"
	public static String accessToken

	@BeforeProcess
	public static void beforeProcess() {
		HTTPRequestControl.setConnectionTimeout(300000)
		test = new GTest(1, "gjgs-test.com")
		request = new HTTPRequest()
		
		// login
		loginRequest = new HTTPRequest()

		// Set param data
		params.put("type", "ALL")
		grinder.logger.info("before process.")
	}

	@BeforeThread
	public void beforeThread() {
		// Set Login
		loginHeaders.put("KakaoAccessToken", "Bearer Example")
		loginHeaders.put("Content-Type", "application/json")
		loginRequest.setHeaders(loginHeaders)
		HTTPResponse loginResponse = loginRequest.POST("https://gjgs-test.com/api/v1/ngrinder/login", fcmTokenBody.getBytes());
		grinder.logger.info(loginResponse.getBodyText())
		accessToken = loginResponse.getBody(toJSON).tokenDto.grantType + " " + loginResponse.getBody(toJSON).tokenDto.accessToken

		test.record(this, "test")
		grinder.statistics.delayReports = true
		grinder.logger.info("before thread.")
	}

	@Before
	public void before() {
		headers.put("Authorization", accessToken)
		headers.put("Content-Type", "application/json")
		request.setHeaders(headers)
		CookieManager.addCookies(cookies)
		grinder.logger.info("before. init headers and cookies")
	}

	@Test
	public void test() {
		HTTPResponse response = request.GET("https://gjgs-test.com/api/v1/notices", params)

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}
}
```
import groovy.json.JsonSlurper 를 통해서 Json을 파싱하도록 toJSON을 선언합니다.  
BeforeThread에서 로그인 요청을 보내고 응답 파싱해서 JWT 토큰을 static 변수에 저장해줍니다.  
각각의 Test 전에 수행되는 Before에서 토큰을 헤더에 세팅해줍니다.  



<Br><Br>

__참고__  
<a href="https://github.com/naver/ngrinder/wiki/Installation-Guide" target="_blank"> Installation Guide 서비스</a>   






