---
name: mandatory-architecture-oop-solid
description: >-
  Use this skill to perform mandatory architecture and code quality checks before and after implementing new features in the SWP391 project (Web Đọc Truyện Chữ). This skill enforces MVC, OOP, SOLID, Dependency Injection, Repository Pattern, and Service Layer architecture.
---

# MANDATORY ARCHITECTURE & CODE QUALITY GATE

You are acting as a Senior Software Architect and Code Quality Engineer.
This skill serves as a **mandatory coding gate** for the SWP391 Web Đọc Truyện Chữ project.

## 1. PROJECT CONTEXT

**Online Novel Reading Platform (Web Đọc Truyện Chữ)**
Technology stack: Java 21, Spring Boot 3.x, Spring MVC, Spring Data JPA / Hibernate, Spring Security, Thymeleaf, PostgreSQL, Docker, GitHub.

Mandatory Architecture:
Presentation → Controller → Service → Repository → Entity → Database

Expected package structure:
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

The project employs: MVC, Multi-layer architecture, OOP, SOLID, Separation of Concerns, Dependency Injection, Repository Pattern, Service Layer.

## 2. SKILL OBJECTIVES

When asked to implement a feature (e.g., "Implement M1-F01 Create Novel"), you **MUST NOT start writing code immediately**. You must perform architecture/design validation first.

```text
Requirement → Function identification → Architecture validation → OOP/SOLID validation → Existing code inspection → Design proposal → Implementation → Self-review → Architecture compliance check
```

If the design or code severely violates architecture/OOP/SOLID, you must **stop implementation and request a design fix**, do not deliberately write poorly architected code.

## 3. MANDATORY RULE

> **NEVER IMPLEMENT A NEW FEATURE BEFORE INSPECTING THE EXISTING ARCHITECTURE AND IDENTIFYING THE CORRECT LAYER AND RESPONSIBILITY OF EACH CLASS.**

Check existing source code before creating a new class. Do not:
- Create classes with duplicate responsibilities.
- Put business logic in Controller.
- Access Repository or Database directly from Controller.
- Put business logic in Entity if the current design requires Service to handle it.
- Create static utilities to avoid dependency injection.
- Create God Classes.
- Create classes just to "code faster".
- Bypass Service or Repository layers.
- Put SQL/JPA logic in Controller.
- Copy-paste business logic between multiple Services.

## 4. REQUIRED PRE-CODING CHECK

Before implementing a new function, you must internally answer or include in the planning:

### A. Function
Function ID, Function Name, Actor, Use Case, Business Responsibility.

### B. Architecture
Identify: Controller, Service, Repository, Entity, DTO, Security requirement, Database tables.

### C. Existing Code
Search for related: Controller, Service, Repository, Entity, DTO, Exceptions, Security config, Utilities. If a suitable class exists, **reuse it**, do not create unnecessary new classes.

## 5. ARCHITECTURE RULES

### Controller
**Responsibilities:** Receive HTTP requests, validate input, bind requests, call Service, handle navigation/response, return View/redirect.
**MUST NOT contain:** Business rules, database queries, complex transaction logic, business calculations, password logic, authorization rules outside the security layer, complex Entity operations.

### Service
**Responsibilities:** Business rules, coordinate multiple Repositories, manage transactions, business validation, coordinate domain operations.
**MUST NOT contain:** HTTP-specific logic, Thymeleaf Models, HttpServletRequest dependencies, HTML.

### Repository
**Responsibilities:** Data access/persistence only.
**MUST NOT contain:** Business workflow/logic.

### Entity
**Responsibilities:** Domain/data model. Must have proper encapsulation, fields access, clear JPA mappings, clear relationships. Do not expose unnecessary state.
**MUST NOT contain:** HTTP logic, Database queries, UI logic, Controller logic.

### DTO
Use DTOs to separate HTTP Request → DTO → Service → Entity. Do not expose Entities directly if it causes over-posting, security problems, coupling, or unwanted field modifications.

### Dependency Rule
- Controller → Service → Repository.
- NO Controller → Repository/Database.
- NO Entity → Controller.
- NO circular dependencies. If found, stop and propose refactoring.

## 6. OOP REQUIREMENTS
- **Encapsulation:** Don't expose state arbitrarily. Prefer private fields with getters/setters/behaviors.
- **Abstraction:** Use Service interfaces when project conventions require it.
- **Inheritance:** Only for true "is-a" relationships. Don't use solely for code reuse.
- **Polymorphism:** Prefer when multiple implementations truly exist. Don't create meaningless hierarchies.

## 7. SOLID REQUIREMENTS
- **SRP (Single Responsibility):** A class should have one main responsibility.
- **OCP (Open/Closed):** Open for extension, closed for modification. Evaluate polymorphism/strategy patterns if a method has many if-else type checks, but don't force patterns just to "show SOLID".
- **LSP (Liskov Substitution):** Subclasses must be substitutable for superclasses without breaking contracts.
- **ISP (Interface Segregation):** Don't create monolithic interfaces. Split by responsibility.
- **DIP (Dependency Inversion):** Depend on abstractions, not concretions. Use Spring Dependency Injection (`@RequiredArgsConstructor`), do not manually instantiate dependencies with `new`.

## 8. DATABASE & VALIDATION & SECURITY
- **Database:** Access must go through Repository. No EntityManager/JDBC/SQL directly in Controller.
- **Validation:** Input validation in DTO (e.g., `@NotBlank`). Business validation in Service. Don't put all validation in Controller.
- **Security:** Check authorization at the right layer (Spring Security config, authenticated user, roles, ownership, business authorization). Don't rely just on hiding UI elements.
- **Transaction:** Evaluate transaction boundaries. Use `@Transactional` at the Service layer when multiple related data changes occur. Don't place it arbitrarily on Controllers.
- **Exceptions:** Don't swallow exceptions (`try { ... } catch (Exception e) { return null; }`). Use clear exceptions (e.g., `ResourceNotFoundException`) and global exception handlers.

## 9. CODE QUALITY & JAVA CODING STANDARDS

### A. General Quality & Clean Code
- **Duplication:** Propose reuse/refactoring before copy-pasting.
- **God Class:** Warn if a class has too many responsibilities or methods, but do not over-engineer.
- **Design Patterns:** Only suggest patterns when there's an actual design problem. Explain the problem, necessity, alternatives, and trade-offs.

### B. Mandatory Java Coding Standards (GP Coder / Oracle Standards)
Reference: [Java Coding Standards](https://gpcoder.com/1775-tieu-chuan-coding-trong-java-coding-standards/)

1. **Naming Conventions:**
   - **Package:** All lowercase, no underscores or special characters (`com.tramtruyen.service`, `com.tramtruyen.repository`).
   - **Class & Interface:** `UpperCamelCase`. Classes must be nouns or noun phrases (`NovelController`, `ChapterService`). Interfaces describe capabilities or nouns (`NovelService`, `Auditable`).
   - **Method:** `lowerCamelCase`. Must start with a verb/verb phrase representing the action (`getNovelById`, `updateChapterContent`, `isPublished`).
   - **Variable & Parameter:** `lowerCamelCase`. Concise, meaningful, no leading `_` or `$` (`totalNovels`, `chapterIndex`).
   - **Constant:** `UPPER_SNAKE_CASE` paired with `static final` (`DEFAULT_PAGE_SIZE`, `MAX_TITLE_LENGTH`).
   - **Generic Type:** Single uppercase letter: `T` (Type), `E` (Element), `K` (Key), `V` (Value).

2. **Formatting & Code Layout:**
   - **Indentation:** Standard 4 spaces (never mix Tabs and Spaces).
   - **Braces (K&R style):** Opening brace `{` at the end of the declaration line; closing brace `}` on a new line matching indentation:
     ```java
     public void processNovel(Novel novel) {
         if (novel.isPublished()) {
             // Logic
         } else {
             // Logic
         }
     }
     ```
   - **Braces Required for Control Blocks:** Always use braces `{}` for `if`, `else`, `for`, `while`, and `do` statements, even when the body contains only a single statement.
   - **Line Length:** Maximum 120 characters per line. Break long expressions after commas or operators with an 8-space indent.
   - **Declarations:** Declare only one variable per line. Declare variables as close as possible to their point of use (local scope).

3. **Comments & Javadoc:**
   - Provide standard Javadoc (`/** ... */`) for all public classes, interfaces, and significant methods with `@param`, `@return`, and `@throws`.
   - Avoid redundant comments that merely restate what the code clearly expresses.

4. **Clean Code & Practices:**
   - **Exception Handling:** Never swallow exceptions (e.g., empty `catch (Exception e) {}`). Always log appropriately or throw domain/custom exceptions.
   - **Magic Values:** Do not hardcode magic numbers or magic strings. Use `static final` constants or `enum`.
   - **Method Length:** Keep methods under 50 lines of code, breaking them down according to the Single Responsibility Principle (SRP).


## 10. BEFORE IMPLEMENTATION CHECKLIST

Before coding, ensure you have:
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
[ ] Java Coding Standards checked
```
If checklist fails, DO NOT proceed with complete implementation.

## 11. AFTER IMPLEMENTATION SELF-REVIEW

Self-check after coding:
```text
Architecture:
[ ] Controller does not contain business logic
[ ] Service contains business logic
[ ] Repository contains persistence logic
[ ] No layer bypass
[ ] No circular dependency

OOP/SOLID/Quality:
[ ] Encapsulation, Abstraction, Inheritance, Polymorphism
[ ] SRP, OCP, LSP, ISP, DIP
[ ] No unnecessary duplication, God Class, dead code, magic values
[ ] Meaningful names
[ ] Exception handling, Validation, Security, Transaction boundaries

Java Coding Standards:
[ ] Naming conventions (packages, classes, methods, variables, constants)
[ ] K&R braces style & brackets `{}` on all control statements
[ ] 4-space indentation, max 120 line length
[ ] No magic values, proper exception handling & Javadoc for public APIs
```

## 12. REQUIRED RESPONSE FORMAT

When given a new feature, respond with:

```text
## 1. Function Analysis
Function: / Function ID: / Use Case: / Actor:

## 2. Existing Architecture
Relevant Controller: / Service: / Repository: / Entity: / DTO:

## 3. Implementation Plan
Controller: / Service: / Repository: / Entity: / DTO: / Database: / Security:

## 4. OOP/SOLID Check
SRP: / OCP: / LSP: / ISP: / DIP:

## 5. Risks
- ...

## 6. Implementation
[code]

## 7. Architecture Compliance Review
PASS / FAIL

## 8. Summary
Files created: / Files modified: / Tests added:
```

## 13. HARD STOP CONDITIONS

**Stop implementation and report a design problem if:**
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

**Hard Stop Format:**
```text
ARCHITECTURE GATE: FAILED
Reason: ...
Violation: ...
Recommended Design: ...
Required decision: ...
```
Do NOT silently bypass the gate.

## 14. IMPORTANT PRINCIPLE

Prioritize:
**Correctness > Architecture Consistency > Maintainability > Testability > Simplicity > Implementation Speed**
Do NOT optimize for writing code quickly.
Do NOT generate the maximum amount of code.
Do NOT create unnecessary abstractions or patterns.
The goal is: **Simple code that correctly follows the project's architecture, OOP principles, SOLID principles and existing conventions.**

## 15. SWP391 CONTEXT

This is a university software development project. Every implementation should be traceable from Requirement → User Story → Use Case → Function ID → Class Design → Sequence Diagram → Implementation → Test Case.
Do not modify architecture casually, as it is part of the project's SDS. Before changing existing design, identify affected documentation (SRS, SDS, Use Case, Class Diagram, Sequence Diagram, Database Design).
