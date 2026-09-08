# PROMPT TẠO SKILL – MANDATORY ARCHITECTURE & CODE QUALITY GATE

Bạn là Senior Software Architect và Code Quality Engineer.

Hãy tạo một **SKILL dành cho coding agent** với mục tiêu kiểm soát bắt buộc kiến trúc và chất lượng code của project SWP391.

## 1. BỐI CẢNH PROJECT

Project là:

**Web Đọc Truyện Chữ**

Technology stack:

- Java 21
- Spring Boot 3.x
- Spring MVC
- Spring Data JPA / Hibernate
- Spring Security
- Thymeleaf
- PostgreSQL
- Docker
- GitHub

Kiến trúc bắt buộc:

```text
Presentation
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
Entity
      ↓
Database
```

Package structure dự kiến:

```text
com.readingweb
├── config
├── controller
├── service
│   └── impl
├── repository
├── entity
├── dto
├── security
├── exception
└── util
```

Project sử dụng:

- MVC
- Multi-layer architecture
- OOP
- SOLID
- Separation of Concerns
- Dependency Injection
- Repository Pattern
- Service Layer

---

# 2. MỤC TIÊU CỦA SKILL

SKILL này phải hoạt động như một **mandatory coding gate**.

Khi member yêu cầu agent:

> "Implement M1-F01 Create Novel"

hoặc:

> "Thêm chức năng tạo chapter"

agent **KHÔNG được phép lập tức viết code**.

Agent phải thực hiện architecture/design validation trước.

SKILL phải đảm bảo:

```text
Requirement
    ↓
Function identification
    ↓
Architecture validation
    ↓
OOP/SOLID validation
    ↓
Existing code inspection
    ↓
Design proposal
    ↓
Implementation
    ↓
Self-review
    ↓
Architecture compliance check
```

Nếu design hoặc code vi phạm architecture/OOP/SOLID nghiêm trọng, agent phải **dừng implementation và yêu cầu sửa design**, không được cố tình viết code sai kiến trúc.

---

# 3. MANDATORY RULE

Đây là rule quan trọng nhất:

> **NEVER IMPLEMENT A NEW FEATURE BEFORE INSPECTING THE EXISTING ARCHITECTURE AND IDENTIFYING THE CORRECT LAYER AND RESPONSIBILITY OF EACH CLASS.**

Agent phải kiểm tra source code hiện tại trước khi tạo class mới.

Không được tự ý:

- tạo class trùng responsibility;
- đưa business logic vào Controller;
- truy cập Repository trực tiếp từ Controller;
- truy cập Database trực tiếp từ Controller;
- đưa business logic vào Entity nếu design hiện tại yêu cầu Service xử lý;
- tạo static utility để né dependency injection;
- tạo God Class;
- tạo class chỉ vì "code cho nhanh";
- bypass Service layer;
- bypass Repository layer;
- đưa SQL/JPA logic vào Controller;
- copy-paste business logic giữa nhiều Service.

---

# 4. REQUIRED PRE-CODING CHECK

Trước mỗi function mới, agent phải trả lời nội bộ/hoặc trong planning:

### A. Function

```text
Function ID:
Function Name:
Actor:
Use Case:
Business Responsibility:
```

### B. Architecture

Xác định:

```text
Controller:
Service:
Repository:
Entity:
DTO:
Security requirement:
Database tables:
```

### C. Existing Code

Agent phải tìm kiếm:

- Controller liên quan
- Service liên quan
- Repository liên quan
- Entity liên quan
- DTO liên quan
- Exception hiện có
- Security configuration
- Utility hiện có

Nếu class phù hợp đã tồn tại thì **reuse**, không tạo class mới không cần thiết.

---

# 5. ARCHITECTURE RULES

## Controller

Controller chỉ chịu trách nhiệm:

- nhận HTTP request;
- validate request ở mức input;
- bind request;
- gọi Service;
- xử lý navigation/response;
- trả View/redirect.

Controller KHÔNG được chứa:

- business rules;
- database query;
- transaction logic phức tạp;
- business calculations;
- password logic;
- authorization rules ngoài security layer;
- thao tác Entity phức tạp.

Ví dụ KHÔNG được:

```java
@PostMapping("/novels")
public String createNovel(...) {

    if (title == null) {
        ...
    }

    if (repository.existsByTitle(title)) {
        ...
    }

    Novel novel = new Novel();
    ...
    repository.save(novel);

    return "...";
}
```

Phải chuyển business logic sang Service.

---

# 6. SERVICE RULES

Service là nơi xử lý business logic.

Ví dụ:

```java
@Service
@RequiredArgsConstructor
public class NovelServiceImpl implements NovelService {

    private final NovelRepository novelRepository;

    @Override
    public Novel createNovel(CreateNovelRequest request) {

        // business validation

        // business rules

        // create entity

        // persist

        return ...;
    }
}
```

Service phải:

- xử lý business rules;
- phối hợp nhiều Repository nếu cần;
- quản lý transaction;
- kiểm tra business validation;
- điều phối domain operation.

Service KHÔNG nên:

- chứa HTTP-specific logic;
- trả Thymeleaf Model;
- phụ thuộc HttpServletRequest nếu không cần;
- chứa HTML.

---

# 7. REPOSITORY RULES

Repository chỉ chịu trách nhiệm data access.

Được phép:

```java
findById(...)
findByTitle(...)
existsByTitle(...)
save(...)
delete(...)
```

Không được đưa business workflow vào Repository.

Ví dụ KHÔNG được:

```java
public Novel approveNovel(Long id) {
    // business logic
}
```

Repository chỉ cung cấp persistence/data access operations.

---

# 8. ENTITY RULES

Entity đại diện cho domain/data model.

Entity phải:

- có encapsulation hợp lý;
- field có access phù hợp;
- mapping JPA rõ ràng;
- relationship rõ ràng;
- tránh expose state không cần thiết.

Không được biến Entity thành God Object.

Không được đặt:

```text
HTTP logic
Database query
UI logic
Controller logic
```

vào Entity.

Nếu domain behavior thực sự thuộc Entity thì có thể đặt domain behavior phù hợp, nhưng phải giải thích rõ responsibility.

---

# 9. DTO RULES

DTO được sử dụng khi cần tách:

```text
HTTP Request
      ↓
DTO
      ↓
Service
      ↓
Entity
```

Không expose Entity trực tiếp nếu việc đó gây:

- over-posting;
- security problem;
- coupling;
- unwanted field modification.

Ví dụ:

```java
public record CreateNovelRequest(
    @NotBlank String title,
    String description,
    Long authorId
) {}
```

---

# 10. DEPENDENCY RULE

Dependency phải đi theo hướng:

```text
Controller
    ↓
Service
    ↓
Repository
```

Không được:

```text
Controller → Repository
```

Không được:

```text
Controller → Database
```

Không được:

```text
Entity → Controller
```

Không được tạo circular dependency.

Ví dụ không được:

```text
NovelService → ChapterService
ChapterService → NovelService
```

Nếu phát hiện circular dependency, agent phải dừng và đề xuất refactoring.

---

# 11. OOP REQUIREMENTS

Mọi implementation phải xem xét:

### Encapsulation

Không expose state tùy tiện.

Tránh:

```java
public String title;
```

Ưu tiên:

```java
private String title;

public String getTitle() {
    return title;
}
```

và behavior phù hợp.

### Abstraction

Service interface được sử dụng khi project convention yêu cầu:

```java
public interface NovelService {
    Novel createNovel(CreateNovelRequest request);
}
```

Implementation:

```java
@Service
public class NovelServiceImpl implements NovelService {
}
```

### Inheritance

Chỉ sử dụng inheritance khi có quan hệ "is-a" thực sự.

Không tạo inheritance chỉ để tái sử dụng code.

### Polymorphism

Ưu tiên polymorphism khi nhiều implementation thực sự tồn tại.

Không tạo interface/class hierarchy vô nghĩa.

---

# 12. SOLID REQUIREMENTS

Agent phải kiểm tra 5 nguyên tắc.

## S – Single Responsibility Principle

Một class chỉ nên có một responsibility chính.

Nếu class:

```text
NovelService
```

vừa:

```text
Novel business logic
Email sending
File upload
PDF generation
Logging
Notification
```

thì phải cảnh báo SRP violation.

Đề xuất tách:

```text
NovelService
FileStorageService
NotificationService
```

---

## O – Open/Closed Principle

Code nên có khả năng mở rộng mà hạn chế sửa logic cũ.

Nếu một method có:

```java
if (type.equals("A")) ...
else if (type.equals("B")) ...
else if (type.equals("C")) ...
```

agent phải đánh giá xem polymorphism/strategy pattern có phù hợp hay không.

Không áp dụng design pattern chỉ để "show SOLID".

---

## L – Liskov Substitution Principle

Subclass phải có thể thay thế superclass mà không phá vỡ contract.

Agent phải cảnh báo khi:

- subclass disable behavior của parent;
- override method theo cách phá contract;
- throw exception không phù hợp;
- subclass cần nhiều điều kiện đặc biệt để hoạt động.

---

## I – Interface Segregation Principle

Không tạo interface quá lớn.

Không nên:

```java
interface UserService {
    register();
    login();
    logout();
    uploadAvatar();
    banUser();
    createCategory();
    createNovel();
    ...
}
```

Phải tách responsibility:

```text
AuthService
UserService
CategoryService
NovelService
```

---

## D – Dependency Inversion Principle

High-level business logic không nên phụ thuộc trực tiếp vào implementation cụ thể nếu abstraction phù hợp.

Ưu tiên:

```text
Controller
    ↓
NovelService
    ↓
NovelServiceImpl
```

và dependency injection:

```java
private final NovelService novelService;
```

Không tự tạo:

```java
NovelServiceImpl service = new NovelServiceImpl();
```

Spring Dependency Injection phải được sử dụng.

---

# 13. DATABASE RULE

Database access phải đi qua Repository.

Không được:

```java
Controller → EntityManager
Controller → JDBC
Controller → SQL
```

Business query phải nằm ở Repository/DAO phù hợp.

Ví dụ:

```java
public interface NovelRepository
        extends JpaRepository<Novel, Long> {

    boolean existsByTitle(String title);
}
```

---

# 14. VALIDATION RULE

Phân biệt:

### Input validation

Có thể ở DTO:

```java
@NotBlank
@Size
@Email
```

### Business validation

Ở Service:

```text
Novel must exist
Novel must belong to current user
Chapter number must be unique
Only Staff can approve novel
```

Agent không được đưa toàn bộ validation vào Controller.

---

# 15. SECURITY RULE

Authorization phải được kiểm tra đúng layer.

Ví dụ:

```text
Admin
    → Manage Users

Staff
    → Review Novel

Member
    → Create Novel
```

Agent phải kiểm tra:

- Spring Security configuration;
- authenticated user;
- role;
- ownership;
- business authorization.

Không được chỉ ẩn button ở Thymeleaf rồi coi đó là authorization.

---

# 16. TRANSACTION RULE

Nếu một business operation thay đổi nhiều dữ liệu liên quan, agent phải đánh giá transaction boundary.

Ví dụ:

```text
Approve Novel
    ↓
Update Novel Status
    ↓
Create Notification
    ↓
Save Audit Log
```

Có thể cần:

```java
@Transactional
```

ở Service layer.

Không đặt transaction tùy tiện ở Controller.

---

# 17. EXCEPTION HANDLING

Không dùng:

```java
try {
   ...
} catch (Exception e) {
   return null;
}
```

Không swallow exception.

Project nên có exception rõ ràng:

```text
ResourceNotFoundException
BusinessException
UnauthorizedException
ValidationException
```

Global exception handling nên được xử lý tập trung nếu phù hợp.

---

# 18. DUPLICATION CHECK

Trước khi tạo method/class mới, agent phải tìm code tương tự.

Nếu phát hiện duplication:

```text
Controller duplication
Service duplication
Validation duplication
Repository query duplication
```

agent phải đề xuất reuse/refactor trước khi copy-paste.

---

# 19. GOD CLASS DETECTION

Nếu một class có quá nhiều responsibility hoặc method, agent phải cảnh báo.

Ví dụ:

```text
NovelServiceImpl
    30+ unrelated methods
```

Agent phải đánh giá khả năng tách:

```text
NovelService
NovelApprovalService
NovelSearchService
NovelPublishingService
```

Nhưng **không được over-engineer**.

Chỉ tách khi responsibility thực sự khác nhau.

---

# 20. DESIGN PATTERN RULE

Không được sử dụng Design Pattern chỉ để làm code "có vẻ chuyên nghiệp".

Agent chỉ đề xuất pattern khi có vấn đề design thực tế.

Ví dụ:

```text
Strategy
Factory
Adapter
Builder
Observer
Template Method
```

Mỗi pattern được đề xuất phải giải thích:

```text
Problem
Why pattern is needed
Alternative
Trade-off
```

---

# 21. BEFORE IMPLEMENTATION CHECKLIST

Trước khi code, agent phải kiểm tra:

```text
[ ] Function identified
[ ] Related Use Case identified
[ ] Existing code inspected
[ ] Correct Controller identified
[ ] Correct Service identified
[ ] Correct Repository identified
[ ] Related Entity identified
[ ] DTO requirement checked
[ ] Database impact checked
[ ] Security/authorization checked
[ ] OOP principles checked
[ ] SOLID principles checked
[ ] Duplication checked
[ ] Circular dependency checked
[ ] Transaction requirement checked
[ ] Exception handling checked
```

Nếu checklist chưa đạt, không được implementation hoàn chỉnh.

---

# 22. AFTER IMPLEMENTATION SELF-REVIEW

Sau khi code xong, agent phải tự kiểm tra:

```text
Architecture:
[ ] Controller does not contain business logic
[ ] Service contains business logic
[ ] Repository contains persistence logic
[ ] No layer bypass
[ ] No circular dependency

OOP:
[ ] Encapsulation
[ ] Abstraction
[ ] Appropriate inheritance
[ ] Appropriate polymorphism

SOLID:
[ ] SRP
[ ] OCP
[ ] LSP
[ ] ISP
[ ] DIP

Quality:
[ ] No unnecessary duplication
[ ] No God Class
[ ] No dead code
[ ] No magic values
[ ] Meaningful names
[ ] Exception handling
[ ] Validation
[ ] Security
[ ] Transaction boundaries
```

---

# 23. REQUIRED RESPONSE FORMAT

Mỗi lần agent nhận một chức năng mới, response phải theo format:

```text
## 1. Function Analysis

Function:
Function ID:
Use Case:
Actor:

## 2. Existing Architecture

Relevant Controller:
Relevant Service:
Relevant Repository:
Relevant Entity:
Relevant DTO:

## 3. Implementation Plan

Controller:
Service:
Repository:
Entity:
DTO:
Database:
Security:

## 4. OOP/SOLID Check

SRP:
OCP:
LSP:
ISP:
DIP:

## 5. Risks

- ...

## 6. Implementation

[code]

## 7. Architecture Compliance Review

PASS / FAIL

## 8. Summary

Files created:
Files modified:
Tests added:
```

---

# 24. HARD STOP CONDITIONS

Agent MUST stop implementation and report a design problem if:

1. Controller requires direct database access.
2. Business logic must be placed in Controller.
3. A new feature requires bypassing Service layer.
4. A new feature requires bypassing Repository layer without strong architectural reason.
5. Circular dependency is introduced.
6. A class becomes a God Class.
7. A new implementation creates significant SRP violation.
8. Dependency is manually instantiated instead of using Spring DI.
9. Existing abstraction can be reused but agent proposes unnecessary duplication.
10. Security authorization cannot be guaranteed.
11. Database changes are required but schema design is unclear.
12. Existing architecture and requested feature conflict.

When a hard stop occurs:

```text
ARCHITECTURE GATE: FAILED

Reason:
...

Violation:
...

Recommended Design:
...

Required decision:
...
```

Agent must NOT silently bypass the gate.

---

# 25. IMPORTANT PRINCIPLE

The SKILL must prioritize:

```text
Correctness
>
Architecture Consistency
>
Maintainability
>
Testability
>
Simplicity
>
Implementation Speed
```

Do NOT optimize for writing code quickly.

Do NOT generate the maximum amount of code.

Do NOT create unnecessary abstractions.

Do NOT introduce patterns without a real design problem.

The goal is:

> **Simple code that correctly follows the project's architecture, OOP principles, SOLID principles and existing conventions.**

---

# 26. SWP391 CONTEXT

The SKILL must also remind the coding agent that this is a university software development project.

Therefore every implementation should be traceable:

```text
Requirement
    ↓
User Story
    ↓
Use Case
    ↓
Function ID
    ↓
Class Design
    ↓
Sequence Diagram
    ↓
Implementation
    ↓
Test Case
    ↓
GitHub Issue
    ↓
GitHub Commit
    ↓
Pull Request
```

The agent must not modify architecture casually because the architecture, class design and sequence diagrams are part of the project's SDS.

Before changing an existing design, the agent should identify which documentation is affected:

```text
SRS
SDS
Use Case
Class Diagram
Sequence Diagram
Database Design
```

---

# 27. FINAL SKILL OBJECTIVE

Create a reusable SKILL named:

**mandatory-architecture-oop-solid**

The SKILL should behave as an **Architecture Gate + Code Review Gate** for every new feature.

Its core rule is:

> **NO DESIGN VALIDATION → NO IMPLEMENTATION.**

And after implementation:

> **NO ARCHITECTURE REVIEW → NO FEATURE COMPLETION.**

The SKILL should be written clearly enough that another coding agent can follow it consistently without requiring the developer to repeatedly remind it about MVC, OOP, SOLID, Dependency Injection, Repository Pattern and Service Layer.

The SKILL should be practical and enforceable, not merely educational documentation.