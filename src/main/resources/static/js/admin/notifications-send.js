        function toggleScope(scope) {
            const container = document.getElementById('recipientSearchContainer');
            const cardAll = document.getElementById('scopeAllCard');
            const cardSpec = document.getElementById('scopeSpecificCard');

            if (scope === 'SPECIFIC') {
                container.classList.remove('hidden');
                cardSpec.classList.add('selected');
                cardAll.classList.remove('selected');
            } else {
                container.classList.add('hidden');
                cardAll.classList.add('selected');
                cardSpec.classList.remove('selected');
                clearSelectedRecipient();
            }
        }

        // Init scope state
        document.addEventListener('DOMContentLoaded', function () {
            const checked = document.querySelector('input[name="scope"]:checked');
            if (checked && checked.value === 'SPECIFIC') {
                toggleScope('SPECIFIC');
            } else {
                toggleScope('ALL');
            }

            // Input character counters
            document.getElementById('titleInput').addEventListener('input', function () {
                document.getElementById('titleCounter').textContent = this.value.length + '/255 ký tự';
            });

            document.getElementById('contentInput').addEventListener('input', function () {
                document.getElementById('contentCounter').textContent = this.value.length + '/2000 ký tự';
            });

            // User autocomplete search
            const searchInput = document.getElementById('userSearchInput');
            const dropdown = document.getElementById('autocompleteDropdown');
            const spinner = document.getElementById('searchSpinner');
            let debounceTimer;

            searchInput.addEventListener('input', function () {
                clearTimeout(debounceTimer);
                const q = this.value.trim();
                if (q.length < 2) {
                    dropdown.classList.add('hidden');
                    dropdown.innerHTML = '';
                    return;
                }

                spinner.classList.remove('hidden');
                debounceTimer = setTimeout(() => {
                    fetch('/admin/notifications/search-users?q=' + encodeURIComponent(q))
                        .then(res => res.json())
                        .then(data => {
                            spinner.classList.add('hidden');
                            dropdown.innerHTML = '';
                            if (!data || data.length === 0) {
                                dropdown.innerHTML = '<div class="p-3 text-xs text-[#8a7664] text-center">Không tìm thấy người dùng phù hợp</div>';
                            } else {
                                data.forEach(user => {
                                    const item = document.createElement('div');
                                    item.className = 'p-3 hover:bg-[#faf7f2] cursor-pointer flex items-center justify-between text-xs';

                                    const leftWrapper = document.createElement('div');
                                    leftWrapper.className = 'flex items-center gap-2';

                                    const avatarEl = document.createElement('div');
                                    avatarEl.className = 'w-7 h-7 rounded-full bg-[#f4ece1] text-[#875200] font-bold flex items-center justify-center text-xs';
                                    avatarEl.textContent = user.initial || 'U';

                                    const infoWrapper = document.createElement('div');

                                    const nameEl = document.createElement('div');
                                    nameEl.className = 'font-bold text-[#2b2118]';
                                    nameEl.textContent = user.fullName || '';

                                    const emailEl = document.createElement('div');
                                    emailEl.className = 'text-[11px] text-[#8a7664]';
                                    emailEl.textContent = (user.email || '') + ' (ID: #' + user.id + ')';

                                    infoWrapper.appendChild(nameEl);
                                    infoWrapper.appendChild(emailEl);

                                    leftWrapper.appendChild(avatarEl);
                                    leftWrapper.appendChild(infoWrapper);

                                    const selectBtn = document.createElement('span');
                                    selectBtn.className = 'text-[#bc7d39] font-semibold text-[11px]';
                                    selectBtn.textContent = 'Chọn';

                                    item.appendChild(leftWrapper);
                                    item.appendChild(selectBtn);

                                    item.onclick = () => selectRecipient(user);
                                    dropdown.appendChild(item);
                                });
                            }
                            dropdown.classList.remove('hidden');
                        })
                        .catch(() => spinner.classList.add('hidden'));
                }, 300);
            });

            document.addEventListener('click', function (e) {
                if (!searchInput.contains(e.target) && !dropdown.contains(e.target)) {
                    dropdown.classList.add('hidden');
                }
            });
        });

        function selectRecipient(user) {
            document.getElementById('recipientIdInput').value = user.id;
            document.getElementById('recipientDisplayName').textContent = `${user.fullName} (${user.email})`;
            document.getElementById('selectedRecipientBadge').classList.remove('hidden');
            document.getElementById('userSearchInput').value = '';
            document.getElementById('autocompleteDropdown').classList.add('hidden');
        }

        function clearSelectedRecipient() {
            document.getElementById('recipientIdInput').value = '';
            document.getElementById('recipientDisplayName').textContent = '';
            document.getElementById('selectedRecipientBadge').classList.add('hidden');
        }

        function resetForm() {
            document.getElementById('notificationForm').reset();
            clearSelectedRecipient();
            toggleScope('ALL');
            document.getElementById('titleCounter').textContent = '0/255 ký tự';
            document.getElementById('contentCounter').textContent = '0/2000 ký tự';
        }

        // === PREVIEW MODAL LOGIC ===
        const previewModal = document.getElementById('previewNotificationModal');

        function openPreviewModal() {
            const title = (document.getElementById('titleInput')?.value || '').trim();
            const content = (document.getElementById('contentInput')?.value || '').trim();
            const scope = document.querySelector('input[name="scope"]:checked')?.value || 'ALL';
            const recipientId = (document.getElementById('recipientIdInput')?.value || '').trim();
            const recipientName = (document.getElementById('recipientDisplayName')?.textContent || '').trim();

            const warningBanner = document.getElementById('previewWarningBanner');
            const titleElem = document.getElementById('previewModalTitle');
            const contentElem = document.getElementById('previewModalContent');
            const recipientElem = document.getElementById('previewRecipientBadge');

            const warnings = [];
            if (!title) {
                warnings.push('Chưa nhập tiêu đề thông báo.');
            }
            if (!content) {
                warnings.push('Chưa nhập nội dung văn thư.');
            }
            if (scope === 'SPECIFIC' && !recipientId) {
                warnings.push('Đã chọn phạm vi "Người dùng cụ thể" nhưng chưa chọn độc giả nhận.');
            }

            if (warnings.length > 0) {
                warningBanner.innerHTML = '';
                warnings.forEach(w => {
                    const p = document.createElement('p');
                    p.className = 'flex items-center gap-1.5';
                    const icon = document.createElement('span');
                    icon.className = 'material-symbols-outlined text-[16px] text-amber-700 shrink-0';
                    icon.textContent = 'warning';
                    const text = document.createElement('span');
                    text.textContent = w;
                    p.appendChild(icon);
                    p.appendChild(text);
                    warningBanner.appendChild(p);
                });
                warningBanner.classList.remove('hidden');
            } else {
                warningBanner.innerHTML = '';
                warningBanner.classList.add('hidden');
            }

            if (scope === 'ALL') {
                recipientElem.textContent = 'Toàn hệ thống (Tất cả độc giả)';
            } else if (recipientName && recipientName !== 'Chưa chọn') {
                recipientElem.textContent = 'Độc giả: ' + recipientName + (recipientId ? ' (ID #' + recipientId + ')' : '');
            } else {
                recipientElem.textContent = 'Chưa chọn người nhận';
            }

            titleElem.textContent = title || '(Chưa nhập tiêu đề)';
            contentElem.textContent = content || '(Chưa nhập nội dung)';

            if (previewModal) {
                previewModal.classList.remove('hidden');
            }
        }

        function closePreviewModal() {
            if (previewModal) {
                previewModal.classList.add('hidden');
            }
        }

        function handlePreviewBackdropClick(e) {
            if (e.target === previewModal) {
                closePreviewModal();
            }
        }

        // === HISTORY ACTIONS & DETAIL MODAL LOGIC ===
        let currentHistoryTarget = null;
        const historyDropdown = document.getElementById('historyActionDropdown');
        const detailModal = document.getElementById('notificationDetailModal');

        function openHistoryMenu(btn, e) {
            e.stopPropagation();
            if (historyDropdown && !historyDropdown.classList.contains('hidden') && currentHistoryTarget === btn) {
                closeHistoryMenu();
                return;
            }
            currentHistoryTarget = btn;
            const rect = btn.getBoundingClientRect();
            const menuWidth = 176;
            const menuHeight = 85;

            // Position: Align right with the button
            let left = rect.right - menuWidth;
            if (left < 10) left = 10;

            // Open upwards if near bottom of viewport
            let top = rect.bottom + 4;
            if (top + menuHeight > window.innerHeight) {
                top = rect.top - menuHeight - 4;
            }

            historyDropdown.style.top = `${top}px`;
            historyDropdown.style.left = `${left}px`;
            historyDropdown.classList.remove('hidden');
        }

        function closeHistoryMenu() {
            if (historyDropdown) {
                historyDropdown.classList.add('hidden');
            }
            currentHistoryTarget = null;
        }

        function openDetailModal() {
            if (detailModal) {
                detailModal.classList.remove('hidden');
            }
        }

        function closeDetailModal() {
            if (detailModal) {
                detailModal.classList.add('hidden');
            }
        }

        function handleMenuDetail() {
            if (!currentHistoryTarget) return;
            const d = currentHistoryTarget.dataset;
            document.getElementById('detailTitle').textContent = d.title || '';
            document.getElementById('detailContent').textContent = d.content || '';
            document.getElementById('detailRecipient').textContent = d.recipient || 'Toàn hệ thống';
            document.getElementById('detailTime').textContent = d.time || '--';
            closeHistoryMenu();
            openDetailModal();
        }

        function handleMenuTemplate() {
            if (!currentHistoryTarget) return;
            const d = currentHistoryTarget.dataset;
            const titleInput = document.getElementById('titleInput');
            const contentInput = document.getElementById('contentInput');

            titleInput.value = d.title || '';
            contentInput.value = d.content || '';

            document.getElementById('titleCounter').textContent = titleInput.value.length + '/255 ký tự';
            document.getElementById('contentCounter').textContent = contentInput.value.length + '/2000 ký tự';

            closeHistoryMenu();

            titleInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
            titleInput.focus();
        }

        // Đóng dropdown khi click ra ngoài
        document.addEventListener('click', function (e) {
            if (historyDropdown && !historyDropdown.contains(e.target)) {
                closeHistoryMenu();
            }
        });

        // Đóng dropdown khi cuộn trang
        window.addEventListener('scroll', function () {
            closeHistoryMenu();
        }, { passive: true });

        // 3 cách đóng modal "Xem chi tiết":
        // 1. Nút X và nút Đóng (onclick="closeDetailModal()")
        // 2. Click nền tối (backdrop)
        if (detailModal) {
            detailModal.addEventListener('click', function (e) {
                if (e.target === detailModal) {
                    closeDetailModal();
                }
            });
        }

        // 3. Phím Escape (đóng cả modal và dropdown)
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                closeHistoryMenu();
                closeDetailModal();
                closePreviewModal();
            }
        });
    