/**
 * ========================================================
 * COIN PACKAGE MANAGEMENT MODAL CONTROLLER (ADMIN)
 * Quản lý các Modal Thêm, Sửa, Xóa gói nạp Coin
 * Tuân thủ cơ chế thông báo lỗi chuẩn của Trạm Truyện
 * ========================================================
 */

/**
 * Hiển thị thông báo Toast lỗi chuẩn của Trạm Truyện (đồng bộ với flash-toast).
 */
function showAdminErrorToast(message) {
    let container = document.getElementById('admin-toast');
    if (!container) {
        container = document.createElement('div');
        container.id = 'admin-toast';
        container.className = 'fixed top-20 right-6 z-[100] flex flex-col gap-3 animate-fade-in pointer-events-none';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = 'bg-[#fff4f2] border-l-4 border-[#9e422c] rounded-r-xl p-4 text-[#7e2b17] flex items-start gap-3 shadow-lg max-w-sm pointer-events-auto transition-all duration-300';
    toast.innerHTML = `
        <span class="material-symbols-outlined text-[#9e422c] text-[24px] mt-0.5">error</span>
        <div class="flex flex-col">
            <span class="text-xs font-bold leading-relaxed">${message}</span>
        </div>
        <button type="button" class="text-[#9e422c] opacity-70 hover:opacity-100 transition-opacity ml-auto">
            <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
    `;
    const closeBtn = toast.querySelector('button');
    if (closeBtn) {
        closeBtn.addEventListener('click', function () {
            toast.remove();
        });
    }
    container.appendChild(toast);
    setTimeout(function () {
        toast.style.opacity = '0';
        setTimeout(function () {
            toast.remove();
        }, 400);
    }, 5000);
}

/**
 * Kiểm tra tính hợp lệ của giá tiền và cập nhật số Coin tự động (1.000đ = 10 Coin).
 *
 * @param {HTMLInputElement} priceInput Input giá tiền
 * @param {HTMLInputElement} coinInput Input số Coin (readonly)
 * @param {HTMLElement} errorBox Phần tử chứa khung thông báo lỗi hệ thống
 * @returns {string|null} Chuỗi thông báo lỗi nếu không hợp lệ, null nếu hợp lệ
 */
function validateAndSyncCoin(priceInput, coinInput, errorBox) {
    if (!priceInput || !coinInput) return null;

    const rawVal = priceInput.value.trim();
    if (!rawVal) {
        coinInput.value = '';
        if (errorBox) {
            errorBox.classList.add('hidden');
            const msgEl = errorBox.querySelector('.error-msg');
            if (msgEl) msgEl.textContent = '';
        }
        priceInput.classList.remove('border-[#9e422c]', 'ring-1', 'ring-[#9e422c]');
        priceInput.classList.add('border-[#eae2d5]');
        return null;
    }

    const price = parseInt(rawVal, 10);
    if (isNaN(price) || price <= 0) {
        coinInput.value = '';
    } else {
        // Tự động tính số Coin: 1.000 VNĐ = 10 Coin (100 VNĐ = 1 Coin)
        coinInput.value = Math.floor(price / 100);
    }

    let error = null;
    if (isNaN(price) || price < 10000) {
        error = 'Giá gói nạp tối thiểu là 10.000 VNĐ.';
    } else if (price > 2000000) {
        error = 'Giá gói nạp tối đa là 2.000.000 VNĐ.';
    } else if (price % 1000 !== 0) {
        error = 'Giá gói nạp phải là số tròn nghìn (bội số của 1.000 VNĐ), không được nhập số lẻ như '
            + price.toLocaleString('vi-VN') + ' VNĐ.';
    }

    if (errorBox) {
        const msgEl = errorBox.querySelector('.error-msg');
        if (error) {
            if (msgEl) msgEl.textContent = error;
            errorBox.classList.remove('hidden');
            priceInput.classList.remove('border-[#eae2d5]');
            priceInput.classList.add('border-[#9e422c]', 'ring-1', 'ring-[#9e422c]');
        } else {
            if (msgEl) msgEl.textContent = '';
            errorBox.classList.add('hidden');
            priceInput.classList.remove('border-[#9e422c]', 'ring-1', 'ring-[#9e422c]');
            priceInput.classList.add('border-[#eae2d5]');
        }
    }

    return error;
}

function openAddPackageModal() {
    const modal = document.getElementById('addPackageModal');
    if (!modal) return;

    const form = document.getElementById('addPackageForm');
    if (form) form.reset();

    const priceInput = document.getElementById('addPackagePrice');
    const coinInput = document.getElementById('addPackageCoin');
    const errorBox = document.getElementById('addPackagePriceError');
    if (coinInput) coinInput.value = '';
    if (errorBox) errorBox.classList.add('hidden');
    if (priceInput) {
        priceInput.classList.remove('border-[#9e422c]', 'ring-1', 'ring-[#9e422c]');
        priceInput.classList.add('border-[#eae2d5]');
    }

    const warningEl = document.getElementById('addPackageActiveWarning');
    if (warningEl) {
        const currentActive = parseInt(document.getElementById('coinPackageBridge')?.dataset.activeCount || '0', 10);
        if (currentActive >= 6) {
            warningEl.classList.remove('hidden');
        } else {
            warningEl.classList.add('hidden');
        }
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
    document.body.classList.add('overflow-hidden');
}

function closeAddPackageModal() {
    const modal = document.getElementById('addPackageModal');
    if (!modal) return;
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    document.body.classList.remove('overflow-hidden');
}

function openEditPackageModal(btn) {
    const modal = document.getElementById('editPackageModal');
    if (!modal || !btn) return;

    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name') || '';
    const price = btn.getAttribute('data-price') || '';
    const status = btn.getAttribute('data-status') || 'ACTIVE';

    const form = document.getElementById('editPackageForm');
    if (form) {
        form.action = '/admin/settings/packages/' + id + '/edit';
    }

    const nameInput = document.getElementById('editPackageName');
    if (nameInput) nameInput.value = name;

    const priceInput = document.getElementById('editPackagePrice');
    if (priceInput) priceInput.value = price;

    const coinInput = document.getElementById('editPackageCoin');
    const errorBox = document.getElementById('editPackagePriceError');

    // Đồng bộ tính số Coin tự động cho gói đang sửa
    validateAndSyncCoin(priceInput, coinInput, errorBox);

    const statusSelect = document.getElementById('editPackageStatus');
    if (statusSelect) {
        statusSelect.value = status;
        statusSelect.setAttribute('data-original-status', status);
    }

    const warningEl = document.getElementById('editPackageActiveWarning');
    if (warningEl) {
        warningEl.classList.add('hidden');
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
    document.body.classList.add('overflow-hidden');
}

function closeEditPackageModal() {
    const modal = document.getElementById('editPackageModal');
    if (!modal) return;
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    document.body.classList.remove('overflow-hidden');
}

function openDeletePackageModal(btn) {
    const modal = document.getElementById('deletePackageModal');
    if (!modal || !btn) return;

    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name') || '';

    const form = document.getElementById('deletePackageForm');
    if (form) {
        form.action = '/admin/settings/packages/' + id + '/delete';
    }

    const nameSpan = document.getElementById('deletePackageName');
    if (nameSpan) {
        nameSpan.textContent = name;
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
    document.body.classList.add('overflow-hidden');
}

function closeDeletePackageModal() {
    const modal = document.getElementById('deletePackageModal');
    if (!modal) return;
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    document.body.classList.remove('overflow-hidden');
}

document.addEventListener('DOMContentLoaded', function () {
    // Phím Esc để đóng modal đang mở
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            closeAddPackageModal();
            closeEditPackageModal();
            closeDeletePackageModal();
        }
    });

    // Lắng nghe sự kiện gõ giá trên form Thêm mới -> Tự động tính Coin & Validate
    const addPrice = document.getElementById('addPackagePrice');
    const addCoin = document.getElementById('addPackageCoin');
    const addError = document.getElementById('addPackagePriceError');
    if (addPrice && addCoin) {
        addPrice.addEventListener('input', function () {
            validateAndSyncCoin(addPrice, addCoin, addError);
        });
    }

    // Lắng nghe sự kiện gõ giá trên form Chỉnh sửa -> Tự động tính Coin & Validate
    const editPrice = document.getElementById('editPackagePrice');
    const editCoin = document.getElementById('editPackageCoin');
    const editError = document.getElementById('editPackagePriceError');
    if (editPrice && editCoin) {
        editPrice.addEventListener('input', function () {
            validateAndSyncCoin(editPrice, editCoin, editError);
        });
    }

    // Kiểm tra giới hạn 6 gói khi đổi trạng thái ở form sửa
    const editStatus = document.getElementById('editPackageStatus');
    if (editStatus) {
        editStatus.addEventListener('change', function () {
            const originalStatus = editStatus.getAttribute('data-original-status');
            const warningEl = document.getElementById('editPackageActiveWarning');
            const currentActive = parseInt(document.getElementById('coinPackageBridge')?.dataset.activeCount || '0', 10);

            if (editStatus.value === 'ACTIVE' && originalStatus !== 'ACTIVE' && currentActive >= 6) {
                if (warningEl) warningEl.classList.remove('hidden');
            } else {
                if (warningEl) warningEl.classList.add('hidden');
            }
        });
    }

    // Kiểm tra giới hạn 6 gói khi chọn trạng thái ở form thêm
    const addStatus = document.getElementById('addPackageStatus');
    if (addStatus) {
        addStatus.addEventListener('change', function () {
            const warningEl = document.getElementById('addPackageActiveWarning');
            const currentActive = parseInt(document.getElementById('coinPackageBridge')?.dataset.activeCount || '0', 10);

            if (addStatus.value === 'ACTIVE' && currentActive >= 6) {
                if (warningEl) warningEl.classList.remove('hidden');
            } else {
                if (warningEl) warningEl.classList.add('hidden');
            }
        });
    }

    // Xử lý submit form Thêm mới với thông báo lỗi hệ thống
    const addForm = document.getElementById('addPackageForm');
    if (addForm) {
        addForm.addEventListener('submit', function (e) {
            const nameInput = document.getElementById('addPackageName');
            if (!nameInput || !nameInput.value.trim()) {
                e.preventDefault();
                showAdminErrorToast('Tên gói nạp không được để trống.');
                if (nameInput) nameInput.focus();
                return;
            }

            const error = validateAndSyncCoin(addPrice, addCoin, addError);
            if (error) {
                e.preventDefault();
                showAdminErrorToast(error);
                if (addPrice) addPrice.focus();
                return;
            }

            // Đảm bảo trường coinAmount có giá trị hợp lệ trước khi submit
            if (addPrice && addCoin && (!addCoin.value || addCoin.value <= 0)) {
                addCoin.value = Math.floor(parseInt(addPrice.value, 10) / 100);
            }
        });
    }

    // Xử lý submit form Sửa với thông báo lỗi hệ thống
    const editForm = document.getElementById('editPackageForm');
    if (editForm) {
        editForm.addEventListener('submit', function (e) {
            const nameInput = document.getElementById('editPackageName');
            if (!nameInput || !nameInput.value.trim()) {
                e.preventDefault();
                showAdminErrorToast('Tên gói nạp không được để trống.');
                if (nameInput) nameInput.focus();
                return;
            }

            const error = validateAndSyncCoin(editPrice, editCoin, editError);
            if (error) {
                e.preventDefault();
                showAdminErrorToast(error);
                if (editPrice) editPrice.focus();
                return;
            }

            // Đảm bảo trường coinAmount có giá trị hợp lệ trước khi submit
            if (editPrice && editCoin && (!editCoin.value || editCoin.value <= 0)) {
                editCoin.value = Math.floor(parseInt(editPrice.value, 10) / 100);
            }
        });
    }
});
