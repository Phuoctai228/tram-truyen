/* ========================================================
 * SYSTEM SETTINGS & INITIALIZATION (UC-52)
 * Quản lý cấu hình chung & vòng đời nạp trang
 * ======================================================== */

        function restoreDefaultSystemSettings() {
            if (!confirm("Bạn có chắc chắn muốn khôi phục toàn bộ biểu mẫu về cấu hình tiêu chuẩn ban đầu của Trạm Truyện (10 Coin / 1.000 VNĐ, Hotline 1900-1234, support@tramtruyen.com)?")) {
                return;
            }

            const rateInput = document.getElementById('vndToCoinRateInput');
            if (rateInput) rateInput.value = 10;

            const phoneInput = document.getElementById('contactPhoneInput');
            if (phoneInput) phoneInput.value = "1900-1234";

            const emailInput = document.getElementById('supportEmailInput');
            if (emailInput) emailInput.value = "support@tramtruyen.com";

            const tos = document.getElementById('tosTextarea');
            if (tos) {
                tos.value = "Chào mừng bạn đến với Trạm Truyện. Bằng việc truy cập hoặc sử dụng trang web, bạn đồng ý tuân thủ các quy định về bản quyền tác phẩm văn học, ứng xử văn minh trong bình luận, không sao chép hoặc phân phối lại nội dung chương VIP khi chưa có sự đồng ý bằng văn bản của Ban Quản Trị.";
            }

            const pp = document.getElementById('ppTextarea');
            if (pp) {
                pp.value = "Trạm Truyện cam kết bảo mật tuyệt đối thông tin cá nhân của người dùng bao gồm địa chỉ email, lịch sử đọc truyện và thông tin giao dịch nạp Coin. Chúng tôi không chia sẻ hoặc cung cấp thông tin người dùng cho bất kỳ bên thứ ba nào ngoại trừ các trường hợp theo yêu cầu của cơ quan pháp luật có thẩm quyền.";
            }
        }


// Tự động nạp dữ liệu và mở lại Modal nếu có lỗi từ server (UC-52)
document.addEventListener("DOMContentLoaded", function () {
    initExistingThemeFolders();

    const addForm = document.getElementById('addThemeForm');
    if (addForm) {
        addForm.addEventListener('submit', function () {
            const tokens = getAll18Tokens('add');
            const tokensJson = document.getElementById('addThemeTokensJson');
            if (tokensJson) tokensJson.value = JSON.stringify(tokens);

            const folderInput = document.getElementById('addThemeFolder');
            const nameInput = document.getElementById('addThemeName');
            if (folderInput && (!folderInput.value || folderInput.value.trim() === '')) {
                folderInput.value = generateUniqueThemeFolder(nameInput ? nameInput.value : '');
            }
        });
    }
    const editForm = document.getElementById('editThemeForm');
    if (editForm) {
        editForm.addEventListener('submit', function () {
            const tokens = getAll18Tokens('edit');
            const tokensJson = document.getElementById('editThemeTokensJson');
            if (tokensJson) tokensJson.value = JSON.stringify(tokens);
        });
    }

    // Đọc dữ liệu từ cầu nối Thymeleaf nếu server trả về lỗi biểu mẫu theme (UC-52)
    const bridge = document.getElementById('themeFormErrorBridge');
    if (bridge) {
        const reopen = bridge.dataset.reopen || null;
        if (reopen === 'add') {
            const fName = bridge.dataset.failedName || '';
            const fFolder = bridge.dataset.failedFolder || '';
            const fTokens = bridge.dataset.failedTokens || '';
            const fOp = bridge.dataset.failedOpacity ? parseInt(bridge.dataset.failedOpacity) : 12;
            const fBl = bridge.dataset.failedBlur ? parseInt(bridge.dataset.failedBlur) : 4;
            openAddThemeModal(fName, fFolder, fTokens);
            if (fOp) {
                const slOp = document.getElementById('add_bg_opacity');
                if (slOp) { slOp.value = fOp; onBgOpacityChange('add', fOp); }
            }
            if (fBl !== null && fBl !== undefined) {
                const slBl = document.getElementById('add_bg_blur');
                if (slBl) { slBl.value = fBl; onBgBlurChange('add', fBl); }
            }
        } else if (reopen === 'edit') {
            const fId = bridge.dataset.failedId ? parseInt(bridge.dataset.failedId) : null;
            const fName = bridge.dataset.failedName || '';
            const fTokens = bridge.dataset.failedTokens || '';
            const fOp = bridge.dataset.failedOpacity ? parseInt(bridge.dataset.failedOpacity) : 12;
            const fBl = bridge.dataset.failedBlur ? parseInt(bridge.dataset.failedBlur) : 4;
            if (fId) {
                const btn = document.querySelector('button[data-theme-id="' + fId + '"]');
                const existingBgUrl = btn ? btn.getAttribute('data-theme-bg-url') : '';
                const existingFolder = btn ? btn.getAttribute('data-theme-folder') : '';
                openEditModalDirect(fId, fName, existingFolder, fTokens, existingBgUrl, fOp, fBl);
            }
        }
    }
});

