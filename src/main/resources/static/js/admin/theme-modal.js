/* ========================================================
 * THEME MODALS & PREVIEW MANAGEMENT (UC-52)
 * Giao diện modal Thêm/Sửa/Kích hoạt/Xóa chủ đề giao diện
 * ======================================================== */

        // Bộ nhớ lưu 18 tokens cho Add và Edit modal
        const currentThemeTokens = {
            add: {},
            edit: {}
        };
        // Bộ nhớ lưu ảnh nền cho Add và Edit modal
        const currentThemeBg = {
            add: { url: '', opacity: 12, blur: 4 },
            edit: { url: '', opacity: 12, blur: 4 }
        };
        const existingThemeFolderPaths = new Set();


        // 3. RENDER UI BƯỚC 1 (MẪU MÀU) & MỤC NÂNG CAO (18 TOKEN)
        // ========================================================
        function renderPresetCards(containerId, prefix) {
            const container = document.getElementById(containerId);
            if (!container) return;
            let html = '';
            THEME_PRESETS.forEach(p => {
                html += `
                <button type="button" onclick="selectPreset('${prefix}', '${p.id}')"
                        class="p-2.5 rounded-xl border border-[#eae2d5] bg-white hover:border-[#875200] hover:bg-[#faf7f2] transition-all text-left flex flex-col justify-between gap-1.5 shadow-sm group">
                    <div class="flex items-center gap-1.5">
                        <span class="w-4 h-4 rounded-full border border-black/10 shrink-0 shadow-sm" style="background-color: ${p.primary}"></span>
                        <span class="w-4 h-4 rounded-full border border-black/10 shrink-0 shadow-sm" style="background-color: ${p.secondary}"></span>
                        <span class="w-4 h-4 rounded-full border border-black/10 shrink-0 shadow-sm" style="background-color: ${p.background}"></span>
                        <span class="w-4 h-4 rounded-full border border-black/10 shrink-0 shadow-sm" style="background-color: ${p.on_background}"></span>
                    </div>
                    <div>
                        <div class="text-xs font-bold text-[#2b2118] group-hover:text-[#875200] truncate">${p.name}</div>
                        <div class="text-[10px] text-[#8a7664] line-clamp-1">${p.desc}</div>
                    </div>
                </button>
                `;
            });
            container.innerHTML = html;
        }

        // ========================================================
        // 3. EVENT HANDLERS & ĐỒNG BỘ MÀU SẮC
        // ========================================================
        function selectPreset(prefix, presetId) {
            const p = THEME_PRESETS.find(item => item.id === presetId);
            if (!p) return;
            apply4Colors(prefix, p.primary, p.secondary, p.background, p.on_background, true);
            if (prefix === 'edit') {
                const notice = document.getElementById('edit_token_error_notice');
                if (notice) notice.classList.add('hidden');
                const submitBtn = document.getElementById('edit_submit_btn');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.classList.remove('opacity-50', 'cursor-not-allowed');
                }
            }
        }

        function onMainColorPickerChange(prefix, field, hexVal) {
            const hexInputMap = {
                'primary': `${prefix}_hex_primary`,
                'secondary': `${prefix}_hex_secondary`,
                'background': `${prefix}_hex_bg`,
                'on_background': `${prefix}_hex_text`
            };
            const hexInput = document.getElementById(hexInputMap[field]);
            if (hexInput) hexInput.value = hexVal.toUpperCase();
            triggerMainColorUpdate(prefix);
        }

        function onMainHexInputChange(prefix, field, hexVal) {
            if (/^#[0-9A-Fa-f]{6}$/.test(hexVal)) {
                const colorInputMap = {
                    'primary': `${prefix}_main_primary`,
                    'secondary': `${prefix}_main_secondary`,
                    'background': `${prefix}_main_bg`,
                    'on_background': `${prefix}_main_text`
                };
                const colorInput = document.getElementById(colorInputMap[field]);
                if (colorInput) colorInput.value = hexVal;
                triggerMainColorUpdate(prefix);
            }
        }

        function triggerMainColorUpdate(prefix) {
            const pEl = document.getElementById(`${prefix}_main_primary`);
            const sEl = document.getElementById(`${prefix}_main_secondary`);
            const bgEl = document.getElementById(`${prefix}_main_bg`);
            const textEl = document.getElementById(`${prefix}_main_text`);
            if (pEl && sEl && bgEl && textEl) {
                apply4Colors(prefix, pEl.value, sEl.value, bgEl.value, textEl.value, false);
                if (prefix === 'edit') {
                    const notice = document.getElementById('edit_token_error_notice');
                    if (notice) notice.classList.add('hidden');
                    const submitBtn = document.getElementById('edit_submit_btn');
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.classList.remove('opacity-50', 'cursor-not-allowed');
                    }
                }
            }
        }

        function apply4Colors(prefix, primary, secondary, bg, text, update4Inputs) {
            if (update4Inputs) {
                const pInput = document.getElementById(`${prefix}_main_primary`);
                const pHex = document.getElementById(`${prefix}_hex_primary`);
                if (pInput) pInput.value = primary;
                if (pHex) pHex.value = primary.toUpperCase();

                const sInput = document.getElementById(`${prefix}_main_secondary`);
                const sHex = document.getElementById(`${prefix}_hex_secondary`);
                if (sInput) sInput.value = secondary;
                if (sHex) sHex.value = secondary.toUpperCase();

                const bgInput = document.getElementById(`${prefix}_main_bg`);
                const bgHex = document.getElementById(`${prefix}_hex_bg`);
                if (bgInput) bgInput.value = bg;
                if (bgHex) bgHex.value = bg.toUpperCase();

                const textInput = document.getElementById(`${prefix}_main_text`);
                const textHex = document.getElementById(`${prefix}_hex_text`);
                if (textInput) textInput.value = text;
                if (textHex) textHex.value = text.toUpperCase();
            }

            // 18 tokens luôn được tính tự động từ 4 màu chính (Logic ngầm không đổi)
            currentThemeTokens[prefix] = generate14Tokens(primary, secondary, bg, text);

            updateStatusAndPreview(prefix);
        }

        // ========================================================
        // 4. QUẢN LÝ MÃ THƯ MỤC GIAO DIỆN (TỰ SINH DUY NHẤT)
        // ========================================================
        function initExistingThemeFolders() {
            existingThemeFolderPaths.clear();
            document.querySelectorAll('[data-folder-path]').forEach(el => {
                const fp = el.getAttribute('data-folder-path');
                if (fp && fp.trim()) {
                    existingThemeFolderPaths.add(fp.trim().toLowerCase());
                }
            });
        }

        function slugifyThemeName(name) {
            if (!name || !name.trim()) return 'theme-custom';
            let str = name.toLowerCase()
                .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
                .replace(/[đĐ]/g, 'd')
                .replace(/[^a-z0-9\s-]/g, '')
                .trim()
                .replace(/\s+/g, '-');
            if (!str.startsWith('theme-')) {
                str = 'theme-' + str;
            }
            return str.substring(0, 55).replace(/-+$/, '');
        }

        function generateUniqueThemeFolder(name) {
            initExistingThemeFolders();
            let base = slugifyThemeName(name);
            if (!existingThemeFolderPaths.has(base)) {
                return base;
            }
            let counter = 2;
            while (counter < 1000) {
                let suffix = '-' + counter;
                let maxBaseLen = 55 - suffix.length;
                let trimmedBase = base.substring(0, maxBaseLen).replace(/-+$/, '');
                let candidate = trimmedBase + suffix;
                if (!existingThemeFolderPaths.has(candidate)) {
                    return candidate;
                }
                counter++;
            }
            return base;
        }

        function onThemeNameInput(prefix, nameVal) {
            if (prefix === 'add') {
                const folderInput = document.getElementById('addThemeFolder');
                if (folderInput) {
                    folderInput.value = generateUniqueThemeFolder(nameVal);
                }
            }
        }

        // ========================================================
        // 4B. XỬ LÝ ẢNH NỀN GIAO DIỆN & XEM TRƯỚC
        // ========================================================
        function onBgFileSelected(prefix, event) {
            const file = event.target.files && event.target.files[0];
            if (!file) return;

            if (file.size > 3 * 1024 * 1024) {
                alert("Dung lượng file ảnh nền vượt quá giới hạn 3MB. Vui lòng chọn ảnh nhẹ hơn.");
                event.target.value = '';
                return;
            }

            const localUrl = URL.createObjectURL(file);
            currentThemeBg[prefix].url = localUrl;

            const emptyZone = document.getElementById(`${prefix}_bg_empty_zone`);
            const previewZone = document.getElementById(`${prefix}_bg_preview_zone`);
            const previewImg = document.getElementById(`${prefix}_bg_preview_img`);
            const previewName = document.getElementById(`${prefix}_bg_preview_name`) || document.getElementById(`${prefix}_bg_file_name`);

            if (emptyZone) emptyZone.classList.add('hidden');
            if (previewZone) previewZone.classList.remove('hidden');
            if (previewImg) previewImg.src = localUrl;
            if (previewName) previewName.innerText = file.name;

            if (prefix === 'edit') {
                const removeInput = document.getElementById('edit_remove_bg') || document.getElementById('editRemoveBg');
                if (removeInput) removeInput.value = 'false';
            }

            updateBgPreview(prefix);
        }

        function onBgOpacityChange(prefix, val) {
            const num = parseInt(val) || 12;
            currentThemeBg[prefix].opacity = num;
            const label = document.getElementById(`${prefix}_bg_opacity_val`);
            if (label) label.innerText = num + '%';
            updateBgPreview(prefix);
        }

        function onBgBlurChange(prefix, val) {
            const num = parseInt(val) || 0;
            currentThemeBg[prefix].blur = num;
            const label = document.getElementById(`${prefix}_bg_blur_val`);
            if (label) label.innerText = num + 'px';
            updateBgPreview(prefix);
        }

        function removeBackground(prefix) {
            currentThemeBg[prefix].url = '';
            const fileInput = document.getElementById(`${prefix}_bg_file`);
            if (fileInput) fileInput.value = '';

            const emptyZone = document.getElementById(`${prefix}_bg_empty_zone`);
            const previewZone = document.getElementById(`${prefix}_bg_preview_zone`);
            const previewImg = document.getElementById(`${prefix}_bg_preview_img`);

            if (previewZone) previewZone.classList.add('hidden');
            if (emptyZone) emptyZone.classList.remove('hidden');
            if (previewImg) previewImg.src = '';

            if (prefix === 'edit') {
                const removeInput = document.getElementById('edit_remove_bg') || document.getElementById('editRemoveBg');
                if (removeInput) removeInput.value = 'true';
            }

            updateBgPreview(prefix);
        }

        function updateBgPreview(prefix) {
            const bg = currentThemeBg[prefix];
            const layer = document.getElementById(`${prefix}_prev_bg_layer`);
            const prevBody = document.getElementById(`${prefix}_prev_body`);
            const prevBox = document.getElementById(`${prefix}_preview_box`);
            const tokens = getAll18Tokens(prefix);

            if (prevBox) {
                prevBox.style.backgroundColor = tokens.background || '#FFF0D4';
            }

            if (layer) {
                if (bg && bg.url) {
                    layer.style.backgroundImage = `url("${bg.url}")`;
                    layer.style.opacity = ((bg.opacity || 12) / 100).toString();
                    layer.style.filter = (bg.blur && bg.blur > 0) ? `blur(${bg.blur}px)` : 'none';
                    layer.style.display = 'block';
                } else {
                    layer.style.backgroundImage = 'none';
                    layer.style.display = 'none';
                }
            }

            if (prevBody) {
                if (bg && bg.url) {
                    prevBody.style.backgroundColor = 'transparent';
                } else {
                    prevBody.style.backgroundColor = tokens.background || '#FFF0D4';
                }
            }
        }

        // ========================================================
        // 5. CẬP NHẬT TRẠNG THÁI KIỂM TRA ĐỌC ĐƯỢC & XEM TRƯỚC
        // ========================================================
        function getAll18Tokens(prefix) {
            return currentThemeTokens[prefix] || {};
        }

        function updateStatusAndPreview(prefix) {
            const tokens = getAll18Tokens(prefix);
            const hiddenJson = document.getElementById(`${prefix}ThemeTokensJson`);
            if (hiddenJson) {
                hiddenJson.value = JSON.stringify(tokens);
            }

            // 1. Cập nhật Khung Xem Trước (Inline Styles)
            const prevBox = document.getElementById(`${prefix}_preview_box`);
            if (prevBox) prevBox.style.backgroundColor = tokens.background || '#FFF0D4';

            const prevHeader = document.getElementById(`${prefix}_prev_header`);
            if (prevHeader) {
                prevHeader.style.backgroundColor = tokens.surface_container_highest || tokens.surface_container || '#F3E3C8';
                prevHeader.style.borderBottomColor = tokens.outline_variant || '#D8C6AE';
            }
            const prevLogo = document.getElementById(`${prefix}_prev_logo`);
            if (prevLogo) {
                prevLogo.style.backgroundColor = tokens.primary || '#B91C1C';
                prevLogo.style.color = tokens.on_primary || '#FFFFFF';
            }
            const prevBrand = document.getElementById(`${prefix}_prev_brand`);
            if (prevBrand) prevBrand.style.color = tokens.on_surface || '#3B1F14';

            const prevMenu1 = document.getElementById(`${prefix}_prev_menu1`);
            if (prevMenu1) prevMenu1.style.color = tokens.on_surface_variant || '#725A4A';
            const prevMenu2 = document.getElementById(`${prefix}_prev_menu2`);
            if (prevMenu2) prevMenu2.style.color = tokens.on_surface_variant || '#725A4A';

            const prevSearch = document.getElementById(`${prefix}_prev_search`);
            if (prevSearch) {
                prevSearch.style.backgroundColor = tokens.surface_container_high || '#EFDFC5';
                prevSearch.style.borderColor = tokens.outline_variant || '#D8C6AE';
                prevSearch.style.color = tokens.on_surface_variant || '#725A4A';
            }

            const prevCoin = document.getElementById(`${prefix}_prev_coin`);
            if (prevCoin) {
                prevCoin.style.backgroundColor = tokens.primary_container || '#AA1A1A';
                prevCoin.style.color = tokens.on_primary_container || '#FFFFFF';
            }

            const prevBody = document.getElementById(`${prefix}_prev_body`);
            const bgInfo = currentThemeBg[prefix];
            if (prevBody) {
                if (bgInfo && bgInfo.url) {
                    prevBody.style.backgroundColor = 'transparent';
                } else {
                    prevBody.style.backgroundColor = tokens.background || '#FFF0D4';
                }
            }

            const prevTitle = document.getElementById(`${prefix}_prev_title`);
            if (prevTitle) prevTitle.style.color = tokens.on_background || '#3B1F14';

            const prevLinkMore = document.getElementById(`${prefix}_prev_link_more`);
            if (prevLinkMore) prevLinkMore.style.color = tokens.primary || '#B91C1C';

            const prevCard = document.getElementById(`${prefix}_prev_card`);
            if (prevCard) {
                prevCard.style.backgroundColor = tokens.surface || '#FAEBCF';
                prevCard.style.borderColor = tokens.outline || '#A7927E';
            }

            const prevCover = document.getElementById(`${prefix}_prev_cover`);
            if (prevCover) {
                prevCover.style.backgroundColor = tokens.surface_container_low || '#F9EACE';
                prevCover.style.borderColor = tokens.outline_variant || '#D8C6AE';
                prevCover.style.color = tokens.on_surface_variant || '#725A4A';
            }

            const prevNovelTitle = document.getElementById(`${prefix}_prev_novel_title`);
            if (prevNovelTitle) prevNovelTitle.style.color = tokens.on_surface || '#3B1F14';

            const prevNovelAuthor = document.getElementById(`${prefix}_prev_novel_author`);
            if (prevNovelAuthor) prevNovelAuthor.style.color = tokens.secondary || '#8A5A00';

            const prevRating = document.getElementById(`${prefix}_prev_rating`);
            if (prevRating) prevRating.style.color = tokens.primary || '#B91C1C';

            const prevBadgeVip = document.getElementById(`${prefix}_prev_badge_vip`);
            if (prevBadgeVip) {
                prevBadgeVip.style.backgroundColor = tokens.primary_container || '#AA1A1A';
                prevBadgeVip.style.color = tokens.on_primary_container || '#FFFFFF';
            }

            const prevBadgeStatus = document.getElementById(`${prefix}_prev_badge_status`);
            if (prevBadgeStatus) {
                prevBadgeStatus.style.backgroundColor = tokens.surface_container_high || '#EFDFC5';
                prevBadgeStatus.style.color = tokens.on_surface_variant || '#725A4A';
            }

            const prevExcerptBox = document.getElementById(`${prefix}_prev_excerpt_box`);
            if (prevExcerptBox) {
                prevExcerptBox.style.backgroundColor = tokens.surface_container_low || '#F9EACE';
            }
            const prevExcerptText = document.getElementById(`${prefix}_prev_excerpt_text`);
            if (prevExcerptText) prevExcerptText.style.color = tokens.on_background || '#3B1F14';
            const prevExcerptLink = document.getElementById(`${prefix}_prev_excerpt_link`);
            if (prevExcerptLink) prevExcerptLink.style.color = tokens.primary || '#B91C1C';

            const prevDivider = document.getElementById(`${prefix}_prev_divider`);
            if (prevDivider) prevDivider.style.backgroundColor = tokens.outline_variant || '#D8C6AE';

            const prevBtnPrimary = document.getElementById(`${prefix}_prev_btn_primary`);
            if (prevBtnPrimary) {
                prevBtnPrimary.style.backgroundColor = tokens.primary || '#B91C1C';
                prevBtnPrimary.style.color = tokens.on_primary || '#FFFFFF';
            }

            const prevBtnSec = document.getElementById(`${prefix}_prev_btn_sec`);
            if (prevBtnSec) {
                prevBtnSec.style.backgroundColor = tokens.surface_container_low || '#F9EACE';
                prevBtnSec.style.borderColor = tokens.outline || '#A7927E';
                prevBtnSec.style.color = tokens.on_surface || '#3B1F14';
            }

            // 2. Kiểm Tra Các Cặp Độ Đọc Được (Chuẩn WCAG AA >= 4.5:1)
            const failingAreas = [];
            CONTRAST_PAIRS_META.forEach(pair => {
                let fg = '#000000';
                if (pair.fg === 'real_secondary_text') {
                    const isDarkTheme = getLuminance(tokens.background || '#FFFFFF') < 0.2;
                    fg = isDarkTheme ? (tokens.on_secondary || '#082F49') : '#FFFFFF';
                } else if (pair.fg.startsWith('#')) {
                    fg = pair.fg;
                } else {
                    fg = tokens[pair.fg] || '#000000';
                }
                const bg = tokens[pair.bg] || '#FFFFFF';
                const ratio = getContrastRatio(fg, bg);
                if (ratio < 4.5) {
                    failingAreas.push(pair.label);
                }
            });

            // 3. Cập nhật cảnh báo đỏ gọn & Khóa/Mở nút Lưu
            const warnBox = document.getElementById(`${prefix}_contrast_warning`);
            const warnText = document.getElementById(`${prefix}_contrast_warning_text`);
            const submitBtn = document.getElementById(`${prefix}_submit_btn`);

            if (failingAreas.length === 0) {
                // Mọi cặp đạt: KHÔNG hiện gì (ẩn cảnh báo đỏ, không hiện hộp xanh)
                if (warnBox) warnBox.classList.add('hidden');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.classList.remove('opacity-50', 'cursor-not-allowed');
                }
            } else {
                // Có cặp không đạt: hiện MỘT cảnh báo đỏ gọn kèm nút "Tự chỉnh cho dễ đọc" và khoá nút Lưu
                if (warnBox) warnBox.classList.remove('hidden');
                if (warnText) {
                    warnText.innerText = `Chữ khó đọc ở: ${failingAreas.slice(0, 2).join(', ')}${failingAreas.length > 2 ? '...' : ''}`;
                }
                if (submitBtn) {
                    submitBtn.disabled = true;
                    submitBtn.classList.add('opacity-50', 'cursor-not-allowed');
                }
            }
        }

        // ========================================================
        // 6. NÚT "TỰ CHỈNH CHO DỄ ĐỌC" (AUTO-FIX)
        // ========================================================
        function autoFixContrast(prefix) {
            const pInput = document.getElementById(`${prefix}_main_primary`);
            const sInput = document.getElementById(`${prefix}_main_secondary`);
            const bgInput = document.getElementById(`${prefix}_main_bg`);
            const textInput = document.getElementById(`${prefix}_main_text`);
            if (!pInput || !sInput || !bgInput || !textInput) return;

            let p = pInput.value;
            let s = sInput.value;
            let bg = bgInput.value;
            let text = textInput.value;

            const isDark = getLuminance(bg) < 0.2;

            // 1. Chỉnh text so với bg
            if (getContrastRatio(text, bg) < 4.5) {
                const target = isDark ? '#FFFFFF' : '#000000';
                for (let w = 0.1; w <= 0.95; w += 0.05) {
                    const cand = mixColors(text, target, w);
                    if (getContrastRatio(cand, bg) >= 4.5) {
                        text = cand;
                        break;
                    }
                }
            }

            // 2. Chỉnh primary so với bg
            if (getContrastRatio(p, bg) < 4.5) {
                const target = isDark ? '#FFFFFF' : '#000000';
                for (let w = 0.1; w <= 0.95; w += 0.05) {
                    const cand = mixColors(p, target, w);
                    if (getContrastRatio(cand, bg) >= 4.5) {
                        p = cand;
                        break;
                    }
                }
            }

            // 3. Chỉnh secondary so với bg
            if (getContrastRatio(s, bg) < 4.5) {
                const target = isDark ? '#FFFFFF' : '#000000';
                for (let w = 0.1; w <= 0.95; w += 0.05) {
                    const cand = mixColors(s, target, w);
                    if (getContrastRatio(cand, bg) >= 4.5) {
                        s = cand;
                        break;
                    }
                }
            }

            apply4Colors(prefix, p, s, bg, text, true);
        }

        // ========================================================
        // 7. MỞ / ĐÓNG MODAL (ADD & EDIT)
        // ========================================================
        function openAddThemeModal(failedName, failedFolder, failedTokensRaw) {
            initExistingThemeFolders();
            renderPresetCards('add_presets_container', 'add');

            // Reset background UI
            removeBackground('add');
            const addOpSlider = document.getElementById('add_bg_opacity');
            if (addOpSlider) { addOpSlider.value = 12; onBgOpacityChange('add', 12); }
            const addBlSlider = document.getElementById('add_bg_blur');
            if (addBlSlider) { addBlSlider.value = 4; onBgBlurChange('add', 4); }

            let initialTokens = null;
            if (failedTokensRaw) {
                try { initialTokens = JSON.parse(failedTokensRaw); } catch (e) { }
            }

            const nameVal = failedName || '';
            const nameInput = document.getElementById('addThemeName');
            if (nameInput) nameInput.value = nameVal;

            const folderInput = document.getElementById('addThemeFolder');
            if (folderInput) {
                folderInput.value = failedFolder || generateUniqueThemeFolder(nameVal || 'Xuân Cổ Truyền');
            }

            if (initialTokens && typeof initialTokens === 'object' && Object.keys(initialTokens).length > 0) {
                const p = initialTokens.primary || THEME_PRESETS[0].primary;
                const s = initialTokens.secondary || THEME_PRESETS[0].secondary;
                const bg = initialTokens.background || THEME_PRESETS[0].background;
                const text = initialTokens.on_background || THEME_PRESETS[0].on_background;
                apply4Colors('add', p, s, bg, text, true);
            } else {
                // Mặc định chọn mẫu Xuân Cổ Truyền
                const def = THEME_PRESETS[0];
                apply4Colors('add', def.primary, def.secondary, def.background, def.on_background, true);
            }

            const modal = document.getElementById('addThemeModal');
            if (modal) modal.classList.remove('hidden');
        }

        function closeAddThemeModal() {
            const modal = document.getElementById('addThemeModal');
            if (modal) modal.classList.add('hidden');
        }

        function openEditModal(btn) {
            const id = btn.getAttribute('data-theme-id');
            const name = btn.getAttribute('data-theme-name');
            const folder = btn.getAttribute('data-theme-folder');
            const tokensRaw = btn.getAttribute('data-theme-tokens');
            const bgUrl = btn.getAttribute('data-theme-bg-url');
            const bgOpacity = btn.getAttribute('data-theme-bg-opacity');
            const bgBlur = btn.getAttribute('data-theme-bg-blur');
            openEditModalDirect(id, name, folder, tokensRaw, bgUrl, bgOpacity, bgBlur);
        }

        function resetEditPreviewBox() {
            const prevBox = document.getElementById('edit_preview_box');
            if (prevBox) prevBox.style.backgroundColor = '#eae4dc';

            const prevHeader = document.getElementById('edit_prev_header');
            if (prevHeader) {
                prevHeader.style.backgroundColor = '#d8cfc4';
                prevHeader.style.borderBottomColor = '#c4b8aa';
            }
            const prevLogo = document.getElementById('edit_prev_logo');
            if (prevLogo) {
                prevLogo.style.backgroundColor = '#999999';
                prevLogo.style.color = '#ffffff';
            }
            const prevBrand = document.getElementById('edit_prev_brand');
            if (prevBrand) prevBrand.style.color = '#444444';

            const prevMenu1 = document.getElementById('edit_prev_menu1');
            if (prevMenu1) prevMenu1.style.color = '#777777';
            const prevMenu2 = document.getElementById('edit_prev_menu2');
            if (prevMenu2) prevMenu2.style.color = '#777777';

            const prevSearch = document.getElementById('edit_prev_search');
            if (prevSearch) {
                prevSearch.style.backgroundColor = '#ded6cb';
                prevSearch.style.borderColor = '#c4b8aa';
                prevSearch.style.color = '#777777';
            }

            const prevCoin = document.getElementById('edit_prev_coin');
            if (prevCoin) {
                prevCoin.style.backgroundColor = '#999999';
                prevCoin.style.color = '#ffffff';
            }

            const prevBody = document.getElementById('edit_prev_body');
            if (prevBody) prevBody.style.backgroundColor = '#eae4dc';

            const prevTitle = document.getElementById('edit_prev_title');
            if (prevTitle) prevTitle.style.color = '#333333';

            const prevLinkMore = document.getElementById('edit_prev_link_more');
            if (prevLinkMore) prevLinkMore.style.color = '#666666';

            const prevCard = document.getElementById('edit_prev_card');
            if (prevCard) {
                prevCard.style.backgroundColor = '#ffffff';
                prevCard.style.borderColor = '#d8cfc4';
            }
            const prevCover = document.getElementById('edit_prev_cover');
            if (prevCover) {
                prevCover.style.backgroundColor = '#ece6de';
                prevCover.style.borderColor = '#d8cfc4';
                prevCover.style.color = '#888888';
            }
            const prevNovelTitle = document.getElementById('edit_prev_novel_title');
            if (prevNovelTitle) prevNovelTitle.style.color = '#222222';

            const prevNovelAuthor = document.getElementById('edit_prev_novel_author');
            if (prevNovelAuthor) prevNovelAuthor.style.color = '#666666';

            const prevRating = document.getElementById('edit_prev_rating');
            if (prevRating) prevRating.style.color = '#888888';

            const prevBadgeVip = document.getElementById('edit_prev_badge_vip');
            if (prevBadgeVip) {
                prevBadgeVip.style.backgroundColor = '#ded6cb';
                prevBadgeVip.style.color = '#444444';
            }

            const prevBadgeStatus = document.getElementById('edit_prev_badge_status');
            if (prevBadgeStatus) {
                prevBadgeStatus.style.backgroundColor = '#ece6de';
                prevBadgeStatus.style.color = '#555555';
            }

            const prevExcerptBox = document.getElementById('edit_prev_excerpt_box');
            if (prevExcerptBox) {
                prevExcerptBox.style.backgroundColor = '#ded6cb';
                prevExcerptBox.style.color = '#444444';
            }

            const prevDivider = document.getElementById('edit_prev_divider');
            if (prevDivider) prevDivider.style.backgroundColor = '#c4b8aa';

            const prevBtnPrimary = document.getElementById('edit_prev_btn_primary');
            if (prevBtnPrimary) {
                prevBtnPrimary.style.backgroundColor = '#cccccc';
                prevBtnPrimary.style.color = '#666666';
            }
            const prevBtnSec = document.getElementById('edit_prev_btn_sec');
            if (prevBtnSec) {
                prevBtnSec.style.backgroundColor = '#ffffff';
                prevBtnSec.style.borderColor = '#c4b8aa';
                prevBtnSec.style.color = '#666666';
            }
        }

        function openEditModalDirect(id, name, folder, tokensRaw, bgUrl, bgOpacity, bgBlur) {
            // 1. Reset toàn bộ trạng thái trước khi nạp giao diện mới (LỖI 1 & B)
            currentThemeTokens['edit'] = null;
            currentThemeBg.edit = { url: null, opacity: 12, blur: 4 };

            const nameInput = document.getElementById('editThemeName');
            if (nameInput) nameInput.value = name || '';

            const folderInput = document.getElementById('editThemeFolder');
            if (folderInput) folderInput.value = folder || '';

            const editForm = document.getElementById('editThemeForm');
            if (editForm) editForm.action = '/admin/settings/themes/' + id + '/edit';

            const hiddenTokensJson = document.getElementById('editThemeTokensJson');
            if (hiddenTokensJson) hiddenTokensJson.value = '';

            // Reset 4 ô màu chính
            const pMain = document.getElementById('edit_main_primary');
            const pHex = document.getElementById('edit_hex_primary');
            if (pMain) pMain.value = '#875200';
            if (pHex) pHex.value = '';

            const sMain = document.getElementById('edit_main_secondary');
            const sHex = document.getElementById('edit_hex_secondary');
            if (sMain) sMain.value = '#9E422C';
            if (sHex) sHex.value = '';

            const bgMain = document.getElementById('edit_main_bg');
            const bgHex = document.getElementById('edit_hex_bg');
            if (bgMain) bgMain.value = '#FFF8F5';
            if (bgHex) bgHex.value = '';

            const textMain = document.getElementById('edit_main_text');
            const textHex = document.getElementById('edit_hex_text');
            if (textMain) textMain.value = '#231A11';
            if (textHex) textHex.value = '';

            renderPresetCards('edit_presets_container', 'edit');

            // Cấu hình ảnh nền & thanh trượt
            const removeInput = document.getElementById('edit_remove_bg') || document.getElementById('editRemoveBg');
            if (removeInput) removeInput.value = 'false';

            const editFileInput = document.getElementById('edit_bg_file');
            if (editFileInput) editFileInput.value = '';

            const opVal = bgOpacity ? parseInt(bgOpacity) : 12;
            const blVal = (bgBlur !== null && bgBlur !== undefined && bgBlur !== '') ? parseInt(bgBlur) : 4;
            const editOpSlider = document.getElementById('edit_bg_opacity');
            if (editOpSlider) { editOpSlider.value = opVal; onBgOpacityChange('edit', opVal); }
            const editBlSlider = document.getElementById('edit_bg_blur');
            if (editBlSlider) { editBlSlider.value = blVal; onBgBlurChange('edit', blVal); }

            if (bgUrl && bgUrl.trim() !== '') {
                currentThemeBg.edit.url = bgUrl;
                const emptyZone = document.getElementById('edit_bg_empty_zone');
                const previewZone = document.getElementById('edit_bg_preview_zone');
                const previewImg = document.getElementById('edit_bg_preview_img');
                const previewName = document.getElementById('edit_bg_preview_name') || document.getElementById('edit_bg_file_name');

                if (emptyZone) emptyZone.classList.add('hidden');
                if (previewZone) previewZone.classList.remove('hidden');
                if (previewImg) previewImg.src = bgUrl;
                if (previewName) previewName.innerText = 'Ảnh nền hiện tại';
            } else {
                removeBackground('edit');
                if (removeInput) removeInput.value = 'false';
            }
            updateBgPreview('edit');

            const errorNotice = document.getElementById('edit_token_error_notice');
            const submitBtn = document.getElementById('edit_submit_btn');

            let tokens = null;
            if (tokensRaw && tokensRaw.trim() !== '' && tokensRaw.trim() !== '{}' && tokensRaw.trim() !== 'null') {
                try {
                    tokens = JSON.parse(tokensRaw);
                } catch (e) {
                    console.warn('Lỗi phân tích cú pháp tokens JSON:', e);
                }
            }

            if (!tokens || typeof tokens !== 'object' || Object.keys(tokens).length === 0) {
                // Theme CHƯA CÓ tokens trong DB: KHÔNG tự điền bộ màu của theme khác!
                if (errorNotice) {
                    errorNotice.classList.remove('hidden');
                    const titleEl = errorNotice.querySelector('.font-bold');
                    if (titleEl) titleEl.innerText = 'Giao diện này chưa có bộ màu, hãy chọn một mẫu bên trên';
                }
                // Khóa nút Cập nhật cho tới khi chọn mẫu
                if (submitBtn) {
                    submitBtn.disabled = true;
                    submitBtn.classList.add('opacity-50', 'cursor-not-allowed');
                }
                resetEditPreviewBox();
            } else {
                if (errorNotice) errorNotice.classList.add('hidden');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.classList.remove('opacity-50', 'cursor-not-allowed');
                }

                // Nạp và tự suy ra các token còn thiếu nếu mở dữ liệu theme cũ
                const bg = tokens.background || '#FFF8F5';
                const onBg = tokens.on_background || '#231A11';
                const p = tokens.primary || '#875200';
                const s = tokens.secondary || '#9E422C';
                const isDark = getLuminance(bg) < 0.2;

                if (!tokens.surface) tokens.surface = mixColors(bg, onBg, 0.025);
                if (!tokens.on_surface) tokens.on_surface = onBg;
                if (!tokens.surface_container_lowest) tokens.surface_container_lowest = isDark ? mixColors(bg, '#000000', 0.25) : '#FFFFFF';
                if (!tokens.surface_container) tokens.surface_container = mixColors(bg, onBg, 0.06);
                if (!tokens.surface_container_low) tokens.surface_container_low = mixColors(bg, tokens.surface_container, 0.5);
                if (!tokens.surface_container_high) tokens.surface_container_high = mixColors(bg, onBg, 0.08);
                if (!tokens.surface_container_highest) tokens.surface_container_highest = mixColors(bg, onBg, 0.12);
                if (!tokens.on_surface_variant) tokens.on_surface_variant = mixColors(onBg, bg, 0.28);
                if (!tokens.outline) tokens.outline = mixColors(bg, onBg, 0.45);
                if (!tokens.outline_variant) tokens.outline_variant = mixColors(bg, onBg, 0.20);
                if (!tokens.on_primary) tokens.on_primary = getContrastRatio('#FFFFFF', p) >= getContrastRatio('#121212', p) ? '#FFFFFF' : '#121212';
                if (!tokens.primary_container) {
                    let pc = p;
                    if (getContrastRatio('#FFFFFF', pc) < 4.5) {
                        for (let w = 0.05; w <= 0.8; w += 0.05) {
                            const cand = mixColors(p, '#000000', w);
                            if (getContrastRatio('#FFFFFF', cand) >= 4.5) { pc = cand; break; }
                        }
                    }
                    tokens.primary_container = pc;
                }
                if (!tokens.on_primary_container) tokens.on_primary_container = '#FFFFFF';
                if (!tokens.on_secondary) tokens.on_secondary = getContrastRatio('#FFFFFF', s) >= getContrastRatio('#121212', s) ? '#FFFFFF' : '#121212';

                currentThemeTokens['edit'] = tokens;

                // 4 ô chính nạp trực tiếp từ tokens của đúng theme
                if (pMain) pMain.value = p;
                if (pHex) pHex.value = p.toUpperCase();

                if (sMain) sMain.value = s;
                if (sHex) sHex.value = s.toUpperCase();

                if (bgMain) bgMain.value = bg;
                if (bgHex) bgHex.value = bg.toUpperCase();

                if (textMain) textMain.value = onBg;
                if (textHex) textHex.value = onBg.toUpperCase();

                updateStatusAndPreview('edit');
            }

            const modal = document.getElementById('editThemeModal');
            if (modal) modal.classList.remove('hidden');
        }

        function closeEditModal() {
            const modal = document.getElementById('editThemeModal');
            if (modal) modal.classList.add('hidden');
            currentThemeTokens['edit'] = null;
        }

        // Web Modals Confirmation Handlers
        function openActivateModal(id, name) {
            document.getElementById('activateThemeName').innerText = name;
            document.getElementById('activateThemeForm').action = '/admin/settings/themes/' + id + '/activate';
            document.getElementById('activateThemeModal').classList.remove('hidden');
        }

        function closeActivateModal() {
            document.getElementById('activateThemeModal').classList.add('hidden');
        }

        function openDeleteModal(id, name) {
            document.getElementById('deleteThemeName').innerText = name;
            document.getElementById('deleteThemeForm').action = '/admin/settings/themes/' + id + '/delete';
            document.getElementById('deleteThemeModal').classList.remove('hidden');
        }

        function closeDeleteModal() {
            document.getElementById('deleteThemeModal').classList.add('hidden');
        }

