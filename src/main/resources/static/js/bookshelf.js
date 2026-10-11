/**
 * ================================================================================
 * TRẠM TRUYỆN - USER BOOKSHELF JAVASCRIPT
 * Handles dropdowns, delete modal, keyboard events, and dynamic progress bar rendering
 * ================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize dynamic progress bar widths from data-progress
    initProgressBars();

    // 2. Synchronize "Đọc tiếp" URLs with localStorage reading history
    syncContinueReadingButtons();

    // 3. Modal backdrop click to close
    const backdrop = document.getElementById('deleteModalBackdrop');
    if (backdrop) {
        backdrop.addEventListener('click', (e) => {
            if (e.target === backdrop) {
                closeDeleteModal();
            }
        });
    }

    // 4. Escape key to close modal
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeDeleteModal();
        }
    });
});

/**
 * Renders progress bar fill widths based on data-progress attribute
 */
function initProgressBars() {
    document.querySelectorAll('[data-progress]').forEach(bar => {
        const progress = bar.getAttribute('data-progress');
        if (progress !== null && progress !== '') {
            bar.style.width = Math.min(100, Math.max(0, parseFloat(progress))) + '%';
        }
    });
}

/**
 * Synchronize "Đọc tiếp" buttons with latest chapter and reading position saved in localStorage
 */
function syncContinueReadingButtons() {
    document.querySelectorAll('.bookshelf-continue-read-btn[data-novel-slug]').forEach(btn => {
        const slug = btn.getAttribute('data-novel-slug');
        if (!slug) return;
        try {
            const raw = localStorage.getItem('tramtruyen_last_read_' + slug);
            if (!raw) return;
            const data = JSON.parse(raw);
            if (data && data.readUrl) {
                btn.href = data.readUrl;
            }
        } catch (e) {
            // Ignore parse errors
        }
    });
}


/**
 * Notify subscription toast/alert
 */
function notifySubscription(btn) {
    alert('Bạn đã bật tính năng nhận thông báo khi có chương mới!');
}

/**
 * Open Delete Confirmation Modal with book details
 */
function openDeleteModalFromBtn(btn) {
    const novelId = btn.getAttribute('data-id');
    const title = btn.getAttribute('data-title');
    const author = btn.getAttribute('data-author');
    const cover = btn.getAttribute('data-cover');
    const category = btn.getAttribute('data-category');
    const chapter = btn.getAttribute('data-chapter');

    const modalTitle = document.getElementById('modalTitle');
    const modalTitleStrong = document.getElementById('modalTitleStrong');
    const modalAuthor = document.getElementById('modalAuthor');
    const modalCover = document.getElementById('modalCover');
    const modalCategory = document.getElementById('modalCategory');
    const modalChapter = document.getElementById('modalChapter');
    const modalProgressText = document.getElementById('modalProgressText');
    const form = document.getElementById('deleteForm');

    if (modalTitle) modalTitle.textContent = title;
    if (modalTitleStrong) modalTitleStrong.textContent = '“' + title + '”';
    if (modalAuthor) modalAuthor.textContent = 'Tác giả: ' + author;
    if (modalCover) modalCover.src = cover;
    if (modalCategory) modalCategory.textContent = category;
    if (modalChapter) modalChapter.textContent = chapter;
    if (modalProgressText) modalProgressText.textContent = chapter;

    if (form) form.action = '/user/bookshelf/' + novelId + '/remove';

    const backdrop = document.getElementById('deleteModalBackdrop');
    const card = document.getElementById('deleteModalCard');

    if (backdrop && card) {
        backdrop.classList.remove('hidden');
        backdrop.classList.add('flex');
        requestAnimationFrame(() => {
            card.classList.remove('scale-95', 'opacity-0');
            card.classList.add('scale-100', 'opacity-100');
        });
    }

}

/**
 * Close Delete Confirmation Modal
 */
function closeDeleteModal() {
    const backdrop = document.getElementById('deleteModalBackdrop');
    const card = document.getElementById('deleteModalCard');
    if (!backdrop || !card) return;

    card.classList.remove('scale-100', 'opacity-100');
    card.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        backdrop.classList.remove('flex');
        backdrop.classList.add('hidden');
    }, 200);
}
