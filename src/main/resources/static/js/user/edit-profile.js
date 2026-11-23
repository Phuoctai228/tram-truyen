document.addEventListener("DOMContentLoaded", function () {
    const fullNameInput = document.getElementById('full-name');
    const errorEl = document.getElementById('fullNameError');
    const nameRegex = /^[\p{L}\s]+$/u;

    if (fullNameInput) {
        function validateFullName() {
            const val = fullNameInput.value.trim();
            if (val.length === 0) {
                if (errorEl) {
                    errorEl.textContent = 'Họ và tên không được để trống';
                    errorEl.classList.remove('hidden');
                }
                fullNameInput.setCustomValidity('Họ và tên không được để trống');
                return false;
            } else if (!nameRegex.test(val)) {
                if (errorEl) {
                    errorEl.textContent = 'Họ và tên không được chứa số và ký tự đặc biệt';
                    errorEl.classList.remove('hidden');
                }
                fullNameInput.setCustomValidity('Họ và tên không được chứa số và ký tự đặc biệt');
                return false;
            } else {
                if (errorEl) {
                    errorEl.textContent = '';
                    errorEl.classList.add('hidden');
                }
                fullNameInput.setCustomValidity('');
                return true;
            }
        }

        // Block typing digits and special characters directly
        fullNameInput.addEventListener('beforeinput', function (e) {
            if (e.data && !nameRegex.test(e.data)) {
                e.preventDefault();
                if (errorEl) {
                    errorEl.textContent = 'Họ và tên không được chứa số và ký tự đặc biệt';
                    errorEl.classList.remove('hidden');
                    setTimeout(() => {
                        if (nameRegex.test(fullNameInput.value.trim()) || fullNameInput.value.trim() === '') {
                            errorEl.textContent = '';
                            errorEl.classList.add('hidden');
                        }
                    }, 2000);
                }
            }
        });

        // Filter out invalid characters when pasting or typing
        fullNameInput.addEventListener('input', function (e) {
            if (e.isComposing) return;
            const original = fullNameInput.value;
            const sanitized = original.replace(/[^\p{L}\s]/gu, '');
            if (original !== sanitized) {
                fullNameInput.value = sanitized;
            }
            validateFullName();
        });

        fullNameInput.addEventListener('compositionend', function () {
            const original = fullNameInput.value;
            const sanitized = original.replace(/[^\p{L}\s]/gu, '');
            if (original !== sanitized) {
                fullNameInput.value = sanitized;
            }
            validateFullName();
        });

        fullNameInput.addEventListener('blur', validateFullName);

        const form = fullNameInput.closest('form');
        if (form) {
            form.addEventListener('submit', function (e) {
                if (!validateFullName()) {
                    e.preventDefault();
                    fullNameInput.focus();
                }
            });
        }
    }
});
