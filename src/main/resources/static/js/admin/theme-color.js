/* ========================================================
 * THEME COLOR CALCULATION & WCAG CONTRAST ENGINE (UC-52)
 * Thuần toán học & thuật toán màu sắc, không chạm DOM
 * ======================================================== */

        const THEME_PRESETS = [
            {
                id: "xuan-co-truyen",
                name: "Xuân Cổ Truyền",
                desc: "Đỏ thắm + Vàng đồng, nền kem ấm",
                primary: "#B91C1C",
                secondary: "#8A5A00",
                background: "#FFF0D4",
                on_background: "#3B1F14"
            },
            {
                id: "thanh-truc",
                name: "Thanh Trúc",
                desc: "Xanh ngọc + Lục trúc, nền xanh ngà",
                primary: "#0A6B4E",
                secondary: "#336340",
                background: "#DDEEE3",
                on_background: "#0F291E"
            },
            {
                id: "da-khuc-dem",
                name: "Dạ Khúc Đêm",
                desc: "Hổ phách + Thiên thanh, nền tối",
                primary: "#F59E0B",
                secondary: "#7DD3FC",
                background: "#0E1013",
                on_background: "#E8EAED"
            },
            {
                id: "hoang-hon",
                name: "Hoàng Hôn",
                desc: "Cam hoàng hôn + Tím, nền kem cam",
                primary: "#C2410C",
                secondary: "#6B21A8",
                background: "#FFF7ED",
                on_background: "#291811"
            },
            {
                id: "dai-duong",
                name: "Đại Dương",
                desc: "Xanh biển + Mòng két, nền lam",
                primary: "#0369A1",
                secondary: "#0D655E",
                background: "#F0F9FF",
                on_background: "#082F49"
            },
            {
                id: "hoa-anh-dao",
                name: "Hoa Anh Đào",
                desc: "Hồng cánh sen + Mận, nền phấn",
                primary: "#BE185D",
                secondary: "#7A193F",
                background: "#FFF1F2",
                on_background: "#370F1E"
            }
        ];

        // 18 Token Whitelist (Hiển thị nhãn tiếng Việt ở dòng chính, token tiếng Anh chỉ nằm trong tooltip)
        const THEME_TOKEN_WHITELIST_META = [
            {
                group: "Nền & Bề mặt",
                icon: "layers",
                tokens: [
                    { key: "background", label: "Màu nền chính của trang", desc: "Nền toàn bộ trang web và khu vực đọc" },
                    { key: "surface", label: "Màu bề mặt thẻ & khối", desc: "Nền các thẻ truyện, khung nội dung" },
                    { key: "surface_container_lowest", label: "Bề mặt khối sáng nhất", desc: "Nền thẻ nổi bật nhất" },
                    { key: "surface_container_low", label: "Bề mặt khối cấp thấp", desc: "Nền khối phụ, thanh lọc" },
                    { key: "surface_container", label: "Bề mặt khối tiêu chuẩn", desc: "Nền khối tiêu chuẩn" },
                    { key: "surface_container_high", label: "Bề mặt khối cấp cao", desc: "Nền thanh tìm kiếm, thẻ phụ" },
                    { key: "surface_container_highest", label: "Bề mặt khối cao nhất", desc: "Nền thanh điều hướng header" }
                ]
            },
            {
                group: "Chữ & Biểu tượng",
                icon: "title",
                tokens: [
                    { key: "on_background", label: "Chữ trên nền chính", desc: "Văn bản hiển thị trên nền chính" },
                    { key: "on_surface", label: "Chữ trên bề mặt thẻ", desc: "Tiêu đề truyện, chữ trên thẻ" },
                    { key: "on_surface_variant", label: "Chữ phụ / nhãn mờ", desc: "Tác giả, ngày đăng, lượt xem" }
                ]
            },
            {
                group: "Màu chủ đạo (Nút & Điểm nhấn)",
                icon: "star",
                tokens: [
                    { key: "primary", label: "Màu chủ đạo", desc: "Nút chính, liên kết, sao đánh giá" },
                    { key: "on_primary", label: "Chữ trên màu chủ đạo", desc: "Chữ trên nút bấm chính" },
                    { key: "primary_container", label: "Hộp màu chủ đạo nhạt", desc: "Nền huy hiệu VIP, hộp nổi bật" },
                    { key: "on_primary_container", label: "Chữ trên hộp chủ đạo", desc: "Chữ trên huy hiệu VIP" }
                ]
            },
            {
                group: "Màu phụ (Huy hiệu & Nhấn phụ)",
                icon: "bookmark",
                tokens: [
                    { key: "secondary", label: "Màu nhấn phụ", desc: "Huy hiệu thể loại, tên tác giả" },
                    { key: "on_secondary", label: "Chữ trên màu phụ", desc: "Chữ trên huy hiệu thể loại" }
                ]
            },
            {
                group: "Đường viền & Phân cách",
                icon: "border_all",
                tokens: [
                    { key: "outline", label: "Đường viền chính", desc: "Viền thẻ truyện, khung phân cách" },
                    { key: "outline_variant", label: "Đường viền phụ", desc: "Đường kẻ ngang mờ" }
                ]
            }
        ];

        // Danh Sách Vùng Kiểm Tra Tương Phản Đầy Đủ (Chuẩn WCAG AA >= 4.5:1)
        const CONTRAST_PAIRS_META = [
            { fg: 'on_background', bg: 'background', label: 'Văn bản trên nền chính' },
            { fg: 'on_surface', bg: 'surface', label: 'Tiêu đề trên thẻ truyện' },
            { fg: 'on_surface_variant', bg: 'surface', label: 'Chữ phụ trên thẻ truyện' },
            { fg: 'on_surface', bg: 'surface_container_lowest', label: 'Tiêu đề trên thẻ nổi bật' },
            { fg: 'on_surface_variant', bg: 'surface_container_lowest', label: 'Chữ phụ trên thẻ nổi bật' },
            { fg: 'on_primary', bg: 'primary', label: 'Chữ trên nút chính' },
            { fg: 'on_primary_container', bg: 'primary_container', label: 'Chữ trên nhãn VIP' },
            { fg: '#FFFFFF', bg: 'primary_container', label: 'Chữ trắng trên nút Đọc ngay (primary_container)' },
            { fg: 'on_secondary', bg: 'secondary', label: 'Chữ trên nhãn thể loại' },
            { fg: 'primary', bg: 'background', label: 'Màu nút chính so với nền' },
            { fg: 'primary', bg: 'surface', label: 'Màu nút chính so với thẻ' },
            { fg: 'secondary', bg: 'surface', label: 'Màu nhãn phụ so với thẻ' },
            { fg: 'primary', bg: 'surface_container', label: 'Chữ chủ đạo trên khối (Pill Coin)' },
            { fg: 'secondary', bg: 'surface_container', label: 'Màu phụ trên khối' },
            { fg: 'on_surface_variant', bg: 'surface_container', label: 'Chữ phụ trên khối' },
            { fg: 'real_secondary_text', bg: 'secondary', label: 'Chữ thực tế trên huy hiệu phụ' }
        ];


        // 2. HÀM XỬ LÝ MÀU SẮC (JS THUẦN, KHÔNG DÙNG THƯ VIỆN)
        // ========================================================
        function hexToRgb(hex) {
            if (!hex || hex.length !== 7 || hex[0] !== '#') return [0, 0, 0];
            return [
                parseInt(hex.substring(1, 3), 16),
                parseInt(hex.substring(3, 5), 16),
                parseInt(hex.substring(5, 7), 16)
            ];
        }

        function rgbToHex(r, g, b) {
            const clamp = val => Math.max(0, Math.min(255, Math.round(val)));
            const toHex = val => clamp(val).toString(16).padStart(2, '0').toUpperCase();
            return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
        }

        function mixColors(color1, color2, weight) {
            const c1 = hexToRgb(color1);
            const c2 = hexToRgb(color2);
            const w = Math.max(0, Math.min(1, weight));
            const r = c1[0] * (1 - w) + c2[0] * w;
            const g = c1[1] * (1 - w) + c2[1] * w;
            const b = c1[2] * (1 - w) + c2[2] * w;
            return rgbToHex(r, g, b);
        }

        function getLuminance(hex) {
            const rgb = hexToRgb(hex).map(v => v / 255.0);
            const rLin = rgb[0] <= 0.04045 ? rgb[0] / 12.92 : Math.pow((rgb[0] + 0.055) / 1.055, 2.4);
            const gLin = rgb[1] <= 0.04045 ? rgb[1] / 12.92 : Math.pow((rgb[1] + 0.055) / 1.055, 2.4);
            const bLin = rgb[2] <= 0.04045 ? rgb[2] / 12.92 : Math.pow((rgb[2] + 0.055) / 1.055, 2.4);
            return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin;
        }

        function getContrastRatio(hex1, hex2) {
            const l1 = getLuminance(hex1);
            const l2 = getLuminance(hex2);
            const max = Math.max(l1, l2);
            const min = Math.min(l1, l2);
            return (max + 0.05) / (min + 0.05);
        }

        // Tự sinh 14 token từ 4 màu chính theo quy tắc đã duyệt
        function generate14Tokens(primary, secondary, background, onBackground) {
            const isDark = getLuminance(background) < 0.2;

            // 1. on_primary: trắng hoặc #121212 tuỳ cái nào tương phản cao hơn với primary
            const onPrimary = getContrastRatio('#FFFFFF', primary) >= getContrastRatio('#121212', primary) ? '#FFFFFF' : '#121212';

            // 2. secondary: tự chỉnh độ sáng nếu tương phản với background < 4.5
            let adjSecondary = secondary;
            if (getContrastRatio(adjSecondary, background) < 4.5) {
                const target = isDark ? '#FFFFFF' : '#000000';
                for (let w = 0.05; w <= 0.95; w += 0.05) {
                    const cand = mixColors(secondary, target, w);
                    if (getContrastRatio(cand, background) >= 4.5) {
                        adjSecondary = cand;
                        break;
                    }
                }
            }

            // 3. on_secondary: trắng hoặc #121212
            const onSecondary = getContrastRatio('#FFFFFF', adjSecondary) >= getContrastRatio('#121212', adjSecondary) ? '#FFFFFF' : '#121212';

            // 4. primary_container: Màu ĐẶC (mid/strong tone) đảm bảo tương phản >= 4.5:1 với chữ trắng (nút Đọc ngay dùng chữ trắng)
            let primaryContainer = primary;
            if (getContrastRatio('#FFFFFF', primaryContainer) < 4.5) {
                for (let w = 0.05; w <= 0.8; w += 0.05) {
                    const cand = mixColors(primary, '#000000', w);
                    if (getContrastRatio('#FFFFFF', cand) >= 4.5) {
                        primaryContainer = cand;
                        break;
                    }
                }
            } else {
                const cand = mixColors(primary, isDark ? '#FFFFFF' : '#000000', 0.08);
                if (getContrastRatio('#FFFFFF', cand) >= 4.5) {
                    primaryContainer = cand;
                }
            }

            // 5. on_primary_container: chữ trên nút Đọc ngay luôn dùng màu trắng đạt chuẩn >= 4.5:1
            const onPrimaryContainer = '#FFFFFF';

            // 6. surface: nền nhích 2.5% về phía chữ
            const surface = mixColors(background, onBackground, 0.025);

            // 7. surface_container_lowest: trắng (sáng) hoặc tối hơn nền (tối)
            const surfaceLowest = isDark ? mixColors(background, '#000000', 0.25) : '#FFFFFF';

            // 8. surface_container tầng 3%, 6%, 9%, 12%
            const surfaceLow = mixColors(background, onBackground, 0.03);
            const surfaceMed = mixColors(background, onBackground, 0.06);
            let surfaceHigh = mixColors(background, onBackground, 0.08);
            if (getContrastRatio(primary, surfaceHigh) < 4.5) {
                surfaceHigh = mixColors(background, onBackground, 0.07);
            }
            const surfaceHighest = mixColors(background, onBackground, 0.12);

            // 9. on_surface & on_surface_variant (tự chỉnh để >= 4.5:1 với nền)
            const onSurface = onBackground;
            let onSurfaceVariant = mixColors(onBackground, background, 0.28);
            if (getContrastRatio(onSurfaceVariant, background) < 4.5) {
                for (let w = 0.25; w >= 0.0; w -= 0.02) {
                    const cand = mixColors(onBackground, background, w);
                    if (getContrastRatio(cand, background) >= 4.5) {
                        onSurfaceVariant = cand;
                        break;
                    }
                }
            }

            // 10. outline (45%) & outline_variant (20%)
            const outline = mixColors(background, onBackground, 0.45);
            const outlineVariant = mixColors(background, onBackground, 0.20);

            return {
                background: background,
                on_background: onBackground,
                surface: surface,
                on_surface: onSurface,
                on_surface_variant: onSurfaceVariant,
                surface_container_lowest: surfaceLowest,
                surface_container_low: surfaceLow,
                surface_container: surfaceMed,
                surface_container_high: surfaceHigh,
                surface_container_highest: surfaceHighest,
                primary: primary,
                primary_container: primaryContainer,
                on_primary: onPrimary,
                on_primary_container: onPrimaryContainer,
                secondary: adjSecondary,
                on_secondary: onSecondary,
                outline: outline,
                outline_variant: outlineVariant
            };
        }

