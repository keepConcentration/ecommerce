### 동시성 제어 방식 비교

#### 1. synchronized

- 구현: `synchronized` 키워드 사용
- 적용 범위: 코드 블록, 메서드 단위
- 장점:
    - 자바에서 기본으로 제공하는 기능
    - 락 해제/예외 처리 자동 보장
    - 코드 가독성 높음
- 단점:
    - 블록 범위가 넓어지면 성능 저하 (모니터 락 경쟁)
    - 타임아웃 등 세밀한 제어 불가
    - 분산 환경에서는 동기화 불가

#### 1.1. 임계 영역

- 한 번에 하나의 쓰레드만 접근을 허용해야 하는 코드 영역
```java
class Counter {
    private int count = 0;

    public void increment() {
        // 임계 영역 시작
        count++;
        // 임계 영역 끝
    }
}
```

#### 1.2. 모니터

- 임계 영역을 관리하는 객체
- 모든 자바 객체는 모니터를 가지고 있음.
- 모니터는 내부적으로 하나의 락(lock) 메커니즘을 가지고 있음.
```java
synchronized void increment() {
    count++; // 이 메서드는 모니터(객체)에 의해 보호됨
}
```

#### 1.3. 모니터 락

- 모니터 내부의 락
- 스레드가 임계영역에 진입하기 위해 필요한 락 객체
- 자바에서 어떤 객체든 `synchronized` 키워드를 이용해 모니터 락을 획득할 수 있음.
```java
synchronized (this) { // this 객체의 모니터 락 획득
    count++;
} // 블록이 끝나면 락 자동 해제
```


#### 2. ReentrantLock

- 구현: `ReentrantLock` + `ConcurrentHashMap`
- 적용 범위: 단일 JVM
- 장점:
    - 구현이 간단함
    - DB에 부하가 없음
    - synchronized에 비해 빠른 성능
    - 타임아웃, 공정성 설정 가능
- 단점:
    - 분산 환경에서 지원하지 않음.
    - 반드시 수동으로 unlock() 호출 필요(메모리 누수 위험).
    - 서버 재시작 시 락 상태 초기화됨

#### 2.1. 공정성

- 스레드가 락을 획득하는 순서에 대한 설정

```java
ReentrantLock nonFairLock = new ReentrantLock(); // 기본값: 비공정 락
ReentrantLock fairLock = new ReentrantLock(true); // 공정락
```

#### 2.1.1. 공정락

- 대기 큐(FIFO)에 들어온 순서대로 락을 획득함
- 대기 순서 보장 덕분에 기아 현상이 거의 없음.
- 비공정 락보다 성능이 떨어짐

#### 2.1.2. 비공정락

- 락 획득 시점에 대기 큐를 무시함
- 락이 해제되는 순간 대기 중인 스레드보다 나중에 온 스레드가 먼저 락을 획득할 수도 있음.
- 대기 순서가 보장되지 않아 기아 현상이 생길 수 있음.
- 공정 락보다 성능이 좋음

#### 3. ReentrantReadWriteLock

- 구현: `ReentrantReadWriteLock`
- 적용 범위: 단일 JVM
- 장점:
    - 읽기/쓰기 작업을 분리하여 여러 스레드가 동시에 읽기 가능
    - DB에 부하가 없음
    - synchronized에 비해 빠른 성능
    - 타임아웃, 공정성 설정 가능
    - 읽기가 많은 환경에서는 성능이 좋음
- 단점:
    - 락 관리가 까다로움
    - 읽기 스레드가 많을 경우 쓰기 스레드가 기아 상태에 빠질 수 있음.
    - 분산 환경에서 지원하지 않음.
    - 반드시 수동으로 unlock() 호출 필요(메모리 누수 위험).
    - 서버 재시작 시 락 상태 초기화됨

#### 3.1. 읽기락

- 여러 스레드가 동시에 읽기 가능

```java
ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
private int value = 0;

public int read() {
  lock.readLock().lock();
  try {
    return value;
  } finally {
    lock.readLock().unlock();
  }
}
```

#### 3.2. 쓰기락

- 단 하나의 스레드만 쓰기 가능
```java
ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
private int value = 0;

public void write(int newValue) {
  lock.writeLock().lock();
  try {
    value = newValue;
  } finally {
    lock.writeLock().unlock();
  }
}
```

#### 3.3. 읽기락과 쓰기락 관계

- 읽기 중에는 쓰기 금지, 쓰기 중에는 읽기 금지
- 읽기 락 → 쓰기 락 업그레이드 불가
- 쓰기 락 → 읽기 락 다운그레이드는 가능

#### 4. 분산 락

- 구현: Redis, ZooKeeper, etcd 등 외부 시스템
- 적용 범위: 분산 환경
- 장점:
    - 여러 JVM 간 동기화 가능
    - 분산 환경에서 데이터 정합성 유지
    -
- 단점:
    - 외부 시스템 의존 (Redis 등)
    - 네트워크 지연 시 성능 저하

---

### 선택 기준 및 트레이드오프

#### 현재 선택: ReentrantLock

**선택 이유:**

- 현재 `ConcurrentHashMap`을 이용한 인메모리 리포지토리 사용
- DB가 없어 DB 락 불가능
- 단일 애플리케이션
- 메모리 내 락으로 성능이 우수함

**트레이드오프:**

- 이득
    - 빠른 개발 속도
    - 단순한 구조
    - 높은 성능
- 손실
    - 단일 JVM 제약
    - 확장성 제한
    - 메모리 누수 위험
