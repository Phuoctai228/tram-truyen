(function () {
    const DEFAULT_FONT_SIZE = 16;

    document.querySelectorAll('[data-rich-editor]').forEach(function (editor) {
        const surface = editor.querySelector('[data-editor-surface]');
        const source = editor.querySelector('[data-editor-source]');
        const form = editor.closest('form');
        const sizeInput = editor.querySelector('[data-font-size]');
        const sourceToggle = editor.querySelector('[data-source-toggle]');
        let savedRange = null;
        let toolbarPointerDown = false;

        surface.style.fontSize = DEFAULT_FONT_SIZE + 'px';
        sizeInput.value = String(DEFAULT_FONT_SIZE);

        editor.querySelectorAll('[data-command]').forEach(function (control) {
            if (control.tagName === 'SELECT') {
                control.addEventListener('mousedown', function () {
                    toolbarPointerDown = true;
                    saveSelection();
                });
                control.addEventListener('change', function () {
                    toolbarPointerDown = false;
                    runCommand(control.dataset.command, control.value);
                });
                control.addEventListener('blur', function () {
                    toolbarPointerDown = false;
                });
                return;
            }

            control.addEventListener('mousedown', function (event) {
                event.preventDefault();
                toolbarPointerDown = true;
                saveSelection();
            });
            control.addEventListener('click', function () {
                toolbarPointerDown = false;
                runCommand(control.dataset.command, null);
            });
        });

        sizeInput.addEventListener('mousedown', function () {
            toolbarPointerDown = true;
            saveSelection();
        });
        sizeInput.addEventListener('blur', function () {
            toolbarPointerDown = false;
        });
        sizeInput.addEventListener('change', function () {
            toolbarPointerDown = false;
            if (sizeInput.value) {
                applyFontSize(sizeInput.value);
            }
        });

        sourceToggle.addEventListener('mousedown', function (event) {
            event.preventDefault();
            toolbarPointerDown = true;
            saveSelection();
        });
        sourceToggle.addEventListener('click', function () {
            toolbarPointerDown = false;
            const sourceMode = editor.classList.toggle('is-source');
            sourceToggle.setAttribute('aria-pressed', String(sourceMode));
            if (sourceMode) {
                syncSource();
                source.focus();
            } else {
                surface.innerHTML = source.value;
                surface.focus();
                placeCaretAtEnd();
                syncSource();
            }
        });

        surface.addEventListener('focus', function () {
            if (!toolbarPointerDown) {
                saveSelection();
            }
        });
        surface.addEventListener('mouseup', function () {
            saveSelection();
            updateToolbar();
        });
        surface.addEventListener('keyup', function () {
            saveSelection();
            updateToolbar();
        });
        surface.addEventListener('input', function () {
            saveSelection();
            syncSource();
            updateToolbar();
        });
        document.addEventListener('selectionchange', function () {
            if (!toolbarPointerDown && selectionInsideSurface()) {
                saveSelection();
                updateToolbar();
            }
        });
        source.addEventListener('input', function () {
            if (editor.classList.contains('is-source')) {
                surface.innerHTML = source.value;
            }
        });
        form.addEventListener('submit', function () {
            removeEmptyFontMarkers();
            syncSource();
        });

        function runCommand(command, value) {
            restoreSelection();
            surface.focus();
            document.execCommand(command, false, value);
            saveSelection();
            syncSource();
            updateToolbar();
        }

        function applyFontSize(value) {
            const size = Number.parseInt(value, 10);
            if (!Number.isInteger(size) || size < 8 || size > 96) {
                sizeInput.value = '';
                return;
            }

            restoreSelection();
            surface.focus();
            const selection = window.getSelection();
            if (!selection.rangeCount || !selectionInsideSurface()) {
                return;
            }

            const range = selection.getRangeAt(0);
            if (range.collapsed) {
                placeFontSizeCaret(range, size);
            } else {
                wrapSelectedContent(range, size);
            }
            saveSelection();
            syncSource();
            updateToolbar();
            sizeInput.value = String(size);
        }

        function wrapSelectedContent(range, size) {
            const fragment = range.extractContents();
            clearFontSize(fragment);
            wrapTextNodes(fragment, size);
            range.insertNode(fragment);

            const nextRange = document.createRange();
            nextRange.setStart(range.startContainer, range.startOffset);
            nextRange.setEnd(range.startContainer, range.startOffset);
            const selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(nextRange);
            savedRange = nextRange.cloneRange();
        }

        function placeFontSizeCaret(range, size) {
            const marker = document.createElement('span');
            marker.dataset.fontMarker = 'true';
            marker.style.fontSize = size + 'px';
            marker.style.lineHeight = 'inherit';
            range.insertNode(marker);

            const caret = document.createRange();
            caret.setStart(marker, 0);
            caret.collapse(true);
            const selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(caret);
            savedRange = caret.cloneRange();
        }

        function clearFontSize(root) {
            const elements = [];
            if (root.nodeType === Node.ELEMENT_NODE && root.style.fontSize) {
                elements.push(root);
            }
            Array.from(root.querySelectorAll('[style*="font-size"]')).forEach(function (element) {
                elements.push(element);
            });
            elements.forEach(function (element) {
                element.style.removeProperty('font-size');
                if (!element.getAttribute('style')) {
                    element.removeAttribute('style');
                }
            });
        }

        function wrapTextNodes(root, size) {
            const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
            const textNodes = [];
            let node = walker.nextNode();
            while (node) {
                if (node.nodeValue.trim()) {
                    textNodes.push(node);
                }
                node = walker.nextNode();
            }
            textNodes.forEach(function (textNode) {
                const wrapper = document.createElement('span');
                wrapper.style.fontSize = size + 'px';
                wrapper.style.lineHeight = 'inherit';
                textNode.parentNode.insertBefore(wrapper, textNode);
                wrapper.appendChild(textNode);
            });
        }

        function removeEmptyFontMarkers() {
            editor.querySelectorAll('[data-font-marker="true"]').forEach(function (marker) {
                if (!marker.textContent) {
                    marker.remove();
                }
            });
        }

        function saveSelection() {
            const selection = window.getSelection();
            if (selection.rangeCount > 0 && selectionInsideSurface()) {
                savedRange = selection.getRangeAt(0).cloneRange();
            }
        }

        function restoreSelection() {
            if (!savedRange) {
                placeCaretAtEnd();
                return;
            }
            const selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(savedRange);
        }

        function placeCaretAtEnd() {
            const range = document.createRange();
            range.selectNodeContents(surface);
            range.collapse(false);
            const selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(range);
            savedRange = range.cloneRange();
        }

        function selectionInsideSurface() {
            const selection = window.getSelection();
            return selection.rangeCount > 0
                && surface.contains(selection.anchorNode)
                && surface.contains(selection.focusNode);
        }

        function updateToolbar() {
            if (!selectionInsideSurface()) {
                return;
            }
            editor.querySelectorAll('[data-command]').forEach(function (control) {
                if (control.tagName === 'SELECT') {
                    updateSelect(control);
                    return;
                }
                const active = document.queryCommandState(control.dataset.command);
                control.classList.toggle('is-active', active);
                control.setAttribute('aria-pressed', String(active));
            });
            updateFontSize();
        }

        function updateSelect(control) {
            let value = document.queryCommandValue(control.dataset.command);
            if (control.dataset.command === 'formatBlock') {
                value = value.replace(/[<>]/g, '').toLowerCase();
            }
            const option = Array.from(control.options).find(function (item) {
                return item.value.toLowerCase() === String(value).toLowerCase();
            });
            if (option) {
                control.value = option.value;
            }
        }

        function updateFontSize() {
            const selection = window.getSelection();
            if (!selection.rangeCount) {
                return;
            }
            let node = selection.anchorNode;
            if (node && node.nodeType === Node.TEXT_NODE) {
                node = node.parentElement;
            }
            const styled = node && node.closest ? node.closest('[style*="font-size"]') : null;
            if (!styled || !surface.contains(styled)) {
                sizeInput.value = '';
                return;
            }
            const match = styled.style.fontSize.match(/(\d+(?:\.\d+)?)px/);
            if (match) {
                sizeInput.value = String(Math.round(Number.parseFloat(match[1])));
            }
        }

        function syncSource() {
            source.value = surface.innerHTML;
        }

        syncSource();
        updateToolbar();
    });
})();
