(() => {
    const MODAL_ID = 'action-confirm-modal';

    function createModal() {
        const modal = document.createElement('div');
        modal.id = MODAL_ID;
        modal.className = 'action-confirm-modal';
        modal.setAttribute('role', 'dialog');
        modal.setAttribute('aria-modal', 'true');
        modal.setAttribute('aria-labelledby', `${MODAL_ID}-title`);
        modal.hidden = true;
        modal.innerHTML = `
            <div class="action-confirm-modal__backdrop" data-confirm-cancel></div>
            <div class="action-confirm-modal__panel" role="document">
                <span class="action-confirm-modal__icon" aria-hidden="true">!</span>
                <h2 id="${MODAL_ID}-title">Xác nhận thao tác</h2>
                <p class="action-confirm-modal__message"></p>
                <div class="action-confirm-modal__actions">
                    <button type="button" class="action-button" data-confirm-cancel>Hủy</button>
                    <button type="button" class="action-button danger" data-confirm-submit>Xác nhận</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        return modal;
    }

    function closeModal(modal) {
        modal.hidden = true;
        document.body.classList.remove('action-confirm-modal-open');
        modal.confirmedForm = null;
    }

    function openModal(modal, form) {
        const message = form.dataset.confirmMessage;
        modal.querySelector('.action-confirm-modal__message').textContent = message;
        modal.confirmedForm = form;
        modal.hidden = false;
        document.body.classList.add('action-confirm-modal-open');
        modal.querySelector('[data-confirm-submit]').focus();
    }

    document.addEventListener('DOMContentLoaded', () => {
        const modal = createModal();

        document.addEventListener('submit', (event) => {
            const form = event.target;
            if (!(form instanceof HTMLFormElement) || !form.dataset.confirmMessage) {
                return;
            }
            event.preventDefault();
            openModal(modal, form);
        });

        modal.addEventListener('click', (event) => {
            if (event.target.closest('[data-confirm-cancel]')) {
                closeModal(modal);
                return;
            }
            if (event.target.closest('[data-confirm-submit]') && modal.confirmedForm) {
                const form = modal.confirmedForm;
                closeModal(modal);
                form.submit();
            }
        });

        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && !modal.hidden) {
                closeModal(modal);
            }
        });
    });
})();
