# Preview Use Case Module 1 - Novel and Chapter Content Administration

**Created By:** Kiet  
**Date Created:** 2026-09-20  
**Status:** Preview, not exported to HTML

## UC-01-Create Novel
- **UC ID and Name:** UC-01-Create Novel
- **Primary Actor:** Staff, Admin
- **Secondary Actors:** Cloudinary
- **Trigger:** Actor selects the create novel function.
- **Description:** Allows the Actor to initialize a novel with identification and initial description data.
- **Preconditions:**
  - **PRE-1.** Actor is logged in and has content administration permission.
  - **PRE-2.** The system is connected to the database.
- **Postconditions:**
  - **POST-1.** A new novel is stored with an appropriate initial status.
  - **POST-2.** The novel is available in the internal management list.
- **Normal Flow:**
  - **A. Create Novel Successfully**
  1. Actor accesses the create novel form.
  2. Actor enters the title, original author, and summary.
  3. The system validates required data.
  4. The system creates the novel record.
  5. The system displays the creation result.
- **Alternative Flows:**
  - **A.2 Cancel Creation**
  1. Actor selects the cancel action.
  2. The system does not save the data.
  3. The system returns Actor to the internal list.
  - **A.3 Invalid Data**
  1. The system identifies invalid form data.
  2. The system displays the fields requiring correction.
  3. Back to step 2.
- **Exceptions:**
  - **EX-4 Novel Save Failure**
  1. The system cannot save the record.
  2. The system logs the failure.
  3. The system displays an operation error message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Actor to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** Medium
- **Business Rules:** Title and author are required; a new novel must not be marked as deleted.
- **Other Information:** A cover image may be added when the novel is updated.
- **Assumptions:** Actor has a stable internet connection.

## UC-02-Update Novel
- **UC ID and Name:** UC-02-Update Novel
- **Primary Actor:** Staff, Admin
- **Secondary Actors:** Cloudinary
- **Trigger:** Actor selects a novel and chooses the update action.
- **Description:** Allows the Actor to edit novel information, publication status, and cover image.
- **Preconditions:**
  - **PRE-1.** Actor is logged in and has content administration permission.
  - **PRE-2.** The novel exists and is not soft-deleted.
- **Postconditions:**
  - **POST-1.** Updated information is stored for the novel.
  - **POST-2.** A new cover URL is linked when a cover is uploaded.
- **Normal Flow:**
  - **A. Update Novel Successfully**
  1. Actor accesses the novel edit form.
  2. The system displays the current information.
  3. Actor updates the novel data.
  4. The system validates the updated data.
  5. The system uploads a new cover when provided.
  6. The system saves the novel information.
  7. The system displays the update result.
- **Alternative Flows:**
  - **A.3 No Data Change**
  1. Actor keeps the current data.
  2. The system creates no new change.
  3. The system returns Actor to the internal detail page.
  - **A.4 Invalid Data**
  1. The system identifies invalid update data.
  2. The system displays the fields requiring correction.
  3. Back to step 3.
- **Exceptions:**
  - **EX-2 Novel Not Found**
  1. The system cannot find the novel.
  2. The system displays a data-not-found message.
  3. The system returns Actor to the internal list.
  - **EX-5 Save or Upload Failure**
  1. The system stops the update.
  2. The system logs the failure.
  3. The system displays an operation error message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Actor to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** High
- **Business Rules:** Status must be ONGOING, COMPLETED, ON_HOLD, or ARCHIVED.
- **Other Information:** The cover is stored as a URL from the storage service.
- **Assumptions:** Actor has a stable internet connection.

## UC-03-Archive Novel
- **UC ID and Name:** UC-03-Archive Novel
- **Primary Actor:** Staff, Admin
- **Secondary Actors:** None
- **Trigger:** Actor selects the archive action for a managed novel.
- **Description:** Changes a novel to ARCHIVED so that it is hidden from public categories.
- **Preconditions:**
  - **PRE-1.** Actor is logged in and has content administration permission.
  - **PRE-2.** The novel exists and is not soft-deleted.
- **Postconditions:**
  - **POST-1.** The novel status is ARCHIVED.
  - **POST-2.** The novel is excluded from public lists.
- **Normal Flow:**
  - **A. Archive Novel Successfully**
  1. Actor opens the novel management page.
  2. Actor selects the novel to archive.
  3. Actor confirms the archive action.
  4. The system checks the novel status.
  5. The system updates the status to ARCHIVED.
  6. The system displays the update result.
- **Alternative Flows:**
  - **A.3 Cancel Archive**
  1. Actor cancels the confirmation dialog.
  2. The system keeps the novel status unchanged.
  3. Back to step 1.
  - **A.4 Novel Already Archived**
  1. The system identifies that the novel is already ARCHIVED.
  2. The system keeps the data unchanged.
  3. Back to step 1.
- **Exceptions:**
  - **EX-5 Status Update Failure**
  1. The system cannot update the novel.
  2. The system logs the failure.
  3. The system displays an operation error message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Actor to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** Low
- **Business Rules:** ARCHIVED novels must not appear in public areas.
- **Other Information:** Archiving does not delete novel data.
- **Assumptions:** Actor has a stable internet connection.

## UC-04-Delete Novel
- **UC ID and Name:** UC-04-Delete Novel
- **Primary Actor:** Admin
- **Secondary Actors:** None
- **Trigger:** Admin selects the delete novel action.
- **Description:** Marks a novel as deleted and removes it from active lists without physically deleting the record.
- **Preconditions:**
  - **PRE-1.** Admin is logged in.
  - **PRE-2.** The novel exists.
- **Postconditions:**
  - **POST-1.** The novel has is_deleted = true.
  - **POST-2.** The novel no longer appears in active lists.
- **Normal Flow:**
  - **A. Delete Novel Successfully**
  1. Admin accesses the internal novel list.
  2. Admin selects the novel to delete.
  3. Admin confirms the delete action.
  4. The system checks the novel record.
  5. The system marks the novel as deleted.
  6. The system removes the novel from active lists.
  7. The system displays the deletion result.
- **Alternative Flows:**
  - **A.3 Cancel Deletion**
  1. Admin cancels the confirmation action.
  2. The system keeps the novel unchanged.
  3. Back to step 1.
- **Exceptions:**
  - **EX-4 Novel Not Found**
  1. The system cannot find the novel.
  2. The system displays a data-not-found message.
  3. Back to step 1.
  - **EX-5 Soft Delete Failure**
  1. The system cannot update the deletion flag.
  2. The system logs the failure.
  3. The system displays an operation error message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Admin to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** Low
- **Business Rules:** This function is restricted to Admin; the system uses soft deletion.
- **Other Information:** Deleted data may be retained for administration.
- **Assumptions:** Admin has a stable internet connection.

## UC-05-View Internal Novels
- **UC ID and Name:** UC-05-View Internal Novels
- **Primary Actor:** Staff, Admin
- **Secondary Actors:** None
- **Trigger:** Actor accesses the content administration area.
- **Description:** Displays all novels in the system and supports search and status filtering.
- **Preconditions:**
  - **PRE-1.** Actor is logged in and has content administration permission.
  - **PRE-2.** The system is connected to the database.
- **Postconditions:**
  - **POST-1.** Actor views the internal novel list.
- **Normal Flow:**
  - **A. View Internal Novels Successfully**
  1. Actor accesses the internal novel list.
  2. The system queries novel records.
  3. The system applies search and filter conditions when provided.
  4. The system calculates pagination data.
  5. The system displays the list and novel statuses.
- **Alternative Flows:**
  - **A.3 Empty Result**
  1. The system identifies that no novels match the conditions.
  2. The system displays an empty-list state.
  3. Back to step 1.
  - **A.3 Change Filter**
  1. Actor changes a search or status condition.
  2. The system queries the data again.
  3. The system displays the new result.
  4. Back to step 5.
- **Exceptions:**
  - **EX-2 Data Query Failure**
  1. The system cannot retrieve the novel list.
  2. The system logs the failure.
  3. The system displays a temporary service disruption message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Actor to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** High
- **Business Rules:** The internal list may include ARCHIVED or soft-deleted novels for administration.
- **Other Information:** The result must display the novel status.
- **Assumptions:** Actor has a stable internet connection.

## UC-06-Create Chapter
- **UC ID and Name:** UC-06-Create Chapter
- **Primary Actor:** Staff, Admin
- **Secondary Actors:** None
- **Trigger:** Actor selects add chapter for a novel.
- **Description:** Allows the Actor to create a chapter with a title, sequence number, and text content.
- **Preconditions:**
  - **PRE-1.** Actor is logged in and has content administration permission.
  - **PRE-2.** The parent novel exists and is not deleted.
- **Postconditions:**
  - **POST-1.** A new chapter is created for the novel.
  - **POST-2.** The chapter has a suitable default status for further configuration.
- **Normal Flow:**
  - **A. Create Chapter Successfully**
  1. Actor accesses the create chapter form.
  2. Actor enters the title, sequence number, and content.
  3. The system validates chapter data.
  4. The system creates the chapter record.
  5. The system displays the creation result.
- **Alternative Flows:**
  - **A.2 Invalid Chapter Data**
  1. The system identifies invalid chapter data.
  2. The system displays the fields requiring correction.
  3. Back to step 2.
  - **A.2 Cancel Chapter Creation**
  1. Actor cancels chapter creation.
  2. The system does not save the data.
  3. The system returns Actor to the chapter list.
- **Exceptions:**
  - **EX-4 Chapter Save Failure**
  1. The system cannot save the chapter.
  2. The system logs the failure.
  3. The system displays an operation error message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Actor to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** High
- **Business Rules:** A chapter must belong to a valid novel; its sequence number must be unique within that novel.
- **Other Information:** The chapter can be configured for publication and VIP access after creation.
- **Assumptions:** Actor has a stable internet connection.

## UC-07-Update Chapter
- **UC ID and Name:** UC-07-Update Chapter
- **Primary Actor:** Staff, Admin
- **Secondary Actors:** None
- **Trigger:** Actor selects a chapter and chooses the update action.
- **Description:** Allows the Actor to edit the title, sequence number, or text content of a chapter.
- **Preconditions:**
  - **PRE-1.** Actor is logged in and has content administration permission.
  - **PRE-2.** The chapter exists and is not soft-deleted.
- **Postconditions:**
  - **POST-1.** The updated chapter data is stored.
- **Normal Flow:**
  - **A. Update Chapter Successfully**
  1. Actor opens the chapter edit form.
  2. The system displays the current data.
  3. Actor edits the chapter data.
  4. The system validates the updated data.
  5. The system saves the chapter changes.
  6. The system displays the update result.
- **Alternative Flows:**
  - **A.3 Invalid Data**
  1. The system identifies invalid update data.
  2. The system displays the fields requiring correction.
  3. Back to step 3.
  - **A.3 Cancel Update**
  1. Actor cancels the update action.
  2. The system keeps the chapter data unchanged.
  3. Back to step 1.
- **Exceptions:**
  - **EX-2 Chapter Not Found**
  1. The system cannot find the chapter.
  2. The system displays a data-not-found message.
  3. The system returns Actor to the chapter list.
  - **EX-5 Chapter Save Failure**
  1. The system cannot save the changes.
  2. The system logs the failure.
  3. The system displays an operation error message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Actor to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** High
- **Business Rules:** The sequence number must be unique within the novel.
- **Other Information:** Published chapters should be reviewed after content changes.
- **Assumptions:** Actor has a stable internet connection.

## UC-08-Lock or Unlock Chapter
- **UC ID and Name:** UC-08-Lock or Unlock Chapter
- **Primary Actor:** Staff, Admin
- **Secondary Actors:** None
- **Trigger:** Actor selects the chapter lock-state action.
- **Description:** Allows the Actor to change a chapter to LOCKED or restore a readable state.
- **Preconditions:**
  - **PRE-1.** Actor is logged in and has content administration permission.
  - **PRE-2.** The chapter exists.
- **Postconditions:**
  - **POST-1.** The chapter lock state is updated.
  - **POST-2.** Public access reflects the new state.
- **Normal Flow:**
  - **A. Change Chapter Lock State Successfully**
  1. Actor opens the chapter list.
  2. Actor selects the chapter to change.
  3. Actor confirms the lock or unlock action.
  4. The system checks the current state.
  5. The system updates the chapter state.
  6. The system displays the change result.
- **Alternative Flows:**
  - **A.3 Cancel Action**
  1. Actor cancels the confirmation dialog.
  2. The system keeps the chapter state unchanged.
  3. Back to step 1.
  - **A.4 State Already Applied**
  1. The system identifies that the requested state already exists.
  2. The system keeps the data unchanged.
  3. Back to step 1.
- **Exceptions:**
  - **EX-4 Chapter Not Found**
  1. The system cannot find the chapter.
  2. The system displays a data-not-found message.
  3. Back to step 1.
  - **EX-5 State Update Failure**
  1. The system cannot update the chapter.
  2. The system logs the failure.
  3. The system displays an operation error message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Actor to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** Medium
- **Business Rules:** A LOCKED chapter must not be readable publicly.
- **Other Information:** Locking a chapter does not delete its content.
- **Assumptions:** Actor has a stable internet connection.

## UC-09-Delete Chapter
- **UC ID and Name:** UC-09-Delete Chapter
- **Primary Actor:** Staff, Admin
- **Secondary Actors:** None
- **Trigger:** Actor selects the delete chapter action.
- **Description:** Soft-deletes a chapter and removes it from the public table of contents.
- **Preconditions:**
  - **PRE-1.** Actor is logged in and has content administration permission.
  - **PRE-2.** The chapter exists.
- **Postconditions:**
  - **POST-1.** The chapter has is_deleted = true.
  - **POST-2.** The chapter no longer appears in the public table of contents.
- **Normal Flow:**
  - **A. Delete Chapter Successfully**
  1. Actor opens the chapter list.
  2. Actor selects the chapter to delete.
  3. Actor confirms the delete action.
  4. The system checks the chapter.
  5. The system marks the chapter as deleted.
  6. The system updates the chapter table of contents.
  7. The system displays the deletion result.
- **Alternative Flows:**
  - **A.3 Cancel Deletion**
  1. Actor cancels the confirmation action.
  2. The system keeps the chapter unchanged.
  3. Back to step 1.
- **Exceptions:**
  - **EX-4 Chapter Not Found**
  1. The system cannot find the chapter.
  2. The system displays a data-not-found message.
  3. Back to step 1.
  - **EX-5 Soft Delete Failure**
  1. The system cannot update the deletion flag.
  2. The system logs the failure.
  3. The system displays an operation error message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Actor to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** Low
- **Business Rules:** The function uses soft deletion; deleted chapters must not be publicly displayed.
- **Other Information:** The record may be retained for administration.
- **Assumptions:** Actor has a stable internet connection.

## UC-10-Configure Chapter
- **UC ID and Name:** UC-10-Configure Chapter
- **Primary Actor:** Staff, Admin
- **Secondary Actors:** None
- **Trigger:** Actor selects the chapter configuration action.
- **Description:** Allows the Actor to set publication status, VIP access, and the Coin price.
- **Preconditions:**
  - **PRE-1.** Actor is logged in and has content administration permission.
  - **PRE-2.** The chapter exists and is not soft-deleted.
- **Postconditions:**
  - **POST-1.** The chapter configuration is stored.
  - **POST-2.** Read access and Coin price reflect the new configuration.
- **Normal Flow:**
  - **A. Configure Chapter Successfully**
  1. Actor opens the chapter configuration form.
  2. The system displays the current configuration.
  3. Actor selects the chapter status.
  4. Actor selects VIP or free mode.
  5. Actor enters the Coin price when the chapter is VIP.
  6. The system validates the configuration.
  7. The system saves the chapter configuration.
  8. The system displays the configuration result.
- **Alternative Flows:**
  - **A.4 Free Chapter**
  1. Actor selects free mode.
  2. The system removes the Coin price requirement.
  3. Back to step 6.
  - **A.6 Invalid Configuration**
  1. The system identifies an invalid configuration.
  2. The system displays the fields requiring correction.
  3. Back to step 3.
  - **A.3 Cancel Configuration**
  1. Actor cancels the configuration action.
  2. The system keeps the current configuration.
  3. Back to step 1.
- **Exceptions:**
  - **EX-2 Chapter Not Found**
  1. The system cannot find the chapter.
  2. The system displays a data-not-found message.
  3. The system returns Actor to the chapter list.
  - **EX-7 Configuration Save Failure**
  1. The system cannot save the configuration.
  2. The system logs the failure.
  3. The system displays an operation error message.
  - **EX-1 Session Invalid (Not Logged In)**
  1. The system displays an authentication error message.
  2. The system redirects Actor to the login page.
- **Priority:** High, Must Have
- **Frequency of Use:** High
- **Business Rules:** Status must be LOCKED, PUBLISHED_REGULAR, PUBLISHED_VIP, or DRAFT; a VIP chapter must have a valid Coin price.
- **Other Information:** Chapter configuration determines public visibility and whether unlocking is required.
- **Assumptions:** Actor has a stable internet connection.
