
# sharding, clustering, replication

## 1. Clustering
---
DB Clustering은 DB 분산 기법 중 하나로 DB 서버를 여러 개 두어 서버 한 대가 죽었을 때 대비할 수 있는 기법입니다.  

### Active - Active Clustering
![그림1](https://github.com/backtony/blog-code/blob/master/interview/db-strategy/img/db-partition-1.PNG?raw=true)  
DB 서버를 여러 개로 구성하여 각 서버를 Active 상태로 두는 방식입니다.  
+ 서버 하나가 죽더라도 다른 서버가 역할을 바로 수행하므로 서비스 중단이 없습니다.
+ 여러 대의 서버가 운영되기 때문에 CPU와 메모리 이용률을 올릴 수 있습니다.
+ 저장소 하나를 공유하게 되면서 병목현상이 발생할 수 있습니다.
+ 서버를 여러대 한꺼번에 운영하므로 비용이 더 발생할 수 있습니다.

### Actice - Standby Clustering
![그림2](https://github.com/backtony/blog-code/blob/master/interview/db-strategy/img/db-partition-2.PNG?raw=true)  
서버를 하나만 운영하고 나머지 서버는 Standby 상태로 두는 방식입니다.  
+ 운영하고 있는 서버가 다운되었을 시에 Standby 상태의 서버를 Active상태로 전환하여 사용하지만 전환하는데 어느정도 시간이 소요됩니다.
+ Active-Active 클러스터링에 비해 적은 비용이 소요됩니다.

<br>

## 2. Replication
---
DB 서버가 죽었을 때는 clustering으로 해결할 수 있었는데 스토리지는 하나이기 때문에 저장된 데이터가 손실되면 문제가 생기게 됩니다.  
이에 대한 해결책으로 실제 데이터가 저장되는 저장소도 복제하는 방식이 Replication 방식 입니다.  
![그림3](https://github.com/backtony/blog-code/blob/master/interview/db-strategy/img/db-partition-3.PNG?raw=true)  
Master DB와 Slave DB로 나뉘어 Master DB에 CRUD를 거치면 해당 데이터를 Slave DB에 동기화 시켜 백업하는 용도로 사용합니다.  
추가적으로 Slave DB를 Read 용도만으로 따로 나눠서 부하부산의 용도로 사용하기도 합니다.  
<br>

## 3. Sharding
---
Sharding이란, 같은 테이블 스키마를 가진 데이터를 다수의 데이터베이스에 분산하여 저장하는 방식입니다.  
![그림4](https://github.com/backtony/blog-code/blob/master/interview/db-strategy/img/db-partition-4.PNG?raw=true)  
sharding은 테이블을 로우 단위로 나눠서 각각의 shard에 저장하는 역할을 하게 됩니다.  
따라서, 데이터를 검색할 때 데이터가 어느 Shard에 있는지 안다면 더욱 빠르게 검색할 수 있게 됩니다.  
Sharding 테이블을 구성하는데 고려할 사항은 다음과 같습니다.  
+ 분산된 Database에 Data를 어떻게 잘 분산시켜 저장할 것인가?
+ 분산된 Database에서 Data를 어떻게 읽을 것인가?

위 사항을 고려할 때 나오는 개념이 Shard Key 입니다.  
Shard Key는 나눠진 Shard 중 어떤 Shard를 선택할지 결정하는 키입니다.  
Shard Key 결정 방식에 따라 Sharding 방법이 나눠집니다.  

### Hash Sharding
![그림5](https://github.com/backtony/blog-code/blob/master/interview/db-strategy/img/db-partition-5.PNG?raw=true)  
위 그림의 shard 뒤에 0~3이 shard key로, shard key를 결정하는데 해시 함수를 사용하는 것이 Hash sharding 방식입니다.  
구현자체가 간단하지만, shard가 늘어나면 해시 함수 자체가 변경되어야 하기 때문에 기존 데이터에 대한 정합성이 깨지게 되어 확장성이 안좋아지는 단점이 있습니다.  
또한, 단순히 해시 함수로 처리하기 때문에 공간에 대한 효율성을 고려하지 않지 않습니다.  

### Dynamic Sharding
![그림6](https://github.com/backtony/blog-code/blob/master/interview/db-strategy/img/db-partition-6.PNG?raw=true)  
Hash Sharding의 확장성 문제를 해결하기 위한 방식입니다.  
Locator Service라는 테이블 형식의 구성요소를 갖고 Shard key를 결정하는 방식입니다.  
Shard가 하나 더 추가된다고 하더라도 Locator Service의 Shard key만 추가하는 방식으로 개선할 수 있습니다.  
Locator Service에 Shard들이 종속적이기 때문에 Locator Service에 문제가 생긴다면 전체에 문제가 생기는 단점이 있습니다.  

### Entity Group
![그림7](https://github.com/backtony/blog-code/blob/master/interview/db-strategy/img/db-partition-7.PNG?raw=true)  
앞선 2가지 경우에는 관계형 데이터베이스보다는 Key - Value 형식의 NoSQL에 적합한 샤딩 방식입니다.  
관계형 데이터베이스에 적합한 방식은 Entity Group 방식입니다.  
Entity Group은 관계가 되어있는 엔티티끼리 같은 샤드에서 관리하도록 만드는 방식입니다.  
단일 샤드 내에서는 쿼리가 효율적이고 강한 응집도를 갖지만, 다른 샤드의 엔티티와 연관되는 경우에는 비효율적입니다.  

<Br><br>

데아터베이스를 나누는 것도 다 비용이기 때문에 현재 상황에 맞는 기술을 활용하는 것이 중요합니다.  
특히 샤딩같은 경우에는 복잡성이 높아지기 때문에 다른 방식을 먼저 고려하는 것이 좋습니다.  





<Br><Br>

__참고__  
<a href="https://www.youtube.com/watch?v=y42TXZKFfqQ&list=PLo0ta52hn1uHQ5iQ3hAeRoMUeLJFIeRew" target="_blank"> [10분 테코톡] 👨‍💻히브리의 Sharding, Clustering, Replication</a>  
