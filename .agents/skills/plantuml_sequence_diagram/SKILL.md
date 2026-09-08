---
name: plantuml_sequence_diagram
description: Guides the generation and modification of standardized, high-quality PlantUML sequence diagrams for application use cases following strict project design guidelines for Spring Boot / Java projects.
---

# PlantUML Sequence Diagram Generation Skill (Spring Boot Context)

This skill provides comprehensive instructions for creating PlantUML sequence diagrams that accurately represent runtime interactions in a Spring Boot MVC/API architecture.

---

## 1. File Naming Convention
All sequence diagram files must follow the `sq_use_case_name.plantuml` naming convention and be saved to `docs/diagrams/sequence_plantuml`.

---

## 2. Participant Naming and Aliasing

### Standardized Primary Actors
- `Guest` (Unauthenticated actor)
- `Member` (Authenticated user/reader)
- `Staff` (Authorized moderator)
- `Admin` (Highest Authority manager)

### Standardized Participant Aliases
| Participant Type | PlantUML Definition Example | Alias | Role |
|---|---|---|---|
| Actor | `actor Member as act` | `act` | Human actor |
| View / UI | `participant "PageName" as view` | `view` | Thymeleaf view or Frontend UI |
| Controller | `participant "ControllerName" as ctrl <<controller>>` | `ctrl` | Spring `@Controller` or `@RestController` |
| Service | `participant ":ServiceName" as svc` | `svc` | Spring `@Service` business logic |
| Entity / DTO | `participant ":ClassName" as dto` | `dto` | DTO or Entity object |
| Repository | `participant ":RepositoryName" as repo` | `repo` | Spring Data `@Repository` |
| Database | `database Database as db` | `db` | The physical database |

### Modeling Rules
- **Interfaces Only:** Model interfaces for injected dependencies (e.g., `:INovelService`).
- **Participant Grouping Order:** `act`, `view`, `ctrl`, `svc`, `dto`, `repo`, `db`.

---

## 3. Style Definitions and Canvas Settings
Every `.plantuml` file must start with:
```plantuml
@startuml 
<style>
    sequenceDiagram{
        FontSize 16
        arrow { MaximumWidth 300 }
        actor{ BackgroundColor white; LineThickness 1.0 }
        participant{ BackgroundColor white; RoundCorner 0 }
        database{ BackgroundColor white }
    }
</style>
hide footbox
```

---

## 4. Interaction Arrow Styles
- `->` (Solid Arrow): Synchronous calls, requests, or internal method invocation.
- `-->>` (Dashed Arrow): Returns, responses, redirects, exceptions.

---

## 5. Lifeline Activation and Deactivation
1. **Actor Activation:** `act ++` at the top.
2. **Activation:** Append `++` to the receiving participant when it receives its first call.
3. **Deactivation:** Append `--` when it returns or throws an exception.
4. **Nested Returns:** Deactivate inner lifeline before outer lifeline makes its return call:
   ```plantuml
   db -->> repo: Return data
   db--
   repo -->> svc: Return data
   ```

---

## 6. Flow Logic Guidelines

### Mapping Use Case to Diagram
- **Alternative flows** must be modeled as `opt`.
- **Exceptions** must be modeled as `alt`.

### Frontend Authentication/Authorization (Spring Security)
Authorization checks in Spring Boot (e.g., filters, `@PreAuthorize`) should be modeled as an initial block in the controller:
```plantuml
ctrl -> ctrl++: Validate SecurityContext/Authorization
ctrl--
alt Unauthorized
    ctrl -->> view: Redirect to Login/Error
    view -->> act--: Display error
else Authorized
```

### Validation (BindingResult)
Form validation using `@Valid` should be modeled:
```plantuml
ctrl -> ctrl++: Validate BindingResult
ctrl--
opt Has Errors
    ctrl -->> view: Return View with errors
    view -->> act: Display validation messages
end
```

### Controller Return Messages
- **MVC Views:** `Return "viewName"` or `Return "redirect:/url"`.
- **REST APIs:** `Return ResponseEntity.ok(...)` or `Return ResponseEntity.badRequest(...)`.

### Database Operations (Spring Data JPA)
- Use semantic repository method names (e.g., `findById`, `save`, `delete`).
- The repository calls the database directly: `repo -> db++: Execute query`.
- **Exceptions:** If the service catches a `DataAccessException` or throws a custom business exception, use an `alt` block.

### Explicit Object Instantiation
When a DTO or Entity is created (e.g., via `new` or MapStruct):
```plantuml
svc -> dto++: Call Novel(request)
dto -->> svc--: Return Novel object
```
