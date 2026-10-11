(function () {
    const editors = document.querySelectorAll('[data-rich-editor]');

    editors.forEach(function (editor) {
        const surface = editor.querySelector('[data-editor-surface]');
        const source = editor.querySelector('[data-editor-source]');

        editor.querySelectorAll('[data-command]').forEach(function (control) {
            control.addEventListener('click', function () {
                surface.focus();
                document.execCommand(control.dataset.command, false, control.value || null);
                syncSource();
            });
            control.addEventListener('change', function () {
                surface.focus();
                document.execCommand(control.dataset.command, false, control.value);
                syncSource();
            });
        });

        const sourceToggle = document.createElement('button');
        sourceToggle.type = 'button';
        sourceToggle.textContent = '</>';
        sourceToggle.title = 'Chuyển sang HTML';
        sourceToggle.addEventListener('click', function () {
            if (editor.classList.toggle('is-source')) {
                source.value = surface.innerHTML;
            } else {
                surface.innerHTML = source.value;
            }
        });
        editor.querySelector('.editor-toolbar').appendChild(sourceToggle);

        surface.addEventListener('input', syncSource);
        source.addEventListener('input', function () {
            if (editor.classList.contains('is-source')) {
                surface.innerHTML = source.value;
            }
        });
        surface.closest('form').addEventListener('submit', syncSource);

        function syncSource() {
            source.value = surface.innerHTML;
        }
    });
})();
