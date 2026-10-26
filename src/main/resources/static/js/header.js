/**
 * ========================================================
 * TRẠM TRUYỆN - HEADER & GLOBAL INTERACTION SCRIPTS
 * Bao gồm:
 * 1. Khởi tạo & đồng bộ chủ đề giao diện (Web Themes UC-07 / UC-52)
 * 2. Tự động ẩn thông báo Toast hệ thống (Toast Auto-hide)
 * 3. Bảo lưu đường dẫn quay lại khi bấm Đăng nhập / Đăng ký
 * 4. Trình đơn người dùng (User Dropdown Menu)
 * 5. Gợi ý tìm kiếm thời gian thực (Live Search Suggestions)
 * ========================================================
 */

// 1. Khởi tạo & đồng bộ Theme ngay lập tức
(function initWebTheme() {
    function applyTheme() {
        const header = document.querySelector('header[data-theme]');
        const currentTheme = (header && header.getAttribute('data-theme'))
            || document.documentElement.getAttribute('data-theme')
            || 'theme-default';

        document.documentElement.setAttribute('data-theme', currentTheme);
        if (document.body) {
            document.body.classList.remove('theme-default', 'theme-tet', 'theme-dark', 'theme-sepia');
            document.body.classList.add(currentTheme);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', applyTheme);
    } else {
        applyTheme();
    }
})();

document.addEventListener('DOMContentLoaded', function () {
    // 2. Tự động ẩn thông báo Toast sau 5 giây
    const toast = document.getElementById('global-toast');
    if (toast) {
        setTimeout(function () {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.5s ease';
            setTimeout(() => toast.remove(), 500);
        }, 5000);
    }

    // 3. Bảo lưu URL chuyển tiếp cho nút Đăng nhập / Đăng ký
    const loginBtn = document.getElementById('navLoginBtn');
    const regBtn = document.getElementById('navRegisterBtn');
    const currentPath = window.location.pathname;
    if (currentPath && currentPath !== '/' && !currentPath.startsWith('/login') && !currentPath.startsWith('/register')) {
        const redirectTarget = encodeURIComponent(currentPath + window.location.search);
        if (loginBtn) loginBtn.href = '/login?redirect=' + redirectTarget;
        if (regBtn) regBtn.href = '/register?redirect=' + redirectTarget;
    }

    // 4. Quản lý hiển thị Dropdown Menu người dùng
    const userMenuBtn = document.getElementById('userMenuBtn');
    const userDropdown = document.getElementById('userDropdown');
    if (userMenuBtn && userDropdown) {
        userMenuBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            userDropdown.classList.toggle('hidden');
        });

        document.addEventListener('click', function (e) {
            if (!userMenuBtn.contains(e.target) && !userDropdown.contains(e.target)) {
                userDropdown.classList.add('hidden');
            }
        });

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                userDropdown.classList.add('hidden');
            }
        });
    }

    // 5. Tìm kiếm gợi ý thời gian thực (Live Search Suggestions)
    const searchInput = document.getElementById('searchInput');
    const searchPopup = document.getElementById('searchSuggestions');
    const suggestKeyword = document.getElementById('suggestKeyword');
    const suggestCount = document.getElementById('suggestCount');
    const suggestList = document.getElementById('suggestList');
    const suggestSeeAll = document.getElementById('suggestSeeAll');

    let debounceTimer;

    if (searchInput && searchPopup) {
        searchInput.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            const query = this.value.trim();

            if (query.length === 0) {
                searchPopup.classList.add('hidden');
                return;
            }

            debounceTimer = setTimeout(() => {
                fetch(`/api/search/suggestions?q=${encodeURIComponent(query)}`)
                    .then(response => response.json())
                    .then(data => {
                        if (suggestKeyword) suggestKeyword.textContent = query;
                        if (suggestCount) suggestCount.textContent = `${data.results ? data.results.length : 0} kết quả nổi bật`;

                        if (suggestList) {
                            suggestList.innerHTML = '';
                            if (!data.results || data.results.length === 0) {
                                suggestList.innerHTML = `<div class="p-4 text-center text-[#8c7a68] text-sm">Không tìm thấy kết quả nào</div>`;
                            } else {
                                data.results.forEach(novel => {
                                    const a = document.createElement('a');
                                    a.href = `/truyen/${novel.slug}`;
                                    a.className = "flex items-center gap-4 p-3 hover:bg-[#faf8f5] rounded-xl transition-colors group";
                                    a.innerHTML = `
                                        <div class="w-12 h-16 shrink-0 rounded-md overflow-hidden bg-gray-100 border border-outline-variant/30">
                                            <img src="${novel.coverUrl || '/images/default-cover.jpg'}" alt="Cover" class="w-full h-full object-cover">
                                        </div>
                                        <div class="flex-1 min-w-0">
                                            <h4 class="font-bold text-[#231A11] text-[15px] mb-0.5 truncate group-hover:text-[#c08d41] transition-colors">${novel.title}</h4>
                                            <p class="text-[#8c7a68] text-[13px] truncate">${novel.author}</p>
                                        </div>
                                        <div class="shrink-0 bg-[#f5eae0] text-[#c08d41] text-[11px] font-bold px-2 py-1 rounded-full uppercase tracking-wider flex items-center gap-1">
                                            ${novel.category} <span class="material-symbols-outlined text-[14px]">arrow_forward</span>
                                        </div>
                                    `;
                                    suggestList.appendChild(a);
                                });
                            }
                        }

                        searchPopup.classList.remove('hidden');
                    })
                    .catch(err => {
                        console.error('Error fetching search suggestions:', err);
                    });
            }, 300);
        });

        if (suggestSeeAll) {
            suggestSeeAll.addEventListener('click', function (e) {
                e.preventDefault();
                const searchForm = document.getElementById('searchForm');
                if (searchForm) searchForm.submit();
            });
        }

        document.addEventListener('click', function (e) {
            if (!searchInput.contains(e.target) && !searchPopup.contains(e.target)) {
                searchPopup.classList.add('hidden');
            }
        });

        searchInput.addEventListener('focus', function () {
            if (this.value.trim().length > 0 && suggestList && suggestList.innerHTML !== '') {
                searchPopup.classList.remove('hidden');
            }
        });
    }
});
