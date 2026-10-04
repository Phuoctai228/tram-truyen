/**
 * ================================================================================
 * TRẠM TRUYỆN - USER BOOKSHELF JAVASCRIPT
 * Handles dropdowns, delete modal, keyboard events, and dynamic progress bar rendering
 * ================================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize dynamic progress bar widths from data-progress
    initProgressBars();

    // 2. Close dropdowns on outside click
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.action-menu-container')) {
            document.querySelectorAll('.action-dropdown').forEach(d => d.classList.add('hidden'));
        }
    });

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
 * Toggle novel card 3-dots action dropdown
 */
function toggleDropdown(button) {
    const container = button.closest('.action-menu-container');
    const dropdown = container.querySelector('.action-dropdown');

    document.querySelectorAll('.action-dropdown').forEach(d => {
        if (d !== dropdown) d.classList.add('hidden');
    });

    dropdown.classList.toggle('hidden');
}

/**
 * Notify subscription toast/alert
 */
function notifySubscription(btn) {
    alert('Bạn đã bật tính năng nhận thông báo khi có chương mới!');
    const container = btn.closest('.action-menu-container');
    const dropdown = container.querySelector('.action-dropdown');
    if (dropdown) dropdown.classList.add('hidden');
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

    // Close any open dropdowns
    document.querySelectorAll('.action-dropdown').forEach(d => d.classList.add('hidden'));
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
