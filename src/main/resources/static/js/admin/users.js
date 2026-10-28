document.addEventListener('DOMContentLoaded', function () {
    // Modal Elements
    const roleModal = document.getElementById('roleModal');
    const banModal = document.getElementById('banModal');
    const unbanModal = document.getElementById('unbanModal');

    // Helper: Close all modals
    function closeAllModals() {
        if (roleModal) roleModal.classList.add('hidden');
        if (banModal) banModal.classList.add('hidden');
        if (unbanModal) unbanModal.classList.add('hidden');
    }

    // Attach close buttons
    document.querySelectorAll('.close-modal-btn').forEach(function (btn) {
        btn.addEventListener('click', closeAllModals);
    });

    // Close on backdrop click
    [roleModal, banModal, unbanModal].forEach(function (modal) {
        if (modal) {
            modal.addEventListener('click', function (e) {
                if (e.target === modal) closeAllModals();
            });
        }
    });

    // Close on Escape key
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeAllModals();
    });

    // -------------------------------------------------------------
    // UC-48: OPEN ROLE MODAL
    // -------------------------------------------------------------
    document.querySelectorAll('.open-role-modal').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const userId = this.getAttribute('data-user-id');
            const userName = this.getAttribute('data-user-name');
            const userEmail = this.getAttribute('data-user-email');
            const initial = this.getAttribute('data-initial') || 'U';
            const isStaff = this.getAttribute('data-is-staff') === 'true';
            const avatarUrl = this.getAttribute('data-avatar-url');

            document.getElementById('roleModalUserId').value = userId;
            document.getElementById('roleModalName').textContent = userName;
            document.getElementById('roleModalEmail').textContent = userEmail;
            document.getElementById('roleModalStaffCheckbox').checked = isStaff;

            const avatarContainer = document.getElementById('roleModalAvatar');
            avatarContainer.textContent = '';
            if (avatarUrl && avatarUrl.trim() !== '') {
                const img = document.createElement('img');
                img.src = avatarUrl;
                img.alt = 'Avatar';
                img.className = 'w-full h-full object-cover';
                avatarContainer.appendChild(img);
            } else {
                const span = document.createElement('span');
                span.id = 'roleModalInitial';
                span.textContent = initial;
                avatarContainer.appendChild(span);
            }

            roleModal.classList.remove('hidden');
        });
    });

    // -------------------------------------------------------------
    // UC-50: OPEN BAN MODAL
    // -------------------------------------------------------------
    document.querySelectorAll('.open-ban-modal').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const userId = this.getAttribute('data-user-id');
            const userName = this.getAttribute('data-user-name');
            const userEmail = this.getAttribute('data-user-email');
            const initial = this.getAttribute('data-initial') || 'U';
            const avatarUrl = this.getAttribute('data-avatar-url');

            document.getElementById('banModalUserId').value = userId;
            document.getElementById('banModalUserName').textContent = userName;
            document.getElementById('banModalUserChip').textContent = '#USR-' + userId;
            document.getElementById('banModalUserEmail').textContent = userEmail;
            document.getElementById('banNoteTextarea').value = '';
            document.getElementById('banReasonSelect').selectedIndex = 0;

            // Reset duration to 3 days default
            document.getElementById('banDurationInput').value = '3';
            document.querySelectorAll('.duration-btn').forEach(function (b) {
                if (b.getAttribute('data-duration') === '3') {
                    b.className = 'duration-btn py-2 px-2 rounded-xl text-xs font-bold border transition-colors bg-[#9e422c] text-white border-[#9e422c]';
                } else {
                    b.className = 'duration-btn py-2 px-2 rounded-xl text-xs font-bold border transition-colors bg-white text-[#524437] border-[#ead8cb] hover:bg-[#fff1e8]';
                }
            });

            const avatarContainer = document.getElementById('banModalAvatar');
            avatarContainer.textContent = '';
            if (avatarUrl && avatarUrl.trim() !== '') {
                const img = document.createElement('img');
                img.src = avatarUrl;
                img.alt = 'Avatar';
                img.className = 'w-full h-full object-cover';
                avatarContainer.appendChild(img);
            } else {
                const span = document.createElement('span');
                span.id = 'banModalInitial';
                span.textContent = initial;
                avatarContainer.appendChild(span);
            }

            banModal.classList.remove('hidden');
        });
    });

    // Duration Button selection
    document.querySelectorAll('.duration-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const duration = this.getAttribute('data-duration');
            document.getElementById('banDurationInput').value = duration;

            document.querySelectorAll('.duration-btn').forEach(function (b) {
                b.className = 'duration-btn py-2 px-2 rounded-xl text-xs font-bold border transition-colors bg-white text-[#524437] border-[#ead8cb] hover:bg-[#fff1e8]';
            });
            this.className = 'duration-btn py-2 px-2 rounded-xl text-xs font-bold border transition-colors bg-[#9e422c] text-white border-[#9e422c]';
        });
    });

    // Ban note required if reason is 'other'
    const banReasonSelect = document.getElementById('banReasonSelect');
    const banNoteTextarea = document.getElementById('banNoteTextarea');
    if (banReasonSelect && banNoteTextarea) {
        banReasonSelect.addEventListener('change', function () {
            if (this.value === 'other') {
                banNoteTextarea.required = true;
                banNoteTextarea.placeholder = 'BẮT BUỘC: Ghi rõ lý do khác và bằng chứng vi phạm...';
                banNoteTextarea.focus();
            } else {
                banNoteTextarea.required = false;
                banNoteTextarea.placeholder = 'Nhập ghi chú hoặc bằng chứng vi phạm...';
            }
        });
    }

    // -------------------------------------------------------------
    // UC-50: OPEN UNBAN MODAL
    // -------------------------------------------------------------
    const unbanTextarea = document.getElementById('unbanReasonTextarea');
    const unbanCharCount = document.getElementById('unbanCharCount');
    const unbanValidationMsg = document.getElementById('unbanValidationMsg');

    document.querySelectorAll('.open-unban-modal').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const userId = this.getAttribute('data-user-id');
            const userName = this.getAttribute('data-user-name');
            const userEmail = this.getAttribute('data-user-email');
            const initial = this.getAttribute('data-initial') || 'U';
            const avatarUrl = this.getAttribute('data-avatar-url');
            const lastReason = this.getAttribute('data-last-ban-reason') || 'Vi phạm điều khoản cộng đồng';
            const lastDate = this.getAttribute('data-last-ban-date') || 'Gần đây';

            document.getElementById('unbanModalUserId').value = userId;
            document.getElementById('unbanModalUserName').textContent = userName;
            document.getElementById('unbanModalUserChip').textContent = '#USR-' + userId;
            document.getElementById('unbanModalUserEmail').textContent = userEmail;
            document.getElementById('unbanModalLastReason').textContent = lastReason;
            document.getElementById('unbanModalLastDate').textContent = lastDate;

            if (unbanTextarea) {
                unbanTextarea.value = '';
                if (unbanCharCount) unbanCharCount.textContent = '0';
                if (unbanValidationMsg) unbanValidationMsg.classList.add('hidden');
            }

            const avatarContainer = document.getElementById('unbanModalAvatar');
            avatarContainer.textContent = '';
            if (avatarUrl && avatarUrl.trim() !== '') {
                const img = document.createElement('img');
                img.src = avatarUrl;
                img.alt = 'Avatar';
                img.className = 'w-full h-full object-cover';
                avatarContainer.appendChild(img);
            } else {
                const span = document.createElement('span');
                span.id = 'unbanModalInitial';
                span.textContent = initial;
                avatarContainer.appendChild(span);
            }

            unbanModal.classList.remove('hidden');
        });
    });

    // Unban Live Character Counter & Validation (20-500 chars)
    if (unbanTextarea && unbanCharCount) {
        unbanTextarea.addEventListener('input', function () {
            const len = this.value.trim().length;
            unbanCharCount.textContent = len;
            if (len < 20) {
                unbanCharCount.classList.add('text-red-500');
                if (unbanValidationMsg) unbanValidationMsg.classList.remove('hidden');
            } else {
                unbanCharCount.classList.remove('text-red-500');
                if (unbanValidationMsg) unbanValidationMsg.classList.add('hidden');
            }
        });
    }

    const unbanForm = document.getElementById('unbanForm');
    if (unbanForm && unbanTextarea) {
        unbanForm.addEventListener('submit', function (e) {
            const len = unbanTextarea.value.trim().length;
            if (len < 20) {
                e.preventDefault();
                alert('Vui lòng nhập lý do mở khóa tối thiểu 20 ký tự (sau khi bỏ khoảng trắng thừa).');
                unbanTextarea.focus();
            }
        });
    }
});