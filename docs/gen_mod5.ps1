$css = @"
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
"@

$mod5Dir = "docs/Design Specification/module_5_khanh"
New-Item -ItemType Directory -Force -Path $mod5Dir | Out-Null

function Save-Doc($name, $content) {
    $path = Join-Path $mod5Dir $name
    [System.IO.File]::WriteAllText($path, $content, [System.Text.Encoding]::UTF8)
    Write-Host "Created: $path"
}

# M5-F03
$m5_f03 = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Design Specification: Resolve Reports (M5-F03)</title>
$css
</head>
<body>
    <h2>5. System Administration & Moderation</h2>
    <h3>5.1 Content Moderation & Reports</h3>
    <h4>*a. Resolve Reports*</h4>

    <p>This screen allows Staff and Admin to inspect, review, and process user report tickets regarding comments, novels, or chapters.</p>
    <p>Related use cases:</p>
    <ul>
        <li><a href="../../use_case/module_5_khanh/en/uc_m5_f03_resolve_reports.html">UC-M5-F03_Resolve Reports</a></li>
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
                <td colspan="3">***Report Navigation Tabs***</td>
            </tr>
            <tr>
                <td>Comment Reports Tab</td>
                <td>Button</td>
                <td>Switches view to reported comments</td>
            </tr>
            <tr>
                <td>Content Reports Tab</td>
                <td>Button</td>
                <td>Switches view to reported chapters and novels</td>
            </tr>
            <tr>
                <td>Status Filter</td>
                <td>Combo Box Single-Choice</td>
                <td>Options: RECEIVED, PROCESSED, REJECTED</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Data Table***</td>
            </tr>
            <tr>
                <td>Report ID</td>
                <td>Integer</td>
                <td>Unique identifier of report</td>
            </tr>
            <tr>
                <td>Target Content</td>
                <td>Text</td>
                <td>Quoted content or chapter/novel title</td>
            </tr>
            <tr>
                <td>Reason</td>
                <td>Text</td>
                <td>Reporter's explanation of the violation</td>
            </tr>
            <tr>
                <td>Reporter Name</td>
                <td>Text</td>
                <td>Member who submitted the report</td>
            </tr>
            <tr>
                <td>Status</td>
                <td>Text</td>
                <td>Current status of the report</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Action Modal***</td>
            </tr>
            <tr>
                <td>Admin Note*</td>
                <td>Text Area</td>
                <td>Mandatory note recording the reason for resolution</td>
            </tr>
            <tr>
                <td>Resolution Action*</td>
                <td>Radio Button Group</td>
                <td>Options: Hide/Delete Content, Lock Chapter, Reject Report</td>
            </tr>
            <tr>
                <td>Submit Resolution</td>
                <td>Button</td>
                <td>Commits moderation action, marks report PROCESSED, and sends notification</td>
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
                <td>comment_reports</td>
                <td style="text-align: center;">RU</td>
                <td>Query and update status of comment reports</td>
            </tr>
            <tr>
                <td>content_reports</td>
                <td style="text-align: center;">RU</td>
                <td>Query and update status of content reports</td>
            </tr>
            <tr>
                <td>comments</td>
                <td style="text-align: center;">U</td>
                <td>Hide or soft-delete violating comments</td>
            </tr>
            <tr>
                <td>chapters</td>
                <td style="text-align: center;">U</td>
                <td>Lock chapters violating policies</td>
            </tr>
            <tr>
                <td>notifications</td>
                <td style="text-align: center;">C</td>
                <td>Notify reporting user regarding the outcome of their ticket</td>
            </tr>
        </tbody>
    </table>

    <p><b><i>SQL Commands:</i></b></p>
    <p>1/ Query and update status of comment reports</p>
    <div class="sql-block">SELECT cr.id, cr.comment_id, c.content AS comment_content, u.full_name AS reporter_name,
       cr.reason, cr.status, cr.created_at
FROM comment_reports cr
JOIN comments c ON cr.comment_id = c.id
JOIN users u ON cr.user_id = u.id
WHERE cr.status = ? ORDER BY cr.created_at DESC;

UPDATE comment_reports
SET status = ?, admin_note = ?, resolved_by = ?, processed_at = CURRENT_TIMESTAMP
WHERE id = ?;</div>

    <p>2/ Hide or soft-delete violating comments</p>
    <div class="sql-block">UPDATE comments SET status = 'HIDDEN' WHERE id = ?;</div>

    <p>3/ Lock chapters violating policies</p>
    <div class="sql-block">UPDATE chapters SET status = 'LOCKED', updated_at = CURRENT_TIMESTAMP WHERE id = ?;</div>

    <p>4/ Notify reporting user regarding the outcome of their ticket</p>
    <div class="sql-block">INSERT INTO notifications (user_id, title, content)
VALUES (?, 'Report Resolution Notice', ?);</div>
</body>
</html>
"@
Save-Doc "ds_m5_f03_resolve_reports.html" $m5_f03

# M5-F04
$m5_f04 = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Design Specification: Send Notification (M5-F04)</title>
$css
</head>
<body>
    <h2>5. System Administration & Moderation</h2>
    <h3>5.2 Communication & Notifications</h3>
    <h4>*a. Send Notification*</h4>

    <p>This screen allows Staff and Admin to compose and send notifications to all users or specific individuals.</p>
    <p>Related use cases:</p>
    <ul>
        <li><a href="../../use_case/module_5_khanh/en/uc_m5_f04_send_notification.html">UC-M5-F04_Send Notification</a></li>
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
                <td colspan="3">***Notification Form***</td>
            </tr>
            <tr>
                <td>Recipient Scope*</td>
                <td>Radio Button Group</td>
                <td>Options: All Users (Broadcast), Specific User</td>
            </tr>
            <tr>
                <td>Target User Email</td>
                <td>Text Box</td>
                <td>Enabled only if "Specific User" selected</td>
            </tr>
            <tr>
                <td>Title*</td>
                <td>Text Box</td>
                <td>Subject line of notification (max 255 chars)</td>
            </tr>
            <tr>
                <td>Content*</td>
                <td>Text Area</td>
                <td>Message body</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Actions***</td>
            </tr>
            <tr>
                <td>Send Notification</td>
                <td>Button</td>
                <td>Dispatches notification record(s)</td>
            </tr>
            <tr>
                <td>Reset</td>
                <td>Button</td>
                <td>Clears form inputs</td>
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
                <td>users</td>
                <td style="text-align: center;">R</td>
                <td>Validate target recipient account if specific user selected</td>
            </tr>
            <tr>
                <td>notifications</td>
                <td style="text-align: center;">C</td>
                <td>Insert broadcast or single user notification record</td>
            </tr>
            <tr>
                <td>system_audit_logs</td>
                <td style="text-align: center;">C</td>
                <td>Record notification dispatch audit record</td>
            </tr>
        </tbody>
    </table>

    <p><b><i>SQL Commands:</i></b></p>
    <p>1/ Validate target recipient account if specific user selected</p>
    <div class="sql-block">SELECT id, email, full_name FROM users WHERE email = ? AND status = 'ACTIVE';</div>

    <p>2/ Insert broadcast or single user notification record</p>
    <div class="sql-block">INSERT INTO notifications (user_id, title, content, is_read)
VALUES (?, ?, ?, FALSE);</div>

    <p>3/ Record notification dispatch audit record</p>
    <div class="sql-block">INSERT INTO system_audit_logs (admin_id, action_type, target_entity, target_id, new_value, ip_address)
VALUES (?, 'SEND_NOTIFICATION', 'NOTIFICATIONS', ?, ?, ?);</div>
</body>
</html>
"@
Save-Doc "ds_m5_f04_send_notification.html" $m5_f04

# M5-F05
$m5_f05 = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Design Specification: View Notifications (M5-F05)</title>
$css
</head>
<body>
    <h2>5. System Administration & Moderation</h2>
    <h3>5.2 Communication & Notifications</h3>
    <h4>*b. View Notifications*</h4>

    <p>This screen allows Members to receive system notifications and responses regarding submitted report tickets.</p>
    <p>Related use cases:</p>
    <ul>
        <li><a href="../../use_case/module_5_khanh/en/uc_m5_f05_view_mailbox.html">UC-M5-F05_View Notifications</a></li>
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
                <td colspan="3">***Mailbox Controls***</td>
            </tr>
            <tr>
                <td>Filter View</td>
                <td>Combo Box Single-Choice</td>
                <td>Options: All, Unread Only</td>
            </tr>
            <tr>
                <td>Mark All as Read</td>
                <td>Button</td>
                <td>Sets all unread notifications to read status</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Notification List***</td>
            </tr>
            <tr>
                <td>Title</td>
                <td>Text</td>
                <td>Notification subject</td>
            </tr>
            <tr>
                <td>Message Preview</td>
                <td>Text</td>
                <td>First 100 characters of content</td>
            </tr>
            <tr>
                <td>Date Received</td>
                <td>Text</td>
                <td>Timestamp when notification arrived</td>
            </tr>
            <tr>
                <td>Status Badge</td>
                <td>Text</td>
                <td>"Unread" (highlighted) or "Read"</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Actions***</td>
            </tr>
            <tr>
                <td>Mark Read</td>
                <td>icon</td>
                <td>Clicks to toggle read state</td>
            </tr>
            <tr>
                <td>Delete</td>
                <td>icon</td>
                <td>Removes notification from user mailbox</td>
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
                <td>notifications</td>
                <td style="text-align: center;">RUD</td>
                <td>Fetch notifications for user, mark as read, or delete</td>
            </tr>
        </tbody>
    </table>

    <p><b><i>SQL Commands:</i></b></p>
    <p>1/ Fetch notifications for user</p>
    <div class="sql-block">SELECT id, title, content, is_read, created_at
FROM notifications
WHERE user_id = ? OR user_id IS NULL
ORDER BY created_at DESC LIMIT ? OFFSET ?;</div>

    <p>2/ Mark as read</p>
    <div class="sql-block">UPDATE notifications SET is_read = TRUE WHERE id = ? AND (user_id = ? OR user_id IS NULL);</div>

    <p>3/ Mark all notifications as read</p>
    <div class="sql-block">UPDATE notifications SET is_read = TRUE WHERE user_id = ? OR user_id IS NULL;</div>
</body>
</html>
"@
Save-Doc "ds_m5_f05_view_notifications.html" $m5_f05

# M5-F06
$m5_f06 = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Design Specification: Audit Transactions (M5-F06)</title>
$css
</head>
<body>
    <h2>5. System Administration & Moderation</h2>
    <h3>5.4 System Configuration & Audit</h3>
    <h4>*a. Audit Transactions*</h4>

    <p>This screen allows Admin to audit all balance fluctuations including VNPay deposits and Coin usage across the entire system.</p>
    <p>Related use cases:</p>
    <ul>
        <li><a href="../../use_case/module_5_khanh/en/uc_m5_f06_audit_transactions.html">UC-M5-F06_Audit Transactions</a></li>
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
                <td colspan="3">***Audit Filters***</td>
            </tr>
            <tr>
                <td>User Email</td>
                <td>Text Box</td>
                <td>Filter by user email</td>
            </tr>
            <tr>
                <td>Type</td>
                <td>Combo Box Single-Choice</td>
                <td>Values: All (Default), TOPUP_COIN, UNLOCK_CHAPTER, GIFT_COIN</td>
            </tr>
            <tr>
                <td>From Date</td>
                <td>Datepicker</td>
                <td>Start date of audit window</td>
            </tr>
            <tr>
                <td>To Date</td>
                <td>Datepicker</td>
                <td>End date of audit window</td>
            </tr>
            <tr>
                <td>Search</td>
                <td>Button</td>
                <td>Executes query</td>
            </tr>
            <tr>
                <td>Export Report</td>
                <td>Button</td>
                <td>Exports transaction log to CSV</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Summary Cards***</td>
            </tr>
            <tr>
                <td>Total Deposited</td>
                <td>Text</td>
                <td>Total Coins bought via VNPay</td>
            </tr>
            <tr>
                <td>Total Consumed</td>
                <td>Text</td>
                <td>Total Coins spent unlocking chapters</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Audit Ledger Table***</td>
            </tr>
            <tr>
                <td>ID</td>
                <td>Integer</td>
                <td>Transaction primary key</td>
            </tr>
            <tr>
                <td>User Email</td>
                <td>Text</td>
                <td>User involved in transaction</td>
            </tr>
            <tr>
                <td>Amount</td>
                <td>Text</td>
                <td>Fluctuation amount (+ / -)</td>
            </tr>
            <tr>
                <td>Balance After</td>
                <td>Text</td>
                <td>Remaining wallet balance</td>
            </tr>
            <tr>
                <td>Type</td>
                <td>Text</td>
                <td>Transaction category</td>
            </tr>
            <tr>
                <td>Reference</td>
                <td>Text</td>
                <td>Associated order code or chapter ID</td>
            </tr>
            <tr>
                <td>Timestamp</td>
                <td>Text</td>
                <td>Date and time recorded</td>
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
                <td>transactions, users</td>
                <td style="text-align: center;">R</td>
                <td>Query all transaction logs and compute system-wide coin statistics</td>
            </tr>
        </tbody>
    </table>

    <p><b><i>SQL Commands:</i></b></p>
    <p>1/ Query all transaction logs</p>
    <div class="sql-block">SELECT t.id, t.user_id, u.email AS user_email, t.amount, t.balance_after,
       t.type, t.reference_id, t.description, t.created_at
FROM transactions t
JOIN users u ON t.user_id = u.id
WHERE (u.email ILIKE ? OR ? IS NULL) AND (t.type = ? OR ? IS NULL)
  AND t.created_at >= ? AND t.created_at <= ?
ORDER BY t.created_at DESC LIMIT ? OFFSET ?;</div>

    <p>2/ Compute system-wide coin statistics</p>
    <div class="sql-block">SELECT SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) AS total_deposited,
       SUM(CASE WHEN amount < 0 THEN ABS(amount) ELSE 0 END) AS total_consumed
FROM transactions
WHERE created_at >= ? AND created_at <= ?;</div>
</body>
</html>
"@
Save-Doc "ds_m5_f06_audit_transactions.html" $m5_f06

# M5-F07
$m5_f07 = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Design Specification: Configure System (M5-F07)</title>
$css
</head>
<body>
    <h2>5. System Administration & Moderation</h2>
    <h3>5.4 System Configuration & Audit</h3>
    <h4>*b. Configure System*</h4>

    <p>This screen allows Admin to configure global settings: exchange rates, system contacts, terms of use, and web themes.</p>
    <p>Related use cases:</p>
    <ul>
        <li><a href="../../use_case/module_5_khanh/en/uc_m5_f07_configure_system.html">UC-M5-F07_Configure System</a></li>
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
                <td colspan="3">***General Settings Tab***</td>
            </tr>
            <tr>
                <td>Exchange Rate*</td>
                <td>Text Box</td>
                <td>Conversion rate (e.g. 1000 VND = 100 Coins)</td>
            </tr>
            <tr>
                <td>Support Email*</td>
                <td>Text Box</td>
                <td>Official contact email for user inquiries</td>
            </tr>
            <tr>
                <td>Terms of Service*</td>
                <td>Text Area</td>
                <td>Policy and community guidelines text</td>
            </tr>
            <tr>
                <td>Save General Settings</td>
                <td>Button</td>
                <td>Updates system_settings key-value pairs</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Theme Settings Tab***</td>
            </tr>
            <tr>
                <td>Theme Name*</td>
                <td>Text Box</td>
                <td>Name of the web theme (e.g. "Lunar New Year 2026")</td>
            </tr>
            <tr>
                <td>Folder Path*</td>
                <td>Text Box</td>
                <td>Server directory path for CSS/assets</td>
            </tr>
            <tr>
                <td>Thumbnail Upload</td>
                <td>File Upload</td>
                <td>Upload thumbnail preview of the theme</td>
            </tr>
            <tr>
                <td>Active Themes List</td>
                <td>Combo Box Single-Choice</td>
                <td>Selects which theme is currently active</td>
            </tr>
            <tr>
                <td>Apply Theme</td>
                <td>Button</td>
                <td>Activates selected theme across the site</td>
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
                <td>system_settings</td>
                <td style="text-align: center;">RU</td>
                <td>Retrieve and update key-value parameters</td>
            </tr>
            <tr>
                <td>web_settings</td>
                <td style="text-align: center;">CRU</td>
                <td>Manage web themes list and toggle active theme</td>
            </tr>
            <tr>
                <td>system_audit_logs</td>
                <td style="text-align: center;">C</td>
                <td>Audit system configuration changes</td>
            </tr>
        </tbody>
    </table>

    <p><b><i>SQL Commands:</i></b></p>
    <p>1/ Retrieve and update key-value parameters</p>
    <div class="sql-block">SELECT setting_key, setting_value, description FROM system_settings;

UPDATE system_settings
SET setting_value = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP
WHERE setting_key = ?;</div>

    <p>2/ Manage web themes list and toggle active theme</p>
    <div class="sql-block">SELECT id, name, folder_path, thumbnail_url, is_active FROM web_settings ORDER BY created_at DESC;

UPDATE web_settings SET is_active = FALSE WHERE is_active = TRUE;
UPDATE web_settings SET is_active = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = ?;</div>

    <p>3/ Audit system configuration changes</p>
    <div class="sql-block">INSERT INTO system_audit_logs (admin_id, action_type, target_entity, target_id, old_value, new_value, ip_address)
VALUES (?, 'UPDATE_SYSTEM_SETTINGS', 'SYSTEM_SETTINGS', NULL, ?, ?, ?);</div>
</body>
</html>
"@
Save-Doc "ds_m5_f07_configure_system.html" $m5_f07

# M5-F08
$m5_f08 = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Design Specification: View User List (M5-F08)</title>
$css
</head>
<body>
    <h2>5. System Administration & Moderation</h2>
    <h3>5.3 User & Staff Administration</h3>
    <h4>*a. View User List*</h4>

    <p>This screen allows Admin to retrieve, search, and filter the list of all registered accounts in the system.</p>
    <p>Related use cases:</p>
    <ul>
        <li><a href="../../use_case/module_5_khanh/en/uc_m5_f08_view_user_list.html">UC-M5-F08_View User List</a></li>
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
                <td colspan="3">***Search & Filter Bar***</td>
            </tr>
            <tr>
                <td>Search Keyword</td>
                <td>Text Box</td>
                <td>Search by user email or full name</td>
            </tr>
            <tr>
                <td>Role Filter</td>
                <td>Combo Box Single-Choice</td>
                <td>Values: All Roles (Default), ROLE_ADMIN, ROLE_STAFF, ROLE_MEMBER</td>
            </tr>
            <tr>
                <td>Status Filter</td>
                <td>Combo Box Single-Choice</td>
                <td>Values: All Statuses (Default), ACTIVE, PENDING_VERIFICATION, BANNED</td>
            </tr>
            <tr>
                <td>Search</td>
                <td>Button</td>
                <td>Executes query with specified filters</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***User Data Table***</td>
            </tr>
            <tr>
                <td>User ID</td>
                <td>Integer</td>
                <td>Auto-incremented primary key</td>
            </tr>
            <tr>
                <td>Avatar</td>
                <td>Image</td>
                <td>User avatar thumbnail</td>
            </tr>
            <tr>
                <td>Full Name</td>
                <td>Text</td>
                <td>User's full display name</td>
            </tr>
            <tr>
                <td>Email</td>
                <td>Text</td>
                <td>User's unique login email</td>
            </tr>
            <tr>
                <td>Role(s)</td>
                <td>Text</td>
                <td>Assigned roles</td>
            </tr>
            <tr>
                <td>Wallet Balance</td>
                <td>Integer</td>
                <td>Current Coin balance</td>
            </tr>
            <tr>
                <td>Status</td>
                <td>Text</td>
                <td>Account status badge</td>
            </tr>
            <tr>
                <td>Joined Date</td>
                <td>Text</td>
                <td>Registration timestamp</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Data Actions***</td>
            </tr>
            <tr>
                <td>Manage Roles</td>
                <td>icon</td>
                <td>Opens Assign Roles dialog</td>
            </tr>
            <tr>
                <td>Ban/Enable</td>
                <td>icon</td>
                <td>Triggers Ban/Enable user action</td>
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
                <td>users, user_roles, roles</td>
                <td style="text-align: center;">R</td>
                <td>Query user accounts with aggregated roles matching filters</td>
            </tr>
        </tbody>
    </table>

    <p><b><i>SQL Commands:</i></b></p>
    <p>1/ Query user accounts with aggregated roles matching filters</p>
    <div class="sql-block">SELECT u.id, u.email, u.full_name, u.avatar_url, u.wallet_balance, u.status, u.created_at,
       STRING_AGG(r.name, ', ') AS role_names
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE (u.email ILIKE ? OR u.full_name ILIKE ?) AND (u.status = ? OR ? IS NULL)
GROUP BY u.id, u.email, u.full_name, u.avatar_url, u.wallet_balance, u.status, u.created_at
ORDER BY u.created_at DESC LIMIT ? OFFSET ?;</div>
</body>
</html>
"@
Save-Doc "ds_m5_f08_view_user_list.html" $m5_f08

# M5-F09
$m5_f09 = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Design Specification: Assign Roles (M5-F09)</title>
$css
</head>
<body>
    <h2>5. System Administration & Moderation</h2>
    <h3>5.3 User & Staff Administration</h3>
    <h4>*b. Assign Roles*</h4>

    <p>This screen allows Admin to grant or revoke user roles such as Staff or Admin.</p>
    <p>Related use cases:</p>
    <ul>
        <li><a href="../../use_case/module_5_khanh/en/uc_m5_f09_manage_staffs.html">UC-M5-F09_Assign Roles</a></li>
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
                <td colspan="3">***Role Assignment Form***</td>
            </tr>
            <tr>
                <td>User Name</td>
                <td>Text</td>
                <td>Full name of target user (Read-only)</td>
            </tr>
            <tr>
                <td>User Email</td>
                <td>Text</td>
                <td>Email address of target user (Read-only)</td>
            </tr>
            <tr>
                <td>Role Selection*</td>
                <td>Checkbox Group</td>
                <td>Checkbox options: ROLE_MEMBER, ROLE_STAFF, ROLE_ADMIN</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Actions***</td>
            </tr>
            <tr>
                <td>Save Roles</td>
                <td>Button</td>
                <td>Updates user_roles mapping table</td>
            </tr>
            <tr>
                <td>Cancel</td>
                <td>Button</td>
                <td>Dismisses dialog</td>
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
                <td>roles</td>
                <td style="text-align: center;">R</td>
                <td>Fetch all existing roles</td>
            </tr>
            <tr>
                <td>user_roles</td>
                <td style="text-align: center;">RCD</td>
                <td>Query, insert, and delete user role mappings</td>
            </tr>
            <tr>
                <td>system_audit_logs</td>
                <td style="text-align: center;">C</td>
                <td>Audit role assignment modifications</td>
            </tr>
        </tbody>
    </table>

    <p><b><i>SQL Commands:</i></b></p>
    <p>1/ Query, insert, and delete user role mappings</p>
    <div class="sql-block">SELECT r.id, r.name,
       CASE WHEN ur.user_id IS NOT NULL THEN TRUE ELSE FALSE END AS is_assigned
FROM roles r
LEFT JOIN user_roles ur ON r.id = ur.role_id AND ur.user_id = ?;

DELETE FROM user_roles WHERE user_id = ? AND role_id NOT IN (?);

INSERT INTO user_roles (user_id, role_id) VALUES (?, ?) ON CONFLICT DO NOTHING;</div>

    <p>2/ Audit role assignment modifications</p>
    <div class="sql-block">INSERT INTO system_audit_logs (admin_id, action_type, target_entity, target_id, old_value, new_value, ip_address)
VALUES (?, 'ASSIGN_ROLE', 'USER_ROLES', ?, ?, ?, ?);</div>
</body>
</html>
"@
Save-Doc "ds_m5_f09_assign_roles.html" $m5_f09

# M5-F10
$m5_f10 = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Design Specification: Ban/Enable User (M5-F10)</title>
$css
</head>
<body>
    <h2>5. System Administration & Moderation</h2>
    <h3>5.3 User & Staff Administration</h3>
    <h4>*c. Ban/Enable User*</h4>

    <p>This function allows Admin to ban or re-enable user accounts that violate community terms.</p>
    <p>Related use cases:</p>
    <ul>
        <li><a href="../../use_case/module_5_khanh/en/uc_m5_f10_ban_enable_user.html">UC-M5-F10_Ban/Enable User</a></li>
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
                <td colspan="3">***Account Status Modal***</td>
            </tr>
            <tr>
                <td>Target User</td>
                <td>Text</td>
                <td>User email and name display</td>
            </tr>
            <tr>
                <td>Action Type</td>
                <td>Text</td>
                <td>"Ban User" or "Enable User"</td>
            </tr>
            <tr>
                <td>Reason*</td>
                <td>Text Area</td>
                <td>Mandatory audit justification for banning or unbanning account</td>
            </tr>
            <tr class="group-header">
                <td colspan="3">***Actions***</td>
            </tr>
            <tr>
                <td>Confirm Action</td>
                <td>Button</td>
                <td>Updates user status to BANNED or ACTIVE</td>
            </tr>
            <tr>
                <td>Cancel</td>
                <td>Button</td>
                <td>Dismisses modal without change</td>
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
                <td>users</td>
                <td style="text-align: center;">U</td>
                <td>Update status column to BANNED or ACTIVE</td>
            </tr>
            <tr>
                <td>system_audit_logs</td>
                <td style="text-align: center;">C</td>
                <td>Record ban/enable action log</td>
            </tr>
        </tbody>
    </table>

    <p><b><i>SQL Commands:</i></b></p>
    <p>1/ Update status column to BANNED or ACTIVE</p>
    <div class="sql-block">UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?;</div>

    <p>2/ Record ban/enable action log</p>
    <div class="sql-block">INSERT INTO system_audit_logs (admin_id, action_type, target_entity, target_id, old_value, new_value, ip_address)
VALUES (?, ?, 'USERS', ?, ?, ?, ?);</div>
</body>
</html>
"@
Save-Doc "ds_m5_f10_ban_enable_user.html" $m5_f10
