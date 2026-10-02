# M1-F01, M1-F02, M1-F04 Novel CRUD Implementation Plan

## 1. Function Analysis

| Function | Actor | Use case | Responsibility |
|---|---|---|---|
| M1-F01 Create Novel | Staff, Admin | UC-01 Create Novel | Validate and persist a new novel, optionally uploading its cover. |
| M1-F02 Update Novel | Staff, Admin | UC-02 Update Novel | Update editable metadata and optionally replace the cover. |
| M1-F04 Delete Novel | Admin | UC-04 Delete Novel | Soft-delete the novel without removing its database record. |

Archive (M1-F03) remains a separate status operation and is not merged into delete.

## 2. Existing Architecture Check

- Existing entity: `com.tramtruyen.entity.Novel` already maps the `novels` table and contains `isDeleted`.
- Existing repository: `NovelRepository` extends `JpaRepository<Novel, Integer>` and is used by the public home service.
- Existing presentation: Thymeleaf templates under `src/main/resources/templates` and standalone CSS under `src/main/resources/static/css`.
- Missing admin CRUD layers: no Novel admin controller, service, form DTO, or Cloudinary adapter exists yet.
- Database impact: none. The existing `novels.sql` schema contains all fields needed by these functions.

## 3. Proposed Design

```text
Thymeleaf novel-admin.html / novel-form.html
    -> NovelAdminController
    -> NovelService / NovelServiceImpl
    -> NovelRepository
    -> Novel
```

- `NovelForm`: request DTO with Bean Validation; does not expose entity-only fields such as views, ratings, timestamps, uploader, or deletion state.
- `MediaStorageService`: media abstraction used by the service; `CloudinaryMediaStorageService` is the infrastructure implementation.
- `CloudinaryConfig`: constructs the Cloudinary client from environment-backed Spring properties. No credential is stored in source.
- `NovelService`: owns validation, status checks, soft-delete behavior, and cover upload orchestration.
- Authentication and registration are outside this CRUD scope and are intentionally not included.

## 4. Security, Transactions, and Errors

- This slice does not implement authentication or authorization. Role enforcement belongs to a later security feature.
- Delete is implemented as `isDeleted = true`.
- Create/update/delete run in service-layer transactions.
- Missing novels raise a domain `ResourceNotFoundException` and are handled by the controller with a user-facing error message.
- Cloudinary credentials are read from `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`, and `CLOUDINARY_UPLOAD_PRESET` through Spring placeholders.

## 5. Validation and Checks

- Required: title, author, and summary.
- Status is constrained to `ONGOING`, `COMPLETED`, `ON_HOLD`, or `ARCHIVED`.
- Cheap discriminating check: run `mvn test` after implementation; this catches compilation, Spring context, and service test failures without requiring a live database or Cloudinary upload.

## 6. Scope Boundary

This change does not implement chapter management, category assignment, authentication/account persistence, or archive UI beyond displaying the existing status. Those functions remain separate responsibilities.