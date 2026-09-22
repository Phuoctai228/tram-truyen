# Screenflow Diagram for each Actor
Based on `danh_sach_chuc_nang.txt` and `phan_quyen_actor.md`, below are the Screenflow diagrams for each Actor in the system.

These diagrams are formatted with standard Mermaid syntax so you can easily copy and paste them directly into **Draw.io** (Menu: Arrange -> Insert -> Advanced -> Mermaid...).

## 1. GUEST

```mermaid
flowchart TD
    HomePage["Home Page"]
    RegisterPage["Register Page"]
    VerifyOTP["Verify OTP Page"]
    LoginPage["Login Page"]
    ForgotPass["Forgot Password Page"]
    HomeMember["Home Page (Logged In)"]
    CategoryPage["Category Page"]
    SearchResult["Search Results Page"]
    LeaderboardPage["Leaderboard Page"]
    FilterSortResult["Filtered/Sorted Novel List"]
    NovelDetail["Novel Detail Page"]
    CommentsSection["Comments Section"]
    ReadingPage["Reading Page"]

    HomePage -->|Register| RegisterPage
    RegisterPage -->|Enter Information| VerifyOTP
    VerifyOTP -->|Enter valid OTP| LoginPage
    HomePage -->|Login| LoginPage
    LoginPage -->|Forgot Password| ForgotPass
    LoginPage -->|Login with Google| HomeMember
    LoginPage -->|Enter Email/Password| HomeMember

    HomePage -->|Select Category| CategoryPage
    HomePage -->|Enter Keyword| SearchResult
    HomePage -->|Leaderboard| LeaderboardPage

    CategoryPage -->|Filter & Sort| FilterSortResult
    SearchResult -->|Filter & Sort| FilterSortResult

    HomePage -->|Click Novel| NovelDetail
    CategoryPage -->|Click Novel| NovelDetail
    SearchResult -->|Click Novel| NovelDetail
    LeaderboardPage -->|Click Novel| NovelDetail
    FilterSortResult -->|Click Novel| NovelDetail

    NovelDetail -->|Scroll Down| CommentsSection
    NovelDetail -->|Read Novel| ReadingPage
```

## 2. MEMBER

```mermaid
flowchart TD
    HomeMember["Home Page (Logged In)"]
    ProfilePage["Profile Page"]
    MailboxPage["Mailbox Page"]
    BookshelfPage["Bookshelf Page"]
    CheckinModal["Daily Check-in Popup"]
    HistoryPage["Reading History Page"]
    HomeGuest["Home Page (Guest)"]
    EditProfile["Edit Profile Page"]
    ChangePass["Change Password Page"]
    CoinHistory["Transaction History Page"]
    TopupPage["VNPay Top-up Page"]
    ReadingPage["Reading Page"]
    NovelDetail["Novel Detail Page"]
    ReviewModal["Review Popup"]
    ReportModal["Report Popup"]
    CommentsSection["Comments Section"]
    UnlockModal["Unlock Chapter Popup"]
    AudioPlayer["Audio Player"]

    HomeMember -->|Profile| ProfilePage
    HomeMember -->|Mailbox| MailboxPage
    HomeMember -->|Bookshelf| BookshelfPage
    HomeMember -->|Check-in| CheckinModal
    HomeMember -->|Reading History| HistoryPage
    HomeMember -->|Logout| HomeGuest

    ProfilePage -->|Update Profile| EditProfile
    ProfilePage -->|Change Password| ChangePass
    ProfilePage -->|Transaction History| CoinHistory
    ProfilePage -->|Top-up Coin| TopupPage
    TopupPage -->|Process Payment| ProfilePage

    BookshelfPage -->|Remove Novel| BookshelfPage
    BookshelfPage -->|Continue Reading| ReadingPage

    HomeMember -->|Click Novel| NovelDetail
    NovelDetail -->|Add to Bookshelf| NovelDetail
    NovelDetail -->|Rate Stars| ReviewModal
    NovelDetail -->|Report| ReportModal
    NovelDetail -->|Post Comment| CommentsSection

    NovelDetail -->|Unlock VIP| UnlockModal
    UnlockModal -->|Purchase Successful| ReadingPage

    NovelDetail -->|Read Novel| ReadingPage
    ReadingPage -->|Listen Audio| AudioPlayer
    ReadingPage -->|Scroll Auto-save| ReadingPage
    ReadingPage -->|Post Comment| CommentsSection
    ReadingPage -->|Report| ReportModal
```

## 3. STAFF

```mermaid
flowchart TD
    StaffDashboard["Staff Dashboard"]
    HomeGuest["Home Page (Guest)"]
    NovelManage["Internal Novel Management"]
    ReportManage["Report Resolution"]
    NotifManage["Send Notification"]
    CreateNovel["Create New Novel"]
    EditNovel["Update Novel"]
    ChapterManage["Chapter Management"]
    CreateChapter["Create New Chapter"]
    EditChapter["Edit Chapter"]
    ConfigureChapter["Configure Chapter"]

    StaffDashboard -->|Logout| HomeGuest
    StaffDashboard -->|Manage Novels| NovelManage
    StaffDashboard -->|Manage Reports| ReportManage
    StaffDashboard -->|Send Notifications| NotifManage

    NovelManage -->|Create New| CreateNovel
    NovelManage -->|Edit| EditNovel
    NovelManage -->|Archive| NovelManage
    NovelManage -->|Categorize| NovelManage

    NovelManage -->|Chapter List| ChapterManage
    ChapterManage -->|Add Chapter| CreateChapter
    ChapterManage -->|Edit| EditChapter
    ChapterManage -->|Configure VIP Coin| ConfigureChapter
    ChapterManage -->|Lock/Unlock| ChapterManage
    ChapterManage -->|Soft Delete| ChapterManage
```

## 4. ADMIN

```mermaid
flowchart TD
    AdminDashboard["Admin Dashboard"]
    NovelManage["Novel Management"]
    CategoryManage["Category Management"]
    AuditManage["Transaction Audit"]
    SysConfig["System Configuration"]
    UserManage["User Management"]
    HomeGuest["Home Page (Guest)"]
    CreateCategory["Create Category"]
    EditCategory["Edit Category"]

    AdminDashboard -->|Manage Novels| NovelManage
    AdminDashboard -->|Manage Categories| CategoryManage
    AdminDashboard -->|Audit Transactions| AuditManage
    AdminDashboard -->|Configure System| SysConfig
    AdminDashboard -->|Manage Users| UserManage
    AdminDashboard -->|Logout| HomeGuest

    NovelManage -->|Hard Delete| NovelManage

    CategoryManage -->|Create New| CreateCategory
    CategoryManage -->|Edit| EditCategory
    CategoryManage -->|Delete| CategoryManage

    UserManage -->|Assign Role| UserManage
    UserManage -->|Ban/Unban| UserManage
```
