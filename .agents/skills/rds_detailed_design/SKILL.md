---
name: rds_detailed_design
description: Specializes in creating, formatting, and generating Detailed Design Specifications (Section III: Design Specifications in the RDS document) as individual, standalone HTML files stored inside the `docs/Design Specification/` folder for each use case. Enforces standard HTML document formatting, the strict rule of leaving the UI prototype mockup empty with double quotes `""`, comprehensive UI fields tables, database CRUD access matrices, and concrete parameterized SQL commands.
---

# RDS Detailed Design Specification Skill

This skill provides comprehensive instructions for generating, formatting, and storing **Detailed Design Specifications** (Section III: Design Specifications of the RDS document) as **individual HTML files** corresponding to each use case within the project.

---

## 1. Directory Structure & File Organization

All generated Detailed Design Specification files must be stored inside a dedicated folder under `docs/`:

```
docs/
└── Design Specification/
    ├── module_1_kiet/
    │   ├── ds_m1_f01_create_novel.html
    │   ├── ds_m1_f02_update_novel.html
    │   ├── ds_m1_f03_archive_novel.html
    │   ├── ds_m1_f04_delete_novel.html
    │   ├── ds_m1_f06_create_chapter.html
    │   ├── ds_m1_f07_update_chapter.html
    │   ├── ds_m1_f08_lock_unlock_chapter.html
    │   ├── ds_m1_f09_delete_chapter.html
    │   └── ds_m1_f10_configure_chapter.html
    ├── module_2_tai/
    │   ├── ds_m2_f05_save_reading_progress.html
    │   ├── ds_m2_f10_view_reading_history.html
    │   └── ds_m2_f11_daily_checkin.html
    ├── module_3_ngoc_nguyen/
    │   ├── ds_m3_f09_view_coin_history.html
    │   └── ds_m3_f11_topup_coin.html
    ├── module_4_nguyen_ho_trong_dat/
    │   ├── ds_m4_f01_create_category.html
    │   ├── ds_m4_f02_update_category.html
    │   ├── ds_m4_f04_delete_category.html
    │   └── ds_m4_f05_categorize_novel.html
    └── module_5_khanh/
        ├── ds_m5_f03_resolve_reports.html
        ├── ds_m5_f04_send_notification.html
        ├── ds_m5_f05_view_notifications.html
        ├── ds_m5_f06_audit_transactions.html
        ├── ds_m5_f07_configure_system.html
        ├── ds_m5_f08_view_user_list.html
        ├── ds_m5_f09_assign_roles.html
        └── ds_m5_f10_ban_enable_user.html
```

### File Naming Convention
- Prefix: `ds_` (Design Specification)
- Module and Function code: `m[ModuleNumber]_f[FunctionNumber]_`
- Function Name slug: lowercase, underscored (e.g. `ds_m1_f01_create_novel.html`, `ds_m3_f11_topup_coin.html`)
- Each HTML file corresponds strictly to one Use Case / Function listed in `docs/Plan/danh_sach_chuc_nang.txt` and `docs/use_case/`.

---

## 2. Standard HTML Document Structure & Styling

Every HTML file must be a complete, self-contained HTML5 document adhering to academic/project typography standards:
- **Font Family**: `'Times New Roman', Times, serif`
- **Font Size**: `12pt` (Base), `14pt` - `16pt` (Headers)
- **Line Height**: `1.5`
- **Tables**: `border-collapse: collapse; width: 100%; margin: 12px 0; border: 1px solid #000;`
- **Table Cells (`th`, `td`)**: `border: 1px solid #000; padding: 6px 10px; font-size: 12pt; vertical-align: top;`
- **Table Header (`th`)**: `background-color: #f2f2f2; font-weight: bold; text-align: left;`

### Embedded CSS Template:
```html
<style>
    body {
        font-family: 'Times New Roman', Times, serif;
        font-size: 12pt;
        line-height: 1.5;
        color: #000000;
        margin: 20px 30px;
        background-color: #ffffff;
    }
    h2 { font-size: 16pt; font-weight: bold; margin-bottom: 6px; }
    h3 { font-size: 14pt; font-weight: bold; margin-bottom: 6px; }
    h4 { font-size: 13pt; font-style: italic; font-weight: bold; margin-bottom: 4px; }
    h5 { font-size: 12pt; font-weight: bold; margin-top: 14px; margin-bottom: 4px; }
    p { margin: 6px 0; }
    table {
        border-collapse: collapse;
        width: 100%;
        margin: 10px 0 16px 0;
        border: 1px solid #000000;
    }
    th, td {
        border: 1px solid #000000;
        padding: 6px 10px;
        text-align: left;
        vertical-align: top;
        font-size: 12pt;
    }
    th {
        background-color: #f2f2f2;
        font-weight: bold;
    }
    .group-header {
        background-color: #fafafa;
        font-weight: bold;
        font-style: italic;
    }
    .sql-block {
        background-color: #f9f9f9;
        border-left: 3px solid #333333;
        padding: 8px 12px;
        margin: 8px 0;
        font-family: 'Courier New', Courier, monospace;
        font-size: 11pt;
        white-space: pre-wrap;
    }
    .mockup-placeholder {
        margin: 6px 0 12px 0;
        font-size: 12pt;
    }
</style>
```

---

## 3. Detailed Specification Sections in Each HTML File

Each HTML file must contain the following standardized sections:

### 3.1 Header & Overview
- **Hierarchical Title**:
  - `<h2>[Feature Number]. [Feature Name]</h2>`
  - `<h3>[SubFeature Number] [SubFeature Name]</h3>`
  - `<h4>*[Screen/Function Identifier]. [Screen/Function Name]*</h4>`
- **Brief Description**: 1–2 concise sentences stating the objective and actor of the screen/function.
- **Related Use Cases**: Clickable link or text reference to the corresponding Use Case (e.g., `UC-M1-F01_Create Novel`).

### 3.2 UI Design Specification
- **Section Heading**: `<h5><b>UI Design</b></h5>`
- **Mockup Prototype Placeholder (`""`)**:
  - **MANDATORY STRICT RULE**: Do NOT insert images, `<img>` tags, file paths, or text like `<<Mockup prototype>>`.
  - **Must output empty double quotes `""`** directly beneath the `UI Design` heading:
    ```html
    <div class="mockup-placeholder">""</div>
    ```
- **UI Fields Table**:
  - Table Columns:
    1. `Field Name`: Exact label of UI element. Append `*` for mandatory fields (e.g. `Title*`, `Password*`).
    2. `Field Type`: Accurate UI component type (`Text Box`, `Password Box`, `Button`, `Hyperlink`, `Combo Box Single-Choice`, `Combo Box Multi-Choice`, `Datepicker`, `Checkbox`, `Radio Button`, `icon`, `Text`, `Integer`, `Image`, `File Upload`, `Text Area`).
    3. `Description`: Purpose, validation criteria, default values, and action response.
  - **Field Groupings**: Group logical sections using `<tr class="group-header"><td colspan="3">***[Group Name]***</td></tr>` (e.g., `***Search Fields***`, `***Data Table***`, `***Input Form***`, `***Data Actions***`).

### 3.3 Database Access Specification
- **Section Heading**: `<h5><b>Database Access</b></h5>`
- **CRUD Mapping Table**:
  - Columns:
    1. `Table`: Exact database table name(s) cross-referenced against `database/novels.sql`. Multiple tables separated by commas.
    2. `CRUD`: Single or combined operation codes (`C`, `R`, `U`, `D`, `CR`, `RU`, `CRUD`).
    3. `Description`: Clear explanation in business terms of what data is read or manipulated.

### 3.4 SQL Commands Specification
- **Section Heading**: `<p><b><i>SQL Commands:</i></b></p>`
- **Numbered Operations**: Match each row from the Database Access table sequentially (`1/ [Description]`, `2/ [Description]`).
- **Concrete & Parameterized SQL**:
  - Must write valid SQL matching PostgreSQL schema in `database/novels.sql`.
  - Must use `?` parameter placeholders for dynamic user input (prepared statements).
  - Explicitly list column names in `SELECT` queries (avoid `SELECT *`).

---

## 4. Complete Reference Example of an HTML Specification File

Below is the complete reference implementation for `docs/Design Specification/module_1_kiet/ds_m1_f01_create_novel.html`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Design Specification: Create Novel (M1-F01)</title>
    <style>
        body {
            font-family: 'Times New Roman', Times, serif;
            font-size: 12pt;
            line-height: 1.5;
            color: #000000;
            margin: 20px 30px;
            background-color: #ffffff;
        }
        h2 { font-size: 16pt; font-weight: bold; margin-bottom: 6px; }
        h3 { font-size: 14pt; font-weight: bold; margin-bottom: 6px; }
        h4 { font-size: 13pt; font-style: italic; font-weight: bold; margin-bottom: 4px; }
        h5 { font-size: 12pt; font-weight: bold; margin-top: 14px; margin-bottom: 4px; }
        p { margin: 6px 0; }
        table {
            border-collapse: collapse;
            width: 100%;
            margin: 10px 0 16px 0;
            border: 1px solid #000000;
        }
        th, td {
            border: 1px solid #000000;
            padding: 6px 10px;
            text-align: left;
            vertical-align: top;
            font-size: 12pt;
        }
        th {
            background-color: #f2f2f2;
            font-weight: bold;
        }
        .group-header {
            background-color: #fafafa;
            font-weight: bold;
            font-style: italic;
        }
        .sql-block {
            background-color: #f9f9f9;
            border-left: 3px solid #333333;
            padding: 8px 12px;
            margin: 8px 0;
            font-family: 'Courier New', Courier, monospace;
            font-size: 11pt;
            white-space: pre-wrap;
        }
        .mockup-placeholder {
            margin: 6px 0 12px 0;
            font-size: 12pt;
        }
    </style>
</head>
<body>

    <h2>1. Novel & Chapter Management</h2>
    <h3>1.1 Novel Management</h3>
    <h4>*a. Create Novel*</h4>

    <p>This screen allows Staff and Admin to register a new novel into the system with initial metadata.</p>
    <p>Related use cases:</p>
    <ul>
        <li><a href="../../use_case/module_1_kiet/en/uc_m1_f01_create_novel.html">UC-M1-F01_Create Novel</a></li>
    </ul>

    <h5><b>UI Design</b></h5>
    <div class="mockup-placeholder">""</div>

    <table>
        <thead>
            <tr>
                <th style="width: 25%;">Field Name</th>
                <th style="width: 25%;">Field Type</th>
                <th style="width: 50%;">Description</th>
            </tr>
        </thead>
        <tbody>
            <tr class="group-header">
                <td colspan="3">***Input Form***</td>
            </tr>
            <tr>
                <td>Title*</td>
                <td>Text Box</td>
                <td>Input the title of the novel (String, max 255 chars, unique check)</td>
            </tr>
            <tr>
                <td>Author*</td>
                <td>Text Box</td>
                <td>Input the original author's name (String, max 100 chars)</td>
            </tr>
            <tr>
                <td>Summary*</td>
                <td>Text Area</td>
                <td>Input the synopsis or introduction of the novel</td>
            </tr>
            <tr>
                <td>Cover Image</td>
                <td>File Upload</td>
                <td>Select and upload a cover photo to Cloudinary (JPG/PNG format)</td>
            </tr>
            <tr>
                <td>Initial Status*</td>
                <td>Combo Box Single-Choice</td>
                <td>Select publishing status: ONGOING (Default) or ON_HOLD</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Form Actions***</td>
            </tr>
            <tr>
                <td>Submit</td>
                <td>Button</td>
                <td>Validates inputs, uploads cover image, and saves the novel to the database</td>
            </tr>
            <tr>
                <td>Cancel</td>
                <td>Button</td>
                <td>Discards inputs and navigates back to the Internal Novel List</td>
            </tr>
        </tbody>
    </table>

    <h5><b>Database Access</b></h5>
    <table>
        <thead>
            <tr>
                <th style="width: 25%;">Table</th>
                <th style="width: 15%; text-align: center;">CRUD</th>
                <th style="width: 60%;">Description</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>novels</td>
                <td style="text-align: center;">C</td>
                <td>Insert a new novel record into the database</td>
            </tr>
            <tr>
                <td>system_audit_logs</td>
                <td style="text-align: center;">C</td>
                <td>Record novel creation activity by the logged-in staff/admin</td>
            </tr>
        </tbody>
    </table>

    <p><b><i>SQL Commands:</i></b></p>
    <p>1/ Insert a new novel record into the database</p>
    <div class="sql-block">INSERT INTO novels (title, author, summary, cover_url, status, uploader_id, is_deleted)
VALUES (?, ?, ?, ?, ?, ?, FALSE) RETURNING id;</div>

    <p>2/ Record novel creation activity by the logged-in staff/admin</p>
    <div class="sql-block">INSERT INTO system_audit_logs (admin_id, action_type, target_entity, target_id, new_value, ip_address)
VALUES (?, 'CREATE_NOVEL', 'NOVELS', ?, ?, ?);</div>

</body>
</html>
```

---

## 5. Generation Workflow

When instructed to generate Detailed Design Specifications:
1. **Locate Target Function / Use Case**: Identify module, actor, and function code from `docs/Plan/danh_sach_chuc_nang.txt`.
2. **Schema Verification**: Check `database/novels.sql` to ensure exact table names, column types, and constraints.
3. **Directory Verification**: Ensure the target directory `docs/Design Specification/[module_folder]/` exists. Create it if missing.
4. **HTML File Creation**: Write the standalone HTML file named `ds_[module_code]_[function_code]_[name].html` using the template above, strictly leaving mockup placeholder as `""`.
5. **Clean Up Temporary Scripts**: If you create any temporary helper scripts (like `.py`, `.ps1`, `.bat`) to automate the batch generation process, you MUST automatically delete those temporary script files after the generation is successfully completed.
