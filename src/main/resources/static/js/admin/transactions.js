    function setTypeFilter(t) {
        document.getElementById('filterType').value = t;
        document.getElementById('filterForm').submit();
    }

    function setQuickDate(type) {
        const today = new Date();
        const formatDate = (d) => {
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        };

        const startInput = document.getElementById('startDateInput');
        const endInput = document.getElementById('endDateInput');

        if (type === 'today') {
            const str = formatDate(today);
            startInput.value = str;
            endInput.value = str;
        } else if (type === '7days') {
            const past7 = new Date();
            past7.setDate(today.getDate() - 7);
            startInput.value = formatDate(past7);
            endInput.value = formatDate(today);
        } else if (type === 'month') {
            const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
            startInput.value = formatDate(firstDay);
            endInput.value = formatDate(today);
        }
        document.getElementById('filterForm').submit();
    }

    // Modal IPN Log handling
    function openIpnModal(orderCode) {
        if (!orderCode || orderCode === '—') return;
        const modal = document.getElementById('ipnModal');
        const badge = document.getElementById('modalOrderCodeBadge');
        const body = document.getElementById('ipnModalBody');

        badge.textContent = orderCode;
        body.innerHTML = `
            <div class="text-center py-6 text-[#75695d]">
                <span class="material-symbols-outlined text-[32px] animate-spin text-[#875200]">progress_activity</span>
                <p class="text-xs mt-2">Đang tải lịch sử IPN log...</p>
            </div>
        `;
        modal.classList.remove('hidden');

        fetch(`/admin/transactions/ipn-logs?orderCode=${encodeURIComponent(orderCode)}`)
            .then(res => res.json())
            .then(data => {
                if (!data || data.length === 0) {
                    body.innerHTML = `
                        <div class="text-center py-8 text-[#a09081]">
                            <span class="material-symbols-outlined text-[40px] text-[#cfc3b7] mb-1">info</span>
                            <p class="text-xs font-semibold text-[#75695d]">Chưa có log IPN cho đơn này</p>
                        </div>
                    `;
                    return;
                }

                let html = '';
                data.forEach((item, idx) => {
                    let statusClass = 'bg-[#e8f7ee] text-[#1b7a42] border-[#b8e8cb]';
                    if (item.status === 'ERROR') {
                        statusClass = 'bg-[#feebe8] text-[#c53d26] border-[#f7c8c0]';
                    } else if (item.status === 'UNMATCHED') {
                        statusClass = 'bg-[#fef8e7] text-[#9c6a15] border-[#f5e3b5]';
                    }

                    html += `
                        <div class="border border-[#ead8cb] rounded-2xl p-4 bg-[#faf6f2]">
                            <div class="flex items-center justify-between mb-2">
                                <div class="flex items-center gap-2">
                                    <span class="text-xs font-bold text-[#231a11]">Log #${idx + 1}</span>
                                    <span class="text-[11px] px-2 py-0.5 rounded-full border font-bold ${statusClass}">${escapeHtml(item.status || 'N/A')}</span>
                                </div>
                                <span class="text-[11px] text-[#75695d]">${escapeHtml(item.formattedCreatedAt || '')}</span>
                            </div>
                            <div class="text-xs text-[#75695d] mb-2 flex items-center gap-1.5">
                                <span class="material-symbols-outlined text-[14px]">router</span>
                                <span>IP Cổng thanh toán: <b class="text-[#231a11]">${escapeHtml(item.ipAddress || 'N/A')}</b></span>
                            </div>
                            <div class="mt-2">
                                <span class="text-[11px] font-bold text-[#75695d] uppercase block mb-1">Payload thô (JSON):</span>
                                <pre class="text-xs font-mono bg-white border border-[#ead8cb] rounded-xl p-3 max-h-48 overflow-y-auto text-[#231a11] whitespace-pre-wrap break-all">${escapeHtml(item.prettyPayload || '')}</pre>
                            </div>
                        </div>
                    `;
                });
                body.innerHTML = html;
            })
            .catch(err => {
                body.innerHTML = `
                    <div class="text-center py-6 text-rose-600 text-xs">
                        Lỗi khi tải IPN log: ${escapeHtml(err.message)}
                    </div>
                `;
            });
    }

    function closeIpnModal() {
        document.getElementById('ipnModal').classList.add('hidden');
    }

    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // Modal close events
    document.getElementById('ipnModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeIpnModal();
        }
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeIpnModal();
        }
    });