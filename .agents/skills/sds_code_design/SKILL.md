---
name: sds_code_design
description: Specializes in creating, formatting, and generating Software Design Specification (SDS) Code Design documents (Section II: Code Designs — specifically "b. Class Specifications" and "d. Database Queries" in Template3_SDS Document.md) as individual, standalone HTML files stored inside the `docs/SDS/` folder for each use case. Enforces strict synchronization with Mermaid Class Diagrams, PlantUML Sequence Diagrams, and `database/novels.sql`, with fixed-width tables, normal-text class titles, 100% pure black Times New Roman 12pt text, and unboxed SQL queries.
---

# SDS Code Design Specification Skill (`b. Class Specifications` & `d. Database Queries`)

This skill provides the mandatory rules, workflow, and HTML/CSS template for generating **Section II: Code Designs** (**b. Class Specifications** and **d. Database Queries**) of the Software Design Specification (`docs/SDS/Template3_SDS Document.md`) as **standalone HTML files** (one HTML file per function/use case).

---

## 1. Directory Structure & File Naming Convention

All generated SDS Code Design HTML files must be organized by module inside `docs/SDS/`:

```text
docs/
└── SDS/
    ├── Template3_SDS Document.md
    ├── module_1_kiet/
    ├── module_2_tai/
    │   ├── sds_uc06_view_novel_list.html
    │   ├── sds_uc10_view_novel_details.html
    │   ├── sds_uc11_read_chapter.html
    │   ├── sds_uc18_view_bookshelf.html
    │   ├── sds_uc19_remove_novel_from_bookshelf.html
    │   ├── sds_uc20_add_novel_to_bookshelf.html
    │   ├── sds_uc25_audio_reader.html
    │   ├── sds_uc26_unlock_paid_chapter.html
    │   ├── sds_uc27_save_reading_progress.html
    │   ├── sds_uc28_view_reading_history.html
    │   └── sds_uc32_daily_checkin.html
    ├── module_3_ngoc_nguyen/
    ├── module_4_nguyen_ho_trong_dat/
    └── module_5_khanh/
```

### Naming & Separation Rules
- **One Function = One HTML File**: Never combine multiple functions/use cases into a single HTML file.
- **Filename Pattern**: `sds_uc[XX]_[function_slug].html` (synchronized with `docs/diagrams/class_mermaid/cls_mm_uc[XX]_[function_slug].mmd` and `docs/diagrams/sequence_plantuml_renumbered/sq_uc[XX]_[function_slug].plantuml`).
- **Language**: **100% English** (`Tiếng Anh`), synchronized with `Template3_SDS Document.md`, the RDS document, class/sequence diagrams, and `database/novels.sql`.

---

## 2. Mandatory Formatting & Styling Rules

Every generated HTML file must strictly obey the following visual and structural rules so it can be copied and pasted directly into Microsoft Word or Google Docs without formatting issues:

1. **Typography & Pure Black Color (`#000000`)**:
   - Font family: `'Times New Roman', Times, serif` everywhere.
   - Base font size: `12pt`, line-height: `1.5`.
   - Text color: **100% pure black (`#000000`)** on all elements (`body`, `h2`, `h3`, `p`, `table`, `th`, `td`).
   - **STRICT PROHIBITION ON `<code>` TAGS**: Do **NOT** use `<code>...</code>` tags inside `Description` or anywhere in the document. `<code>` changes the font to thin monospace (`Courier New`) and renders with a lighter/grayish appearance. All method names, parameters, types, endpoints, and SQL statements must be written in plain `Times New Roman` `12pt` `#000000`.

2. **Class & Interface Titles (`Normal Text`, NOT `Heading 4`)**:
   - Do **NOT** use `<h4>` for class or interface titles (e.g., `HomeController Class`, `HomeService Interface`), because Word maps `<h4>` to `Heading 4` and pollutes the document's Table of Contents.
   - Always use a normal `<p>` paragraph styled with `.class-title`:
     ```html
     <p class="class-title"><b><i>HomeController Class</i></b></p>
     ```

3. **Fixed Table Frame & Column Alignment (`b. Class Specifications`)**:
   - Every table must have the exact same outer frame width and vertical column alignment (`HomeController Class` / `HomeService Interface` standard).
   - Enforce `table-layout: fixed; width: 100%;` on `table` and include `<colgroup>` in every table with exact column proportions:
     - Column 1 (`No`, `.col-no`): `width: 6%; text-align: center;` (formatted as `01`, `02`, `03`, ...)
     - Column 2 (`Method`, `.col-method`): `width: 28%; font-weight: bold;`
     - Column 3 (`Description`, `.col-desc`): `width: 66%;`
   - Table header (`th`) background color must be `#fbe5d6` with bold `#000000` text and `1px solid #000000` borders.
   - Include `overflow-wrap: break-word; word-wrap: break-word; word-break: break-word;` on `th, td` so long Spring Data JPA repository method names wrap cleanly inside the `28%` `Method` column or `66%` `Description` column without ever stretching the table width.

4. **Class Scope & Method Description Structure (`b. Class Specifications`)**:
   - **Included Classes**: Include only classes/interfaces that contain methods in the function's Mermaid Class Diagram (`Controller` Class, `Service` Interface, `ServiceImpl` Class, Policy/Helper/Utility Class, and `Repository` Interfaces).
   - **Excluded Classes**: Exclude pure data-model `Entity` and `DTO` classes (which only hold fields and Lombok annotations).
   - **3-Bullet `Description` Format**: Every row in the `Description` column must follow this exact structure:
     ```html
     <b>- Input:</b> param1: Type1, param2: Type2 (or None.).<br>
     <b>- Output:</b> ReturnType.<br>
     <b>- Internal Method Processing:</b> Detailed description of validation, business logic, service/repository calls, and return value matching the source code and Sequence Diagram.
     ```

5. **Database Queries Formatting (`d. Database Queries`)**:
   - **Query Title (`.sql-item`)**:
     - Format as `<p class="sql-item">1. [Concise description of the query]</p>`.
     - **DO NOT include the repository method name in parentheses** at the end of the title (e.g., write `1. Retrieve top 12 recently updated novels`, **NOT** `1. Retrieve top 12 recently updated novels (NovelRepository.findTop12ByIsDeletedFalseOrderByUpdatedAtDesc)`).
   - **Unboxed SQL Query (`.sql-block`)**:
     - **DO NOT put a border, box, left-border bar, or gray background** around the SQL query.
     - Render the SQL query as plain `Times New Roman` `12pt` `#000000` text with `white-space: pre-wrap; margin: 4px 0 14px 0;`.
   - **Parameterized PostgreSQL**:
     - All SQL statements (`SELECT`, `INSERT`, `UPDATE`, `DELETE`) must match the exact table and column names in `database/novels.sql` and the DB interactions in the function's PlantUML Sequence Diagram.
     - Use `?` placeholders for parameters and explicitly list columns in `SELECT` clauses (avoid `SELECT *`).

---

## 3. Standard HTML & CSS Template

Use the following exact HTML5 and CSS template for every SDS Code Design file:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SDS - UC-06: View Novel List</title>
    <style>
        body {
            font-family: 'Times New Roman', Times, serif;
            font-size: 12pt;
            line-height: 1.5;
            color: #000000;
            margin: 20px 30px;
            background-color: #ffffff;
        }
        h2 { font-size: 15pt; font-weight: bold; color: #000000; margin-bottom: 8px; }
        h3 { font-size: 13pt; font-weight: bold; font-style: italic; color: #000000; margin-top: 16px; margin-bottom: 8px; }
        p { margin: 6px 0; font-family: 'Times New Roman', Times, serif; font-size: 12pt; color: #000000; }
        .class-title { font-family: 'Times New Roman', Times, serif; font-size: 12pt; font-weight: bold; font-style: italic; color: #000000; margin-top: 14px; margin-bottom: 4px; }
        table {
            border-collapse: collapse;
            width: 100%;
            table-layout: fixed;
            margin: 8px 0 18px 0;
            border: 1px solid #000000;
            color: #000000;
        }
        th, td {
            border: 1px solid #000000;
            padding: 6px 10px;
            text-align: left;
            vertical-align: top;
            font-family: 'Times New Roman', Times, serif;
            font-size: 12pt;
            color: #000000;
            overflow-wrap: break-word;
            word-wrap: break-word;
            word-break: break-word;
        }
        th {
            background-color: #fbe5d6;
            font-weight: bold;
            color: #000000;
        }
        .col-no { width: 6%; text-align: center; color: #000000; }
        .col-method { width: 28%; font-weight: bold; color: #000000; }
        .col-desc { width: 66%; color: #000000; }
        .sql-item { margin-top: 12px; margin-bottom: 4px; font-weight: bold; color: #000000; }
        .sql-block {
            margin: 4px 0 14px 0;
            font-family: 'Times New Roman', Times, serif;
            font-size: 12pt;
            color: #000000;
            white-space: pre-wrap;
        }
    </style>
</head>
<body>

    <h2>1. View Novel List (UC-06)</h2>

    <h3>b. Class Specifications</h3>

    <p class="class-title"><b><i>HomeController Class</i></b></p>
    <table>
        <colgroup>
            <col class="col-no">
            <col class="col-method">
            <col class="col-desc">
        </colgroup>
        <thead>
            <tr>
                <th class="col-no">No</th>
                <th class="col-method">Method</th>
                <th class="col-desc">Description</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td class="col-no">01</td>
                <td class="col-method">home</td>
                <td class="col-desc">
                    <b>- Input:</b> authentication: Authentication, model: Model.<br>
                    <b>- Output:</b> String (View name "home/index").<br>
                    <b>- Internal Method Processing:</b> Handles GET / request to display the public novel list on the homepage. Invokes homeService.getHomePageData() to retrieve categorized novel lists (recently updated, hot/most-viewed, completed, recommended, and newly created novels). Extracts the top 5 hot novels for the hero banner, checks whether the featured hero novel is already in the authenticated user's bookshelf via bookshelfService.isNovelInBookshelf(), populates the Spring MVC Model, and returns the "home/index" view.
                </td>
            </tr>
        </tbody>
    </table>

    <p class="class-title"><b><i>HomeService Interface</i></b></p>
    <table>
        <colgroup>
            <col class="col-no">
            <col class="col-method">
            <col class="col-desc">
        </colgroup>
        <thead>
            <tr>
                <th class="col-no">No</th>
                <th class="col-method">Method</th>
                <th class="col-desc">Description</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td class="col-no">01</td>
                <td class="col-method">getHomePageData</td>
                <td class="col-desc">
                    <b>- Input:</b> None.<br>
                    <b>- Output:</b> HomePageDTO.<br>
                    <b>- Internal Method Processing:</b> Defines the service contract to aggregate and return all public novel lists displayed on the homepage.
                </td>
            </tr>
        </tbody>
    </table>

    <h3>d. Database Queries</h3>

    <p class="sql-item">1. Retrieve top 12 recently updated novels</p>
    <div class="sql-block">SELECT id, title, slug, author, summary, cover_url, status, is_deleted, uploader_id,
       views, average_rating, rating_count, created_at, updated_at
FROM novels
WHERE is_deleted = FALSE
ORDER BY updated_at DESC
LIMIT 12;</div>

</body>
</html>
```

---

## 4. Step-by-Step Generation Workflow

When asked to generate SDS Code Design specifications (`b. Class Specifications` and `d. Database Queries`) for a use case or module:

1. **Read the Diagrams**:
   - Read the corresponding Mermaid Class Diagram (`docs/diagrams/class_mermaid/cls_mm_uc*.mmd`) to get the exact list of classes/interfaces and their methods.
   - Read the corresponding PlantUML Sequence Diagram (`docs/diagrams/sequence_plantuml_renumbered/sq_uc*.plantuml`) to trace the exact execution flow and database calls.
2. **Inspect the Source Code & Schema**:
   - Read the actual Java `Controller`, `Service`, `ServiceImpl`, Helper/Policy, and `Repository` files under `src/main/java/com/example/ WebNovel/` to verify method inputs, outputs, and internal processing logic.
   - Cross-check all table and column names against `database/novels.sql`.
3. **Generate Individual HTML Files**:
   - Write one standalone `.html` file per function in `docs/SDS/<module_folder>/` using the template in Section 3.
   - Verify:
     - All tables use `table-layout: fixed; width: 100%;` and `<colgroup>` (`6% | 28% | 66%`).
     - Class names use `<p class="class-title"><b><i>...</i></b></p>` (NOT `<h4>`).
     - No `<code>` tags exist anywhere in the file, and all text is `#000000` `Times New Roman` `12pt`.
     - Query titles in `d. Database Queries` do not have `(Repository.methodName)` in parentheses, and `.sql-block` has no border or background box.

