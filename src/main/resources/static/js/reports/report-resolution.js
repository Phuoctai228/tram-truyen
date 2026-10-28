        function escapeHtml(str) {
            if (!str) return '';
            return String(str)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }

        function renderReasonWithBadge(rawReason) {
            if (!rawReason) return '<span class="text-gray-400 italic">Không có mô tả</span>';
            const match = rawReason.match(/^\[([A-Z_]+)\]\s*([\s\S]*)$/);
            if (!match) {
                return '<div class="text-xs text-[#2b2118] whitespace-pre-line bg-white p-2.5 rounded-lg border border-[#eae2d5]">' + escapeHtml(rawReason) + '</div>';
            }
            const tag = match[1];
            const desc = match[2];
            const labelMap = {
                'CHAPTER_ERROR': { label: 'Lỗi chương', cls: 'bg-rose-100 text-rose-800 border-rose-200' },
                'TYPO_FORMAT': { label: 'Lỗi chính tả / Format', cls: 'bg-amber-100 text-amber-800 border-amber-200' },
                'WRONG_INFO': { label: 'Sai thông tin', cls: 'bg-orange-100 text-orange-800 border-orange-200' },
                'INAPPROPRIATE': { label: 'Nội dung vi phạm', cls: 'bg-red-100 text-red-800 border-red-200' },
                'COPYRIGHT': { label: 'Vi phạm bản quyền', cls: 'bg-purple-100 text-purple-800 border-purple-200' },
                'SPAM': { label: 'Spam / Quảng cáo', cls: 'bg-amber-100 text-amber-800 border-amber-200' },
                'HARASSMENT': { label: 'Xúc phạm / Đả kích', cls: 'bg-rose-100 text-rose-800 border-rose-200' },
                'HATE_SPEECH': { label: 'Ngôn từ thù ghét', cls: 'bg-red-100 text-red-800 border-red-200' },
                'SPOILER': { label: 'Tiết lộ nội dung (Spoiler)', cls: 'bg-blue-100 text-blue-800 border-blue-200' },
                'OTHER': { label: 'Vi phạm khác', cls: 'bg-stone-100 text-stone-800 border-stone-200' }
            };
            const info = labelMap[tag] || { label: tag, cls: 'bg-stone-100 text-stone-800 border-stone-200' };
            let html = '<div class="flex items-center gap-2 mb-1.5">';
            html += '<span class="px-2 py-0.5 rounded-md text-[11px] font-bold border ' + info.cls + '">' + escapeHtml(info.label) + '</span>';
            html += '</div>';
            html += '<div class="text-xs text-[#2b2118] whitespace-pre-line bg-white p-2.5 rounded-lg border border-[#eae2d5]">' + escapeHtml(desc || '(Không có mô tả chi tiết)') + '</div>';
            return html;
        }

        function openCommentResolveModal(el) {
            let id, status, reporter, comment, reason, note, author, novelTitle, novelSlug, commentId;
            if (el && el.dataset) {
                id = el.dataset.id;
                status = el.dataset.status;
                reporter = el.dataset.reporter;
                comment = el.dataset.comment;
                reason = el.dataset.reason;
                note = el.dataset.note;
                author = el.dataset.commentAuthor;
                novelTitle = el.dataset.novelTitle;
                novelSlug = el.dataset.novelSlug;
                commentId = el.dataset.commentId;
            } else if (arguments.length > 1) {
                id = arguments[0];
                status = arguments[1];
                reporter = arguments[2];
                comment = arguments[3];
                reason = arguments[4];
                note = arguments[5];
            }
            document.getElementById('modalCommentTitle').textContent = 'Xử lý báo cáo bình luận #CR-' + id;
            document.getElementById('mCommentReporter').textContent = reporter || 'Độc giả';
            document.getElementById('mCommentAuthor').textContent = author || 'Độc giả';

            const novelLink = document.getElementById('mCommentNovelLink');
            const novelWrapper = document.getElementById('mCommentNovelWrapper');
            if (novelSlug) {
                novelLink.textContent = novelTitle || 'Xem truyện';
                novelLink.href = '/truyen/' + encodeURIComponent(novelSlug) + (commentId ? '#comment-' + commentId : '');
                novelWrapper.style.display = 'block';
            } else {
                novelWrapper.style.display = 'none';
            }

            document.getElementById('mCommentContent').textContent = '"' + (comment || '') + '"';
            document.getElementById('mCommentReason').innerHTML = renderReasonWithBadge(reason);
            document.getElementById('commentAdminNote').value = note || '';

            const form = document.getElementById('commentResolveForm');
            form.action = form.dataset.urlTemplate.replace('{id}', id);

            const hideRadio = form.querySelector('input[name="action"][value="HIDE"]');
            if (hideRadio) {
                hideRadio.checked = true;
            }

            const isReadOnly = (status !== 'RECEIVED');
            document.getElementById('commentActionSection').style.display = isReadOnly ? 'none' : 'block';
            document.getElementById('commentModalActions').style.display = isReadOnly ? 'none' : 'flex';
            document.getElementById('commentAdminNote').readOnly = isReadOnly;

            document.getElementById('resolveCommentModal').classList.remove('hidden');
        }

        function closeResolveCommentModal() {
            document.getElementById('resolveCommentModal').classList.add('hidden');
        }

        function submitCommentDecision(decision) {
            document.getElementById('commentDecisionInput').value = decision;
            const note = document.getElementById('commentAdminNote').value.trim();
            if (!note) {
                alert('Vui lòng nhập ghi chú xử lý / lý do từ chối.');
                return;
            }
            document.getElementById('commentResolveForm').submit();
        }

        function openContentResolveModal(el) {
            let id, status, reporter, novel, chapter, reason, note;
            if (el && el.dataset) {
                id = el.dataset.id;
                status = el.dataset.status;
                reporter = el.dataset.reporter;
                novel = el.dataset.novel;
                chapter = el.dataset.chapter;
                reason = el.dataset.reason;
                note = el.dataset.note;
            } else if (arguments.length > 1) {
                id = arguments[0];
                status = arguments[1];
                reporter = arguments[2];
                novel = arguments[3];
                chapter = arguments[4];
                reason = arguments[5];
                note = arguments[6];
            }
            document.getElementById('modalContentTitle').textContent = 'Xử lý báo cáo nội dung #TR-' + id;
            document.getElementById('mContentReporter').textContent = reporter || 'Độc giả';
            document.getElementById('mContentNovel').textContent = novel || '';
            document.getElementById('mContentChapter').textContent = chapter || '';
            document.getElementById('mContentReason').innerHTML = renderReasonWithBadge(reason);
            document.getElementById('contentAdminNote').value = note || '';

            const form = document.getElementById('contentResolveForm');
            form.action = form.dataset.urlTemplate.replace('{id}', id);

            const isReadOnly = (status !== 'RECEIVED');
            document.getElementById('contentActionSection').style.display = isReadOnly ? 'none' : 'block';
            document.getElementById('contentModalActions').style.display = isReadOnly ? 'none' : 'flex';
            document.getElementById('contentAdminNote').readOnly = isReadOnly;

            document.getElementById('resolveContentModal').classList.remove('hidden');
        }

        function closeResolveContentModal() {
            document.getElementById('resolveContentModal').classList.add('hidden');
        }

        function submitContentDecision(decision) {
            document.getElementById('contentDecisionInput').value = decision;
            const note = document.getElementById('contentAdminNote').value.trim();
            if (!note) {
                alert('Vui lòng nhập ghi chú xử lý / lý do từ chối.');
                return;
            }
            document.getElementById('contentResolveForm').submit();
        }
    