---
name: mermaid_class_diagram
description: Guides the generation, modification, and translation (from PlantUML) of standardized, high-quality Mermaid class diagrams for application modules following strict project design and styling guidelines for Spring Boot / Java projects.
---

# Mermaid Class Diagram Generation Skill (Spring Boot Context)

This skill provides comprehensive instructions, architectural guidelines, styling rules, and concrete examples for creating and refactoring Mermaid class diagrams. It ensures diagrams represent class definitions according to a standard Spring Boot architecture.

> [!IMPORTANT]
> **Output Location and Naming Convention**
> All generated Mermaid class diagrams are exported and saved to the `docs/diagrams/class_mermaid` directory within the workspace. The file naming convention must follow the format `cls_mm_use_case_name` or `cls_mm_functionality_name` with a `.mmd` extension. For example, a diagram for creating a novel must be named `cls_mm_create_novel.mmd`.

---

## 1. General Participants (Stereotypes & Components)

Depending on the specific use case, a class diagram may include:
- **MVC Controller (`@Controller`)**: Controllers serving Thymeleaf views.
- **REST Controller (`@RestController`)**: API controllers handling backend routes.
- **Service (`@Service`) & Interface**: Business logic implementation and its corresponding interface.
- **DTO**: Data Transfer Objects (e.g., Request, Response).
- **Repository (`@Repository`)**: Spring Data JPA repository interfaces (extending `JpaRepository`).
- **Entity (`@Entity`)**: Domain entities.
- **Exclude**: Exclude any framework-native, external library, or programming-language-native participants (e.g., `List`, `Map`, `String`, `HttpServletRequest`, `Model`, `ResponseEntity`, `BindingResult`). Skip classes that are referenced but have no drawn methods/attributes.

---

## 2. Allowed Relationships & Syntax

| Relationship Type | Mermaid Syntax | Direction/Meaning |
| :--- | :---: | :--- |
| **Dependency** | `..>` | Points *towards* the class being depended on (e.g., `A ..> B`) |
| **Unidirectional Association** | `-->` | Points *towards* the reference target (e.g., `A --> B`) |
| **Bidirectional Association** | `--` | Standard reference link with no specific arrow direction |
| **Aggregation** | `o--` | Part-of relationship; hollow diamond |
| **Composition** | `*--` | Strong ownership; filled diamond |
| **Realization (Implementation)**| `..\|>` | Interface implementation |
| **Inheritance** | `--\|>` | Class inheritance |

> [!IMPORTANT]
> - **Multiplicity / Cardinality**: Exclude all multiplicity labels.
> - **No Relationship Labels**: Exclude all text on relationship lines.
> - **No PK / FK Markers**: Exclude markers like `<<PK>>` or `<<FK>>`.

---

## 3. Detailed Relationship Assignment Rules

### A. Aggregation (`o--`) & Composition (`*--`)
Use for Entity-to-Entity or DTO-to-DTO. Example: `Novel *-- Chapter` (Composition, since chapters cannot exist without a novel). Use Association (`--`) for loose navigation.

### B. Dependency (`..>`)
Use when Class A instantiates, accepts, or returns Class B.
- **Controller to DTO**: Controller handles DTO in request/response (`Controller ..> DTO`).
- **Service to Entity**: For Create/Update logic where Service instantiates or maps to an Entity, `Service ..> Entity`. For Read logic, Service might only depend on DTO and Repository.

### C. Unidirectional Association (`-->`)
Use for Dependency Injection fields (pointing to Services, Repositories).
Example: `NovelController --> NovelService` (via constructor injection or `@Autowired`).

### D. Realization (`..|>`) and Inheritance (`--|>`)
- `NovelServiceImpl ..|> NovelService`
- `NovelRepository --|> JpaRepository` (if JpaRepository is drawn, otherwise omit framework interfaces).

---

## 4. Best Practices & Styling

- **Standard Layout**: Start with `classDiagram` and `direction LR`.
- **Members Representation & Signatures**:
  - **Field Attributes**: Format as `visibility attributeName : Type` (e.g., `- novelRepository : NovelRepository`).
  - **Full Method Signatures**: Use Java conventions. E.g., `+ createNovel(request: CreateNovelRequest) Novel`.
  - **Generics**: Use tildes (`~`) instead of angle brackets (`<` `>`). E.g., `List~NovelDTO~`.
- **Concrete Stereotypes**: 
  - `<<controller>>` or `<<rest controller>>`
  - `<<service interface>>`, `<<service>>`
  - `<<repository>>`
  - `<<entity>>`, `<<dto>>`
- **Interface Methods**: Explicitly mark as abstract by appending an asterisk (`*`) at the end of the signature.
- **Styling**: Minimalist design. Define at the end:
  ```mermaid
  classDef default fill:#FFFFFF,stroke:#000000,stroke-width:1px,color:#000000
  ```
