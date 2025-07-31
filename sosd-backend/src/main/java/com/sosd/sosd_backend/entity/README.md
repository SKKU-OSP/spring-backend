### 📁 `entity/` 디렉토리

이 디렉토리는 프로젝트의 **도메인 목표(Entity)** 클래스를 정의합니다.
각 클래스는 데이터베이스 테이블과 매핑되며, JPA 또는 ORM 프레임워크를 사용해서 영속성 처리를 담당합니다.

---

### 📌 주요 목적

* 데이터베이스 테이블과의 매핑
* 도메인 객체 구조 정의
* 연관 관계(OneToMany, ManyToOne 등) 설정
* 기본 생성자, equals/hashCode, toString 정의 등

---

### 📆 예시 클래스

```java
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    // 기본 생성자 (JPA용)
    protected User() {}

    // 일반 생성자 (비즈니스 로직용)
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getter, Setter, Constructor, equals/hashCode, toString 등
}
```

---

### 📙 규칙

* `@Entity` 애너테이션 필수
* 기본 생성자(default constructor) 필요
* 연관 관계는 명확히 명시
* 비지니스 로직은 가까운 Service 레이어로 분리
