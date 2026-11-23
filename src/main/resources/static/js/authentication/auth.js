document.addEventListener("DOMContentLoaded", function () {
    const toggleBtn = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('passwordEyeIcon');

    if (toggleBtn && passwordInput && eyeIcon) {
        toggleBtn.addEventListener('click', function () {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
            eyeIcon.textContent = isPassword ? 'visibility_off' : 'visibility';
        });
    }

    // Validation for Full Name (no numbers or special characters)
    const fullNameInput = document.getElementById('fullName');
    const clientError = document.getElementById('fullNameClientError');
    const nameRegex = /^[\p{L}\s]+$/u;

    if (fullNameInput) {
        function validateName() {
            const val = fullNameInput.value.trim();
            if (val.length > 0 && !nameRegex.test(val)) {
                if (clientError) {
                    clientError.textContent = 'Họ và tên không được chứa số và ký tự đặc biệt';
                    clientError.classList.remove('hidden');
                }
                fullNameInput.classList.add('border-error');
                fullNameInput.setCustomValidity('Họ và tên không được chứa số và ký tự đặc biệt');
                return false;
            } else {
                if (clientError) {
                    clientError.textContent = '';
                    clientError.classList.add('hidden');
                }
                fullNameInput.classList.remove('border-error');
                fullNameInput.setCustomValidity('');
                return true;
            }
        }

        // Block typing digits and special characters directly
        fullNameInput.addEventListener('beforeinput', function (e) {
            if (e.data && !nameRegex.test(e.data)) {
                e.preventDefault();
                if (clientError) {
                    clientError.textContent = 'Họ và tên không được chứa số và ký tự đặc biệt';
                    clientError.classList.remove('hidden');
                    setTimeout(() => {
                        if (nameRegex.test(fullNameInput.value.trim()) || fullNameInput.value.trim() === '') {
                            clientError.textContent = '';
                            clientError.classList.add('hidden');
                        }
                    }, 2000);
                }
            }
        });

        // Filter out invalid characters when pasting or finishing input
        fullNameInput.addEventListener('input', function (e) {
            if (e.isComposing) return;
            const original = fullNameInput.value;
            const sanitized = original.replace(/[^\p{L}\s]/gu, '');
            if (original !== sanitized) {
                fullNameInput.value = sanitized;
            }
            validateName();
        });

        fullNameInput.addEventListener('compositionend', function () {
            const original = fullNameInput.value;
            const sanitized = original.replace(/[^\p{L}\s]/gu, '');
            if (original !== sanitized) {
                fullNameInput.value = sanitized;
            }
            validateName();
        });

        fullNameInput.addEventListener('blur', validateName);

        const form = fullNameInput.closest('form');
        if (form) {
            form.addEventListener('submit', function (e) {
                if (!validateName()) {
                    e.preventDefault();
                    fullNameInput.focus();
                }
            });
        }
    }
});
