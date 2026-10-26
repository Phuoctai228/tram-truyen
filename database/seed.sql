-- ================================================================================
-- DATABASE SEED DATA: TRẠM TRUYỆN (SWP391)
-- ================================================================================

-- 1. Seed default roles (Bỏ id, bắt trùng theo name)
INSERT INTO roles (name) VALUES 
('ROLE_ADMIN'),
('ROLE_STAFF'),
('ROLE_MEMBER')
ON CONFLICT (name) DO NOTHING;

-- 2. Seed default users (Bỏ id, bắt trùng theo email)
-- Mật khẩu sử dụng mã hóa BCrypt của chuỗi '12345678'
-- $2a$10$LmC09bKcSP1wc7N7eArmUe8kOsjWYqjPYoTeIMJseMNNoRpPiGwSS
INSERT INTO users (email, password, full_name, wallet_balance, status) VALUES 
('admin@tramtruyen.com', '$2a$10$LmC09bKcSP1wc7N7eArmUe8kOsjWYqjPYoTeIMJseMNNoRpPiGwSS', 'System Admin', 10000, 'ACTIVE'),
('staff@tramtruyen.com', '$2a$10$LmC09bKcSP1wc7N7eArmUe8kOsjWYqjPYoTeIMJseMNNoRpPiGwSS', 'Content Staff', 5000, 'ACTIVE'),
('member@tramtruyen.com', '$2a$10$LmC09bKcSP1wc7N7eArmUe8kOsjWYqjPYoTeIMJseMNNoRpPiGwSS', 'Standard Reader', 1000, 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- 3. Seed user_roles (Map động theo email và role name, không set cứng id)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM (VALUES 
    ('admin@tramtruyen.com', 'ROLE_ADMIN'),
    ('staff@tramtruyen.com', 'ROLE_STAFF'),
    ('member@tramtruyen.com', 'ROLE_MEMBER')
) AS mapping(email, role_name)
JOIN users u ON u.email = mapping.email
JOIN roles r ON r.name = mapping.role_name
ON CONFLICT DO NOTHING;

-- 4. Seed categories (Bỏ id, bắt trùng theo slug)
INSERT INTO categories (name, slug, description) VALUES
('Tiên Hiệp', 'tien-hiep', 'Truyện về thế giới tu đạo, phi thăng tiên giới, trường sinh bất lão.'),
('Kiếm Hiệp', 'kiem-hiep', 'Truyện về thế giới võ lâm giang hồ, môn phái, hiệp khách, ân oán tình cừu.'),
('Huyền Huyễn', 'huyen-huyen', 'Truyện về thế giới ma pháp, dị giới, năng lực siêu nhiên phương Đông và phương Tây.'),
('Ngôn Tình', 'ngon-tinh', 'Truyện khai thác đề tài tình cảm lãng mạn, thanh xuân vườn trường, ngọt sủng.'),
('Đô Thị', 'do-thi', 'Truyện bối cảnh thế giới hiện đại, thương trường, dị năng đô thị, đời sống thường nhật.')
ON CONFLICT (slug) DO NOTHING;

-- 5. Seed system_settings (Cấu hình tỷ giá nạp Coin, thông tin liên hệ và chính sách bản quyền)
INSERT INTO system_settings (setting_key, setting_value, description) VALUES 
('vnd_to_coin_rate', '10', 'Tỷ giá nạp tiền: 1.000 VNĐ = 10 Coin (tương đương 100 VNĐ / 1 Coin khi nạp)'),
('support_email', 'support@tramtruyen.com', 'Email tiếp nhận khiếu nại bản quyền và hỗ giả'),
('contact_phone', '1900-1234', 'Hotline hỗ trợ kỹ thuật và chăm sóc khách hàng 24/7'),
('notice_takedown_policy', 'Cam kết thẩm tra và tạm ẩn tác phẩm vi phạm bản quyền trong vòng 24h kể từ khi nhận được khiếu nại hợp lệ', 'Chính sách xử lý vi phạm bản quyền và gỡ bỏ tác phẩm vi phạm trong 24h')
ON CONFLICT (setting_key) DO UPDATE 
SET setting_value = EXCLUDED.setting_value, description = EXCLUDED.description;

-- 5.1 Seed web_settings (Chủ đề và giao diện website - UC-07 / UC-52)
INSERT INTO web_settings (name, folder_path, thumbnail_url, is_active) VALUES
('Trạm Truyện Chuẩn (Mặc định)', 'theme-default', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80', TRUE),
('Xuân Cổ Truyền (Tết & Lễ Hội)', 'theme-tet', 'https://images.unsplash.com/photo-1576085898323-218337e3e43c?auto=format&fit=crop&w=600&q=80', FALSE),
('Dạ Khúc Đêm (Midnight Dark)', 'theme-dark', 'https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=600&q=80', FALSE),
('Thanh Trúc Tiên Giới (Kiếm Hiệp & Tu Tiên)', 'theme-bamboo', 'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=600&q=80', FALSE)
ON CONFLICT (folder_path) DO NOTHING;

-- 6. Seed coin_packages (6 gói nạp Coin chuẩn thiết kế Figma)
INSERT INTO coin_packages (name, price, coin_amount, status) VALUES
('Gói Khởi Điểm', 20000, 200, 'ACTIVE'),
('Gói Tân Thủ (+10 Tặng)', 50000, 510, 'ACTIVE'),
('Gói Tiêu Chuẩn (+50 Tặng)', 100000, 1050, 'ACTIVE'),
('Gói Trưởng Thành (+150 Tặng)', 200000, 2150, 'ACTIVE'),
('Gói Tiết Kiệm (+500 Tặng)', 500000, 5500, 'ACTIVE'),
('Gói Đại Gia VIP (+1.200 Tặng)', 1000000, 11200, 'ACTIVE')
ON CONFLICT DO NOTHING;

-- 7. Seed sample novels, categories, and chapters (mỗi chương > 2000 chữ)
-- ================================================================================
-- DỮ LIỆU MẪU NOVELS & CHAPTERS (TẤT CẢ CHƯƠNG ĐỀU TRÊN 2000 TỪ)
-- Tự động sinh bởi generator: 8 truyện, 16 chương
-- ================================================================================

-- ================================================================================
-- Truyện: Võ Đạo Đan Thần (vo-dao-dan-than)
-- ================================================================================
INSERT INTO novels (title, slug, author, summary, cover_url, status, is_deleted, uploader_id, views, average_rating, rating_count)
VALUES (
    'Võ Đạo Đan Thần',
    'vo-dao-dan-than',
    'Tiêu Dao Du',
    'Thiếu niên Lâm Phong vốn là đệ tử bình thường của Lâm gia tại Vân Sơn Thành, linh mạch bẩm sinh bế tắc, chịu đủ mọi chế giễu khinh khi. Trong một lần mạo hiểm tìm kiếm linh thảo, hắn vô tình đạt được cơ duyên nghịch thiên cùng truyền thừa cổ xưa, từ đó bước lên con đường tu luyện nghịch thiên cải mệnh, khuấy đảo phong vân...',
    'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=600&q=80',
    'ONGOING',
    false,
    (SELECT id FROM users WHERE email = 'admin@tramtruyen.com'),
    150,
    5.0,
    10
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    author = EXCLUDED.author,
    summary = EXCLUDED.summary,
    cover_url = EXCLUDED.cover_url,
    status = EXCLUDED.status,
    is_deleted = false;

INSERT INTO novel_categories (novel_id, category_id)
SELECT n.id, c.id
FROM novels n
CROSS JOIN categories c
WHERE n.slug = 'vo-dao-dan-than'
  AND c.slug IN ('tien-hiep', 'huyen-huyen')
ON CONFLICT DO NOTHING;

WITH target_novel AS (
    SELECT id FROM novels WHERE slug = 'vo-dao-dan-than'
)
INSERT INTO chapters (novel_id, chapter_number, title, content, price, status, views, is_deleted)
SELECT
    tn.id,
    c.chapter_number, c.title, c.content, c.price, c.status, c.views, c.is_deleted
FROM target_novel tn,
(VALUES
(1, 'Khởi Đầu Gian Nan', 'Trời đổ cơn mưa rào mùa hạ xối xả xuống mái ngói rêu phong của đình viện Lâm gia tại Vân Sơn Thành. Từng hạt mưa nặng trĩu đập liên hồi vào những phiến đá xanh xám, tạo nên thanh âm rào rạt như tiếng thác đổ nơi núi sâu hoang vắng. Thiếu niên Lâm Phong đứng lặng lẽ dưới mái hiên nhà tranh dột nát nơi góc đông bắc của phủ đệ, đôi mắt đen láy nhìn chăm chú vào dòng nước mưa cuộn xoáy đang chảy tràn qua rãnh thoát nước. Hơi nước bốc lên lạnh buốt phả vào gương mặt gầy guộc nhưng góc cạnh cương nghị của thiếu niên, hòa cùng vị đất ẩm nồng nặc của những ngày đầu hạ oi bức.

Mười sáu tuổi, cái tuổi đẹp nhất của đời người tu giả, cái tuổi mà con cháu của các thế gia vọng tộc trong toàn thành Vân Sơn đang háo hức phô diễn thiên phú. Những người cùng lứa với hắn, con em các dòng họ Triệu, họ Lý, hay thậm chí là đám đệ tử chi thứ của chính Lâm gia, người kém cỏi nhất cũng đã sớm đả thông ba kinh mạch, bước vào Luyện Khí Tầng Ba hoặc Tầng Bốn. Vài ba vị thiên tài trác tuyệt được gia tộc dốc lòng bồi dưỡng bằng vô số linh đan diệu dược thậm chí đã chạm tới ngưỡng cửa Tụ Khí Cảnh, chuẩn bị nghênh tiếp khảo hạch của các đại tông môn danh tiếng.

Còn hắn, Lâm Phong, con trai độc nhất của chi trưởng từng một thời hiển hách, nay lại bị xem như nỗi sỉ nhục lớn nhất của toàn bộ dòng tộc. Ngay từ ngày đầu tiên thức tỉnh võ hồn năm mười tuổi, các vị trưởng lão đã bàng hoàng phát hiện chín đại chủ kinh mạch trong cơ thể hắn bẩm sinh đã bế tắc hoàn toàn, không có lấy một tia linh khí nào có thể lưu chuyển. Ở thế giới Thần Võ Đại Lục nơi thực lực vi tôn, người không thể tu luyện chẳng khác nào kẻ tàn phế, một con sâu cái kiến mặc cho kẻ khác tùy ý giẫm đạp chà đạp dưới chân.

Suốt sáu năm ròng rã, trong khi đám đồng trang lứa ngồi trong tĩnh thất ấm áp hấp thu linh thạch, Lâm Phong phải dậy từ lúc trời chưa hửng sáng, gánh nước quét sân, dầm mưa dãi nắng rèn luyện thể phách theo phương pháp thô sơ nhất. Hắn không tin vào số phận nghiệt ngã đã an bài. Mỗi một giọt mồ hôi rơi xuống phiến đá, mỗi một vết trầy xước rướm máu trên da thịt đều là minh chứng cho ý chí bất khuất của hắn. Dù nỗ lực gấp mười, gấp trăm lần người thường, đan điền của hắn vẫn trơ trơ như đá tảng, chỉ tích tụ được một tia khí cảm yếu ớt tương đương Luyện Khí Tầng Một.

"Phong nhi, chớ bao giờ nản lòng hay oán hận đất trời. Thiên đạo vô biên, vạn vật đều có một tia sinh cơ huyền diệu. Chỉ cần tâm niệm của con bất diệt, ý chí của con kiên định tựa bàn thạch, sẽ có một ngày con tìm được con đường độc nhất vô nhị của chính bản thân mình." Lời trăn trối ấm áp của mẫu thân trước lúc lâm chung vào ba năm trước vẫn luôn khắc sâu trong tâm khảm Lâm Phong. Đó là điểm tựa tinh thần duy nhất giúp hắn đứng vững trước muôn vàn lời gièm pha, nhục mạ và những ánh mắt khinh miệt của tộc nhân suốt bấy nhiêu năm qua.

Kỷ niệm về mẫu thân không chỉ là những lời khuyên nhủ ân cần, mà còn là những đêm dài bà chong đèn dạy hắn nhận biết từng loại cỏ cây dược liệu. Mẫu thân hắn từng là một đan sư tài hoa trước khi lâm trọng bệnh, và bà đã truyền dạy cho hắn toàn bộ kiến thức uyên bác về thảo mộc mà một thiếu niên cùng trang lứa không bao giờ có được. Nhờ đó, dù không thể ngưng tụ linh lực, Lâm Phong vẫn có thể chỉ mặt đặt tên hàng ngàn loại linh thảo, hiểu rõ tương sinh tương khắc giữa các vị thuốc.

Lâm Phong khẽ thở dài một hơi, bàn tay chai sần đưa lên siết chặt chuôi của thanh đoản kiếm rỉ sét giắt bên hông. Hắn quay người bước vào gian phòng xập xệ, lấy ra một chiếc áo tơi rách rưới khoác lên người. Mục tiêu của hắn hôm nay là Tử Phong Sơn – dãy núi hiểm trở nằm sừng sững phía sau thành Vân Sơn. Nơi ấy rừng thiêng nước độc, dã thú và yêu thú cấp thấp hoành hành, ngay cả thợ săn lão luyện cũng không dám mạo hiểm đi sâu vào những ngày mưa bão. Nhưng sáng nay, hắn tình cờ nghe được tin tức từ đám gia đinh rằng có người vừa nhìn thấy một cây Huyết Linh Thảo ba trăm năm tuổi sắp nở hoa nơi vách đá Tử Nguyệt.

Huyết Linh Thảo là dược liệu quý hiếm có tác dụng tẩy trừ tạp chất và kích thích khí huyết. Theo một phương thuốc cổ tàn khuyết mà mẫu thân để lại trong chiếc hộp gỗ cũ, nếu kết hợp Huyết Linh Thảo với ba loại thảo dược thông thường khác, có thể luyện chế thành một viên Tẩy Tủy Đan sơ cấp. Dù cơ hội thành công mong manh như ngọn đèn trước gió, Lâm Phong vẫn quyết tâm liều mạng một phen. Nếu cứ tiếp tục chôn chân ở góc sân này đợi đến kỳ sát hạch cuối năm, hắn chắc chắn sẽ bị trục xuất khỏi gia tộc, vĩnh viễn không thể ngẩng đầu lên làm người.

Bước chân ra khỏi cổng sau của Lâm gia, Lâm Phong hòa mình vào màn mưa xối xả, cắm đầu chạy về phía chân núi Tử Phong. Bùn đất nhớp nháp bám chặt vào đôi giày vải đã mòn gót, từng đợt gió lạnh rít qua kẽ lá như muốn thổi bay tấm thân gầy gò của thiếu niên. Rừng rậm bạt ngàn hiện ra trước mắt với vẻ u ám rợn người. Những thân cây đại thụ cao hàng chục trượng che khuất cả bầu trời, dây leo chằng chịt quấn quanh những tảng đá rêu phong trơn trượt. Không gian thỉnh thoảng lại vang lên tiếng gầm gừ trầm đục của bầy Thiết Tí Viên đang ẩn nấp trong các hang động tối tăm.

Trên đường đi, hắn phải liên tục dừng lại quan sát địa hình để tránh né những bẫy rập tự nhiên và lãnh địa của bầy độc trùng. Với khứu giác nhạy bén được rèn luyện qua năm tháng, hắn phân biệt được mùi ẩm mốc của cỏ mục và mùi xạ hương tanh tưởi của loài mãnh thú ăn thịt. Càng dấn sâu vào tử địa, áp lực đè nặng lên đôi vai thiếu niên càng lớn, nhưng khát vọng thay đổi vận mệnh đã tiếp thêm cho hắn nguồn dũng khí vô tận để vượt qua nỗi sợ hãi nguyên thủy nhất.

Dựa vào trí nhớ và sự quan sát tỉ mỉ tích lũy từ những lần hái thuốc trước đây, Lâm Phong thận trọng lần theo mép vực sâu Tử Nguyệt. Nơi này vách đá dựng đứng như bị rìu chém, bên dưới là vực thẳm sâu không thấy đáy, mây mù cuồn cuộn che phủ tứ bề. Sau hơn ba canh giờ lặn lội trong mưa gió đến mức kiệt sức, hai mắt hắn bỗng sáng rực lên khi bắt gặp một điểm sáng đỏ rực le lói giữa khe nứt đá cheo leo cách mép vực chừng mười thước. Đúng là Huyết Linh Thảo! Cánh hoa màu đỏ thẫm như ngọc lưu ly, đang khẽ lay động tỏa ra làn hương thơm ngát giữa làn mưa lạnh giá.

Không chần chừ, Lâm Phong buộc chặt một sợi dây thừng bện bằng vỏ cây rừng vào một gốc tùng cổ thụ gần đó, rồi cẩn thận tụt mình xuống vách đá. Gió rít gào bên tai, từng đợt nước mưa táp rát bỏng vào mắt, nhưng ánh mắt thiếu niên vẫn kiên định nhìn thẳng vào đóa chu thảo. Từng tấc, từng tấc một, hắn nhích lại gần khe đá. Khi ngón tay chỉ còn cách gốc linh thảo nửa tấc, một luồng kình phong tanh tưởi bỗng nhiên từ trong bóng tối của hốc đá phóng vọt ra!

"Xì xì!" Một cái đầu rắn to như cái đấu với lớp vảy đen kịt ánh lên sắc kim loại bổ nhào tới. Đôi mắt đỏ ngầu như hai đốm lửa ma trơi của Hắc Lân Mãng – yêu thú cấp một đỉnh phong – khóa chặt vào yết hầu của Lâm Phong. Tốc độ của nó nhanh như một tia chớp đen, mùi nọc độc hôi thối xộc thẳng vào mũi thiếu niên. Trong khoảnh khắc sinh tử ngàn cân treo sợi tóc, Lâm Phong không hề hoảng loạn. Hắn mượn quán tính đung đưa của sợi dây, gập người lại né tránh cú đớp chết người, đồng thời rút phắt đoản kiếm chém mạnh một nhát vào mắt trái của con quái thú.

"Keng!" Mũi kiếm gãy đôi, nhưng nhát đâm chuẩn xác đã đâm thủng mắt con cự xà, máu đen bắn tung tóe. Con mãng xà rống lên một tiếng đau đớn cuồng nộ, cái đuôi khổng lồ nặng nề quét ngang một vòng với sức mạnh ngàn cân. "Rầm!" Cú quật sấm sét giáng thẳng vào lồng ngực Lâm Phong, sợi dây thừng đứt phựt, cả người hắn bay vút ra giữa khoảng không mênh mông của đáy vực thẳm. Trước khi rơi tự do vào màn sương mù vô tận, bàn tay phải của hắn vẫn kịp chộp lấy đóa Huyết Linh Thảo nhổ phắt khỏi khe đá.

Thân thể rơi nhanh vun vút giữa không trung, tiếng gió rít bên tai tựa như khúc ca ai điếu. Cảm giác xương sườn gãy vụn đâm vào phổi khiến Lâm Phong đau đớn đến mức không thể thở nổi. Ý thức của hắn dần mờ nhạt, bầu trời mưa gió phía trên thu nhỏ lại thành một vệt sáng mỏng manh. "Chẳng lẽ cuộc đời ta kết thúc ở đây sao? Mẫu thân... con phụ lòng người rồi..." Một giọt nước mắt hòa cùng nước mưa lăn dài trên gò má thiếu niên trước khi thân xác hắn va đập dữ dội vào những cành cây rậm rạp dưới đáy vực rồi rơi bịch xuống một bãi cỏ ẩm ướt.

Thời gian chầm chậm trôi qua. Khi cơn mưa dứt hẳn, bầu trời đêm trên đỉnh núi Tử Phong quang đãng lạ thường, vầng trăng khuyết treo lơ lửng rải những dải lụa bạc lung linh xuống đáy vực sâu. Giữa lớp cỏ úa và bùn lầy, Lâm Phong nằm bất động trong vũng máu. Máu tươi từ miệng và lồng ngực hắn chảy tràn xuống nền đất, vô tình thấm ướt một vật thể hình tròn bằng ngọc đang bị vùi lấp nửa phần dưới lớp đất đá bên cạnh.

Chiếc ngọc bội cổ xưa ấy vốn dĩ u tối như đá cuội, nhưng khi tiếp xúc với máu tươi thuần dương của Lâm Phong, bề mặt của nó bỗng nhiên rung chuyển khe khẽ. Chín đạo tinh quang màu lam nhạt từ từ bừng sáng, khắc họa hình ảnh chín vì sao cổ đại vần vũ xung quanh một chiếc lò luyện đan thần bí. Một luồng linh áp uy nghiêm cổ kính từ thời hồng hoang bừng tỉnh, bao trùm lấy toàn bộ không gian bán kính mười trượng, xua tan mọi chướng khí và độc trùng xung quanh.

Cổ vật thần bí này đã nằm im lìm dưới đáy vực Tử Phong suốt vạn năm qua, trải qua muôn vàn phong ba bão táp mà không một sinh linh nào phát hiện. Nó là chí bảo trấn tông của một thế lực đan đạo viễn cổ đã phi thăng tiên giới, chờ đợi một người có tâm tính kiên cường tột bậc và ý chí bất khuất đến để kế thừa y bát.

Một thanh âm trầm hùng, vang vọng như tiếng chuông đồng từ cửu thiên vọng xuống thức hải của thiếu niên đang hấp hối: "Chín mạch bế tắc, ý chí bất diệt, huyết mạch tương hợp... Kẻ có duyên, nhận lấy truyền thừa Cửu Tinh Diễn Thiên Quyết của Bản tôn!" Dứt lời, chiếc ngọc bội hóa thành một luồng ánh sáng chói lòa, bắn thẳng vào giữa trán của Lâm Phong, bắt đầu một quá trình tái sinh thần kỳ sẽ làm đảo lộn cả càn khôn đại lục.', 0, 'PUBLISHED', 45, false),
(2, 'Thức Tỉnh Cửu Tinh Quyết', 'Luồng ánh sáng màu lam nhạt từ ngọc bội cổ xưa sau khi tiến vào thức hải của Lâm Phong liền hóa thành một biển năng lượng mênh mông vô tận. Từng đợt sóng linh khí thuần khiết cuồn cuộn chảy tràn, bao bọc lấy linh hồn đang mờ nhạt của thiếu niên. Lâm Phong cảm thấy toàn bộ ý thức của mình như bị kéo vào một khoảng không gian tinh không bao la, nơi có chín ngôi sao khổng lồ rực rỡ xoay vần theo những quỹ đạo huyền ảo khó lường. Ở trung tâm của chín ngôi sao ấy, một chiếc đan đỉnh bằng đồng cổ kính tỏa ra hào quang bảy màu, uy áp bức người khiến vạn vật phải quỳ gối phủ phục.

Dưới đáy vực sâu lạnh lẽo, thân xác của Lâm Phong bắt đầu xảy ra những biến đổi kinh thiên động địa. Năng lượng của Cửu Tinh ngọc bội tràn vào tứ chi bách hài, tựa như hàng ngàn dòng suối ấm áp len lỏi vào từng thớ thịt, tế bào và kinh mạch bị tổn thương. Xương cốt gãy vụn nơi lồng ngực bắt đầu kêu lên răng rắc, tự động gắn kết và hồi phục với tốc độ mắt thường cũng có thể nhìn thấy. Làn da bầm tím bong tróc từng mảng, nhường chỗ cho một lớp da mới mịn màng và dẻo dai như ngọc thạch.

Những tế bào da thịt vừa tái sinh không chỉ lành lặn mà còn hấp thu tinh hoa của tinh thần chi lực, trở nên cứng cáp như sắt nguội tôi luyện qua trăm ngàn độ lửa. Các vết sẹo cũ kỹ tích tụ từ những năm tháng bị kẻ khác đánh đập hành hạ đều biến mất không để lại một dấu vết nào, thay vào đó là một cơ thể hoàn mỹ cân đối, tràn trề sinh lực và tiềm năng vô hạn.

Quan trọng hơn cả, chín đường kinh mạch bế tắc suốt mười sáu năm qua của Lâm Phong dưới sự thanh tẩy của tinh thần chi lực bắt đầu rạn nứt. Từng khối tạp chất đen ngòm, hôi hám tích tụ từ lúc lọt lòng bị luồng năng lượng thần thánh nghiền nát thành tro bụi, rồi theo các lỗ chân lông đào thải toàn bộ ra bên ngoài cơ thể. Cơn đau đớn khi kinh mạch bị xé toạc rồi tái tạo lại còn dữ dội hơn gấp trăm lần việc bị dã thú cắn xé. Từng tấc da thịt như bị ngọn lửa vô hình thiêu đốt, từng khúc xương tủy như bị búa tạ nện vào.

Lâm Phong tỉnh lại giữa cơn đau thấu trời ấy. Nhưng thay vì gào thét hay ngất đi, hắn cắn chặt răng đến mức bật máu tươi nơi khóe môi, hai bàn tay bấu chặt vào đất đá dưới thân đến mức móng tay gãy vụn. Hắn biết rõ, đây chính là cơ hội duy nhất mà ông trời ban cho hắn để nghịch thiên cải mệnh. Nếu không chịu nổi nỗi đau lột xác này, hắn sẽ mãi mãi là một tên phế vật bị người đời khinh khi chà đạp. Ý chí kiên cường trui rèn qua sáu năm cay đắng đã giúp hắn giữ vững một tia thanh minh cuối cùng giữa cơn bão táp năng lượng.

"Cửu Tinh Diễn Thiên, tâm như gương sáng, tụ khí quy nguyên, khai thiên lập địa!" Từng dòng khẩu quyết cổ xưa bằng chữ vàng kim tự động hiện lên trong tâm trí hắn. Lâm Phong theo bản năng điều động tâm thức, ngồi xếp bằng ngay trên bãi cỏ, dẫn dắt luồng tinh hoa năng lượng khổng lồ đang chạy loạn trong cơ thể quay trở về đan điền. Hắn bắt đầu vận hành tầng thứ nhất của Cửu Tinh Diễn Thiên Quyết.

"Oanh!" Một tiếng nổ trầm đục vang lên trong cơ thể Lâm Phong tựa như tiếng sấm đầu mùa. Đường kinh mạch thứ nhất – Thái Âm Phế Kinh – vốn dĩ bị tắc nghẽn hoàn toàn nay đã bị luồng tinh thần chi lực oanh tạc mở toang! Linh khí thiên địa xung quanh đáy vực lập tức như ong vỡ tổ, điên cuồng ùa vào cơ thể thiếu niên qua từng lỗ chân lông. Đan điền của hắn vốn khô cằn như sa mạc nay bắt đầu xuất hiện một luồng khí xoáy màu lam nhạt, xoay tròn không ngừng và tỏa ra khí tức dồi dào.

Luyện Khí Tầng Hai! Cảnh giới mà hắn khao khát suốt ba năm qua rốt cuộc đã nhẹ nhàng vượt qua chỉ trong một cái chớp mắt. Nhưng tất cả mới chỉ là sự khởi đầu. Dược lực của đóa Huyết Linh Thảo ba trăm năm tuổi mà hắn vẫn nắm chặt trong tay bỗng nhiên tan chảy dưới nhiệt độ của ngọc bội, hóa thành một dòng huyết quang thuần khiết dung nhập vào dòng năng lượng. Hai luồng sức mạnh một chí dương một chí âm hòa quyện vào nhau, thúc đẩy đà đột phá tiếp tục dâng cao như vũ bão.

"Rắc!" Đường kinh mạch thứ hai – Dương Minh Đại Trường Kinh – tiếp tục bị khai thông! Khí tức của Lâm Phong lại tăng vọt lên một nấc thang mới. Luyện Khí Tầng Ba! Cả cơ thể thiếu niên tỏa ra ánh sáng lung linh mờ ảo, lớp bùn đất và máu khô trên người bị sóng xung kích đánh bay ra xa, để lộ thân hình thon gọn nhưng ẩn chứa sức bật kinh người của một loài báo săn nơi thảo nguyên.

Chưa dừng lại ở đó, luồng năng lượng vẫn cuồn cuộn không dứt, trực tiếp xông thẳng vào kinh mạch thứ ba. Lâm Phong gầm nhẹ một tiếng, khí thế toàn thân bùng nổ, ép gãy cả những cành cây bụi gai xung quanh trong phạm vi ba trượng. Luyện Khí Tầng Bốn! Chỉ trong vỏn vẹn một đêm, từ một kẻ tàn phế Luyện Khí Tầng Một yếu ớt, hắn đã liên tục phá vỡ ba tầng bình cảnh, trở thành một tu sĩ Luyện Khí Tầng Bốn trung kỳ!

Tốc độ tu luyện này nếu truyền ra ngoài thành Vân Sơn, chắc chắn sẽ khiến toàn bộ các bậc lão quái vật của các đại gia tộc phải kinh hãi đến rụng rời tay chân. Ngay cả thiên tài số một của thành thị cũng phải mất ít nhất hai đến ba năm khổ tu cùng hàng đống đan dược mới có thể từ tầng một leo lên tầng bốn. Nhưng Lâm Phong hiểu rằng, sự lột xác thực sự của hắn không chỉ nằm ở cảnh giới bên ngoài, mà nằm ở chất lượng linh lực trong cơ thể.

Linh lực màu lam nhạt mà Cửu Tinh Diễn Thiên Quyết ngưng tụ ra có độ tinh khiết và uy lực vượt xa linh lực thông thường của các công pháp hoàng giai hay huyền giai. Nó mang theo đặc tính hủy diệt và tái sinh của tinh thần chi lực, vừa sắc bén như đao kiếm, vừa bền bỉ dẻo dai khôn cùng. Không chỉ vậy, khi Lâm Phong nhắm mắt lại tập trung tinh thần, hắn phát hiện trong thức hải của mình xuất hiện một kho tàng tri thức đan đạo khổng lồ. Hàng vạn phương thuốc cổ xưa, bí quyết nhận biết dược tính, cách điều khiển đan hỏa và kỹ thuật luyện đan thất truyền đều được khắc sâu vào tâm trí hắn.

Trong kho tàng ấy, hắn nhìn thấy Thần Nông Cửu Chuyển Đỉnh thần bí, thấy được ngọn lửa Cửu U Tinh Hỏa có thể tinh luyện mọi loại linh dược thế gian đến độ thuần khiết tuyệt đối mười phần. Người tu luyện Cửu Tinh Diễn Thiên Quyết khi đạt tới cảnh giới cao thâm có thể một tay luyện đan, một tay trấn áp vạn cổ cự đầu, tung hoành giữa trời đất mà không gặp bất kỳ đối thủ nào xứng tầm.

Bên cạnh đó, vô số tâm đắc về phương pháp phối hợp dược liệu, thuật khống chế hỏa hầu và quy trình kết đan huyền diệu cũng lần lượt được khắc sâu vào tâm trí hắn. Những kiến thức mà mẫu thân truyền dạy năm xưa giờ đây được kết nối một cách hoàn hảo với truyền thừa viễn cổ, mở ra trước mắt Lâm Phong một chân trời đan đạo thênh thang vô hạn.

Lâm Phong hít sâu một hơi để ổn định lại tâm thần đang dậy sóng. Hắn biết rằng cơ duyên càng lớn thì trách nhiệm và hung hiểm đi kèm càng nhiều. Một khi tin tức về việc hắn sở hữu bảo vật viễn cổ này bị rò rỉ ra ngoài, không chỉ bản thân hắn mà ngay cả thành Vân Sơn này cũng sẽ bị các đại thế lực san bằng trong chốc lát. Hắn nhất định phải cẩn trọng, giấu giếm thực lực, tích lũy nội hàm cho đến ngày đủ lông đủ cánh.

Hắn nhìn xuống cơ thể mình, y phục đã rách tơi tả và lấm lem bùn đất, nhưng khí huyết bên trong lại cuồn cuộn dâng trào như sóng dữ. Hắn cảm nhận được từng mạch máu đang đập mạnh mẽ, từng thớ cơ bắp tràn đầy sức bật. Đây chính là cảm giác của một tu giả thực thụ, cảm giác mà hắn đã từng mơ ước trong vô vọng suốt sáu năm trời đằng đẵng.

Lâm Phong từ từ mở mắt ra. Đôi mắt đen láy giờ đây sáng rực như những vì sao trên bầu trời đêm, sâu thẳm và ẩn chứa một cỗ uy nghiêm trầm tĩnh vượt xa lứa tuổi. Hắn đứng bật dậy, nhẹ nhàng vung ra một quyền vào tảng đá hoa cương to bằng chiếc bàn học bên cạnh. "Oanh!" Không cần sử dụng bất kỳ võ kỹ nào, chỉ bằng sức mạnh thể chất thuần túy kết hợp với một tia linh lực tầng bốn, tảng đá cứng rắn lập tức nứt toác thành hàng trăm mảnh vụn bắn tung tóe ra xung quanh.

"Sức mạnh này... ít nhất cũng ngang ngửa với một đòn toàn lực của tu sĩ Luyện Khí Tầng Sáu!" Lâm Phong nhìn nắm đấm của mình, khóe miệng khẽ nhếch lên một nụ cười rạng rỡ. Sáu năm nhục nhã, sáu năm uất ức kìm nén trong lòng, vào giây phút này dường như đã tìm được lối thoát. Hắn ngửa mặt lên nhìn bầu trời đêm đầy sao qua miệng vực thẳm, cất lên một tiếng thét dài vang vọng khắp núi rừng Tử Phong.

Tiếng gầm thét như rồng ngâm hổ khiếu làm chấn động cả chim chóc và thú dữ đang ngủ say trong rừng. Đám sói hoang cách đó mấy dặm cũng phải cụp đuôi trốn sâu vào hang đá, cảm nhận được một luồng khí tức thượng cổ tôn quý vừa giáng lâm.

Tiếng thét mang theo sự giải thoát, mang theo sự tự tin kiêu hãnh của một con rồng vừa thức tỉnh khỏi vực sâu lầy lội. Thu dọn lại y phục rách nát, Lâm Phong nhìn về phía vách đá cheo leo. Giờ đây với thể lực phi thường và linh lực dồi dào, vách đá dựng đứng từng suýt cướp đi mạng sống của hắn chẳng còn là trở ngại. Hắn tung người nhảy lên, nhẹ nhàng như một cánh chim ưng lướt qua các mỏm đá, nhanh chóng leo lên đỉnh vực.

Phía chân trời đằng đông, ánh rạng đông màu hồng ngọc bắt đầu xua tan màn đêm lạnh giá, chiếu rọi những tia nắng ấm áp đầu tiên xuống thành Vân Sơn cổ kính. Lâm Phong đứng trên đỉnh núi cao lộng gió, áo choàng bay phần phật, ánh mắt sắc lạnh như kiếm quang hướng về phía phủ đệ Lâm gia xa xăm. "Lâm gia, Triệu gia, những kẻ đã từng coi ta là cỏ rác... Hãy đợi đấy! Lâm Phong ta đã trở về, và thời đại của các ngươi sắp kết thúc rồi!"', 0, 'PUBLISHED', 32, false)
) AS c(chapter_number, title, content, price, status, views, is_deleted)
ON CONFLICT (novel_id, chapter_number) DO UPDATE
SET title = EXCLUDED.title, content = EXCLUDED.content, price = EXCLUDED.price, status = EXCLUDED.status;

-- ================================================================================
-- Truyện: Đấu Phá Khung Thương (dau-pha-khung-thuong)
-- ================================================================================
INSERT INTO novels (title, slug, author, summary, cover_url, status, is_deleted, uploader_id, views, average_rating, rating_count)
VALUES (
    'Đấu Phá Khung Thương',
    'dau-pha-khung-thuong',
    'Thiên Tàm Thổ Đậu',
    'Nơi đây là thế giới thuộc về Đấu Khí, không có ma pháp hoa tiếu diễm lệ, chỉ có Đấu Khí phồn diễn đến đỉnh cao...',
    'https://images.unsplash.com/photo-1614730321146-b6fa6a46bcb4?auto=format&fit=crop&w=600&q=80',
    'COMPLETED',
    false,
    (SELECT id FROM users WHERE email = 'admin@tramtruyen.com'),
    3200,
    4.8,
    500
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    author = EXCLUDED.author,
    summary = EXCLUDED.summary,
    cover_url = EXCLUDED.cover_url,
    status = EXCLUDED.status,
    is_deleted = false;

INSERT INTO novel_categories (novel_id, category_id)
SELECT n.id, c.id
FROM novels n
CROSS JOIN categories c
WHERE n.slug = 'dau-pha-khung-thuong'
  AND c.slug IN ('huyen-huyen')
ON CONFLICT DO NOTHING;

WITH target_novel AS (
    SELECT id FROM novels WHERE slug = 'dau-pha-khung-thuong'
)
INSERT INTO chapters (novel_id, chapter_number, title, content, price, status, views, is_deleted)
SELECT
    tn.id,
    c.chapter_number, c.title, c.content, c.price, c.status, c.views, c.is_deleted
FROM target_novel tn,
(VALUES
(1, 'Thiên Tài Vẫn Lạc', '"Đấu Chi Khí, tam đoạn!"

Nhìn năm chữ to màu đỏ chói mắt hiển thị rõ ràng trên bề mặt Trắc Nghiệm Ma Thạch Bi sừng sững giữa quảng trường, thiếu niên Tiêu Viêm mặt không chút biểu tình, khóe môi khẽ mím chặt mang theo vài phần tự giễu cay đắng. Đôi bàn tay giấu trong ống tay áo rộng nắm chặt lại vì dùng sức quá mức mà trở nên trắng bệch, móng tay cắm sâu vào da thịt mang theo từng cơn đau đớn nhói buốt nơi lòng bàn tay. Nhưng cơn đau thể xác ấy chẳng thể nào xua đi được nỗi tuyệt vọng cùng cực đang gặm nhấm tâm can hắn từng ngày từng giờ suốt ba năm qua.

"Tiêu Viêm, Đấu Chi Khí, tam đoạn! Cấp bậc: Cấp thấp!" Bên cạnh Ma Thạch Bi cổ kính, vị trung niên trắc nghiệm viên của gia tộc liếc mắt nhìn thoáng qua bảng ghi chép trong tay, giọng nói hờ hững lạnh lùng vang lên khắp quảng trường rộng lớn. Thanh âm ấy không mang theo chút cảm xúc nào, giống như một phán quyết quen thuộc được lặp đi lặp lại hàng năm, đóng chặt cánh cửa tương lai của một người từng được xem là hy vọng số một của toàn bộ gia tộc.

Tiếng nói vừa dứt, toàn bộ quảng trường vốn đang tĩnh lặng lập tức như một chảo dầu sôi sùng sục bị hắt vào một gáo nước lạnh. Hàng trăm ánh mắt của đám đệ tử trẻ tuổi Tiêu gia đứng xung quanh đồng loạt đổ dồn về phía thiếu niên áo đen đang đứng đơn độc trên đài cao. Trong những ánh mắt ấy, có sự thương hại của vài kẻ yếu đuối, có sự tiếc nuối giả tạo của đám trưởng bối, nhưng nhiều nhất, áp đảo nhất vẫn là sự hả hê, khinh miệt và châm chọc cay độc của những kẻ từng một thời phải cúi đầu trước hắn.

"Tam đoạn? Chậc chậc, quả nhiên không nằm ngoài dự liệu của mọi người. Vị thiên tài số một của Tiêu gia chúng ta từ trước tới nay, ba năm trôi qua rồi mà vẫn kiên định giậm chân tại chỗ ở Đấu Chi Khí tam đoạn! Thật là một kỳ tích hiếm có trong lịch sử Ô Thản Thành!"

"Phế vật đúng là phế vật, thật sự đã làm mất sạch thể diện của liệt tổ liệt tông Tiêu gia rồi. Nếu không phải phụ thân hắn là tộc trưởng Tiêu Chiến đương nhiệm, loại phế nhân bất tài vô dụng như hắn đã sớm bị tước đoạt toàn bộ đãi ngộ, đuổi xéo đến các trang viên biên ải hẻo lánh làm nông phu từ lâu rồi, làm sao còn có mặt mũi đứng trên đài trắc nghiệm này nữa?"

"Nghĩ lại năm xưa mà xem, lúc hắn mới bốn tuổi bắt đầu luyện khí, mười tuổi đã đạt tới Cửu đoạn Đấu Chi Khí, mười một tuổi ngưng tụ Đấu Chi Khí Toàn thành công, trở thành Đấu Giả trẻ tuổi nhất của toàn bộ Gia Mã Đế Quốc trong suốt một trăm năm qua! Khi đó hắn kiêu ngạo biết bao nhiêu, chói sáng như vầng thái dương trên đỉnh trời, bọn ta đứng trước mặt hắn ngay cả thở mạnh cũng không dám. Thật không ngờ trời cao có mắt, một thiên tài lừng lẫy rốt cuộc cũng có ngày ngã từ trên mây xanh xuống vũng bùn lầy, biến thành trò cười cho toàn bộ đế quốc!"

Những lời bàn tán xì xầm mang đầy nọc độc như những mũi tên nhọn hoắt liên tiếp xuyên thẳng vào màng nhĩ Tiêu Viêm. Thiếu niên mười lăm tuổi hít sâu một hơi không khí lạnh buốt, lồng ngực phập phồng dữ dội. Hắn ngẩng đầu lên, ánh mắt sắc sảo lướt qua từng gương mặt quen thuộc đang vây quanh khán đài. Những gương mặt ấy, ba năm trước đây khi hắn còn ngự trị trên đỉnh cao danh vọng, từng ngày ngày vây quanh hắn nịnh bợ, gọi một tiếng Tiêu Viêm ca ca, hai tiếng biểu ca thiên tài, sẵn sàng làm bất cứ điều gì chỉ để đổi lấy một cái gật đầu của hắn.

Trên khán đài danh dự phía đông, ba vị đại trưởng lão của Tiêu gia ngồi vuốt râu mỉm cười kín đáo, trong ánh mắt lộ rõ sự hài lòng khi thấy Tiêu Viêm thất bại. Bọn họ bấy lâu nay luôn tìm cách chèn ép chi trưởng của tộc trưởng Tiêu Chiến để tranh giành quyền quản lý các sản nghiệp béo bở trong thành. Mỗi một bước thụt lùi của Tiêu Viêm đều là cái cớ hoàn hảo để bọn họ gây áp lực đòi cắt giảm tài nguyên bồi dưỡng dành cho chi trưởng.

Ngồi ở vị trí trung tâm, tộc trưởng Tiêu Chiến với vóc dáng vạm vỡ uy nghi siết chặt tay vịn chiếc ghế bành bằng gỗ mun đến mức rạn nứt. Ánh mắt ông nhìn về phía con trai vừa chan chứa nỗi đau xót vô bờ, vừa tràn ngập sự bất lực cay đắng. Ông biết con trai mình đã nỗ lực đến nhường nào, biết đứa trẻ này phải thức thâu đêm suốt sáng chịu đựng nỗi đau giày vò, nhưng trước quy tắc gia tộc và sự săm soi của trưởng lão viện, ngay cả một tộc trưởng như ông cũng không thể thiên vị một cách mù quáng.

Vậy mà khi biến cố giáng xuống, khi đấu khí khổ tu trong cơ thể hắn không rõ nguyên do bỗng nhiên biến mất không còn một mảnh, chính những kẻ từng khúm núm ấy lại là những người đầu tiên quay lưng buông lời sỉ nhục, giẫm đạp lên lòng tự trọng của hắn một cách tàn nhẫn nhất. Thế thái viêm lương, lòng người ấm lạnh, trong ba năm ngắn ngủi nơi đáy vực sâu tăm tối này, Tiêu Viêm đã nếm trải đủ mọi cay đắng tủi nhục mà một đời người có thể trải qua.

Mỗi một bữa ăn, mỗi một buổi phát đan dược hàng tháng, hắn đều phải chịu đựng những cái bĩu môi và sự bớt xén trắng trợn của đám quản sự phòng kho. Ngay cả phụ thân hắn, Tiêu Chiến, dù là tộc trưởng quyền uy nhưng vì che chở cho hắn mà cũng chịu không ít sức ép từ ba vị đại trưởng lão trong gia tộc. Mỗi lần nhìn thấy mái tóc hoa râm ngày một nhiều thêm trên đầu cha mình, lòng Tiêu Viêm như bị dao cứa, đau đớn khôn cùng.

Nhưng cũng chính nhờ ba năm đọa đày nghiệt ngã ấy, tính cách của Tiêu Viêm đã được tôi luyện trở nên kiên định, trầm ổn và sâu sắc vượt xa bất kỳ thiếu niên mười lăm tuổi nào trên thế gian. Hắn không còn là một kẻ ngông cuồng tự phụ dựa vào thiên phú bẩm sinh, mà đã hóa thành một con sói cô độc ẩn mình trong bóng tối, nhẫn nhục chịu đựng chờ đợi ngày giương nanh múa vuốt. Hắn khẽ thở dài một tiếng, xoay người cất bước đi xuống khỏi đài trắc nghiệm. Dưới ánh hoàng hôn đỏ quạch như máu, bóng lưng đơn độc của thiếu niên kéo dài trên nền đá hoa cương lạnh lẽo.

"Tiêu Viêm ca ca..." Một giọng nói trong trẻo, êm dịu tựa như tiếng suối mát đầu nguồn bỗng vang lên bên tai hắn. Giữa đám đông đang hò reo mỉa mai, một thiếu nữ áo tím thanh tú tao nhã nhẹ nhàng bước ra đón hắn. Nàng sở hữu dung mạo tuyệt mỹ nghiêng nước nghiêng thành tựa như hoa sen chớm nở trong sương sớm, khí chất thoát tục cao quý như tiên tử hạ phàm khiến đám thiếu niên xung quanh đều phải tự ti cúi đầu không dám nhìn thẳng. Đó chính là Tiêu Huân Nhi – viên minh châu rực rỡ nhất của Tiêu gia hiện tại, người duy nhất có thân phận bí ẩn khiến ngay cả tộc trưởng cũng phải kính trọng ba phần.

Nàng mỉm cười nhìn Tiêu Viêm, trong đôi mắt phượng tuyệt đẹp không hề có lấy nửa phần khinh khi hay giả tạo, chỉ tràn ngập sự dịu dàng và tin tưởng tuyệt đối: "Huân Nhi tin tưởng, Tiêu Viêm ca ca nhất định sẽ có ngày lấy lại toàn bộ vinh quang của năm xưa. Những kẻ thiển cận tầm thường kia làm sao có thể hiểu được ý chí và sự kiên cường của huynh."

Nhìn nụ cười chân thành ấm áp của thiếu nữ, tảng đá đè nặng ngàn cân trong lồng ngực Tiêu Viêm dường như tan biến đi đôi chút. Hắn đưa bàn tay chai sần xoa nhẹ mái tóc đen mượt mà của nàng, khóe môi khẽ nở một nụ cười ấm áp: "Huân Nhi, muội không cần phải an ủi ta đâu. Ba năm qua ta đã quen với những chuyện này rồi, một chút gió sương ấy chẳng thể đánh gục được ta đâu. Đi thôi, phụ thân chắc đang đợi ở đại sảnh để chuẩn bị tiếp đón sứ giả của Vân Lam Tông."

Hai người sóng vai nhau bước đi dưới bóng râm của hàng liễu ven hồ, bỏ lại sau lưng những tiếng xì xào ghen tị của đám thiếu niên Tiêu gia. Trong mắt bọn họ, Huân Nhi tựa như đóa sen tuyết trên đỉnh núi cao thiêng liêng không thể khinh nhờn, vậy mà lại luôn dành cho một tên phế vật như Tiêu Viêm sự dịu dàng và quan tâm hết mực.

Rời khỏi quảng trường náo nhiệt đầy rẫy thị phi, Tiêu Viêm trở về căn phòng nhỏ quen thuộc của mình nơi góc đông viện tĩnh mịch. Hắn đóng chặt then cài cửa sổ, ngả phịch người xuống chiếc giường gỗ thô ráp, mệt mỏi nhắm nghiền hai mắt. Bàn tay hắn theo thói quen vô thức đưa lên sờ vào chiếc nhẫn cổ bằng đồng thau màu đen xám đang đeo trên ngón tay trỏ. Chiếc nhẫn này có bề mặt xù xì thô ráp, hoa văn mờ nhạt cổ xưa, là di vật duy nhất mà mẫu thân để lại cho hắn trước lúc qua đời.

"Ba năm rồi... rốt cuộc là vì sao cơ chứ?!" Tiêu Viêm nghiến răng lẩm bẩm, âm thanh nghẹn ngào run rẩy vì uất ức. "Mỗi một ngày ta khổ công ngồi nhập định ngưng tụ đấu khí, chỉ sau một đêm liền tan biến vào hư vô không để lại một dấu vết. Rốt cuộc trong cơ thể ta có cái quái gì đang nuốt chửng đấu khí của ta?!"

Càng nghĩ, ngọn lửa uất hận trong lòng Tiêu Viêm càng bốc cháy dữ dội. Hắn không cam tâm sống một cuộc đời vô danh tiểu tốt, mặc cho số phận trêu đùa. Hắn thề rằng dù có phải trả giá bằng cả tính mạng, hắn cũng phải tìm ra chân tướng sự việc để đòi lại công đạo cho bản thân và làm rạng danh phụ thân. Đúng lúc hắn chuẩn bị chìm vào tuyệt vọng, chiếc nhẫn màu đen trên ngón tay bỗng nhiên phát ra một luồng u quang kỳ dị...

Luồng ánh sáng ấy ban đầu chỉ le lói như đốm lân tinh trong đêm tối, nhưng chỉ vài giây sau đã bùng lên rực rỡ, chiếu sáng bừng từng góc ngách của căn phòng tối tăm. Một luồng dao động linh hồn kỳ dị lan tỏa, mở màn cho biến cố lớn nhất cuộc đời thiếu niên.

Tiêu Viêm kinh ngạc đứng sững lại giữa phòng, bàn tay bất giác siết chặt chuôi dao găm. Hắn nín thở theo dõi từng tia sáng u lam đang nhảy múa trên bề mặt kim loại cũ kỹ, cảm nhận một luồng sinh mệnh thức tỉnh sau giấc ngủ vùi ngàn năm. Trực giác mách bảo cho hắn biết rằng cuộc đời u tối của hắn sắp sửa lật sang một trang hoàn toàn mới.', 0, 'PUBLISHED', 1500, false),
(2, 'Dược Lão Thức Tỉnh', 'Ánh sáng lân tinh màu u lam từ chiếc nhẫn cổ bằng đồng thau màu đen xám bỗng nhiên tỏa ra chói mắt, tạo thành một quầng sáng mờ ảo bao phủ lấy toàn bộ gian phòng ngủ của Tiêu Viêm. Cùng lúc đó, nhiệt độ trong phòng đột ngột hạ xuống thấp đến mức khiến từng hơi thở của thiếu niên ngưng tụ thành những làn khói trắng xóa phả vào không trung. Một luồng uy áp linh hồn vô hình nhưng thâm sâu tựa biển cả bao trùm lấy không gian, đè ép khiến lồng ngực Tiêu Viêm nghẹn lại, hô hấp trở nên cực kỳ khó khăn.

"Ai?! Kẻ nào ở đó?!" Tiêu Viêm giật mình bật dậy khỏi giường, theo bản năng sinh tồn rút thanh chủy thủ sắc bén giấu dưới gối ra thủ thế trước ngực. Đôi mắt đen láy của hắn tràn đầy cảnh giác cao độ, ánh mắt khóa chặt vào quầng sáng đang lập lòe trên ngón tay mình. Hắn chưa từng thấy chiếc nhẫn của mẫu thân có bất kỳ biểu hiện kỳ lạ nào như thế này suốt mười lăm năm qua.

Ngay trước ánh mắt kinh ngạc tột độ của thiếu niên, luồng bạch quang từ chiếc nhẫn dần dần bay bổng lên cao rồi ngưng tụ lại giữa không trung căn phòng, từ từ hóa thành một bóng người hư ảo trôi nổi bềnh bồng. Đó là một lão giả râu tóc bạc phơ, khoác trên mình một bộ trường bào màu trắng phiêu dật như tiên phong đạo cốt. Khuôn mặt lão tràn đầy những nếp nhăn của thời gian và năm tháng, nhưng đôi mắt sâu thẳm như chứa đựng cả tinh không vũ trụ bao la, mang theo vẻ tang thương nhưng lại lộ rõ khí thế ngạo nghễ bễ nghễ thiên hạ.

Lão giả lơ lửng giữa phòng, ung dung vuốt chòm râu bạc dài đến ngực, nhìn thiếu niên đang cầm đoản kiếm run rẩy trước mặt rồi bật cười khặc khặc: "Khặc khặc, tiểu tử, phản ứng cũng không tệ lắm, ít nhất còn biết cầm dao phòng thân chứ không đến mức tè ra quần. Đáng tiếc là cảnh giới Đấu Chi Khí tam đoạn của ngươi nếu gặp phải cường giả thực sự thì chẳng khác nào lấy trứng chọi đá, một đầu ngón tay cũng đủ nghiền nát ngươi thành tro bụi."

"Ngươi... ngươi rốt cuộc là người hay là quỷ?! Tại sao lại trốn trong chiếc nhẫn của mẫu thân ta bấy lâu nay?!" Tiêu Viêm cố gắng đè nén nỗi kinh hoàng đang cuộn trào trong lồng ngực, lớn tiếng quát hỏi để lấy lại can đảm. Bàn tay cầm dao của hắn đã ướt đẫm mồ hôi hột trơn trượt.

Lão giả bay lượn một vòng quanh người Tiêu Viêm, ánh mắt quan sát hắn từ trên xuống dưới đầy vẻ soi mói và tán thưởng: "Ta không phải quỷ, cũng chưa hẳn là người sống, tạm thời ngươi cứ coi ta là một linh hồn thể may mắn thoát chết đi. Còn về việc vì sao ta ở trong chiếc nhẫn này... hừm, tiểu tử, ngươi phải cảm tạ trời đất mười đời vì chiếc nhẫn này đã rơi vào tay ngươi đấy."

Nói đến đây, lão giả dừng lại một chút, trên gương mặt già nua lộ ra một nụ cười đầy vẻ đắc ý và gian xảo: "Nói cho ngươi biết một bí mật nhỏ, ba năm qua, số đấu khí mà ngươi ngày đêm khổ cực tu luyện được mỗi ngày... tất cả đều là do lão phu mượn tạm để tẩm bổ cho linh hồn đang ngủ say này đấy! Nếu không nhờ vào sự kiên trì bền bỉ không ngừng nghỉ và số đấu khí thuần khiết của ngươi suốt ba năm ròng rã, lão phu e rằng vẫn còn hôn mê sâu thêm vài trăm năm nữa mới có thể tỉnh lại."

Lời nói vừa dứt, toàn bộ căn phòng dường như rơi vào một khoảng lặng chết chóc đến nghẹt thở. Từng giọt mồ hôi trên trán Tiêu Viêm đông cứng lại. Đồng tử của thiếu niên co rút lại thành hai điểm kim ti sắc nhọn. Một ngọn lửa phẫn nộ ngập trời từ tận đáy lòng bỗng nhiên bùng cháy dữ dội như núi lửa phun trào, thiêu đốt toàn bộ lý trí và sự sợ hãi của hắn!

Ba năm! Ròng rã ba năm trời hắn phải sống trong cảnh nhục nhã ê chề, chịu đựng muôn vàn lời sỉ nhục của tộc nhân, ánh mắt cười nhạo của toàn bộ người dân thành Ô Thản, nỗi đau đớn cùng cực của một thiên tài bị đày đọa xuống bùn đen... Tất cả những bi kịch đẫm nước mắt ấy, hóa ra không phải vì bản thân hắn vô dụng hay phế vật, mà là vì lão già quỷ quái không biết từ đâu chui ra này đã lén lút hút cạn toàn bộ thành quả tu luyện của hắn!

"Lão khốn kiếp! Ta liều mạng với ngươi!" Tiêu Viêm gầm lên một tiếng như dã thú phát điên bị dồn vào đường cùng, hoàn toàn không màng đến sự chênh lệch sức mạnh kinh người, hắn dồn toàn bộ sức lực của Đấu Chi Khí tam đoạn vào cánh tay, vung mạnh thanh đoản kiếm sắc nhọn đâm thẳng vào giữa ngực của bóng người hư ảo!

Thế nhưng, lưỡi dao thép lạnh ngắt chỉ đâm xuyên qua làn khói mờ ảo như chém vào không khí mà không chạm được vào bất cứ thực thể nào. Lão giả chỉ khẽ phẩy nhẹ ống tay áo rộng một cái, một cơn gió lốc vô hình nhưng mạnh mẽ như sóng thần lập tức hất văng Tiêu Viêm bay ngược ra sau, ngã nhào xuống sàn gạch, thanh đoản kiếm rơi leng keng xuống đất.

"Bình tĩnh lại đi tiểu hỏa tử! Nóng nảy như vậy làm sao thành đại sự được!" Lão giả cười ha hả nói tiếp, giọng điệu có phần dỗ dành: "Lão phu thừa nhận hành động mượn đấu khí ba năm qua quả thực đã khiến ngươi phải chịu không ít ấm ức và tủi nhục. Nhưng ngươi hãy bình tâm suy nghĩ kỹ lại xem, nếu không nhờ ba năm chịu đựng ma luyện nơi vực thẳm ấy, tâm tính của một thiếu niên mười lăm tuổi như ngươi liệu có thể đạt được sự kiên cường, trầm ổn và nhẫn nhịn như ngày hôm nay hay không? Những thiên tài chết yểu trên đại lục này phần lớn đều chết vì ngạo mạn tự phụ. Ba năm rèn giũa tâm tính này đối với con đường tu luyện tương lai của ngươi, chính là một món quà vô giá mà không một kho vàng nào có thể mua được!"

"Cút mẹ ngươi đi! Ta không cần thứ quà tặng chết tiệt đó! Mau trả lại đấu khí ba năm qua cho ta!" Tiêu Viêm chống tay gượng dậy khỏi mặt đất, đôi mắt đỏ ngầu gắt gao trừng trừng nhìn đối phương, lồng ngực phập phồng như chiếc ống bễ lò rèn.

Lão giả lắc đầu cười khổ, sau đó thu lại vẻ mặt cợt nhả, thần sắc đột nhiên trở nên trang nghiêm và uy nghi lạ thường. Khí chất của lão trong nháy mắt biến đổi hoàn toàn, tựa như một vị chúa tể chí cao vô thượng vừa thức tỉnh sau giấc ngủ ngàn năm: "Đấu khí đã hấp thu vào linh hồn thì không thể hoàn lại. Nhưng lão phu có thể bồi thường cho ngươi gấp mười lần, gấp trăm lần những gì ngươi đã mất." Lão dừng lại một nhịp, cất giọng sang sảng đầy kiêu hãnh: "Tiểu tử, ngươi có biết trên Đấu Khí Đại Lục rộng lớn vô biên này, chức nghiệp nào là tôn quý, quyền lực và được vạn người kính ngưỡng nhất không?"

Ánh mắt Tiêu Viêm khẽ dao động, một ý nghĩ lóe lên trong đầu: "Luyện Dược Sư?" Lão giả cười lớn một tiếng đầy ngạo nghễ: "Chính xác! Luyện Dược Sư hô phong hoán vũ, một viên đan dược xuất thế có thể khiến các bậc Đấu Hoàng, Đấu Tông phải quỳ gối cầu xin! Chỉ cần ngươi chịu bái ta làm sư phụ, không quá một năm, lão phu không chỉ giúp ngươi khôi phục lại thiên phú năm xưa, mà còn đào tạo ngươi trở thành một Luyện Dược Sư đỉnh phong nhất đại lục!"

Nói đoạn, trên đầu ngón tay trong suốt của lão giả bỗng nhiên bùng lên một ngọn lửa màu trắng ngà kỳ dị. Ngọn lửa ấy cháy rực rỡ nhưng không hề tỏa ra nhiệt lượng nóng bức, ngược lại còn khiến không khí xung quanh đóng băng thành từng lớp sương muối trắng xóa. Đó chính là Cốt Nhiễm Lãnh Hỏa – một trong những loại Dị Hỏa bài danh thượng thừa trên Dị Hỏa Bảng của đại lục!

Lão giả phẩy nhẹ ngón tay, ngọn lửa trắng thu về trong lòng bàn tay rồi tan biến như chưa từng xuất hiện. Lão mỉm cười nhìn Tiêu Viêm, ánh mắt tràn đầy sự tự tin tuyệt đối: "Lão phu tên Dược Trần, người đời gọi là Dược Tôn Giả. Ngươi nghĩ sao tiểu tử? Bái ta làm sư phụ, ngươi không những không thiệt thòi mà còn có thể bước chân lên đỉnh phong cao nhất của thế giới này, ngạo nghễ nhìn xuống vạn vật!"

Tiêu Viêm ngơ ngẩn nhìn ngọn lửa trắng kỳ dị vừa biến mất. Dị Hỏa – thứ thần vật trong truyền thuyết mà hắn chỉ được nghe qua trong các điển tịch cổ xưa – vậy mà lại nằm gọn trong tay lão giả này. Trong lòng hắn dâng lên một đợt sóng thần kinh ngạc. Nếu những lời lão nói là thật, thì đây không chỉ là sự bồi thường, mà là một cơ duyên nghịch thiên đủ để thay đổi hoàn toàn vận mệnh của hắn và cả gia tộc Tiêu gia.

Hắn hít sâu một hơi, quỳ sụp xuống dập đầu ba cái trước bóng người hư ảo: "Đệ tử Tiêu Viêm, bái kiến Dược Lão sư phụ!"

Dược Trần khẽ gật đầu, ánh mắt lóe lên vài phần hoài niệm: "Con đường trở thành Luyện Dược Sư khó hơn lên trời. Điều kiện tiên quyết là ngươi phải sở hữu thuộc tính hỏa mang theo một tia mộc thuộc tính, đồng thời linh hồn cảm tri lực phải vượt xa người thường. May mắn thay cho ngươi, thể chất của ngươi hoàn toàn thỏa mãn những điều kiện khắt khe đó, thậm chí linh hồn lực sinh ra từ hai kiếp người còn mạnh mẽ hơn bất kỳ đệ tử thế gia nào mà ta từng gặp."

Lão dừng lại một chút, nụ cười trên môi càng thêm thâm trầm: "Ngày mai, nghe nói tiểu nha đầu của Vân Lam Tông sẽ đến đây để từ hôn với ngươi đúng không? Cứ để cho bọn chúng đắc ý một thời gian đi. Ba năm sau, lão phu muốn xem xem biểu cảm của tông môn đứng đầu đế quốc đó sẽ ra sao khi nhìn thấy ngươi một cước đạp vỡ thiên kiêu của bọn chúng!"

Tiêu Viêm ngẩng đầu lên, ánh mắt bốc cháy ngọn lửa kiên định chưa từng thấy: "Ba năm... Ta nhất định sẽ khiến cho những kẻ khinh khi phụ tử ta phải cúi đầu hối hận! Ba mươi năm Hà Đông, ba mươi năm Hà Tây, đừng khinh thiếu niên nghèo!"

Đêm khuya thanh vắng, ánh trăng bàng bạc chiếu qua khung cửa sổ nhỏ. Trong căn phòng đơn sơ, một già một trẻ bắt đầu những bước đi đầu tiên trên con đường phục hưng của vị thiên tài trẻ tuổi.

Tiêu Viêm nắm chặt bàn tay, cảm nhận được một luồng nhiệt lưu ấm áp đang dần lan tỏa từ đan điền, báo hiệu sự hồi sinh thần kỳ của dòng đấu khí đã ngủ quên suốt ba năm dài.

Dược Trần vuốt râu cười vang đầy mãn nguyện: "Tốt! Rất tốt! Kể từ hôm nay, con đường của ngươi sẽ bắt đầu lại từ con số không, nhưng đích đến sẽ là bầu trời vô tận!"', 0, 'PUBLISHED', 1450, false)
) AS c(chapter_number, title, content, price, status, views, is_deleted)
ON CONFLICT (novel_id, chapter_number) DO UPDATE
SET title = EXCLUDED.title, content = EXCLUDED.content, price = EXCLUDED.price, status = EXCLUDED.status;

-- ================================================================================
-- Truyện: Toàn Trí Độc Giả (toan-tri-doc-gia)
-- ================================================================================
INSERT INTO novels (title, slug, author, summary, cover_url, status, is_deleted, uploader_id, views, average_rating, rating_count)
VALUES (
    'Toàn Trí Độc Giả',
    'toan-tri-doc-gia',
    'Sing N Song',
    'Có một câu chuyện mà chỉ mình tôi biết kết cục. Và khi câu chuyện đó trở thành hiện thực, tôi là người duy nhất nắm giữ chìa khóa để sống sót.',
    'https://images.unsplash.com/photo-1618365908648-713d0141d06c?auto=format&fit=crop&w=600&q=80',
    'ONGOING',
    false,
    (SELECT id FROM users WHERE email = 'admin@tramtruyen.com'),
    5000,
    4.95,
    1200
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    author = EXCLUDED.author,
    summary = EXCLUDED.summary,
    cover_url = EXCLUDED.cover_url,
    status = EXCLUDED.status,
    is_deleted = false;

INSERT INTO novel_categories (novel_id, category_id)
SELECT n.id, c.id
FROM novels n
CROSS JOIN categories c
WHERE n.slug = 'toan-tri-doc-gia'
  AND c.slug IN ('do-thi', 'huyen-huyen')
ON CONFLICT DO NOTHING;

WITH target_novel AS (
    SELECT id FROM novels WHERE slug = 'toan-tri-doc-gia'
)
INSERT INTO chapters (novel_id, chapter_number, title, content, price, status, views, is_deleted)
SELECT
    tn.id,
    c.chapter_number, c.title, c.content, c.price, c.status, c.views, c.is_deleted
FROM target_novel tn,
(VALUES
(1, 'Khởi Đầu Của Sự Kết Thúc', 'Tôi tên là Kim Dokja, hai mươi tám tuổi, hiện đang là một nhân viên hợp đồng tại Mino Soft – một công ty phát triển trò chơi điện tử trực tuyến hạng trung nằm ở quận Gangnam sầm uất của thủ đô Seoul. Tên của tôi trong tiếng Hàn được viết là Dokja. Cha tôi đặt cái tên này với mong muốn gửi gắm rằng sau này tôi sẽ trở thành một người đàn ông độc lập, mạnh mẽ và kiên cường tự đứng vững trên đôi chân của mình. Thế nhưng trong tiếng Hàn, từ Dokja còn mang hai tầng ý nghĩa khác: một là "đứa con duy nhất", và hai là "độc giả". Định mệnh trớ trêu dường như đã lựa chọn ý nghĩa thứ ba cho cuộc đời tôi.

Suốt hai mươi tám năm tồn tại trên cõi đời này, cuộc sống của tôi luôn gắn liền với sự cô độc tột cùng và thói quen vùi đầu vào những cuốn tiểu thuyết mạng để trốn tránh thực tại xám xịt. Tôi không có một gia đình khá giả làm chỗ dựa, không sở hữu ngoại hình bảnh bao thu hút ánh nhìn, tài khoản ngân hàng lúc nào cũng trong tình trạng báo động đỏ sau khi thanh toán tiền thuê phòng trọ và hóa đơn sinh hoạt, còn công việc làm thêm tại Mino Soft thì chẳng hứa hẹn bất cứ tương lai biên chế chính thức nào. Niềm vui duy nhất, sự cứu rỗi duy nhất giúp tôi níu giữ lấy sự tỉnh táo giữa guồng quay tẻ nhạt vô vị này chính là một bộ web novel ít người biết đến: "Ba Cách Để Sinh Tồn Trong Một Thế Giới Hủy Diệt", hay thường được gọi tắt là "Con Đường Sinh Tồn".

Bộ tiểu thuyết này chắc chắn không thể được xếp vào hàng ngũ kiệt tác văn học. Ngược lại hoàn toàn, nó là một tác phẩm mạng dài lê thê với tổng cộng 3.149 chương, sở hữu văn phong vụng về, tình tiết lặp đi lặp lại một cách nhàm chán, và nhân vật chính mang tên Yoo Joonghyuk thì lãnh khốc, vô tình đến mức khiến bất kỳ độc giả bình thường nào cũng phải bực bội bỏ cuộc sau vài chục chương đầu. Thế nhưng, trong suốt mười năm ròng rã, kể từ những ngày tôi còn là một cậu học sinh trung học nhút nhát bị bạn bè bắt nạt, cho đến khi bước vào quân ngũ rồi ra trường bươn chải kiếm sống, tôi là độc giả trung thành duy nhất theo dõi tác phẩm từ chương mở đầu cho đến tận ngày hôm nay.

Mỗi buổi tối sau khi hoàn thành những giờ học căng thẳng hoặc ca làm thêm mệt nhọc, tôi lại ngồi ở cửa hàng tiện lợi ăn chiếc cơm nắm tam giác rẻ tiền và mở điện thoại ra đọc từng chương truyện mới. Yoo Joonghyuk đã hồi quy hàng ngàn lần qua các kịch bản tàn khốc, chứng kiến đồng đội ngã xuống và tự mình gánh vác sứ mệnh cứu rỗi thế giới. Trong tâm trí tôi, Yoo Joonghyuk vừa là một người bạn vô hình, vừa là tấm gương phản chiếu những góc khuất u tối nhất trong nội tâm của chính tôi.

Tôi nhớ như in từng chi tiết của từng kịch bản, từng chỉ số thuộc tính của các nhân vật chính và phụ, từng cái bẫy chết người mà hệ thống Star Stream đã giăng ra để tiêu diệt nhân loại. Mười năm đồng hành cùng bộ truyện đã biến những con chữ khô khan ấy thành một phần máu thịt trong ký ức của tôi. Tôi thuộc lòng bản đồ các ga tàu điện ngầm ở Seoul trong thế giới hậu tận thế, biết rõ nơi nào cất giấu bảo vật, nơi nào là sào huyệt của quái thú cấp cao.

Tác giả của bộ truyện sử dụng bút danh bí ẩn là "tls123". Hôm nay, ngày bộ truyện chính thức đăng tải chương thứ 3.149 – chương kết thúc của toàn bộ thiên sử thi diệt thế – số lượt xem hiển thị trên trang web đọc truyện vỏn vẹn là con số 1. Con số 1 ấy chính là tôi. Một bộ truyện có hơn ba ngàn chương nhưng chỉ có một người đọc duy nhất kiên trì đến cuối cùng, nghĩ lại cũng thật là một trải nghiệm kỳ lạ và có phần bi hài.

Ngồi cạnh tôi trên chuyến tàu điện ngầm tuyến số 3 lúc này là Yoo Sangah – nữ đồng nghiệp cùng phòng nhân sự tại công ty. Nàng là mẫu phụ nữ hoàn hảo: xinh đẹp, hòa đồng, luôn đạt thành tích xuất sắc và đang chăm chỉ học tiếng Tây Ban Nha qua ứng dụng trên điện thoại. Nhìn thấy tôi chăm chú nhìn màn hình, nàng nghiêng đầu mỉm cười hỏi han về cuộc sống và chia sẻ về những áp lực công việc của nhân viên hợp đồng sắp đến kỳ đánh giá.

Chúng tôi nói về những dự định tương lai mông lung, về những kỳ vọng của cha mẹ và những áp lực vô hình nơi công sở đô thị phồn hoa. Trong mắt mọi người, Yoo Sangah là một cô gái tài năng luôn tỏa sáng, nhưng ngay cả nàng cũng mang trong mình những nỗi bất an riêng về vị trí của mình trong xã hội.

Chuyến tàu đang lướt qua cây cầu sắt bắc ngang dòng sông Hàn rộng lớn. Qua ô cửa kính phủ bụi mờ, ánh hoàng hôn buông xuống nhuộm đỏ rực cả dòng nước lấp lánh bên dưới, phủ lên những tòa nhà chọc trời của thành phố Seoul một vẻ đẹp yên bình nhưng có phần ma mị. Đó là những khoảnh khắc thanh bình cuối cùng của một nền văn minh hiện đại trước khi cơn bão diệt vong quét qua.

Lời bạt của tác giả tls123 hiện lên ở cuối chương 3.149 với những dòng chữ chân thành: "Cảm ơn cậu, Dokja-ssi. Nhờ có sự đồng hành bền bỉ của cậu suốt mười năm qua mà tôi mới có thể hoàn thành câu chuyện này. Như một lời tri ân sâu sắc nhất, tôi muốn gửi tặng cậu một món quà đặc biệt. Nó sẽ được chuyển đến hòm thư cá nhân của cậu vào đúng 7 giờ tối nay."

Tôi gõ một tin nhắn ngắn gửi lại hòm thư của tác giả để bày tỏ lòng biết ơn vì đã tạo ra một thế giới cứu rỗi tâm hồn tôi suốt một thập kỷ qua, sau đó đưa mắt nhìn lên góc trên bên phải màn hình điện thoại để xem đồng hồ: 6 giờ 59 phút.

Ngay khi con số chuyển sang đúng 7 giờ 00 phút tối, một chuỗi biến cố kinh hoàng và phi lý bắt đầu giáng xuống hiện thực.

Toàn bộ hệ thống đèn huỳnh quang chiếu sáng trong toa tàu số 3807 chớp tắt liên hồi vài cái rồi phụt tắt hoàn toàn, nhấn chìm không gian vào bóng tối đen kịt như mực. Cùng lúc đó, đoàn tàu đang lao vun vút trên đường ray ngầm bỗng nhiên phanh gấp với một lực hãm khủng khiếp. Tiếng kim loại ma sát chói tai rợn người rít lên inh ỏi, kèm theo đó là quán tính cực mạnh hất văng hàng chục hành khách ngã dúi dụi xuống sàn tàu. Tiếng la hét hoảng loạn, tiếng đồ đạc va đập vỡ vụn và tiếng trẻ con khóc thét vang lên hỗn loạn khắp bốn bề.

"Chuyện gì đang xảy ra thế này?!"

"Mất điện diện rộng à? Hay là tàu hỏa bị trật bánh rồi?!"

"Bác tài xế ơi, mau mở cửa thoát hiểm ra đi! Ngột ngạt quá không thở nổi rồi!"

Giữa mớ âm thanh hỗn loạn và nỗi sợ hãi đang bùng phát của đám đông, màn hình chiếc điện thoại trong tay tôi bỗng nhiên tự động sáng bừng lên. Một bức thư điện tử mới toanh từ tác giả tls123 đã được gửi tới hộp thư đến, bên trong đính kèm một tập tin văn bản mang tên "Con Đường Sinh Tồn – Toàn văn kịch bản thế giới". Cùng lúc đó, trước tầm mắt của tôi, một bảng thông báo ba chiều bằng ánh sáng xanh lam trôi nổi giữa không trung, hiện lên những dòng chữ rõ ràng như trong một trò chơi điện tử thực tế ảo:

[Thời gian dịch vụ miễn phí của thế giới đã kết thúc.]

[Kịch bản chính thức của hành tinh 8612 bắt đầu được kích hoạt.]

Trái tim trong lồng ngực tôi đập thót một cái rồi dường như ngừng trệ hoàn toàn. Những dòng chữ này... cấu trúc câu từ này... chẳng phải chính là đoạn văn miêu tả mở đầu cho chương thứ nhất của cuốn tiểu thuyết "Con Đường Sinh Tồn" hay sao?! Tôi còn nhớ rõ từng câu từng chữ miêu tả phản ứng của người dân thành phố, cảnh tượng hoảng loạn tột độ khi các quy luật vật lý thông thường bị bẻ gãy, và sự can thiệp của các thực thể vũ trụ bí ẩn.

Chưa kịp để tôi định thần lại, không gian ở đầu toa tàu bỗng nhiên nứt toác ra một khe hở không thời gian phát ra ánh sáng trắng chói lòa đến lóa mắt. Tiếng xé rách của không gian vang lên như tiếng vải rách giòn giã. Từ trong luồng sáng ấy, một sinh vật kỳ dị từ từ bay lơ lửng bước ra. Nó có thân hình nhỏ thó như một đứa trẻ sơ sinh nhưng làn da lại xám xịt nhăn nheo, trên trán mọc một chiếc sừng nhọn hoắt và khoác trên mình một bộ lễ phục quý tộc rách rưới. Đôi mắt đỏ rực như hai hòn than đang cháy của nó quét qua những hành khách đang co cụm dưới sàn tàu với vẻ thích thú tàn bạo.

Đó là một Dokkaebi – sinh vật dẫn dắt kịch bản tàn bạo trong cuốn tiểu thuyết của tls123. Tên của nó là Bihyung, kẻ sẽ gieo rắc nỗi kinh hoàng đầu tiên cho toàn bộ bán đảo Triều Tiên.

[A, a! Alo 1, 2, 3... Mic có hoạt động tốt không nhỉ?] Sinh vật nhỏ bé cất tiếng nói bằng một thứ ngôn ngữ xa lạ nhưng kỳ diệu thay lại tự động chuyển dịch thành tiếng Hàn vang vọng trực tiếp trong tâm trí của tất cả mọi người. [Xin chào các sinh vật nhỏ bé của tinh cầu này! Chào mừng các ngươi đến với Kịch bản chính thức đầu tiên!] Thế giới bình yên mà tôi từng biết đã hoàn toàn sụp đổ kể từ giây phút này.

Tiếng cười khanh khách man rợ của nó vang vọng khắp toa tàu lạnh lẽo, đánh dấu sự khởi đầu của một kỷ nguyên tận thế nơi mạng người chỉ rẻ rúng như cỏ rác dưới gót chân các thế lực siêu nhiên.

Mọi người xung quanh tôi vẫn chưa thể tin vào mắt mình. Một vài người đàn ông trung niên bắt đầu gào thét đòi báo cảnh sát, trong khi một số phụ nữ ôm chặt lấy con nhỏ vào lòng run rẩy không ngừng. Bầu không khí ngột ngạt đến mức khó thở, mùi ẩm mốc của đường hầm ngầm hòa quyện với nỗi sợ hãi tột cùng của hàng chục con người đang đối mặt với điều chưa biết.

Tôi nhìn quanh, những người bạn đồng nghiệp, những gương mặt xa lạ trên chuyến tàu đi làm về mỗi ngày... giờ đây tất cả họ đã bị kéo vào vòng xoáy của một trò chơi sinh tử tàn khốc mà không có đường lui. Không ai trong số họ chuẩn bị tâm lý cho ngày tận thế này. Không ai có thể ngờ rằng cuốn tiểu thuyết rác rưởi mà tôi đọc suốt mười năm qua lại chính là bản thiết kế chính xác của ngày tận thế.

Tôi nắm chặt lấy chiếc điện thoại đang chứa toàn bộ dữ liệu của cuốn tiểu thuyết, từng ngón tay run rẩy nhưng ánh mắt dần trở nên sắc lạnh. Tôi nhận ra một sự thật chấn động đang đè nặng lên vai mình: Tôi, Kim Dokja, chính là người duy nhất nắm giữ toàn bộ kịch bản của thế giới này! Tôi biết ai sẽ sống, ai sẽ chết, quái vật nào sắp xuất hiện và đâu là con đường duy nhất để đi đến hồi kết cuối cùng của vũ trụ!

Một nụ cười nhạt thoáng hiện trên khóe môi tôi giữa bóng tối bao trùm toa tàu. Nếu thế giới này đã biến thành một cuốn tiểu thuyết, vậy thì tôi sẽ trở thành độc giả duy nhất chứng kiến nó đến trang cuối cùng.', 0, 'PUBLISHED', 4500, false),
(2, 'Kịch Bản Sinh Tồn', 'Ánh mắt của Dokkaebi Bihyung lấp lánh sự tàn bạo lạnh lùng khi nhìn xuống đám đông hành khách đang co cụm lại vì sợ hãi trong bóng tối của toa tàu số 3807. Nó khẽ búng tay một cái, phát ra một tiếng tách giòn giã giữa không gian im ắng nghẹt thở. Ngay lập tức, giữa không trung toa tàu hiện lên một màn hình ảo màu xanh lam khổng lồ, trên đó những dòng chữ đỏ rực như máu tươi từ từ chảy xuống:

[Kịch Bản Chính Thức Số 1 – Chứng Minh Giá Trị Sinh Tồn]
Thể loại: Kịch bản chính
Độ khó: F
Mục tiêu nhiệm vụ: Giết chết ít nhất một sinh vật sống.
Thời gian giới hạn: 30 phút.
Phần thưởng hoàn thành: 300 Coin.
Hình phạt thất bại: Tử vong.

"Giết người ư?! Mày đang nói cái quái quỷ gì thế hả con quái vật kia?! Mày nghĩ bọn tao là lũ ngốc để cho một trò đùa giấu camera của đài truyền hình dắt mũi à?!" Một gã đàn ông to con xăm trổ đầy mình, dường như là một tay anh chị giang hồ cộm cán, giận dữ đứng bật dậy khỏi sàn tàu. Hắn hùng hổ sải những bước chân nặng nề tiến về phía sinh vật nhỏ bé đang lơ lửng giữa không trung, vung nắm đấm to như quả tạ sắt toan đập nát sinh vật quái dị trước mặt để thị uy.

Dokkaebi Bihyung chỉ khẽ nhếch mép cười một cách ghê rợn, trong mắt lóe lên tia khinh miệt sâu sắc: [Thật là ồn ào và ngu xuẩn. Các ngươi vẫn chưa nhận thức được tình cảnh sinh tử của mình sao?]

Nó nhẹ nhàng búng ngón tay lần thứ hai. "BÙM!" Một tiếng nổ trầm đục vang lên xé toạc không khí toa tàu. Đầu của gã đàn ông xăm trổ đột ngột phát nổ tung tóe tựa như một quả dưa hấu bị búa tạ giáng xuống từ trên cao. Máu tươi đỏ thẫm, những mảnh xương sọ sắc nhọn và óc trắng văng tung tóe khắp vách kính toa tàu, bắn dính đầy lên mặt và quần áo của những người ngồi gần đó. Thân thể không đầu khổng lồ giật giật vài cái trong cơn co giật vô thức rồi đổ sụp xuống sàn tàu trong vũng máu lênh láng tanh tưởi.

Một giây im lặng chết chóc bao trùm lấy toàn bộ toa tàu số 3807. Cảnh tượng kinh hoàng vượt xa mọi giới hạn chịu đựng của một người bình thường sống trong thời bình. Ngay sau đó, những tiếng la hét chói tai xé rách màng nhĩ đồng loạt bùng nổ như vỡ trận. Phụ nữ ôm mặt khóc ngất, đàn ông bò lê bò càng tìm chỗ trốn dưới gầm ghế, sự hoảng loạn tột cùng biến không gian chật hẹp thành một địa ngục trần gian thực sự.

[Thời gian đếm ngược chính thức bắt đầu! 29 phút 59 giây... Mau hành động đi nào các khán giả nhỏ bé, nếu không tất cả các ngươi sẽ nổ tung thành từng mảnh như gã vừa rồi đấy!] Tiếng cười the the tàn nhẫn của Bihyung vang vọng khắp toa tàu rồi bóng dáng nó từ từ tan biến vào không trung, chỉ để lại chiếc đồng hồ đếm ngược bằng chữ số màu đỏ rực lạnh lùng tích tắc trôi qua từng giây.

Đám đông hành khách bắt đầu phát điên thực sự. Vài người liều mạng lao về phía cánh cửa thông giữa các toa tàu hòng tìm đường thoát thân, nhưng cánh cửa kim loại đã bị niêm phong chặt chẽ bởi một lớp màng năng lượng màu xanh lam trong suốt không thể suy chuyển. Khi nhận ra không còn bất kỳ con đường chạy trốn nào, bản năng sinh tồn nguyên thủy và đen tối nhất của con người bắt đầu trỗi dậy một cách đáng sợ.

Trong góc toa tàu, một gã thanh niên mặc âu phục chỉnh tề, hai mắt long sòng sọc đỏ ngầu, bất ngờ lao tới bóp cổ một cụ già tàn tật ngồi trên ghế ưu tiên. Cụ già yếu ớt giãy giụa trong tuyệt vọng, trong khi gã thanh niên vừa nghiến răng siết chặt tay vừa gào thét man dại: "Tôi không muốn chết! Xin lỗi ông cụ, tôi phải sống! Tôi phải sống để về với vợ con!". Ở một góc khác, hai người đàn ông trung niên đang dùng cặp xách và giày da đập liên hồi vào đầu nhau để tranh giành mạng sống.

Mùi máu tanh nồng nặc bốc lên ngột ngạt khắp toa xe đóng kín, hòa cùng tiếng khóc lóc van xin và tiếng rên xiết đau đớn. Nhân tính và đạo đức xã hội được xây dựng suốt hàng ngàn năm bỗng chốc sụp đổ chỉ sau một cú búng tay của quái vật đến từ ngoài hành tinh.

Tôi, Kim Dokja, vẫn đứng yên tựa lưng vào vách toa tàu gần cửa ra vào, cố gắng hít thở từng ngụm khí lạnh để ép nhịp tim đang đập như trống trận phải chậm lại. Trái tim tôi run lên vì kinh sợ trước cảnh tượng đẫm máu đang diễn ra ngay trước mắt, nhưng lý trí của tôi lại tỉnh táo đến mức kỳ lạ.

Tôi biết rõ điều gì đang diễn ra, bởi vì tôi đã đọc đi đọc lại chương này hàng trăm lần trong suốt mười năm gắn bó với "Con Đường Sinh Tồn". Trong nguyên tác của tác giả tls123, toa tàu số 3807 này sẽ biến thành một lò sát sinh đẫm máu nhất của toàn tuyến tàu ngầm. Mọi người sẽ tàn sát lẫn nhau một cách dã man nhất chỉ để hoàn thành kịch bản đầu tiên, và cuối cùng chỉ có vỏn vẹn hai người còn thở bước ra khỏi cánh cửa kia khi thời gian kết thúc.

Nhưng tôi không muốn trở thành một kẻ giết người. Trong thế giới của kịch bản diệt thế này, một khi bạn đã chấp nhận nhúng tay vào máu của đồng loại vô tội chỉ để bảo toàn mạng sống theo luật chơi của lũ Dokkaebi, nhân tính của bạn sẽ vĩnh viễn bị tha hóa, và bạn sẽ mãi mãi chỉ là một con tốt thí bị các Tinh Tọa thượng giới thao túng mua vui mà thôi.

"Mục tiêu nhiệm vụ: Giết chết ít nhất một sinh vật sống..." Tôi lẩm bẩm đọc lại từng chữ từng chữ trong bảng thông báo kịch bản đang lơ lửng trước mặt. Cụm từ được ghi rõ ràng là "sinh vật sống" (Living thing), chứ không hề ghi là "con người" (Human)! Hệ thống của kịch bản luôn cài cắm những lỗ hổng ngữ nghĩa như vậy để thử thách trí tuệ của những kẻ tham gia.

Ánh mắt tôi nhanh chóng quét qua từng ngóc ngách của toa tàu đang hỗn loạn như ong vỡ tổ, và dừng lại ở hàng ghế phía đối diện cách tôi chừng bảy bước chân. Nơi đó, một cậu bé học sinh tiểu học tên là Lee Gilyoung đang co rúm người lại khóc nấc lên trong sợ hãi, hai tay ôm chặt lấy một chiếc hộp nhựa trong suốt có nắp đậy màu xanh lá cây. Trong chiếc hộp nhựa ấy, hàng chục con côn trùng, cào cào và dế mèn đang nhảy tanh tách – đó là thành quả của buổi dã ngoại bắt côn trùng ở công viên mà cậu bé mang về khoe với mẹ.

"Tìm thấy chìa khóa rồi!" Tôi thở phào một hơi nhẹ nhõm, ánh mắt lóe lên tia sáng hy vọng mãnh liệt. Côn trùng chính là sinh vật sống! Giết chết một con côn trùng hoàn toàn đáp ứng đầy đủ điều kiện của Kịch bản số 1 mà không cần phải vấy máu bất kỳ sinh mạng con người nào.

Tôi lập tức sải bước lao nhanh về phía cậu bé Lee Gilyoung trước khi đám người điên loạn ngoài kia kịp nhận ra điều này. Cùng lúc đó, Han Myungoh – gã trưởng phòng hách dịch tại công ty của tôi – cũng đang hớt hải bò lê trên sàn tàu, ánh mắt hoảng loạn đảo quanh tìm kiếm thứ gì đó để bấu víu. Khi nhìn thấy tôi tiến về phía đứa bé, linh tính của một kẻ lọc lõi dường như mách bảo gã điều gì đó bất thường.

"Này Dokja! Cậu đang làm cái gì thế hả?! Có phải cậu biết cách thoát thân không?! Nói mau, đồ khốn nạn!" Tiếng gầm gừ run rẩy của Han Myungoh vang lên phía sau lưng tôi, nhưng tôi hoàn toàn phớt lờ gã.

Một thế giới mới đầy hiểm họa đã mở ra, và với tư cách là độc giả duy nhất biết trước toàn bộ kết cục của thế giới, Kim Dokja tôi sẽ là người viết lại câu chuyện này theo cách của riêng mình! Tôi quỳ một chân xuống trước mặt cậu bé Lee Gilyoung, hạ thấp giọng nói một cách dịu dàng nhất có thể giữa âm thanh gào khóc xung quanh:

"Gilyoung à, đưa chiếc hộp này cho chú được không? Chú hứa sẽ cứu sống cháu và mọi người."

Cậu bé ngấn lệ, nhìn sâu vào đôi mắt kiên định của tôi rồi khẽ gật đầu, run rẩy trao chiếc hộp nhựa trong tay ra. Tôi giơ tay đón lấy chiếc hộp từ tay đứa trẻ đang run rẩy, mở nắp và bắt lấy một chú cào cào to khỏe, dứt khoát kết thúc sinh mệnh nhỏ bé của nó trong lòng bàn tay. Một âm thanh thông báo vang lên bên tai:

[Bạn đã tiêu diệt một sinh vật sống: Cào cào cỏ (Cấp độ F).]

[Bạn đã hoàn thành Kịch bản số 1 thành công rực rỡ!]

[Phần thưởng 300 Coin đã được chuyển vào tài khoản của bạn.]

Ngay khoảnh khắc đó, tôi nhìn lên trần toa tàu, dường như có thể cảm nhận được những ánh mắt tò mò và thích thú từ vô số Tinh Tọa xa xôi trong vũ trụ đang đổ dồn về phía mình. Kịch bản của tôi chỉ mới bắt đầu.

Tôi nhanh chóng mở nắp hộp, đưa những con cào cào và dế mèn còn lại cho cậu bé Lee Gilyoung và cô bạn đồng nghiệp Yoo Sangah. Yoo Sangah hai mắt đẫm lệ, bàn tay trắng nõn run lẩy bẩy không nỡ bóp chết sinh linh nhỏ bé, nhưng trước ánh mắt khích lệ đầy kiên quyết của tôi, nàng cắn môi bóp chết con dế mèn. Luồng sáng xanh lam lập tức bao bọc lấy nàng, báo hiệu nàng đã an toàn.

Những dòng thông báo liên tiếp hiện lên trong tâm trí tôi như một cơn mưa sao băng:

[Tinh Tọa ''Kẻ Lập Kế Hoạch Bí Mật'' đang quan sát hành động khôn khéo của bạn với vẻ tò mò.]

[Tinh Tọa ''Tù Nhân Của Chiếc Vòng Kim Cô'' đang gật gù thích thú trước sự nhanh trí của bạn.]

[Tinh Tọa ''Hắc Hỏa Vực Sâu Long'' đang hưng phấn gầm thét trong hư không.]

[Bạn nhận được 200 Coin tiền tài trợ từ các Tinh Tọa!]

Gã trưởng phòng Han Myungoh thấy vậy liền bổ nhào tới toan cướp lấy chiếc hộp rỗng, nhưng tôi đã nhanh tay ném một con châu chấu còn sót lại cho gã trước khi gã kịp phát điên làm hại người khác. Đồng hồ đếm ngược trên trần toa tàu chỉ còn lại những giây cuối cùng: 00:03... 00:02... 00:01... 00:00!

RẦM! Toàn bộ không gian rung chuyển dữ dội. Những kẻ không kịp hoàn thành kịch bản bỗng nhiên ôm lấy đầu gào thét thảm thiết trước khi đầu của họ phát nổ đồng loạt như những quả pháo hoa máu đỏ rực. Tiếng nổ liên hoàn vang dội từ các toa tàu bên cạnh vọng sang, chứng tỏ số người sống sót trên toàn bộ đoàn tàu ngầm chỉ đếm trên đầu ngón tay.

Cánh cửa toa tàu rung chuyển dữ dội khi lớp màng bảo hộ màu xanh lam tan biến, để lại một khung cảnh tàn khốc đẫm máu mà nhân loại chưa từng chứng kiến. Mùi khét lẹt và mùi máu tanh nồng xộc thẳng vào mũi khiến Yoo Sangah nôn thốc nôn tháo bên cạnh ghế ngồi.

Tôi đứng thẳng dậy, phủi nhẹ vạt áo sơ mi dính vài giọt bụi bẩn, ánh mắt kiên định hướng về phía cánh cửa sắt méo mó dẫn ra thế giới bên ngoài. Một kỷ nguyên mới đã bắt đầu, một kỷ nguyên mà những kẻ yếu sẽ bị nghiền nát và chỉ có những kẻ thấu hiểu luật chơi mới có quyền định đoạt số phận.

Con đường sinh tồn chỉ vừa mới mở ra chương đầu tiên, và tôi sẽ không để bất kỳ ai cản bước mình.', 0, 'PUBLISHED', 4400, false)
) AS c(chapter_number, title, content, price, status, views, is_deleted)
ON CONFLICT (novel_id, chapter_number) DO UPDATE
SET title = EXCLUDED.title, content = EXCLUDED.content, price = EXCLUDED.price, status = EXCLUDED.status;

-- ================================================================================
-- Truyện: Phàm Nhân Tu Tiên (pham-nhan-tu-tien)
-- ================================================================================
INSERT INTO novels (title, slug, author, summary, cover_url, status, is_deleted, uploader_id, views, average_rating, rating_count)
VALUES (
    'Phàm Nhân Tu Tiên',
    'pham-nhan-tu-tien',
    'Vong Ngữ',
    'Thiếu niên bình thường Hàn Lập xuất thân bần hàn, ngẫu nhiên bước chân vào giang hồ tiểu môn phái, tuy tư chất bình thường nhưng dựa vào tâm tính kiên định và chiếc Bình Chưởng Thiên thần bí mà từng bước bước lên con đường tu tiên đầy hiểm hóc...',
    'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80',
    'COMPLETED',
    false,
    (SELECT id FROM users WHERE email = 'admin@tramtruyen.com'),
    8500,
    4.92,
    2100
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    author = EXCLUDED.author,
    summary = EXCLUDED.summary,
    cover_url = EXCLUDED.cover_url,
    status = EXCLUDED.status,
    is_deleted = false;

INSERT INTO novel_categories (novel_id, category_id)
SELECT n.id, c.id
FROM novels n
CROSS JOIN categories c
WHERE n.slug = 'pham-nhan-tu-tien'
  AND c.slug IN ('tien-hiep')
ON CONFLICT DO NOTHING;

WITH target_novel AS (
    SELECT id FROM novels WHERE slug = 'pham-nhan-tu-tien'
)
INSERT INTO chapters (novel_id, chapter_number, title, content, price, status, views, is_deleted)
SELECT
    tn.id,
    c.chapter_number, c.title, c.content, c.price, c.status, c.views, c.is_deleted
FROM target_novel tn,
(VALUES
(1, 'Sơn Thôn Thiếu Niên', 'Hàn Lập sinh ra trong một gia đình nông dân nghèo khó tại một thôn làng hẻo lánh mang tên Ngũ Lý Câu, thuộc phủ Thanh Ngưu của Lam Châu. Gia đình hắn có bảy miệng ăn, quanh năm suốt tháng trông chờ vào mấy sào ruộng cằn cỗi và những gánh củi mà phụ thân hắn nhọc nhằn đốn từ trên núi cao mang ra chợ huyện đổi lấy từng đấu gạo hẩm. Từ nhỏ, Hàn Lập đã thấu hiểu nỗi cơ cực của kiếp phàm nhân, nhìn cha mẹ ngày một còng lưng vì gánh nặng áo cơm, các huynh muội nheo nhóc thiếu ăn thiếu mặc.

Bản thân Hàn Lập tướng mạo bình thường, làn da ngăm đen vì dãi nắng dầm sương, tính tình lại trầm mặc ít nói, chẳng có lấy nửa phần cơ trí hay xuất chúng hơn người. Thế nhưng, vận mệnh của thiếu niên nông thôn ấy đã bước sang một bước ngoặt hoàn toàn mới vào năm hắn mười tuổi. Khi ấy, Tam Thúc của hắn – một người từng rời làng ra ngoài làm ăn buôn bán và trở thành chân chạy việc cho một tiêu cục lớn – trở về thăm quê. Nhìn thấy Hàn Lập chăm chỉ, thật thà lại biết điều, Tam Thúc liền ngỏ ý dẫn hắn đến Thất Huyền Môn, một môn phái giang hồ có tiếng trong vùng, để tham gia kỳ thi tuyển đệ tử ký danh.

"Nhị Lăng Tử, đi chuyến này nếu may mắn được chọn vào môn phái, mỗi tháng con đều có bạc gửi về cho cha mẹ, sau này tương lai cũng rạng rỡ hơn bám lấy mảnh ruộng cằn cỗi này nhiều." Lời căn dặn đầy hy vọng của phụ thân cùng giọt nước mắt nghẹn ngào của mẫu thân trong buổi sáng chia tay đã in sâu vào tâm khảm thiếu niên. Hắn khoác lên mình bộ quần áo lành lặn duy nhất, lặng lẽ bước theo chân Tam Thúc rời khỏi lũy tre làng, hướng về ngọn núi Thái Nhạc trùng điệp.

Đại ca của Hàn Lập là Hàn Thiết cũng dậy từ rất sớm, nắm chặt lấy tay đệ đệ dặn dò đủ điều và nhét vào túi áo hắn vài củ khoai nướng còn nóng hổi. Tình cảm gia đình mộc mạc chân chất ấy là hành trang quý giá nhất mà thiếu niên mang theo trên suốt chặng đường vạn dặm xa xôi.

Trên hành trình kéo dài nửa tháng từ Ngũ Lý Câu đến dãy núi Lạc Nhật, Tam Thúc liên tục kể cho Hàn Lập nghe về thế giới giang hồ bao la bên ngoài lũy tre làng. Ở đó có những môn phái võ lâm hùng cứ một phương, có những cao thủ võ lâm có thể một kiếm chém đứt dòng nước hay phi thân qua những vách núi cao ngút ngàn. Tam Thúc căn dặn hắn rằng chốn giang hồ hiểm ác, lòng người khó đoán, người thật thà chất phác rất dễ chịu thiệt thòi, bởi vậy bước chân vào Thất Huyền Môn phải luôn biết nhẫn nhịn, tai nghe mắt thấy nhưng miệng phải giữ kín như bưng.

Kỳ thi tuyển chọn của Thất Huyền Môn diễn ra vô cùng khốc liệt tại dãy núi Lạc Nhật. Hàng trăm đứa trẻ từ khắp các phủ huyện lân cận đổ về, nhưng chỉ có vài chục suất được nhận vào các phân đường. Thử thách đầu tiên là phải leo lên đỉnh núi Nhai Sơn cheo leo trước khi nén nhang cháy hết. Vách đá dựng đứng, trơn trượt đầy rêu xanh, gió núi rít từng hồi lạnh thấu xương. Nhiều đứa trẻ xuất thân con nhà khá giả được ăn ngon mặc ấm vừa leo được nửa đường đã kiệt sức bỏ cuộc, khóc lóc van xin người nhà đón về.

Hàn Lập cắn chặt răng, đôi bàn tay trầy da tróc vảy rướm máu bám chặt vào từng mỏm đá nhọn và những nhánh dây leo gồ ghề. Đôi giày vải cũ kỹ rách toạc để lộ những ngón chân tím tái vì cọ xát. Dù cơ thể nhỏ bé mệt mỏi rã rời, hai chân run rẩy như muốn khuỵu xuống, ý nghĩ về gánh nặng gia đình và lời dặn của cha mẹ đã tiếp thêm cho hắn sức mạnh ý chí phi thường. Hắn không thể quay về tay trắng, hắn không thể để cha mẹ và các em tiếp tục chịu cảnh đói rét trong căn nhà tranh dột nát.

Từng bước, từng bước một, hắn nhẫn nại bò lên đỉnh núi. Khi nén nhang tàn vừa rụng tàn tro cuối cùng, Hàn Lập cũng khó nhọc trườn được nửa thân mình lên bãi đất bằng trên đỉnh núi, kiệt sức nằm vật ra đất thở dốc, toàn thân đẫm ướt mồ hôi và bụi đất.

Mặc dù vượt qua được thử thách leo núi nhờ ý chí kiên định phi thường, nhưng trong phần kiểm tra căn cốt và tư chất võ học sau đó, Hàn Lập lại khiến các chấp sự Thất Huyền Môn lắc đầu ngán ngẩm. Vị chấp sự trung niên đặt tay lên vai và cổ tay hắn dò xét kinh mạch, ánh mắt lộ vẻ thất vọng rõ rệt: "Căn cốt quá mức tầm thường, kinh mạch cứng nhắc bế tắc, hoàn toàn không thích hợp để tu luyện các môn võ công cương mãnh của bản môn. Hắn không đủ tiêu chuẩn để trở thành đệ tử chính thức của Thất Huyền Môn, chỉ có thể được xếp vào diện đệ tử ký danh hoặc bị trả về quê cũ."

Những đứa trẻ xuất sắc khác được tuyển vào Đoán Cốt Đường hay Phi Vũ Các để học tập đao pháp và kiếm thuật thượng thừa, được các vị đường chủ khen ngợi và ban phát áo gấm cùng tiền bạc. Còn Hàn Lập đứng nép mình ở một góc sân võ trường vắng lặng, lòng nặng trĩu âu lo. Gió núi buổi chiều thổi qua lạnh buốt, nhưng cái lạnh trong lòng thiếu niên còn buốt giá hơn gấp bội. Hắn không sợ khổ, không sợ mệt mỏi nhọc nhằn, hắn chỉ sợ bị đuổi về làng, nhìn thấy ánh mắt thất vọng của phụ thân và cảnh gia đình tiếp tục chìm trong cảnh nghèo túng cơ hàn.

Giữa lúc tuyệt vọng tưởng chừng phải quay về làm một nông phu suốt đời, một vị lão giả gầy gò, râu tóc muối tiêu, khoác trên mình chiếc áo choàng màu xám tro bỗng xuất hiện từ phía con đường mòn sau núi. Lão chống một cây gậy trúc, thỉnh thoảng ho khan từng tiếng khàn khàn. Lão chính là Mặc Đại Phu – một danh y tính tình quái dị sống ẩn dật tại Thần Thủ Cốc của Thất Huyền Môn, người mà ngay cả môn chủ cũng phải kiêng nể vài phần.

Mặc Đại Phu liếc nhìn đám thiếu niên bị loại, ánh mắt u ám sâu thẳm dừng lại trên người Hàn Lập một hồi lâu. Đôi mắt lão lóe lên một tia sáng kỳ dị, sau đó lạnh lùng cất tiếng:

"Đứa nhỏ này khí chất trầm ổn, tuy tư chất tầm thường nhưng có tính kiên nhẫn hiếm thấy. Lão phu đang cần một đệ tử hái thuốc và chăm sóc dược viên ở Thần Thủ Cốc, ngươi có bằng lòng theo ta không?"

Hàn Lập như người chết đuối vớ được cọc, lập tức quỳ sụp xuống dập đầu bái tạ: "Đệ tử Hàn Lập, bái kiến sư phụ!"

Mặc Đại Phu gật đầu nhẹ, dẫn Hàn Lập rời khỏi võ trường ồn ào. Trên con đường mòn dẫn vào thung lũng sâu thẳm, tiếng thông reo rì rào như khúc ca đón chào một số phận mới. Hàn Lập nhìn bóng lưng còng còng của sư phụ, thầm hứa với lòng mình sẽ dốc toàn tâm toàn lực học tập y thuật, không để lãng phí cơ duyên cứu rỗi này.

Thần Thủ Cốc là một thế giới hoàn toàn tách biệt với chốn phàm trần ồn ào. Tại đây, bốn mùa hoa cỏ ngát hương, các loại thảo dược quý hiếm được trồng ngay hàng thẳng lối. Mặc Đại Phu tuy nghiêm khắc nhưng cung cấp cho hắn cơm no áo ấm và một gian phòng nhỏ sạch sẽ bên cạnh kho thuốc. Đêm đầu tiên ngủ tại thung lũng, Hàn Lập trằn trọc không ngủ được vì xúc động. Hắn cẩn thận bọc đồng bạc đầu tiên do môn phái phát thưởng vào mảnh vải đỏ để gửi về cho gia đình, thầm cảm tạ trời phật đã che chở cho số phận mình.

Mỗi buổi sớm tinh sương, khi làn sương trắng còn giăng mắc trên những tán lá thuốc, Hàn Lập đã thức dậy quét dọn sân viện, gánh nước từ dòng suối mát sau núi để tưới cho các luống thảo dược. Hắn chăm chỉ ghi chép từng lời dặn của Mặc Đại Phu về cách phân biệt các loại rễ cây, vỏ cây và hoa lá độc hại, dần dần hiểu rõ đặc tính dược liệu của từng loài cây cỏ.

Mặc Đại Phu nhìn thấy sự cần mẫn chịu thương chịu khó của hắn cũng dần bớt đi vẻ khắt khe ban đầu. Lão bắt đầu cho phép hắn tiếp cận các tủ sách y lý cổ truyền, mở ra cho thiếu niên một kho tàng tri thức y học phong phú.

Tại đây, Hàn Lập dần thấu hiểu quy luật sinh tồn tàn khốc của chốn giang hồ: kẻ mạnh ức hiếp kẻ yếu, ân oán tình thù triền miên không dứt. Hắn từng tận mắt chứng kiến thi thể của một gã đệ tử ngoại môn bị khiêng xuống núi sau một trận tỷ võ sinh tử, máu nhuộm đỏ cả cáng tre. Cảnh tượng kinh hãi ấy như một hồi chuông cảnh tỉnh khắc sâu vào tâm khảm hắn: trên thế gian này, phàm nhân như cỏ rác, nếu không có đủ thực lực và sự tỉnh táo, sinh mạng có thể bị tước đoạt bất cứ lúc nào.

Hằng đêm dưới ánh đèn dầu leo lét, Hàn Lập chăm chú vùi đầu vào những cuốn sách y lý cổ xưa như Bản Thảo Cương Mục, Hoàng Đế Nội Kinh và các pho bí kíp châm cứu nhận huyệt. Nhờ trí nhớ bền bỉ và sự nhẫn nại phi thường, hắn nhanh chóng thuộc lòng tên tuổi, dược tính và độc tính của hàng trăm loại thảo dược như Hoàng Cương, Huyết Sâm, Hà Thủ Ô và Thất Diệp Nhất Chi Hoa. Mặc Đại Phu nhiều lần kiểm tra bất ngờ đều phải âm thầm gật đầu hài lòng trước kiến thức vững vàng của hắn.

Trương Thiết – thiếu niên cùng vào cốc với hắn – là một người tính tình bộc trực, chất phác, xuất thân từ một gia đình thợ săn nghèo trên núi. Hai thiếu niên đồng cảnh ngộ thường ngồi bên đống lửa sưởi ấm tâm sự thâu đêm về quê nhà, về những món ăn đạm bạc mẹ nấu và ước mơ ngày trở về rạng rỡ tổ tông. Nhìn Trương Thiết ngày một to lớn vạm vỡ, Hàn Lập cảm thấy an ủi vì giữa chốn môn phái lạnh lẽo này mình vẫn còn có một người bạn đồng hành chân thành.

Những đêm nằm nghe tiếng côn trùng rả rích nơi núi rừng hoang vắng, Hàn Lập thường nhìn lên bầu trời đầy sao lấp lánh, tự hỏi liệu con đường phía trước sẽ dẫn mình về đâu. Thiếu niên nhỏ bé không hề nuôi mộng tưởng trở thành đại hiệp cái thế lừng lẫy thiên hạ, ước nguyện lớn nhất của hắn lúc này chỉ đơn giản là học thành tài nghệ để có thể bảo bọc gia đình thoát khỏi cảnh bần hàn đói rách.

Số phận của thiếu niên bình phàm từ giây phút ấy chính thức rẽ sang một con đường hoàn toàn mới, một con đường dẫn hắn bước chân vào thế giới tu tiên đầy hiểm hóc và huyền bí mà phàm nhân ngàn năm khó lòng tưởng tượng.', 0, 'PUBLISHED', 3800, false),
(2, 'Mặc Đại Phu Cùng Thần Bí Bình Nhỏ', 'Sau khi bái Mặc Đại Phu làm sư phụ, Hàn Lập cùng một thiếu niên khác tên là Trương Thiết được đưa về Thần Thủ Cốc. Nơi đây là một thung lũng biệt lập nằm sâu trong lòng dãy núi Thái Nhạc, bốn bề vách đá dựng đứng ngăn cách với sự ồn ào náo nhiệt của Thất Huyền Môn. Trong thung lũng trồng bạt ngàn các loại thảo dược quý hiếm, không khí quanh năm thoang thoảng mùi hương thuốc nồng nàn dễ chịu.

Mặc Đại Phu là một người cực kỳ nghiêm khắc và ít nói. Sau nửa năm dạy cho hai thiếu niên nhận biết chữ nghĩa và hàng trăm loại thảo dược cơ bản, lão truyền thụ cho Trương Thiết một môn ngoại công ngạnh khí công mang tên Tượng Giáp Công, còn đối với Hàn Lập, lão truyền thụ một bộ khẩu quyết vô danh kỳ dị, yêu cầu hắn phải dốc lòng tu luyện ngày đêm. Bộ khẩu quyết này chỉ có vỏn vẹn năm tầng, phương thức vận chuyển khí tức hoàn toàn khác biệt với các loại nội công tâm pháp giang hồ thông thường, chú trọng vào việc hấp thu thiên địa linh khí vào đan điền.

Hàn Lập không hề hay biết rằng, bộ khẩu quyết vô danh mà Mặc Đại Phu truyền dạy chính là "Trường Xuân Công" – một bộ công pháp tu tiên chính tông của Mộc hệ linh căn. Người phàm không có linh căn thì tuyệt đối không thể tu luyện, mà cho dù có linh căn, nếu phẩm chất kém cỏi thì tiến độ cũng chậm chạp vô cùng. Trong khi Trương Thiết tu luyện Tượng Giáp Công tiến bộ vượt bậc, thân thể ngày càng vạm vỡ như thiết giáp, thì Hàn Lập ròng rã suốt hai năm trời vẫn không thể cảm ứng được một tia khí cảm nào trong cơ thể.

Thế nhưng biến cố kỳ lạ bất ngờ xảy ra khi Trương Thiết đột ngột mất tích không một dấu vết. Mặc Đại Phu chỉ lạnh lùng tuyên bố Trương Thiết đã được phái đi làm nhiệm vụ bí mật ở phương xa, nhưng linh cảm sắc bén mách bảo Hàn Lập rằng có điều gì đó vô cùng tàn khốc đã diễn ra trong căn mật thất của sư phụ. Hắn từng ngửi thấy mùi máu tanh nồng và những vết cào cấu kỳ lạ trên vách đá sau cốc, khiến nỗi sợ hãi trong lòng hắn dâng lên tột độ.

Mặc Đại Phu ngày một sốt ruột, ánh mắt nhìn Hàn Lập thỉnh thoảng lại lóe lên những tia nhìn quái dị, lúc thì thất vọng, lúc lại mang theo vẻ tàn nhẫn khó hiểu khiến Hàn Lập bất giác rùng mình ớn lạnh. Thiếu niên với sự nhạy cảm bẩm sinh bắt đầu nhận ra sự khác thường, hắn càng thêm liều mạng khổ tu, ngày ngủ không quá hai canh giờ, chỉ mong sớm ngày ngưng tụ được khí cảm để làm hài lòng sư phụ.

Hắn thường ngồi xếp bằng dưới những tán tùng cổ thụ lúc đêm thanh vắng, cố gắng gạt bỏ mọi tạp niệm để cảm nhận dòng chảy linh khí trong trời đất. Đan điền của hắn vẫn như một cái giếng cạn trơ đáy, nhưng sự kiên nhẫn phi thường được rèn luyện từ thời thơ ấu cơ cực đã giúp hắn không hề nản lòng bỏ cuộc dù chỉ một giây phút nào.

Đến năm thứ ba, sau vô số đêm thức trắng đày đọa thể xác, trong một đêm mưa bão sấm chớp rền vang, Hàn Lập cuối cùng cũng cảm nhận được một luồng nhiệt lưu ấm áp nhỏ như sợi tơ xuất hiện tại đan điền, từ từ len lỏi qua các kinh mạch. Hắn đã đột phá tầng thứ nhất của Trường Xuân Công! Khi báo tin này cho Mặc Đại Phu, lão giả mừng rỡ như điên cuồng, ánh mắt lộ vẻ tham lam cuồng nhiệt chưa từng có, thậm chí còn ban thưởng cho Hàn Lập vô số đan dược quý giá để hắn gia tăng tốc độ tu luyện.

Sự cuồng nhiệt bất thường của Mặc Đại Phu không làm Hàn Lập vui mừng, ngược lại càng khiến thiếu niên cảm thấy một luồng sát cơ lạnh lẽo đang âm thầm tích tụ. Hắn biết rõ trên đời không có bữa ăn nào miễn phí, sự ưu ái quá mức này ắt hẳn ẩn chứa một âm mưu động trời.

Một buổi chiều muộn mùa thu, sau khi hoàn thành việc tưới nước cho các luống thảo dược, Hàn Lập đi dạo vào sâu trong một khu rừng thưa phía sau cốc. Khi bước qua một bụi gai rậm rạp, chân hắn bỗng vấp phải một vật thể cứng ngắc nhô lên khỏi mặt đất ẩm ướt. Hắn tò mò dùng chiếc xẻng nhỏ đào bới lớp đất mùn xung quanh, lôi ra một chiếc bình nhỏ bằng sứ màu xanh lục đậm, kích thước chỉ bằng nắm tay trẻ con.

Chiếc bình phủ đầy bùn đất, thoạt nhìn như một món đồ sành sứ bình thường của dân gian bị vứt bỏ. Nhưng khi Hàn Lập đem nó ra bờ suối rửa sạch lớp đất cát, hắn ngạc nhiên phát hiện bề mặt bình được khắc họa vô số hoa văn kỳ lạ hình lá cây và mây trời tinh xảo tuyệt luân. Càng kỳ lạ hơn, nắp bình được niêm phong chặt chẽ đến mức dù hắn dùng hết sức bình sinh, thậm chí lấy dao nạy cũng không thể mở ra được nửa phân.

Đêm hôm đó, Hàn Lập đặt chiếc bình nhỏ màu xanh lục trên bậu cửa sổ phòng ngủ rồi ngồi xếp bằng nhập định tu luyện Trường Xuân Công như thường lệ. Nửa đêm, khi ánh trăng rằm vằng vặc chiếu sáng cả thung lũng, một cảnh tượng kinh thiên động địa bỗng nhiên diễn ra.

Từ trên bầu trời đêm sâu thẳm, vô số đốm sáng màu trắng bạc li ti – tinh hoa của ánh trăng và thiên địa linh khí – bỗng nhiên hội tụ lại thành một dải lụa sáng mờ ảo, từ từ rót thẳng vào chiếc bình nhỏ màu xanh lục trên bậu cửa sổ! Chiếc bình rung lên khe khẽ, phát ra ánh sáng màu xanh ngọc bích rực rỡ, những hoa văn trên thân bình như sống dậy, uốn lượn uyển chuyển trong không gian tĩnh mịch.

Hàn Lập giật mình bừng tỉnh khỏi cơn nhập định, sững sờ nhìn cảnh tượng huyền diệu trước mắt. Hắn cẩn thận tiến lại gần chiếc bình. Lúc này, nắp bình vốn dĩ gắn chặt như đúc liền bỗng nhiên nhẹ nhàng bật mở, để lộ ra bên trong đáy bình một giọt chất lỏng màu xanh lục lấp lánh như ngọc châu, tỏa ra một làn linh khí nồng đậm thuần khiết đến mức chỉ cần hít một hơi, toàn bộ kinh mạch đang bế tắc của hắn liền run lên vì sung sướng.

Hàn Lập cẩn thận nhỏ giọt chất lỏng xanh lục ấy vào một gốc linh chi một năm tuổi trồng trong chậu đất góc phòng. Ngay sáng hôm sau, một cảnh tượng không thể tin nổi đập vào mắt thiếu niên: cây linh chi nhỏ bé đã lớn phổng phao, tán nấm chuyển sang màu tím thẫm của loại dược liệu trăm năm tuổi! Chiếc bình nhỏ này có khả năng thúc đẩy thảo dược sinh trưởng với tốc độ ngàn năm chỉ trong vài ngày!

Hắn tiếp tục thử nghiệm giọt linh dịch thần kỳ lên một gốc Dã Sơn Sâm nhỏ bằng ngón tay út. Chỉ sau ba canh giờ ngắn ngủi, củ sâm đã nở to như bắp tay người lớn, rễ con chằng chịt tỏa ra mùi thơm ngát khắp căn phòng kín. Kỳ tích này khẳng định chắc chắn rằng chiếc bình xanh là một chí bảo nghịch thiên vượt xa mọi quy luật sinh tồn tự nhiên của nhân gian.

Nhìn thấy sự màu nhiệm phi thường của chiếc bình xanh lục, trái tim Hàn Lập đập rộn ràng như muốn nhảy ra khỏi lồng ngực. Hắn lập tức nhận thức sâu sắc được mức độ nguy hiểm tột cùng nếu bí mật kinh thiên này bị Mặc Đại Phu hay bất kỳ cường giả võ lâm nào phát hiện. Chiếc bình này đủ sức châm ngòi cho một cuộc tàn sát đẫm máu trên toàn bộ giang hồ. Hắn cẩn thận đào một cái hốc nhỏ dưới chân giường để giấu chiếc bình, chỉ lấy ra vào những đêm trăng thanh vắng để thu gom từng giọt linh dịch quý giá.

Hàn Lập bắt đầu bí mật dùng củ linh sâm trăm năm tuổi nấu thành canh thuốc để uống hằng đêm. Dược lực bàng bạc của linh thảo trăm năm vừa tràn vào bụng đã hóa thành một dòng nhiệt lưu cuồn cuộn cọ rửa kinh mạch, giúp cơ thể hắn trải qua quá trình tẩy kinh phạt tủy kỳ diệu. Tạp chất trong cơ thể dần được bài tiết ra ngoài qua lỗ chân lông, làn da ngăm đen của thiếu niên bắt đầu phát ra một tầng quang trạch sáng bóng, và tu vi Trường Xuân Công của hắn tăng tiến với tốc độ kinh người, liên tiếp đột phá tầng thứ hai rồi tầng thứ ba chỉ trong vài tháng ngắn ngủi.

Trong một lần bắt mạch kiểm tra tu vi cho Hàn Lập, Mặc Đại Phu giật nảy mình kinh ngạc khi thấy chân khí Trường Xuân Công trong cơ thể đệ tử hùng hậu dị thường. Lão nở một nụ cười quái đản, đôi bàn tay gầy trơ xương vuốt ve chòm râu bạc, trong mắt lóe lên vẻ thèm khát điên dại như loài sói đói nhìn thấy con mồi béo bở. Ánh mắt ấy khiến toàn thân Hàn Lập dựng tóc gáy, một cảm giác nguy cơ sinh tử mãnh liệt ập đến.

Biết rõ ngày tàn sát sắp cận kề, thiếu niên không hề nao núng mà âm thầm chuẩn bị hậu thủ. Hắn lén giấu những cây ngân châm tẩm kịch độc vào tay áo, học thêm các thủ pháp ám khí giang hồ và cất giấu bột vôi cùng độc dược phòng thân trong đai lưng. Hắn tự nhủ bằng mọi giá phải sống sót bước ra khỏi Thần Thủ Cốc này.

Nhờ có linh dịch thần bí thúc đẩy các cây linh thảo ngàn năm tuổi sinh trưởng thần tốc, Hàn Lập bắt đầu tự mình lén lút luyện chế các loại linh đan tẩm bổ cơ thể, từng bước đột phá các tầng tiếp theo của Trường Xuân Công, chuẩn bị cho cuộc đối đầu sinh tử không thể tránh khỏi với người sư phụ đầy tâm cơ Mặc Đại Phu.

Mỗi lần chứng kiến Mặc Đại Phu ho khù khụ trong bóng tối và nhìn thấy ánh mắt tham lam điên dại của lão khi nhìn vào cơ thể mình, Hàn Lập càng củng cố quyết tâm phải nhanh chóng gia tăng thực lực. Trong thế giới tu chân tàn khốc đầy rẫy cạm bẫy, chỉ có sức mạnh tuyệt đối và sự cẩn trọng tột cùng mới là bùa hộ mệnh chân chính duy nhất giúp hắn bảo toàn tính mạng.

Hắn thầm hứa với lòng mình, dù con đường trường sinh phía trước có muôn trùng chông gai và máu chảy thành sông, hắn cũng sẽ từng bước tiến lên, không bao giờ cúi đầu khuất phục trước số phận.

Chính giọt linh dịch thần bí này, và chiếc Chưởng Thiên Bình nghịch thiên này, sẽ trở thành bảo vật quan trọng nhất đưa Hàn Lập từ một phàm nhân tư chất kém cỏi bước lên đỉnh cao của Tiên Giới, mở ra truyền kỳ Phàm Nhân Tu Tiên bất hủ muôn đời.', 0, 'PUBLISHED', 3600, false)
) AS c(chapter_number, title, content, price, status, views, is_deleted)
ON CONFLICT (novel_id, chapter_number) DO UPDATE
SET title = EXCLUDED.title, content = EXCLUDED.content, price = EXCLUDED.price, status = EXCLUDED.status;

-- ================================================================================
-- Truyện: Quỷ Bí Chi Chủ (quy-bi-chi-chu)
-- ================================================================================
INSERT INTO novels (title, slug, author, summary, cover_url, status, is_deleted, uploader_id, views, average_rating, rating_count)
VALUES (
    'Quỷ Bí Chi Chủ',
    'quy-bi-chi-chu',
    'Ái Tiềm Thủy Đích Ô Tặc',
    'Trong làn sóng hơi nước và máy móc cuồn cuộn, ai có thể chạm tới phi phàm? Trong màn sương mù lịch sử và bóng tối, ai đang thì thầm bên tai? Chu Minh Thụy thức tỉnh trong thân xác Klein Moretti, bước lên con đường thành thần...',
    'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=600&q=80',
    'COMPLETED',
    false,
    (SELECT id FROM users WHERE email = 'admin@tramtruyen.com'),
    9800,
    4.98,
    3400
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    author = EXCLUDED.author,
    summary = EXCLUDED.summary,
    cover_url = EXCLUDED.cover_url,
    status = EXCLUDED.status,
    is_deleted = false;

INSERT INTO novel_categories (novel_id, category_id)
SELECT n.id, c.id
FROM novels n
CROSS JOIN categories c
WHERE n.slug = 'quy-bi-chi-chu'
  AND c.slug IN ('huyen-huyen')
ON CONFLICT DO NOTHING;

WITH target_novel AS (
    SELECT id FROM novels WHERE slug = 'quy-bi-chi-chu'
)
INSERT INTO chapters (novel_id, chapter_number, title, content, price, status, views, is_deleted)
SELECT
    tn.id,
    c.chapter_number, c.title, c.content, c.price, c.status, c.views, c.is_deleted
FROM target_novel tn,
(VALUES
(1, 'Đỏ Thẫm', 'Đau đớn! Một cơn đau nhức buốt nhói xé toạc màng óc như thể có một mũi khoan thép sắc nhọn đang điên cuồng xoáy sâu vào từng tế bào thần kinh của Chu Minh Thụy. Hắn khó nhọc mở hai mí mắt nặng trĩu như ngàn cân, tầm nhìn ban đầu nhòe nhoẹt mờ sương dần dần ngưng tụ lại thành những hình ảnh rõ nét của một căn phòng xa lạ.

Không khí chung quanh sặc sụa mùi cồn công nghiệp hòa quyện với mùi ẩm mốc đặc trưng của giấy viết mục rữa và một vệt tanh nồng quen thuộc của kim loại bị gỉ sét. Trước mặt hắn là một chiếc bàn làm việc bằng gỗ sồi thô mộc, bên trên ngổn ngang những trang sách cổ viết bằng thứ chữ tượng hình kỳ dị, một chiếc lọ mực thủy tinh bị lật đổ để lại vệt mực đen loang lổ, và một khẩu súng lục ổ xoay màu đen bóng đang tỏa ra mùi thuốc súng khen khét.

Chu Minh Thụy theo bản năng đưa tay lên ôm lấy vầng trán bên phải của mình. Đầu ngón tay chạm vào một mảng dính nhớp ẩm ướt, ấm nóng và nhớp nháp. Khi rút tay về, đập vào mắt hắn là màu đỏ thẫm chói chang của máu tươi, kèm theo đó là những vụn thịt li ti vỡ vụn. Một cảm giác lạnh toát chạy dọc sống lưng hắn: ngay bên thái dương phải của thân xác này là một lỗ thủng đường kính chừng một ngón tay do đạn bắn tạo thành!

"Mình... mình bị bắn vào đầu ư? Sao mình vẫn chưa chết?" Chu Minh Thụy giật mình kinh hãi, trái tim trong lồng ngực đập thình thịch như muốn nhảy bung ra ngoài lồng ngực.

Hắn nhớ rất rõ, chỉ mới vài giờ trước hắn vẫn còn là một thanh niên bình thường sống tại thành phố hiện đại thời thế kỷ hai mươi mốt, vừa hoàn thành công việc và thực hiện một nghi thức chuyển vận cổ quái bằng cách đi bốn bước ngược chiều kim đồng hồ quanh góc phòng trọ sau khi đọc một bài viết trên mạng Internet. Đột nhiên một cơn lốc xoáy vô hình ập đến cuốn hắn đi, và khi tỉnh lại, hắn đã hiện diện trong căn phòng mang đậm phong cách thời kỳ Cách mạng Công nghiệp phương Tây thế kỷ mười chín này.

Một luồng ký ức khổng lồ và hỗn loạn đột ngột bùng nổ trong đại não hắn tựa như một con đê vỡ nước. Từng mảnh vỡ ký ức xa lạ gào thét chen chúc nhau tái hiện: Klein Moretti – đó chính là tên của chủ nhân cơ thể này. Hắn vừa tốt nghiệp khoa Lịch sử tại Đại học Hoy thuộc Vương quốc Loen, một quốc gia hùng mạnh với nền công nghiệp hơi nước phát triển rực rỡ, nhưng bên dưới bề mặt phồn hoa lại ẩn giấu vô số bí mật đen tối của thế giới siêu nhiên.

Klein sống cùng một người anh trai làm nhân viên văn phòng mẫn cán tên là Benson và một cô em gái nhỏ bé ngoan ngoãn tên là Melissa. Gia đình họ thuộc tầng lớp bình dân nghèo khổ ở thành phố Tingen, chật vật gom góp từng đồng bảng, đồng pence để trang trải cuộc sống và chu cấp tiền học cho Klein.

Nhưng tại sao Klein lại chết? Tại sao lại có khẩu súng lục và phát đạn tự sát xuyên qua màng óc này? Những câu hỏi bủa vây lấy tâm trí hắn như một mớ bòng bong không lời giải đáp.

Chu Minh Thụy loạng choạng gượng đứng dậy khỏi chiếc ghế tựa bọc da rách nát. Hai chân hắn run rẩy đến mức suýt chút nữa ngã quỵ xuống sàn gỗ kêu cọt kẹt. Hắn tiến đến chiếc gương soi hình bầu dục gắn trên tủ quần áo cũ kỹ ở góc phòng để quan sát bản thân.

Trong gương là một thanh niên trạc hai mươi hai tuổi, dáng người hơi gầy gò, nước da trắng xanh xao, mái tóc đen cắt ngắn hơi rối bù và đôi mắt màu nâu sâu thẳm ẩn chứa vẻ hoang mang tột độ. Điều quái dị nhất, rợn người nhất chính là vết thương bên thái dương phải của hắn: miệng vết thương đẫm máu đang tự động khép miệng với tốc độ mắt thường có thể nhìn thấy được! Những sợi thịt li ti như những xúc tu nhỏ bé đang vặn vẹo, kết nối lại với nhau, bịt kín lỗ thủng do đầu đạn súng lục gây ra, chỉ để lại một vệt sẹo mờ nhạt rồi biến mất hoàn toàn.

"Chuyện quái quỷ gì thế này... Cơ thể này có khả năng tự phục hồi sinh mệnh sau cái chết sao?" Chu Minh Thụy thì thào trong bóng tối, mồ hôi lạnh túa ra ướt đẫm lưng áo sơ mi.

Hắn quay trở lại bàn làm việc, bắt đầu cẩn thận lục lọi những cuốn sổ ghi chép và giấy tờ còn sót lại. Một cuốn sổ tay bìa da màu nâu sẫm thu hút toàn bộ sự chú ý của hắn. Trang cuối cùng của cuốn sổ tay được viết bằng tiếng Hermes cổ kính – thứ ngôn ngữ bí thuật dùng trong các nghi lễ huyền bí phương Tây:

"Tất cả mọi người đều sẽ phải chết, bao gồm cả tôi."

Nét chữ viết tay nguệch ngoạc, run rẩy và mang theo một sự tuyệt vọng cùng cực, như thể người viết đang phải đối mặt với một nỗi kinh hoàng vô hình vượt quá giới hạn lý trí của nhân loại. Cạnh dòng chữ ấy là một biểu tượng ma thuật kỳ dị vẽ bằng mực đỏ, hình một con mắt không có đồng tử lồng trong những đường tròn đồng tâm ngoằn ngoèo.

Hắn cầm khẩu súng lục ổ xoay lên, mở ổ đạn ra kiểm tra. Bên trong có sáu ổ đạn, năm viên đạn bằng đồng thau còn nguyên vẹn và một vỏ đạn rỗng đã điểm hỏa. Điều này chứng minh rằng phát đạn xuyên qua đầu Klein chính là phát súng từ khẩu súng này. Hắn cẩn thận lấy khăn tay lau sạch dấu vân tay trên thân súng, cất nó vào nơi kín đáo sâu trong hộc tủ bàn học.

Những cuốn sách trên kệ sách của Klein chủ yếu nói về lịch sử các kỷ nguyên cổ xưa: Kỷ đệ Tứ của Đế quốc Solomon, Triều đại Tudor và Đế quốc Trunsoest. Có vẻ như Klein cùng hai người bạn cùng lớp là Welch và Naya đã vô tình mua được một cuốn nhật ký cổ xưa của gia tộc Antigonus từ một hiệu sách cũ, và chính việc giải mã cuốn nhật ký ma quái ấy đã dẫn đến thảm kịch kinh hoàng này.

Đúng lúc đó, tiếng bước chân nhẹ nhàng vang lên ngoài hành lang kèm theo tiếng gõ cửa cộc cộc. Giọng nói trong trẻo của một thiếu nữ cất lên: "Anh Klein? Anh thức dậy chưa? Em đã chuẩn bị xong bánh mì lúa mạch đen và súp thịt cừu cho bữa sáng rồi. Hôm nay anh còn có buổi phỏng vấn xin việc tại Đại học Tingen đấy, đừng để bị muộn giờ nhé."

Đó là Melissa, cô em gái mười sáu tuổi hiểu chuyện và tháo vát của gia đình Moretti. Chu Minh Thụy giật mình, vội vàng lấy khăn lau sạch vết máu dính trên cổ áo và mặt, chỉnh lại đầu tóc rồi cất giọng hơi khàn khàn đáp lại: "Anh dậy rồi Melissa, anh sẽ ra ngay đây."

Bước ra phòng khách nhỏ, hắn nhìn thấy chiếc bàn ăn phủ khăn ca-rô đơn sơ. Melissa đang cẩn thận múc hai bát súp nóng hổi, còn người anh cả Benson vừa từ bưu điện về, gương mặt lộ vẻ mệt mỏi nhưng nụ cười ấm áp xua tan cái lạnh sáng sớm. Benson đặt một túi giấy nhỏ lên bàn, bên trong là hai chiếc bánh chanh nướng thơm phức – món quà xa xỉ mà người anh cả tiết kiệm tiền mua cho hai đứa em.

Bữa sáng diễn ra trong không khí ấm cúng của tình thân. Benson kể về sự biến động của giá bánh mì, các cuộc bãi công tại nhà máy dệt gần bến cảng Tussock, và những hy vọng rằng nếu Klein trúng tuyển vào vị trí giảng viên lịch sử tại Đại học Tingen, gia đình họ sẽ có thể chuyển đến một căn hộ khang trang hơn ở khu phố Bắc. Nhìn hai anh em yêu thương và quan tâm đến mình, một cảm xúc bồi hồi dâng lên trong lòng Chu Minh Thụy. Dù thân xác này là của Klein, nhưng tình cảm gia đình mộc mạc ấy đã chạm đến nơi sâu kín nhất trong trái tim hắn.

Sau khi Melissa đến trường học và Benson trở lại sở làm, Chu Minh Thụy quay trở về phòng ngủ, khóa chặt cửa lại. Đứng bên khung cửa sổ nhìn ra thành phố Tingen, hắn thấy những cột khói đen đặc từ các ống khói nhà máy cuồn cuộn bốc lên hòa vào sương mù. Thời đại của máy hơi nước, bánh răng kim loại và sự bất bình đẳng giai cấp đang hiển hiện rõ nét trước mắt hắn. Dưới lòng đường, những người phu quét rác trong chiếc áo rách rưới đang cần mẫn làm việc, người công nhân thắp đèn đường đang tắt các ngọn đèn khí đốt, và những cỗ xe ngựa chở quý tộc lướt qua để lại tiếng vó ngựa lộp cộp.

Thế nhưng điều khiến hắn trăn trở nhất là làm sao để trở về thế giới cũ, nơi có cha mẹ và bạn bè thân thương. Một ý nghĩ táo bạo nảy ra trong đầu: nghi thức chuyển vận bốn bước chân đã đưa hắn đến đây, liệu nó có thể đưa hắn quay trở về hay không?

Dù hy vọng vô cùng mong manh, hắn vẫn quyết định phải mạo hiểm thử nghiệm. Hắn lấy bốn mẩu bánh mì khô đặt ở bốn góc phòng ngủ để làm vật tế tượng trưng theo nghi thức ma thuật dân gian. Đứng tại trung tâm căn phòng, hắn hít sâu một hơi rồi bắt đầu cất bước đi bốn bước ngược chiều kim đồng hồ, miệng lẩm bẩm xướng tụng bằng tiếng Trung cổ xưa: "Phúc Sinh Huyền Hoàng Thiên Tôn... Phúc Sinh Huyền Hoàng Thế Giới... Phúc Sinh Huyền Hoàng Thượng Đế..."

Vừa dứt câu xướng tụng cuối cùng, toàn bộ không gian xung quanh hắn bỗng nhiên rung chuyển dữ dội! Trọng lực biến mất hoàn toàn, tầm nhìn bị xé toạc bởi một luồng ánh sáng chói lòa vô tận. Từng đợt tiếng thì thầm điên loạn ma quái của các thực thể vô hình gào thét bên tai tựa như muốn bóp nát thần trí của hắn. Hắn cắn chặt răng chịu đựng cơn đau đớn xé rách linh hồn.

Một màn sương mù xám mênh mông, cổ xưa và vĩnh hằng bất ngờ hiện ra trước tâm trí hắn, cuốn phăng linh hồn hắn bay thẳng vào cõi hư vô vô tận vượt ngoài tầm hiểu biết của phàm nhân. Linh hồn hắn lơ lửng giữa biển sương mù mênh mông không thấy bờ bến, nơi thời gian dường như ngưng đọng từ thuở hồng hoang khai thiên lập địa.

Giữa làn sương mù xám vô tận ấy, những ngôi sao đỏ thẫm lấp lánh như mắt thần, và sừng sững trên đỉnh cao nhất là một tòa cung điện nguy nga tráng lệ vượt qua mọi sự tưởng tượng của nhân loại. Vận mệnh của Chu Minh Thụy – nay là Klein Moretti – đã chính thức gắn chặt với thế giới của những Bí Mật Vực Thẳm và Trật Tự Thần Thánh.', 0, 'PUBLISHED', 4200, false),
(2, 'Bói Toán Và Năng Lực Giả', 'Màn sương mù màu xám tro dày đặc bao trùm lấy toàn bộ nhận thức của Klein Moretti. Trong không gian vô tận không có trọng lực và không có khái niệm phương hướng này, hắn cảm thấy linh hồn mình nhẹ bẫng như một làn khói lơ lửng giữa hư không. Từng đợt sóng năng lượng vô hình nhưng uy nghiêm tột bực cuộn trào quanh thân thể hắn, mang theo hơi thở của những kỷ nguyên xa xưa đã bị dòng sông thời gian vùi lấp.

Trước mặt Klein, sừng sững giữa biển sương mù là một tòa cung điện bằng đá cẩm thạch khổng lồ tựa như nơi trú ngụ của những vị thần viễn cổ. Tòa cung điện được chống đỡ bởi hàng chục cột đá hoa cương to lớn khắc họa những phù điêu thần bí mô tả các vì tinh tú và các biểu tượng huyền học cổ xưa. Ở chính giữa cung điện là một chiếc bàn dài bằng đồng xanh bóng loáng với hai mươi hai chiếc ghế dựa có lưng ghế cao chạm khắc hoa văn tinh xảo.

Klein bước từng bước chậm rãi tiến vào cung điện, kinh ngạc nhận thấy bản thân đang ngồi vào vị trí ghế chủ tọa danh dự nhất ở đầu bàn dài. Chiếc ghế của hắn chạm khắc biểu tượng của "Con Mắt Toàn Tri" và biểu tượng chòm sao không đổi. Ngay khoảnh khắc hắn chạm tay vào mặt bàn đồng cổ kính, một cảm giác quyền năng vô hạn tràn ngập tâm khảm: trong không gian sương mù xám này, hắn chính là chúa tể tuyệt đối, có thể thao túng linh hồn và bóp méo các quy luật thực tại.

Klein khẽ búng ngón tay, hai luồng ánh sáng đỏ thẫm rực rỡ như những ngôi sao băng từ thế giới thực xa xôi bị kéo thẳng vào không gian trên màn sương xám. Hai hình bóng mờ ảo hiện ra trên hai chiếc ghế đối diện: một thiếu nữ quý tộc trẻ tuổi và một người đàn ông trưởng thành từng trải.

"Đây là đâu?! Thần linh hiển linh sao?!" Thiếu nữ quý tộc – với mái tóc vàng óng và bộ váy dạ hội lộng lẫy – thảng thốt kêu lên khi thấy mình đột ngột bị dịch chuyển đến một cung điện thần thánh trôi nổi trên biển mây sương mù.

Người đàn ông đối diện cũng lộ rõ vẻ kinh hoàng, nhưng sự từng trải giúp ông ta nhanh chóng kiềm chế cảm xúc, cẩn trọng quan sát xung quanh và đặc biệt là bóng người bí ẩn đang ngồi ở vị trí thượng thủ, người được che phủ bởi một tầng sương mù dày đặc không thể nhìn thấu dung mạo.

Klein giữ vẻ bình thản uy nghiêm, giọng nói trầm ấm vang vọng khắp cung điện như tiếng chuông đồng từ cõi vĩnh hằng vọng lại: "Một cuộc thử nghiệm nhỏ mà thôi. Các vị có thể gọi tôi là ''Kẻ Khờ'' (The Fool)."

Lời giới thiệu ngắn gọn nhưng đầy sức nặng ấy đã đặt viên đá đầu tiên cho sự ra đời của một tổ chức bí mật vĩ đại nhất lịch sử thế giới siêu nhiên: Hội Tarot. Thiếu nữ nhận danh hiệu "Chính Nghĩa" (Justice), còn người đàn ông nhận danh hiệu "Người Treo Ngược" (The Hanged Man). Từ đây, các thông tin tình báo, các công thức ma dược bí truyền và các cổ vật thần bí bắt đầu được trao đổi dưới sự chứng kiến của Kẻ Khờ.

Cô gái Chính Nghĩa – tiểu thư Audrey Hall của gia tộc Bá tước Hall danh giá tại Backlund – bày tỏ lòng ngưỡng mộ và mong muốn mua công thức ma dược cấp 9 Khán Giả (Spectator) để bước chân vào con đường phi phàm. Người Treo Ngược – thuyền trưởng Alger Wilson của Giáo hội Bão Tố – đồng ý cung cấp công thức ma dược này, bao gồm mắt cá cuồng của sông ngầm Linh Giới và máu rồng biển tinh khiết, đổi lại việc Audrey phải để mắt tới các động thái của Hải quân Hoàng gia tại thủ đô.

Đặc biệt, Audrey đề nghị dâng lên những trang nhật ký bí ẩn của Đại Đế Roselle – vị hoàng đế vĩ đại thời kỳ Phục hưng đã phát minh ra động cơ hơi nước và bài Tarot. Khi những trang nhật ký hiện lên trong tay Klein, trái tim hắn đập thót một cái vì kinh ngạc: đó chính là chữ Hán giản thể hiện đại của Trái Đất! Người khác coi đó là phù chú của thần linh, nhưng Klein đọc vanh vách từng dòng tâm sự đời tư hài hước và ngạo mạn của tiền bối xuyên không, qua đó thấu hiểu những quy luật cốt lõi của thế giới siêu nhiên.

Roselle viết trong nhật ký: "Ngày 4 tháng 3, hôm nay lại đi vũ hội hoàng gia, những quý bà quý cô thật quyến rũ... Nhưng điều quan trọng hơn, ta đã phát hiện ra bí mật của hai mươi hai con đường thần thánh. Ma dược không phải là ân huệ của thần, mà là chất độc chứa đựng nguy cơ điên loạn và tha hóa vĩnh viễn." Những ghi chép quý giá này đã trở thành nguồn tri thức vô tận soi sáng cho bước đường của Klein.

Sau khi giải tán buổi hội ngộ Tarot đầu tiên và đưa hai vị khách trở về thế giới thực, Klein thu hồi linh tính, để bản thân rơi trở lại thể xác đang nằm trong căn phòng nhỏ ở thành phố Tingen.

Hắn mở mắt ra, mồ hôi đầm đìa nhưng tinh thần lại vô cùng minh mẫn. Trở về với thực tại, hắn biết mình phải lập tức đối mặt với những nguy cơ nhãn tiền. Vụ án cái chết kỳ lạ của Klein cũ cùng sự biến mất của cuốn nhật ký cổ chắc chắn sẽ thu hút sự chú ý của các cơ quan quản lý siêu nhiên chính thống.

Đúng như dự đoán của hắn, chỉ chưa đầy hai ngày sau, những kẻ gác đêm của Giáo hội Nữ Thần Đêm Đen – Đội Kẻ Gác Đêm (Nighthawks) phụ trách bảo an siêu nhiên tại Tingen – đã tìm đến gõ cửa phòng hắn. Đội trưởng Dunn Smith, một người đàn ông trung niên với đôi mắt xám tro u buồn và chiếc áo khoác đen dài chấm gối, đã trực tiếp thẩm vấn Klein về những gì xảy ra trong đêm định mệnh.

Bằng sự thông minh nhạy bén và tài hùng biện của một cựu sinh viên lịch sử, Klein đã khéo léo vượt qua cuộc thẩm vấn tâm linh của Dunn Smith mà không làm lộ tẩy thân phận dị giới hay bí mật sương mù xám.

Nhận thấy Klein có năng khiếu thiên bẩm về lịch sử huyền học và là người duy nhất sống sót kỳ diệu sau một sự kiện siêu nhiên tàn khốc, Dunn Smith đã đưa ra lời đề nghị mời hắn gia nhập Đội Kẻ Gác Đêm với tư cách là một nhân viên dân sự chuyên trách nghiên cứu cổ văn hiến tại trụ sở phố Zouteland với mức lương hậu hĩnh ba bảng mỗi tuần.

Tại trụ sở công ty bảo an Blackthorn – vỏ bọc hợp pháp của Đội Kẻ Gác Đêm – Klein được gặp gỡ những đồng đội mới: chàng thi sĩ lãng tử Leonard Mitchell với đôi mắt xanh lục biếc đầy bí ẩn, và chuyên gia giải phẫu pháp y lạnh lùng Frye. Nơi đây mở ra cho hắn một thế giới hoàn toàn khác biệt với xã hội phàm tục bên ngoài.

Đó chính là cơ hội nghìn năm có một mà Klein đang khao khát tìm kiếm! Chỉ khi gia nhập một tổ chức chính thống của Giáo hội, hắn mới có thể danh chính ngôn thuận tiếp cận với con đường phi phàm chân chính: Ma Dược.

Tại căn cứ ngầm dưới lòng đất phía sau Cánh Cổng Chanis huyền bí của Nhà thờ Thánh Selena, Dunn Smith đưa cho hắn danh sách các danh sách Ma Dược Khởi Điểm cấp 9 thuộc Giáo hội Đêm Đen quản lý. Sau khi cân nhắc kỹ lưỡng giữa "Kẻ Nhòm Ngó Bí Ẩn", "Bất Miên Giả" và "Thầy Bói" (Chiêm Bặc Gia), Klein đã dứt khoát lựa chọn con đường Chiêm Bặc Gia – danh sách khởi đầu cho chuỗi ma dược Bói Toán và Kỳ Tích.

Dunn Smith cẩn thận dặn dò hắn về nguyên tắc sống còn của người phi phàm: "Quy luật bất biến của ma dược là: ngươi chỉ đang sắm vai, chứ không phải trở thành ma dược. Luôn giữ vững lý trí và bản ngã của con người, nếu không ngươi sẽ bị sức mạnh thần bí nuốt chửng và biến thành quái vật mất khống chế."

Dưới sự trợ giúp của nhà giả kim thuật trong đội, lọ ma dược Chiêm Bặc Gia được pha chế hoàn tất từ tinh huyết linh giới, bột hoa oải hương và nước cất tinh khiết. Cầm trong tay lọ ma dược màu tím sẫm lấp lánh như bầu trời đêm đầy sao, Klein hít một hơi sâu rồi ngửa cổ uống cạn.

Một dòng chất lỏng the mát kèm theo vị chát nhẹ chảy thẳng xuống cổ họng, lập tức kích phát toàn bộ linh tính trong cơ thể hắn bùng nổ! Tầm nhìn của hắn biến đổi hoàn toàn: hắn có thể nhìn thấy màu sắc linh quang của vạn vật, nghe thấy những lời thì thầm huyền bí của thế giới tâm linh, và cảm nhận rõ ràng dòng chảy của số phận đang chuyển động quanh mình.

Một Kẻ Bói Toán mới đã chính thức ra đời giữa lòng thời đại hơi nước và khói bụi công nghiệp. Klein Moretti mỉm cười nhẹ, kéo vành mũ phớt màu đen xuống thấp, sẵn sàng dấn thân vào con đường siêu phàm đầy hiểm nguy nhưng cũng ngập tràn điều kỳ diệu.

Trong cõi nhân gian bao la vạn dặm, con đường nhân sinh và tu đạo vốn dĩ chưa từng bằng phẳng dễ dàng. Những bậc tiền bối đại năng trong truyền thuyết xưa kia khi đạp mây cưỡi gió, ngạo thị quần hùng đều đã từng phải trải qua vô số năm tháng cơ hàn, nếm trải trăm ngàn cay đắng tủi nhục giữa chốn hồng trần cuồn cuộn. Nhân tâm hiểm ác, thế sự khó lường, kẻ mạnh đứng trên đỉnh cao nhìn xuống thường xem chúng sinh như loài sâu kiến nhỏ nhoi, tùy ý định đoạt vận mệnh của kẻ yếu.

Thế nhưng, chính trong những nghịch cảnh tăm tối nhất của cuộc đời, ý chí bất khuất của con người mới bộc lộ sức mạnh phi thường tiềm ẩn. Dù thể xác có phải chịu đựng đòn roi tra tấn, dù kinh mạch bế tắc hay bị toàn bộ thế giới quay lưng ruồng bỏ, một khi ngọn lửa niềm tin vào chính bản thân mình chưa từng tắt lụi, thì vẫn còn cơ hội để lội ngược dòng nước, nghịch chuyển càn khôn.

Bầu trời đêm sâu thẳm lấp lánh muôn ngàn vì tinh tú xa xôi, mỗi một đốm sáng tượng trưng cho một số phận, một linh hồn đang tranh đấu không ngừng nghỉ để tìm kiếm vị trí của mình trong vũ trụ vô tận. Gió đêm thổi qua những tán cây cổ thụ phát ra âm thanh rì rào như tiếng thở dài của thời gian. Những kỷ nguyên huy hoàng rồi cũng sẽ lụi tàn thành tro bụi, các bậc đế vương bá chủ rồi cũng phải quy về cát bụi thời gian, duy chỉ có những tâm hồn kiên định dũng cảm dám bước đi trên con đường của riêng mình mới có thể lưu danh vĩnh cửu.', 0, 'PUBLISHED', 4100, false)
) AS c(chapter_number, title, content, price, status, views, is_deleted)
ON CONFLICT (novel_id, chapter_number) DO UPDATE
SET title = EXCLUDED.title, content = EXCLUDED.content, price = EXCLUDED.price, status = EXCLUDED.status;

-- ================================================================================
-- Truyện: Tiên Nghịch (tien-nghich)
-- ================================================================================
INSERT INTO novels (title, slug, author, summary, cover_url, status, is_deleted, uploader_id, views, average_rating, rating_count)
VALUES (
    'Tiên Nghịch',
    'tien-nghich',
    'Nhĩ Căn',
    'Thuận vi phàm, nghịch vi tiên, chỉ tại một ý niệm giữa chốn hồng trần. Thiếu niên bình thường Vương Lâm mang theo Thiên Nghịch Châu, bước lên con đường tu tiên tàn khốc đầy rẫy huyết vũ tanh phong, nghịch thiên cải mệnh...',
    'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=600&q=80',
    'COMPLETED',
    false,
    (SELECT id FROM users WHERE email = 'admin@tramtruyen.com'),
    9200,
    4.94,
    2800
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    author = EXCLUDED.author,
    summary = EXCLUDED.summary,
    cover_url = EXCLUDED.cover_url,
    status = EXCLUDED.status,
    is_deleted = false;

INSERT INTO novel_categories (novel_id, category_id)
SELECT n.id, c.id
FROM novels n
CROSS JOIN categories c
WHERE n.slug = 'tien-nghich'
  AND c.slug IN ('tien-hiep')
ON CONFLICT DO NOTHING;

WITH target_novel AS (
    SELECT id FROM novels WHERE slug = 'tien-nghich'
)
INSERT INTO chapters (novel_id, chapter_number, title, content, price, status, views, is_deleted)
SELECT
    tn.id,
    c.chapter_number, c.title, c.content, c.price, c.status, c.views, c.is_deleted
FROM target_novel tn,
(VALUES
(1, 'Thiếu Niên Vương Lâm', 'Núi non trập trùng mây mù bao phủ, dãy núi Hằng Nhạc sừng sững như một thanh cự kiếm chọc thẳng vào trời xanh của Triệu Quốc. Dưới chân núi, hàng trăm thiếu niên thiếu nữ cùng phụ huynh từ khắp nơi trong các châu phủ lân cận đang tề tựu đông đúc, ánh mắt ai nấy đều tràn ngập vẻ háo hức và ngưỡng vọng đối với tiên môn trong truyền thuyết. Hôm nay chính là ngày khai sơn tuyển đệ tử năm năm một lần của Hằng Nhạc Phái – tu tiên môn phái chính thống hiển hách nhất vùng đất này.

Trong đám đông ồn ào ấy, một thiếu niên mặc chiếc áo vải thô màu xám tro vá víu vài chỗ đang đứng nép mình bên cạnh một người đàn ông trung niên gầy gò có đôi bàn tay chai sần chằng chịt. Thiếu niên tên là Vương Lâm, năm nay tròn mười lăm tuổi, con trai của một người thợ mộc nghèo tại một thôn trang nhỏ dưới chân núi. Vương Lâm từ nhỏ thông minh hiếu học, tính tình trầm tĩnh ít nói nhưng trong lòng chứa đựng chí hướng quật cường không chịu khuất phục số phận.

"Thiết Trụ, cơ hội ngàn năm có một này là nhờ Tứ Thúc con nể tình đồng tộc xin cho một suất tiến cử. Nếu con may mắn được Tiên sư thu nhận vào môn phái, từ nay về sau gia đình ta không những nở mày nở mặt với dân làng mà con còn có cơ hội bước lên con đường trường sinh bất tử." Người cha thợ mộc run rẩy vuốt ve mái tóc con trai, trong ánh mắt đục ngầu tràn ngập niềm hy vọng lớn lao nhất của cả một đời người.

Trước ngày lên đường, người mẹ ở nhà đã thức trắng đêm dưới ánh đèn dầu leo lét để khâu lại cho hắn từng đường kim mũi chỉ trên manh áo rách, cẩn thận gói ghém mấy củ khoai nướng và nửa đấu gạo rang nhét vào tay nải cho con trai lót dạ. Trong thôn xóm nhỏ bé, hàng xóm láng giềng xì xào bàn tán, kẻ ghen tị buông lời mỉa mai, người tốt bụng thì gửi lời chúc tụng. Tất cả những ánh mắt, những lời nói ấy đều biến thành gánh nặng vô hình nhưng cũng là động lực lớn lao đè nặng lên đôi vai gầy guộc của thiếu niên.

Vương Lâm nhớ lại những ngày tháng ấu thơ phụ giúp cha cưa gỗ đục mộng trong xưởng mộc nhỏ. Những khúc gỗ mục xù xì qua bàn tay nhẫn nại của người cha dần biến thành những chiếc bàn, chiếc ghế vuông vức. Cha từng dạy hắn: làm người như làm mộc, cốt ở sự kiên tâm bền bỉ, gọt giũa từng li từng tí mới thành tài. Lời dạy mộc mạc ấy đã khắc sâu vào xương tủy thiếu niên.

Vương Lâm nhìn vào đôi mắt đầy nếp nhăn và tấm lưng còng còng vì năm tháng đẽo gọt gỗ mục của cha, trong lòng dâng lên một nỗi xúc động nghẹn ngào. Hắn nắm chặt hai bàn tay khẳng khiu, kiên định gật đầu: "Cha yên tâm, Thiết Trụ nhất định sẽ dốc hết toàn lực, tuyệt đối không để cha mẹ và gia tộc họ Vương thất vọng!"

Đúng giờ Ngọ ba khắc, ba tiếng chuông đồng ngân vang trầm hùng từ đỉnh núi vọng xuống xé tan màn sương sớm. Vài đạo cầu vồng rực rỡ xé toạc tầng mây bay lượn trên không trung, để lộ bóng dáng của những vị Tiên sư Hằng Nhạc Phái khoác đạo bào phiêu dật, đạp trên phi kiếm lơ lửng giữa trời cao. Khí phái thần tiên phi phàm ấy khiến toàn thể phàm nhân bên dưới đồng loạt quỳ rạp xuống đất bái lạy trong kinh ngạc và tôn sùng tột độ.

Thử thách khảo nghiệm tư chất bắt đầu. Toàn bộ thiếu niên tham gia ứng tuyển phải tự mình trèo lên bậc thang đá dài hàng ngàn trượng dẫn thẳng lên sơn môn đỉnh núi trước khi mặt trời lặn. Bậc thang dốc đứng, trơn trượt đầy rêu phong, gió lạnh như dao cạo rít gào từng hồi buốt giá, áp lực vô hình từ đại trận hộ sơn đè nặng lên từng bước chân non nớt.

Nhiều thiếu niên con nhà quyền quý mang theo đồ ăn ngon và áo ấm chỉ leo được một canh giờ đã than khóc bỏ cuộc nằm bẹp dí dưới các tảng đá. Nhưng Vương Lâm cắn chặt răng, từng bước từng bước vững chắc tiến lên phía trước. Đôi giày rơm của hắn rách nát từ lâu, những viên đá dăm sắc nhọn cứa vào lòng bàn chân rướm máu đỏ tươi để lại từng dấu chân đỏ thẫm trên bậc đá hoa cương. Mồ hôi hòa lẫn máu chảy ròng ròng trên gương mặt non nớt, đôi môi khô nứt nẻ vì khát nước, nhưng ánh mắt hắn vẫn kiên định nhìn thẳng lên đỉnh núi mù sương.

Mỗi lần cảm thấy cơ thể kiệt sức muốn ngã quỵ, hình ảnh người cha lam lũ và ánh mắt rưng rưng lệ của mẹ lại hiện lên trong tâm trí hắn. Hắn tự nhủ không được phép gục ngã! Bò bằng hai tay, trườn bằng hai đầu gối, da thịt trầy xước rách bươm va chạm vào đá sắc buốt nhói, nhưng ý chí sắt đá của thiếu niên đã chiến thắng nỗi đau thể xác cùng cực.

Những thiếu niên khác vượt qua hắn, buông lời trêu chọc và khinh bỉ cậu bé nhà quê rách rưới. Vương Lâm không đáp lại một lời, hắn chỉ im lặng thở từng hơi nhọc nhằn, dồn toàn bộ sự tập trung vào từng bậc đá trước mắt. Trong thế giới của hắn lúc này, chỉ có đỉnh núi và mục tiêu phải chạm tới.

Khi ánh hoàng hôn đỏ rực buông xuống nhuộm thẫm chân trời, Vương Lâm với toàn thân rách rưới rớm máu cuối cùng cũng bò được lên bậc thang đá cuối cùng trên đỉnh Hằng Nhạc Sơn. Hắn ngã quỵ xuống đất thở dốc, kiệt sức đến mức không nhấc nổi ngón tay, nhưng trong lòng tràn ngập niềm vui sướng tột cùng vì đã vượt qua thử thách ý chí khắc nghiệt nhất.

Thế nhưng, sự đời trớ trêu và tàn nhẫn vượt xa sức tưởng tượng của thiếu niên ngây thơ. Trong vòng khảo hạch căn cốt linh căn sau đó tại đại điện, vị Đạo trưởng chủ trì tay cầm la bàn phong thủy kiểm tra đan điền của từng người. Khi đặt tay lên trán Vương Lâm, chiếc la bàn không hề phát ra bất kỳ tia sáng nào. Đạo trưởng liếc nhìn hắn một cái với ánh mắt lạnh như băng giá rồi lạnh lùng phán quyết:

"Tư chất cực kỳ hạ phẩm, kinh mạch bế tắc hoàn toàn, thuộc loại ngụy linh căn tạp nham vô dụng nhất trong thiên hạ. Đời này muốn ngưng tụ một tia chân khí cũng khó như lên trời, đừng nói đến việc Trúc Cơ kết đan. Hằng Nhạc Phái danh môn chính phái tuyệt đối không thu nhận loại phế vật này!"

Từng lời nói của Đạo trưởng tựa như những nhát búa tạ giáng thẳng vào lồng ngực Vương Lâm, đập nát toàn bộ ước mơ và niềm hy vọng cháy bỏng của hắn. Nhìn sang bên cạnh, những công tử nhà giàu dù leo núi bỏ cuộc giữa chừng nhưng nhờ có linh căn tốt và gia đình dâng tặng nhiều vàng bạc lễ vật đều được tươi cười đón nhận vào môn phái làm đệ tử ký danh.

Bất công! Sự bất công tột cùng của thế giới tu chân phơi bày trần trụi trước mắt hắn. Tiên nhân trong truyền thuyết hóa ra cũng chỉ là những kẻ hám lợi, khinh nghèo trọng giàu và lạnh lùng tàn nhẫn như chốn phàm trần mà thôi. Vương Lâm quỳ trên nền đá lạnh lẽo van xin các vị trưởng lão cho hắn một cơ hội làm tạp dịch gánh nước chẻ củi, nhưng chỉ nhận lại những ánh mắt khinh miệt lạnh lùng và những lời xua đuổi phũ phàng.

Hắn bị đuổi ra khỏi sơn môn, lảo đảo bước xuống núi trong cơn mưa tầm tã trút nước. Bầu trời đen kịt sấm chớp gầm vang như đang chế giễu số phận bần hàn của thiếu niên. Nghĩ đến hình ảnh người cha già đang khắc khoải chờ đợi tin vui dưới chân núi, nghĩ đến những lời mỉa mai cay nghiệt của dân làng nếu hắn thất bại nhục nhã quay về, lồng ngực Vương Lâm đau thắt lại như bị ngàn mũi dao cào xé.

"Tại sao?! Tại sao trời sinh ta ra lại cho ta tư chất phế vật?! Tại sao số phận người nghèo lại luôn bị dẫm đạp dưới gót chân kẻ khác?!" Vương Lâm ngửa mặt lên trời gào thét trong tuyệt vọng, nước mắt hòa cùng nước mưa lăn dài trên gò má lem luốc bùn đất.

Trong cơn quẫn bách và tinh thần hoảng loạn tột độ, bước chân thiếu niên trượt trên mép đá trơn trượt. Hắn ngã lăn nhào xuống một hẻm núi sâu thăm thẳm sau vách đá, thân hình nhỏ bé va đập liên hồi vào những cành cây gai góc rồi rơi thẳng xuống vực sâu hun hút...

Nhưng chính tại nơi đáy vực tuyệt vọng ngỡ như đã chạm tới cánh cửa tử thần ấy, bánh xe số phận của Vương Lâm đã bắt đầu chuyển động theo một quỹ đạo nghịch thiên mà ngay cả chư thiên thần phật cũng không thể nào lường trước.

Trong cõi nhân gian bao la vạn dặm, con đường nhân sinh và tu đạo vốn dĩ chưa từng bằng phẳng dễ dàng. Những bậc tiền bối đại năng trong truyền thuyết xưa kia khi đạp mây cưỡi gió, ngạo thị quần hùng đều đã từng phải trải qua vô số năm tháng cơ hàn, nếm trải trăm ngàn cay đắng tủi nhục giữa chốn hồng trần cuồn cuộn. Nhân tâm hiểm ác, thế sự khó lường, kẻ mạnh đứng trên đỉnh cao nhìn xuống thường xem chúng sinh như loài sâu kiến nhỏ nhoi, tùy ý định đoạt vận mệnh của kẻ yếu.

Thế nhưng, chính trong những nghịch cảnh tăm tối nhất của cuộc đời, ý chí bất khuất của con người mới bộc lộ sức mạnh phi thường tiềm ẩn. Dù thể xác có phải chịu đựng đòn roi tra tấn, dù kinh mạch bế tắc hay bị toàn bộ thế giới quay lưng ruồng bỏ, một khi ngọn lửa niềm tin vào chính bản thân mình chưa từng tắt lụi, thì vẫn còn cơ hội để lội ngược dòng nước, nghịch chuyển càn khôn.

Bầu trời đêm sâu thẳm lấp lánh muôn ngàn vì tinh tú xa xôi, mỗi một đốm sáng tượng trưng cho một số phận, một linh hồn đang tranh đấu không ngừng nghỉ để tìm kiếm vị trí của mình trong vũ trụ vô tận. Gió đêm thổi qua những tán cây cổ thụ phát ra âm thanh rì rào như tiếng thở dài của thời gian. Những kỷ nguyên huy hoàng rồi cũng sẽ lụi tàn thành tro bụi, các bậc đế vương bá chủ rồi cũng phải quy về cát bụi thời gian, duy chỉ có những tâm hồn kiên định dũng cảm dám bước đi trên con đường của riêng mình mới có thể lưu danh vĩnh cửu.

Từng dòng suy nghĩ lắng đọng lại trong tâm trí, tựa như dòng suối trong vắt gột rửa đi mọi nỗi hoang mang sợ hãi. Hít một hơi thật sâu để lồng ngực tràn ngập sinh khí của đất trời, ánh mắt hướng về phía chân trời xa xăm nơi ánh bình minh sắp sửa ló rạng xua tan màn đêm tăm tối. Con đường phía trước còn muôn trùng chông gai thử thách, nhưng mỗi một bước chân kiên định được đặt xuống hôm nay sẽ là nền móng vững chắc cho sự quật khởi huy hoàng trong ngày mai.', 0, 'PUBLISHED', 4100, false),
(2, 'Thiên Nghịch Châu', 'Cơn đau nhức kịch liệt khắp toàn thân kéo Vương Lâm tỉnh lại từ cơn hôn mê sâu. Hắn thấy mình đang nằm trên một bãi sỏi ẩm ướt dưới đáy hẻm núi tối tăm, bốn bề vách đá thẳng đứng vươn cao chọc trời ngăn cách hoàn toàn với ánh sáng mặt trời bên ngoài. Tiếng nước nhỏ giọt tong tong từ nhũ đá trên cao vọng lại đều đặn giữa không gian tĩnh mịch lạnh lẽo.

Vương Lâm khó nhọc cử động thân thể, may mắn thay nhờ những bụi cây rậm rạp cản bớt lực rơi nên xương cốt của hắn không bị gãy đoạn nào nghiêm trọng, chỉ có da thịt trầy xước rướm máu khắp nơi. Hắn lết thân mình đến bên bờ một dòng suối ngầm trong vắt để rửa sạch những vết thương trên mặt và tay chân.

Khi cúi đầu vốc ngụm nước suối mát lạnh lên uống, ánh mắt hắn bỗng nhiên chú ý đến một vật thể kỳ lạ đang phát ra ánh sáng mờ ảo nằm lẫn giữa đám sỏi đá dưới đáy nước sâu chừng nửa thước. Hắn thò tay vớt vật thể ấy lên kiểm tra.

Đó là một hạt châu bằng đá màu xám tro, kích thước chỉ bằng quả trứng chim cút, bề mặt nhẵn bóng nhưng xúc cảm khi chạm vào lại lạnh buốt thấu xương. Trên thân hạt châu khắc họa năm đám mây hoa văn kỳ dị mờ ảo, thoạt nhìn như những ký tự cổ xưa từ thời hồng hoang viễn cổ. Kỳ lạ hơn cả, khi Vương Lâm vô tình để một giọt máu tươi từ vết thương nơi đầu ngón tay nhỏ trúng vào bề mặt hạt châu, giọt máu ấy lập tức bị hạt châu hút sạch không còn một dấu vết!

"Vật này... chẳng lẽ là một món bảo vật do tiền bối tu tiên nào đó đánh rơi?" Vương Lâm thì thào kinh ngạc. Hắn cẩn thận lau khô hạt châu rồi dùng một sợi dây gai buộc chặt đeo vào cổ, giấu kỹ bên trong lớp áo rách.

Sau hai ngày gian nan tìm đường trèo lên khỏi hẻm núi, Vương Lâm trở về điểm hẹn với phụ thân. Nhìn thấy con trai mình toàn thân thương tích rách rưới nhưng bình an trở về, người cha nghèo ôm chầm lấy hắn khóc nức nở, không một lời trách móc chuyện hắn bị môn phái từ chối. Tình phụ tử ấm áp ấy càng hun đúc thêm ngọn lửa ý chí bất khuất trong lòng thiếu niên: hắn nhất định phải trở thành tu tiên giả bằng chính đôi tay và nghị lực của mình!

Nhờ sự can thiệp và van nài bền bỉ của Tứ Thúc, Hằng Nhạc Phái cuối cùng cũng miễn cưỡng cho Vương Lâm ở lại làm một tên tạp dịch đốn củi phụ trách dọn dẹp chuồng ngựa và gánh nước cho các đệ tử chính thức. Dù thân phận thấp hèn như con sâu cái kiến, thường xuyên bị các đệ tử như Vương Trác và Tôn Hạo ức hiếp sỉ nhục, Vương Lâm vẫn cắn răng nhẫn nhục chịu đựng, ngày đêm tìm kiếm cơ hội tiếp cận công pháp tu luyện.

Mỗi ngày hắn phải gánh hàng chục thùng nước từ chân núi lên đỉnh núi, bổ hàng trăm bó củi thiết mộc cứng như đá. Đôi bàn tay thiếu niên nứt nẻ rớm máu trong gió đông buốt giá, nhưng ánh mắt hắn chưa từng tắt đi ngọn lửa kiên trì. Đêm về trong căn chòi rách nát, hắn thắp ngọn đèn dầu lén đọc từng chữ trong cuốn sổ tay nhập môn cơ bản về kinh mạch mà hắn nhặt được.

Một đêm nọ, khi đang ngồi một mình trong căn lều sau núi, Vương Lâm lấy hạt châu màu xám ra ngắm nghía dưới ánh trăng. Hắn thử nhỏ một giọt nước sương đêm lên hạt châu, bỗng nhiên hạt châu khẽ rung lên, một luồng hấp lực vô hình cực mạnh bộc phát kéo trọn vẹn thần thức của hắn vào bên trong!

Khi mở mắt ra, Vương Lâm bàng hoàng nhận thấy mình đang đứng giữa một không gian huyền ảo vô biên vô tận. Bầu trời không có trăng sao mà chỉ có một màn sương mờ phát ra ánh sáng bàng bạc dịu nhẹ, linh khí thiên địa tại nơi này nồng đậm thuần khiết gấp hàng trăm lần so với thế giới bên ngoài.

Càng kinh thiên động địa hơn nữa, khi Vương Lâm ở trong không gian này luyện tập theo những khẩu quyết dẫn khí cơ bản mà hắn lén học lỏm được suốt nhiều canh giờ, đến khi thần thức hắn quay trở lại thế giới thực tại, hắn ngỡ ngàng phát hiện nén nhang trên bàn chỉ mới cháy được một đoạn ngắn ngủi!

"Thời gian... Tỷ lệ thời gian ở trong hạt châu chậm hơn thế giới bên ngoài tới mười lần!" Trái tim Vương Lâm đập liên hồi như muốn vỡ tung vì chấn động. Ở bên ngoài một ngày thì bên trong hạt châu tương đương với mười ngày khổ tu! Hạt châu này chính là một chí bảo nghịch thiên vượt qua mọi quy luật của thiên đạo luân hồi – Thiên Nghịch Châu!

Tư chất hắn kém cỏi? Vậy thì hắn sẽ dùng thời gian gấp mười lần, gấp trăm lần người khác để bù đắp! Người khác tu luyện một năm, hắn ở trong Thiên Nghịch Không Gian tu luyện mười năm! Người khác cần thiên tài địa bảo để đột phá, hắn dùng sự kiên trì và mồ hôi nước mắt bất tận để san bằng mọi cách biệt tư chất.

Hằng đêm, khi vạn vật chìm vào giấc ngủ say, Vương Lâm lại tiến vào không gian Thiên Nghịch Châu để đả tọa hấp thu linh khí. Hắn kiên nhẫn dẫn dắt từng sợi linh khí mỏng manh đả thông ba trăm sáu mươi lăm huyệt đạo đang bế tắc trong cơ thể. Nỗi đau đớn như róc xương xẻ thịt khi phá vỡ kinh mạch không làm hắn chùn bước, ánh mắt hắn chỉ ngày một thêm sắc bén và lạnh lùng.

Sau ba tháng khổ tu trong không gian nghịch thiên tương đương với gần ba năm công phu của phàm nhân, đan điền của Vương Lâm cuối cùng cũng ngưng tụ thành công luồng khí xoáy chân khí đầu tiên, chính thức bước vào Ngưng Khí tầng thứ nhất! Một kỳ tích mà các đệ tử thiên tài của Hằng Nhạc Phái cũng phải mất ít nhất nửa năm mới làm được!

Hắn còn phát hiện nước ngâm hạt châu có thể biến thành linh dịch chữa lành mọi vết thương và gia tăng công lực. Hắn bí mật dùng linh dịch này bồi bổ thân thể, khiến thể chất của mình ngày càng cứng cáp vượt xa đệ tử cùng trang lứa. Từng tế bào, từng thớ cơ bắp của hắn đều được gội rửa bởi tinh hoa linh khí thuần khiết nhất.

Một ngày nọ, tên sư huynh cai quản tạp dịch định đánh đập hắn vô cớ, nhưng khi nắm đấm của gã vừa vung ra, Vương Lâm chỉ khẽ nghiêng người né tránh rồi vung tay đẩy nhẹ, tên sư huynh to lớn đã bị đánh văng xa mấy trượng. Cảnh tượng ấy khiến toàn bộ tạp dịch phòng chết lặng trong kinh hãi. Vương Lâm thu lại ánh mắt sắc bén, tiếp tục cúi đầu gánh nước như một người phàm bình thường. Sự nhẫn nhịn và thâm trầm của hắn đã đạt đến cảnh giới đáng sợ.

Vương Lâm quỳ xuống mặt đất hướng về phía vầng trăng khuyết trên trời cao, ánh mắt hắn từ đây bừng cháy lên ngọn lửa kiên định sắt đá. Một thiếu niên thợ mộc bình thường mang trong mình ý chí nghịch thiên cải mệnh, từ khoảnh khắc này chính thức bước lên con đường tu đạo đẫm máu tàn khốc, mở ra truyền kỳ chấn động Chư Thiên Vạn Giới mang tên Ma Hoàng Tiên Nghịch!

Trong cõi nhân gian bao la vạn dặm, con đường nhân sinh và tu đạo vốn dĩ chưa từng bằng phẳng dễ dàng. Những bậc tiền bối đại năng trong truyền thuyết xưa kia khi đạp mây cưỡi gió, ngạo thị quần hùng đều đã từng phải trải qua vô số năm tháng cơ hàn, nếm trải trăm ngàn cay đắng tủi nhục giữa chốn hồng trần cuồn cuộn. Nhân tâm hiểm ác, thế sự khó lường, kẻ mạnh đứng trên đỉnh cao nhìn xuống thường xem chúng sinh như loài sâu kiến nhỏ nhoi, tùy ý định đoạt vận mệnh của kẻ yếu.

Thế nhưng, chính trong những nghịch cảnh tăm tối nhất của cuộc đời, ý chí bất khuất của con người mới bộc lộ sức mạnh phi thường tiềm ẩn. Dù thể xác có phải chịu đựng đòn roi tra tấn, dù kinh mạch bế tắc hay bị toàn bộ thế giới quay lưng ruồng bỏ, một khi ngọn lửa niềm tin vào chính bản thân mình chưa từng tắt lụi, thì vẫn còn cơ hội để lội ngược dòng nước, nghịch chuyển càn khôn.

Bầu trời đêm sâu thẳm lấp lánh muôn ngàn vì tinh tú xa xôi, mỗi một đốm sáng tượng trưng cho một số phận, một linh hồn đang tranh đấu không ngừng nghỉ để tìm kiếm vị trí của mình trong vũ trụ vô tận. Gió đêm thổi qua những tán cây cổ thụ phát ra âm thanh rì rào như tiếng thở dài của thời gian. Những kỷ nguyên huy hoàng rồi cũng sẽ lụi tàn thành tro bụi, các bậc đế vương bá chủ rồi cũng phải quy về cát bụi thời gian, duy chỉ có những tâm hồn kiên định dũng cảm dám bước đi trên con đường của riêng mình mới có thể lưu danh vĩnh cửu.

Từng dòng suy nghĩ lắng đọng lại trong tâm trí, tựa như dòng suối trong vắt gột rửa đi mọi nỗi hoang mang sợ hãi. Hít một hơi thật sâu để lồng ngực tràn ngập sinh khí của đất trời, ánh mắt hướng về phía chân trời xa xăm nơi ánh bình minh sắp sửa ló rạng xua tan màn đêm tăm tối. Con đường phía trước còn muôn trùng chông gai thử thách, nhưng mỗi một bước chân kiên định được đặt xuống hôm nay sẽ là nền móng vững chắc cho sự quật khởi huy hoàng trong ngày mai.

Vạn vật trong càn khôn đều vận hành theo quy luật tuần hoàn sinh diệt không ngừng nghỉ. Ngày qua đêm tới, hoa nở hoa tàn, cát bụi lại hóa thành non cao biển rộng. Người đi trên con đường nghịch thiên cải mệnh phải giữ cho tâm cảnh phẳng lặng như mặt hồ không một gợn sóng, bất biến giữa vạn biến, lấy tĩnh chế động, lấy nhu thắng cương.

Sức mạnh chân chính không chỉ nằm ở nắm đấm hay vũ lực hủy thiên diệt địa, mà trước hết bắt nguồn từ một trái tim kiên định không gì lay chuyển nổi. Trước nghịch cảnh không bao giờ khuất phục, trước cám dỗ không đánh mất lương tri bản ngã, đó mới chính là bản lĩnh đích thực của bậc kiêu hùng giữa đất trời.

Những gian khổ hôm nay trải qua rồi cũng sẽ hóa thành ngọn đèn soi sáng cho tương lai mịt mùng phía trước. Con đường tu luyện trường sinh vốn dĩ cô độc, ít người thấu hiểu, đòi hỏi một sự nhẫn nại phi thường và khả năng chịu đựng nỗi cô đơn tột cùng. Nhưng khi đã vượt qua được muôn ngàn hiểm hách, đứng trên đỉnh cao ngắm nhìn thiên hạ thái bình, mọi hy sinh mất mát đều sẽ trở nên vô cùng xứng đáng.', 0, 'PUBLISHED', 4000, false)
) AS c(chapter_number, title, content, price, status, views, is_deleted)
ON CONFLICT (novel_id, chapter_number) DO UPDATE
SET title = EXCLUDED.title, content = EXCLUDED.content, price = EXCLUDED.price, status = EXCLUDED.status;

-- ================================================================================
-- Truyện: Già Thiên (gia-thien)
-- ================================================================================
INSERT INTO novels (title, slug, author, summary, cover_url, status, is_deleted, uploader_id, views, average_rating, rating_count)
VALUES (
    'Già Thiên',
    'gia-thien',
    'Thần Đông',
    'Băng lãnh cùng hắc ám cùng tồn tại trong vũ trụ sâu thẳm. Chín con rồng khổng lồ kéo cỗ quan tài đồng từ ngàn vạn năm trước giáng xuống đỉnh Thái Sơn, đưa Diệp Phàm bước vào thế giới hồng hoang tu chân kỳ vĩ...',
    'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=600&q=80',
    'COMPLETED',
    false,
    (SELECT id FROM users WHERE email = 'admin@tramtruyen.com'),
    9500,
    4.96,
    3100
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    author = EXCLUDED.author,
    summary = EXCLUDED.summary,
    cover_url = EXCLUDED.cover_url,
    status = EXCLUDED.status,
    is_deleted = false;

INSERT INTO novel_categories (novel_id, category_id)
SELECT n.id, c.id
FROM novels n
CROSS JOIN categories c
WHERE n.slug = 'gia-thien'
  AND c.slug IN ('tien-hiep', 'huyen-huyen')
ON CONFLICT DO NOTHING;

WITH target_novel AS (
    SELECT id FROM novels WHERE slug = 'gia-thien'
)
INSERT INTO chapters (novel_id, chapter_number, title, content, price, status, views, is_deleted)
SELECT
    tn.id,
    c.chapter_number, c.title, c.content, c.price, c.status, c.views, c.is_deleted
FROM target_novel tn,
(VALUES
(1, 'Cửu Long Kéo Quan', 'Đỉnh Thái Sơn hùng vĩ nguy nga vươn cao chọc trời, mây mù cuồn cuộn bao phủ khắp các rặng núi đá trập trùng. Từ thuở xa xưa của thời kỳ Thượng Cổ Tam Hoàng Ngũ Đế, ngọn núi linh thiêng này đã là nơi các bậc đế vương cử hành đại lễ Phong Thiền tế cáo trời đất, lưu lại vô số truyền thuyết huyền bí về thần tiên và trường sinh bất lão.

Hôm nay, một nhóm bạn trẻ gồm ba mươi cựu sinh viên đại học vừa tốt nghiệp được ba năm đang tổ chức buổi họp mặt kỷ niệm trên đỉnh Ngọc Hoàng Đỉnh của Thái Sơn. Trong số đó có Diệp Phàm – một thanh niên tuấn tú với vóc dáng đĩnh đạc, tính tình trầm ổn điềm tĩnh, yêu thích tìm hiểu những cuốn cổ tịch hoang đường về thời kỳ thần thoại cổ đại.

Buổi họp lớp ban đầu diễn ra trong không khí rôm rả nhưng cũng không thiếu những toan tính so đo vật chất của chốn thương trường hiện đại. Những người bạn như Lưu Vân Chí diện vest bảnh bao lái xe sang đắt tiền không ngừng phô trương gia thế và thành đạt, trong khi những người bạn bình dị như Diệp Phàm hay Bàng Bác chỉ mỉm cười điềm nhiên thưởng thức cảnh đẹp thiên nhiên hùng vĩ.

Diệp Phàm đứng tựa vào lan can đá nhìn xa xăm, trong lòng nhớ lại những ký ức thời sinh viên vô tư lự. Thời gian ba năm trôi qua như bóng câu qua cửa sổ, xã hội kim tiền đã biến đổi nhiều người bạn cũ thành những kẻ toan tính lạnh lùng. Nhưng bên cạnh hắn vẫn còn có Bàng Bác – một người anh em trung kiên, khẳng khái và luôn sẵn sàng xả thân vì bạn bè.

Ánh hoàng hôn buông xuống nhuộm đỏ rực cả biển mây bồng bềnh dưới chân núi, tạo nên một khung cảnh tráng lệ diễm lệ đến nghẹt thở. Mọi người đang vui vẻ chuyện trò ôn lại kỷ niệm thời sinh viên và chụp ảnh lưu niệm thì bỗng nhiên, một hiện tượng dị thường kinh thiên động địa bất ngờ giáng xuống bầu trời.

Bầu trời ban chiều vốn dĩ trong xanh bỗng chốc tối sầm lại như mực. Từng tầng mây đen cuồn cuộn kéo đến che khuất hoàn toàn ánh mặt trời, sấm sét màu tím xé rách không trung rền vang như tiếng gầm của loài cự thú thời tiền sử. Ngay sau đó, nhiệt độ không khí đột ngột giảm xuống mức đóng băng khiến toàn bộ du khách trên đỉnh núi đều run rẩy rùng mình ớn lạnh.

"Nhìn kìa! Trên trời cao... có vật gì đang rơi xuống!" Một cô gái trong nhóm Diệp Phàm bỗng chỉ tay lên không trung hét lên kinh hãi.

Toàn bộ du khách trên đỉnh Thái Sơn đồng loạt ngước nhìn lên cao và chết lặng trong sự kinh hoàng tột độ. Từ sâu thẳm trong vũ trụ tăm tối xuyên qua tầng khí quyển của Trái Đất, chín sinh vật khổng lồ dài hàng trăm trượng đang uốn lượn bay lượn lao thẳng xuống đỉnh núi!

Đó là chín bộ thi thể rồng thần phương Đông bằng xương bằng thịt khổng lồ, toàn thân phủ kín lớp vảy màu đen nhánh phát ra ánh kim loại lạnh buốt, trên đầu mọc đôi sừng hươu uy nghiêm, bốn móng vuốt sắc nhọn vung vẩy giữa tầng mây. Chín bộ long thi khổng lồ ấy đang kéo theo sau một sợi xích sắt to lớn thô ráp, và ở đầu sợi xích sắt chính là một cỗ quan tài bằng đồng xanh cổ kính dài hơn hai mươi trượng!

"Cửu Long Kéo Quan! Trời đất ơi, thần long kéo quan tài từ ngoài vũ trụ giáng trần sao?!" Tiếng la hét hỗn loạn hoảng sợ tột cùng bùng nổ khắp bốn bề. Hàng ngàn du khách giẫm đạp lên nhau chạy tán loạn tìm đường xuống núi.

RẦM! Một tiếng nổ long trời lở đất vang lên khi cỗ quan tài đồng cùng chín bộ long thi khổng lồ đập thẳng xuống đỉnh Ngọc Hoàng Đỉnh của Thái Sơn! Lực va chạm khủng khiếp làm rung chuyển cả dãy núi Thái Sơn, đất đá sạt lở dữ dội, vách đá nứt toác ra những khe rãnh sâu hoắm.

Nơi cỗ quan tài đồng đáp xuống, toàn bộ mặt đất đá hoa cương nứt toác ra, để lộ ra bên dưới một đàn tế cổ xưa hình tròn xếp bằng những phiến ngọc ngũ sắc và những hòn đá tế cổ có khắc họa vô số phù điêu đồ đằng thần bí thời Thượng Cổ. Đàn tế ngũ sắc ấy bỗng nhiên phát ra một luồng ánh sáng rực rỡ kỳ ảo, tạo thành một Đồ Án Thái Cực Bát Quái khổng lồ xoay tròn giữa không trung.

Diệp Phàm cùng nhóm bạn học của mình do đứng quá gần vị trí va chạm nên không kịp chạy thoát, tất cả bị áp lực vô hình từ Đồ Án Bát Quái ép chặt ngã rạp xuống đất, không thể nhúc nhích dù chỉ nửa phân.

Những ký tự cổ đại trên đàn tế bốc cháy như những ngọn lửa thần thánh, không gian xung quanh bị bẻ cong thành những đường vân gợn sóng kỳ dị. Tiếng tụng kinh cổ xưa vang vọng từ đáy sâu cỗ quan tài đồng như tiếng thì thầm của chư thần ngàn năm trước.

Chiếc nắp của cỗ quan tài đồng khổng lồ bỗng nhiên khẽ dịch chuyển lệch sang một bên, phát ra một tiếng kim loại ma sát trầm đục rợn người. Một luồng hấp lực kinh hoàng tựa như hố đen vũ trụ bùng phát từ bên trong cỗ quan tài, cuốn phăng toàn bộ nhóm người Diệp Phàm bay thẳng vào trong lòng bóng tối vô tận của cỗ quan tài đồng!

Diệp Phàm cố gắng nắm lấy tay Bàng Bác và một vài người bạn ở gần, nhưng lực hút của cỗ quan tài đồng quá đỗi khủng khiếp, tựa như một vòng xoáy hư không không thể cưỡng lại. Từng người một lần lượt bị cuốn vào miệng quan tài khổng lồ.

Nắp quan tài rầm một tiếng đóng chặt lại. Đàn tế ngũ sắc bốc cháy rực rỡ, Đồ Án Bát Quái mở ra một tinh môn không thời gian xuyên qua các vì sao. Chín con rồng khổng lồ lại một lần nữa kéo cỗ quan tài đồng bay vút lên bầu trời, lao thẳng vào vũ trụ sâu thẳm, bỏ lại phía sau một Trái Đất đang bàng hoàng rúng động.

Một chuyến hành trình vượt qua ngàn vạn dặm tinh không, đưa Diệp Phàm bước chân vào thế giới tu chân tàn khốc vĩ đại nhất của Già Thiên, chính thức bắt đầu từ khoảnh khắc này.

Trong cõi nhân gian bao la vạn dặm, con đường nhân sinh và tu đạo vốn dĩ chưa từng bằng phẳng dễ dàng. Những bậc tiền bối đại năng trong truyền thuyết xưa kia khi đạp mây cưỡi gió, ngạo thị quần hùng đều đã từng phải trải qua vô số năm tháng cơ hàn, nếm trải trăm ngàn cay đắng tủi nhục giữa chốn hồng trần cuồn cuộn. Nhân tâm hiểm ác, thế sự khó lường, kẻ mạnh đứng trên đỉnh cao nhìn xuống thường xem chúng sinh như loài sâu kiến nhỏ nhoi, tùy ý định đoạt vận mệnh của kẻ yếu.

Thế nhưng, chính trong những nghịch cảnh tăm tối nhất của cuộc đời, ý chí bất khuất của con người mới bộc lộ sức mạnh phi thường tiềm ẩn. Dù thể xác có phải chịu đựng đòn roi tra tấn, dù kinh mạch bế tắc hay bị toàn bộ thế giới quay lưng ruồng bỏ, một khi ngọn lửa niềm tin vào chính bản thân mình chưa từng tắt lụi, thì vẫn còn cơ hội để lội ngược dòng nước, nghịch chuyển càn khôn.

Bầu trời đêm sâu thẳm lấp lánh muôn ngàn vì tinh tú xa xôi, mỗi một đốm sáng tượng trưng cho một số phận, một linh hồn đang tranh đấu không ngừng nghỉ để tìm kiếm vị trí của mình trong vũ trụ vô tận. Gió đêm thổi qua những tán cây cổ thụ phát ra âm thanh rì rào như tiếng thở dài của thời gian. Những kỷ nguyên huy hoàng rồi cũng sẽ lụi tàn thành tro bụi, các bậc đế vương bá chủ rồi cũng phải quy về cát bụi thời gian, duy chỉ có những tâm hồn kiên định dũng cảm dám bước đi trên con đường của riêng mình mới có thể lưu danh vĩnh cửu.

Từng dòng suy nghĩ lắng đọng lại trong tâm trí, tựa như dòng suối trong vắt gột rửa đi mọi nỗi hoang mang sợ hãi. Hít một hơi thật sâu để lồng ngực tràn ngập sinh khí của đất trời, ánh mắt hướng về phía chân trời xa xăm nơi ánh bình minh sắp sửa ló rạng xua tan màn đêm tăm tối. Con đường phía trước còn muôn trùng chông gai thử thách, nhưng mỗi một bước chân kiên định được đặt xuống hôm nay sẽ là nền móng vững chắc cho sự quật khởi huy hoàng trong ngày mai.

Vạn vật trong càn khôn đều vận hành theo quy luật tuần hoàn sinh diệt không ngừng nghỉ. Ngày qua đêm tới, hoa nở hoa tàn, cát bụi lại hóa thành non cao biển rộng. Người đi trên con đường nghịch thiên cải mệnh phải giữ cho tâm cảnh phẳng lặng như mặt hồ không một gợn sóng, bất biến giữa vạn biến, lấy tĩnh chế động, lấy nhu thắng cương.

Sức mạnh chân chính không chỉ nằm ở nắm đấm hay vũ lực hủy thiên diệt địa, mà trước hết bắt nguồn từ một trái tim kiên định không gì lay chuyển nổi. Trước nghịch cảnh không bao giờ khuất phục, trước cám dỗ không đánh mất lương tri bản ngã, đó mới chính là bản lĩnh đích thực của bậc kiêu hùng giữa đất trời.

Những gian khổ hôm nay trải qua rồi cũng sẽ hóa thành ngọn đèn soi sáng cho tương lai mịt mùng phía trước. Con đường tu luyện trường sinh vốn dĩ cô độc, ít người thấu hiểu, đòi hỏi một sự nhẫn nại phi thường và khả năng chịu đựng nỗi cô đơn tột cùng. Nhưng khi đã vượt qua được muôn ngàn hiểm hách, đứng trên đỉnh cao ngắm nhìn thiên hạ thái bình, mọi hy sinh mất mát đều sẽ trở nên vô cùng xứng đáng.

Từng giọt mồ hôi rơi xuống thấm sâu vào lòng đất mẹ cằn cỗi, như nuôi dưỡng hạt mầm hy vọng đang âm thầm đâm chồi nảy lộc. Không có thành công nào đến sau một đêm, không có vinh quang nào không phải trả giá bằng máu và nước mắt. Thiếu niên mang trong mình trái tim nóng bỏng và hoài bão lớn lao, quyết tâm khắc ghi từng bài học đắt giá vào tâm khảm để không bao giờ lặp lại sai lầm trong quá khứ.

Thời gian là dòng chảy vô tình cuốn trôi tất cả, nhưng cũng là thước đo chuẩn xác nhất thử thách phẩm chất và nghị lực của con người. Những kẻ yếu đuối sẽ bị sóng gió cuộc đời vùi dập và đào thải không thương tiếc, chỉ có những bậc kiên cường mới có thể sừng sững đứng vững tựa như ngọn núi Thái Sơn trước phong ba bão táp.

Trời đất bao la, đạo pháp vô biên, mỗi một sinh mệnh sinh ra đều mang trong mình một sứ mệnh và một ý nghĩa thiêng liêng nhất định. Đừng bao giờ tự ti trước sự nhỏ bé của bản thân, bởi vì một đốm lửa nhỏ cũng đủ sức thiêu rụi cả thảo nguyên bao la khi gió đông nổi lên.', 0, 'PUBLISHED', 4300, false),
(2, 'Hoang Cổ Cấm Địa', 'Bên trong cỗ quan tài bằng đồng xanh cổ kính là một không gian tối tăm như hũ nút và lạnh buốt đến thấu xương. Tiếng khóc lóc, tiếng cầu nguyện và tiếng rên xiết hoảng loạn của các bạn học vang lên hỗn loạn không ngớt. Mọi người dùng ánh sáng yếu ớt từ màn hình điện thoại di động để quan sát xung quanh, kinh hãi nhận thấy bên trong cỗ quan tài đồng khổng lồ này còn chứa một chiếc tiểu quan tài bằng đồng khác nhỏ hơn, trên vách quan tài chạm khắc vô số bức tranh miêu tả các vị thần cổ đại và các loài hung thú hồng hoang.

Diệp Phàm cố gắng trấn an tinh thần của mọi người, cùng một người bạn thân tên là Bàng Bác – một thanh niên to lớn vạm vỡ, trượng nghĩa và dũng cảm – đứng ra tổ chức lại nhóm để cùng nhau tìm cách sinh tồn. Trong cỗ quan tài đồng, Diệp Phàm tìm thấy một chiếc Đèn Đồng Cổ thời Thượng Cổ đang phát ra một tia sáng yếu ớt ấm áp, xua tan cái lạnh giá của vũ trụ.

Sau một hành trình bay lượn dài đằng đẵng xuyên qua các lỗ sâu không gian và đáp tạm thời xuống Huỳnh Hoặc Cổ Tinh (sao Hỏa) đầy rẫy hiểm nguy nơi họ phải chiến đấu sinh tử với loài yêu thú ngầm Đại Lực Ngạc Tổ, cỗ Cửu Long Đồng Quan cuối cùng cũng lao thẳng đến điểm đến định mệnh cuối cùng.

Tại sao Hỏa hoang vu cằn cỗi đầy cát đỏ, họ phát hiện phế tích Đại Lôi Âm Tự cổ xưa của Đức Phật, thu thập được những món pháp bảo phật môn như Kim Cương Chẩm, Đồng Chung và Tích Trượng để chống lại bầy ngạc thú hung hãn trước khi kịp chạy thoát trở lại quan tài. Những pháp bảo cổ xưa ấy đã cứu sống họ trong gang tấc khi bầy tiểu ngạc gào thét xé xác một vài người bạn bất cẩn.

RẦM! Một chấn động kinh hoàng hất văng tất cả mọi người ngã lăn lóc. Nắp quan tài đồng lại một lần nữa mở toang ra, để lộ ánh sáng mặt trời rực rỡ chiếu rọi vào bên trong. Diệp Phàm cùng các bạn học vội vã bò ra khỏi cỗ quan tài, hít thở từng ngụm không khí trong lành tràn ngập linh khí thiên địa.

Cảnh tượng đập vào mắt họ là một thế giới hoàn toàn xa lạ và kỳ vĩ vượt xa mọi tưởng tượng của nhân loại trên Trái Đất. Bầu trời xanh biếc thăm thẳm với hai vầng mặt trời rực rỡ, những dãy núi khổng lồ cao chọc trời phủ kín những cánh rừng nguyên sinh bạt ngàn với những cây cổ thụ cao hàng trăm trượng. Khí tức hoang dã cổ xưa tràn ngập khắp đất trời.

Họ đã đặt chân lên Tinh Đồ Bắc Đẩu – một đại lục tu tiên vô biên vô tận nằm ở nơi sâu thẳm của dải ngân hà!

Nơi họ vừa đáp xuống chính là đỉnh của một ngọn núi đá kỳ dị nằm trong khu vực cấm kỵ đáng sợ nhất của đại lục: Hoang Cổ Cấm Địa. Tại đây, thời gian và không gian bị bóp méo bởi quy luật của Hoang Cổ Tuế Nguyệt.

Đói khát và kiệt sức sau chuyến bay dài xuyên tinh không, Diệp Phàm và Bàng Bác phát hiện bên cạnh một con suối nhỏ trên đỉnh núi có một cây thần thụ nhỏ bé trĩu quả. Cây chỉ cao chừng nửa người nhưng trên cành kết mười mấy quả trái cây màu đỏ rực lấp lánh như mã não, tỏa ra mùi hương thơm ngát mê hồn.

Diệp Phàm và Bàng Bác hái những quả thần thụ chia cho nhau ăn lót dạ, dòng nước ngọt lịm vừa trôi xuống bụng đã lập tức hóa thành những dòng nhiệt lưu cuồn cuộn cọ rửa toàn bộ kinh mạch và lục phủ ngũ tạng của họ. Đó chính là Cửu Diệu Bất Tử Dược thần thánh trong truyền thuyết!

Họ còn múc nước suối thần thánh cho những người bạn khác cùng uống để lấy lại sinh lực sau những ngày dài đói lả. Dòng suối lấp lánh ánh hoàng kim tỏa ra mùi hương thanh khiết tuyệt trần.

Khi nhóm người tiếp tục đi bộ tìm đường thoát khỏi khu rừng hoang, một biến cố kinh hoàng về thời gian bỗng nhiên ập đến. Sức mạnh nguyền rủa Tuế Nguyệt của Hoang Cổ Cấm Địa bắt đầu phát tác: tóc của tất cả các bạn học bắt đầu bạc trắng với tốc độ chóng mặt, da thịt nhăn nheo, lưng còng xuống và biến thành những ông bà lão già nua gần đất xa trời chỉ sau vài bước chân!

Tiếng gào khóc thảm thiết vang vọng khắp cánh rừng nguyên sinh. Những cô gái trẻ đẹp vừa mới đây còn thanh xuân phơi phới nay đã biến thành những bà cụ hom hem rụng hết răng, những chàng trai vạm vỡ nay biến thành những ông lão run rẩy không bước nổi.

Duy chỉ có Diệp Phàm và Bàng Bác – nhờ đã ăn trái Cửu Diệu Thần Quả – không những không bị già đi mà cơ thể còn trải qua quá trình nghịch chuyển sinh mệnh kỳ diệu: thân hình họ thu nhỏ lại thành những thiếu niên mười hai, mười ba tuổi với làn da trắng mịn màng và sinh lực tràn trề sung mãn!

Bước chân ra khỏi Hoang Cổ Cấm Địa, Diệp Phàm chính thức bước vào thế giới tu chân Bắc Đẩu đầy rẫy sự tranh đoạt tàn khốc giữa các Thánh Địa và Hoang Cổ Thế Gia. Mang trong mình thể chất Hoang Cổ Thánh Thể bị coi là phế thể thời mạt pháp, thiếu niên Diệp Phàm với ý chí bất khuất tuyên bố:

"Dù trời muốn diệt ta, ta cũng phải phá trời mà đi! Con đường chứng đạo Thiên Đế của Diệp Phàm ta bắt đầu từ hôm nay!"

Trong cõi nhân gian bao la vạn dặm, con đường nhân sinh và tu đạo vốn dĩ chưa từng bằng phẳng dễ dàng. Những bậc tiền bối đại năng trong truyền thuyết xưa kia khi đạp mây cưỡi gió, ngạo thị quần hùng đều đã từng phải trải qua vô số năm tháng cơ hàn, nếm trải trăm ngàn cay đắng tủi nhục giữa chốn hồng trần cuồn cuộn. Nhân tâm hiểm ác, thế sự khó lường, kẻ mạnh đứng trên đỉnh cao nhìn xuống thường xem chúng sinh như loài sâu kiến nhỏ nhoi, tùy ý định đoạt vận mệnh của kẻ yếu.

Thế nhưng, chính trong những nghịch cảnh tăm tối nhất của cuộc đời, ý chí bất khuất của con người mới bộc lộ sức mạnh phi thường tiềm ẩn. Dù thể xác có phải chịu đựng đòn roi tra tấn, dù kinh mạch bế tắc hay bị toàn bộ thế giới quay lưng ruồng bỏ, một khi ngọn lửa niềm tin vào chính bản thân mình chưa từng tắt lụi, thì vẫn còn cơ hội để lội ngược dòng nước, nghịch chuyển càn khôn.

Bầu trời đêm sâu thẳm lấp lánh muôn ngàn vì tinh tú xa xôi, mỗi một đốm sáng tượng trưng cho một số phận, một linh hồn đang tranh đấu không ngừng nghỉ để tìm kiếm vị trí của mình trong vũ trụ vô tận. Gió đêm thổi qua những tán cây cổ thụ phát ra âm thanh rì rào như tiếng thở dài của thời gian. Những kỷ nguyên huy hoàng rồi cũng sẽ lụi tàn thành tro bụi, các bậc đế vương bá chủ rồi cũng phải quy về cát bụi thời gian, duy chỉ có những tâm hồn kiên định dũng cảm dám bước đi trên con đường của riêng mình mới có thể lưu danh vĩnh cửu.

Từng dòng suy nghĩ lắng đọng lại trong tâm trí, tựa như dòng suối trong vắt gột rửa đi mọi nỗi hoang mang sợ hãi. Hít một hơi thật sâu để lồng ngực tràn ngập sinh khí của đất trời, ánh mắt hướng về phía chân trời xa xăm nơi ánh bình minh sắp sửa ló rạng xua tan màn đêm tăm tối. Con đường phía trước còn muôn trùng chông gai thử thách, nhưng mỗi một bước chân kiên định được đặt xuống hôm nay sẽ là nền móng vững chắc cho sự quật khởi huy hoàng trong ngày mai.

Vạn vật trong càn khôn đều vận hành theo quy luật tuần hoàn sinh diệt không ngừng nghỉ. Ngày qua đêm tới, hoa nở hoa tàn, cát bụi lại hóa thành non cao biển rộng. Người đi trên con đường nghịch thiên cải mệnh phải giữ cho tâm cảnh phẳng lặng như mặt hồ không một gợn sóng, bất biến giữa vạn biến, lấy tĩnh chế động, lấy nhu thắng cương.

Sức mạnh chân chính không chỉ nằm ở nắm đấm hay vũ lực hủy thiên diệt địa, mà trước hết bắt nguồn từ một trái tim kiên định không gì lay chuyển nổi. Trước nghịch cảnh không bao giờ khuất phục, trước cám dỗ không đánh mất lương tri bản ngã, đó mới chính là bản lĩnh đích thực của bậc kiêu hùng giữa đất trời.

Những gian khổ hôm nay trải qua rồi cũng sẽ hóa thành ngọn đèn soi sáng cho tương lai mịt mùng phía trước. Con đường tu luyện trường sinh vốn dĩ cô độc, ít người thấu hiểu, đòi hỏi một sự nhẫn nại phi thường và khả năng chịu đựng nỗi cô đơn tột cùng. Nhưng khi đã vượt qua được muôn ngàn hiểm hách, đứng trên đỉnh cao ngắm nhìn thiên hạ thái bình, mọi hy sinh mất mát đều sẽ trở nên vô cùng xứng đáng.

Từng giọt mồ hôi rơi xuống thấm sâu vào lòng đất mẹ cằn cỗi, như nuôi dưỡng hạt mầm hy vọng đang âm thầm đâm chồi nảy lộc. Không có thành công nào đến sau một đêm, không có vinh quang nào không phải trả giá bằng máu và nước mắt. Thiếu niên mang trong mình trái tim nóng bỏng và hoài bão lớn lao, quyết tâm khắc ghi từng bài học đắt giá vào tâm khảm để không bao giờ lặp lại sai lầm trong quá khứ.

Thời gian là dòng chảy vô tình cuốn trôi tất cả, nhưng cũng là thước đo chuẩn xác nhất thử thách phẩm chất và nghị lực của con người. Những kẻ yếu đuối sẽ bị sóng gió cuộc đời vùi dập và đào thải không thương tiếc, chỉ có những bậc kiên cường mới có thể sừng sững đứng vững tựa như ngọn núi Thái Sơn trước phong ba bão táp.

Trời đất bao la, đạo pháp vô biên, mỗi một sinh mệnh sinh ra đều mang trong mình một sứ mệnh và một ý nghĩa thiêng liêng nhất định. Đừng bao giờ tự ti trước sự nhỏ bé của bản thân, bởi vì một đốm lửa nhỏ cũng đủ sức thiêu rụi cả thảo nguyên bao la khi gió đông nổi lên.

Bước chân vững chãi tiến về phía trước, không quay đầu lại, không hối tiếc những gì đã qua. Phía trước là chân trời rộng mở, là biển rộng trời cao đang vẫy gọi những cánh chim dũng cảm dám vỗ cánh bay ngược chiều bão táp.

Thiên địa này vốn dĩ là một ván cờ lớn vô biên vô tận, nơi chư thần cùng ma quỷ mượn chúng sinh làm quân cờ mua vui. Nhưng ta sinh ra ở đời, há chịu cam tâm làm một con tốt thí ngoan ngoãn bị kẻ khác giật dây? Dù là phàm nhân thân phận thấp hèn, ta cũng quyết chí cầm cờ, tự tay khắc ghi vận mệnh của chính mình lên vách đá thời gian.', 0, 'PUBLISHED', 4200, false)
) AS c(chapter_number, title, content, price, status, views, is_deleted)
ON CONFLICT (novel_id, chapter_number) DO UPDATE
SET title = EXCLUDED.title, content = EXCLUDED.content, price = EXCLUDED.price, status = EXCLUDED.status;

-- ================================================================================
-- Truyện: Mục Thần Ký (muc-than-ky)
-- ================================================================================
INSERT INTO novels (title, slug, author, summary, cover_url, status, is_deleted, uploader_id, views, average_rating, rating_count)
VALUES (
    'Mục Thần Ký',
    'muc-than-ky',
    'Trạch Trư',
    'Đại Khư tàn tích, đêm tối chớ ra ngoài. Thiếu niên Tần Mục được chín dị nhân tàn tật nhặt về nuôi nấng trong Tàn Lão Thôn, tuy là phàm thể nhưng mang danh Bá Thể vạn cổ vô song, quật khởi giữa trời đất...',
    'https://images.unsplash.com/photo-1514539079130-25950c84af65?auto=format&fit=crop&w=600&q=80',
    'COMPLETED',
    false,
    (SELECT id FROM users WHERE email = 'admin@tramtruyen.com'),
    8900,
    4.93,
    2600
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    author = EXCLUDED.author,
    summary = EXCLUDED.summary,
    cover_url = EXCLUDED.cover_url,
    status = EXCLUDED.status,
    is_deleted = false;

INSERT INTO novel_categories (novel_id, category_id)
SELECT n.id, c.id
FROM novels n
CROSS JOIN categories c
WHERE n.slug = 'muc-than-ky'
  AND c.slug IN ('huyen-huyen', 'tien-hiep')
ON CONFLICT DO NOTHING;

WITH target_novel AS (
    SELECT id FROM novels WHERE slug = 'muc-than-ky'
)
INSERT INTO chapters (novel_id, chapter_number, title, content, price, status, views, is_deleted)
SELECT
    tn.id,
    c.chapter_number, c.title, c.content, c.price, c.status, c.views, c.is_deleted
FROM target_novel tn,
(VALUES
(1, 'Đại Khư Tàn Lão Thôn', 'Màn đêm buông xuống trên Đại Khư hoang dã mang theo nỗi kinh hoàng tuyệt đối của bóng tối diệt vong. Tại mảnh đất bị nguyền rủa này, có một quy tắc sinh tồn bất di bất dịch được khắc sâu vào máu tủy của vạn vật: khi trời tối, bóng tối sẽ nuốt chửng tất cả những sinh linh không kịp tìm thấy ánh sáng, biến họ thành những bộ xương khô hoặc những quái vật dị dạng gào thét trong đêm.

Nơi duy nhất an toàn giữa bóng tối Đại Khư chính là những thôn làng có tượng thần cổ xưa bảo hộ. Tàn Lão Thôn là một ngôi làng kỳ dị như thế. Ngôi làng nhỏ bé này nằm nép mình bên bờ sông Dũng Giang cuồn cuộn sóng dữ, được bốn bức tượng đá hình thần thú cổ xưa trấn giữ bốn góc, tỏa ra ánh sáng mờ ảo xua tan bóng tối hắc ám bên ngoài.

Người dân sống trong Tàn Lão Thôn đều là những dị nhân tàn tật quái đản mang trong mình những bí mật kinh thiên động địa: Thôn Trưởng là một lão già mất cả hai tay lẫn hai chân chỉ có thể ngồi trên chiếc giỏ tre; Tư Bà Bà là một bà lão may vá tính tình thất thường; Mã Gia là một hòa thượng cụt một cánh tay chuyên luyện quyền pháp; Dược Sư là một kẻ bị rạch nát toàn bộ khuôn mặt; Mù Lòa là một lão già bị móc mất đôi mắt nhưng lại nhìn thấu vạn vật; Câm Điếc là một gã thợ rèn câm chuyên đúc binh khí thần thánh...

Người bình thường duy nhất trong ngôi làng ấy chính là Tần Mục – một thiếu niên mười lăm tuổi khôi ngô tuấn tú, mắt sáng như sao, được Tư Bà Bà nhặt về từ một chiếc nôi trôi dạt trên dòng sông Dũng Giang vào một đêm bão tuyết mười lăm năm trước.

Tần Mục lớn lên dưới sự nuôi nấng và dạy dỗ kỳ quái của đám người tàn tật. Tư Bà Bà dạy hắn thuật lột da và may vá y phục thần diệu; Mã Gia dạy hắn Lôi Âm Bát Thức quyền pháp cương mãnh vô song; Dược Sư dạy hắn nếm thử vạn loại độc thảo và luyện chế linh đan; Câm Điếc dạy hắn rèn sắt gõ búa thâu đêm; còn Mù Lòa dạy hắn Thần Nhãn Cửu Cực nhìn thấu hư không; Què Lão Đầu dạy hắn Thần Thâu Đoạt Mệnh Thối cước pháp nhanh như chớp giật; Đồ Phu cụt nửa thân dưới dạy hắn Mổ Lợn Đao Pháp cuồng bạo vô song.

Mỗi ngày từ khi gà gáy sáng, Tần Mục đã thức dậy vác tảng đá nặng hàng trăm cân chạy quanh bờ sông để rèn luyện gân cốt. Hắn đấm quyền vào thân cây cổ thụ khiến lá rụng như mưa, nhảy qua những mỏm đá cheo leo giữa dòng nước xiết như loài vượn núi hoang dã. Thân thể thiếu niên săn chắc như đúc bằng thép nguội, ánh mắt tràn đầy vẻ tinh anh và hào sảng hiếm thấy.

Những lão nhân trong làng dù ngoài mặt nghiêm khắc nhưng luôn dành cho Tần Mục sự yêu thương và che chở vô bờ bến. Họ dốc hết tuyệt học cả đời truyền thụ cho hắn, mong muốn một ngày nào đó hắn có thể ngẩng cao đầu bước ra khỏi Đại Khư tàn khốc.

Hôm nay, Tần Mục cùng Tư Bà Bà ra ngoài bờ sông hái thuốc trước khi bóng tối buông xuống. Dọc theo triền sông, những đóa U Linh Hoa màu tím biếc nở rộ ven bờ, tỏa ra mùi hương ma quái. Bỗng nhiên, từ trong khu rừng rậm rạp phía đối diện, một con cự thú gầm lên kinh thiên động địa. Đó là một con Ma Hổ hai đầu khổng lồ cao hơn một trượng, toàn thân phủ kín vảy gai đen nhánh, bốn mắt đỏ ngầu như máu đang nhe nanh giương vuốt lao thẳng về phía hai người!

"Mục nhi, cẩn thận! Mau lùi lại!" Tư Bà Bà khẽ quát lên, tay cầm cây kim thêu bằng bạc toan ra tay trừng trị nghiệt súc.

Thế nhưng, Tần Mục không hề lùi bước. Thiếu niên hít một hơi sâu, hai chân chùng xuống đạp nứt mặt đất bùn lầy, khí huyết trong lồng ngực sôi trào cuộn sóng tựa như tiếng sấm rền vang giữa mùa hạ. Hắn nhớ lại từng lời chỉ điểm của Mã Gia về chân lý của quyền pháp:

"Quyền giả, dũng dã! Lòng không sợ hãi, một quyền có thể chẻ đôi núi sông!"

Vút! Thân hình Tần Mục lao vút ra như một mũi tên xé gió. Hắn vung nắm đấm phải tung ra chiêu thức đệ nhất của Lôi Âm Bát Thức: Hổ Thiền Nhất Quyền! Một tiếng nổ chói tai vang lên giữa không trung kèm theo tiếng sấm sét hư ảo giáng thẳng vào giữa trán con Ma Hổ hai đầu.

RẮC! Hộp sọ cứng như sắt thép của con cự thú vỡ vụn dưới cú đấm kinh hoàng của thiếu niên. Thân xác to lớn như quả đồi của Ma Hổ bay ngược ra sau mười mấy trượng rồi đổ ầm xuống dòng sông, nhuộm đỏ cả một vùng nước xiết!

Tư Bà Bà nhìn thiếu niên đứng sừng sững bên bờ sông với nắm đấm còn bốc lên làn khói trắng mờ ảo, trong mắt bà lão lóe lên vẻ tự hào xen lẫn xúc động khôn nguôi. Đứa trẻ mà bà nhặt được năm xưa nay đã lớn khôn, mang trong mình sức mạnh kinh người đủ sức khuấy đảo cả thiên hạ.

Nhưng bóng tối của Đại Khư đang từ từ bò lan đến từ phía chân trời, che khuất những rặng núi xa xăm. Tần Mục vác thi thể con cự thú lên vai, cùng Tư Bà Bà rảo bước nhanh chóng trở về ngôi làng có những tượng thần đang bắt đầu phát sáng rực rỡ.

Trong cõi nhân gian bao la vạn dặm, con đường nhân sinh và tu đạo vốn dĩ chưa từng bằng phẳng dễ dàng. Những bậc tiền bối đại năng trong truyền thuyết xưa kia khi đạp mây cưỡi gió, ngạo thị quần hùng đều đã từng phải trải qua vô số năm tháng cơ hàn, nếm trải trăm ngàn cay đắng tủi nhục giữa chốn hồng trần cuồn cuộn. Nhân tâm hiểm ác, thế sự khó lường, kẻ mạnh đứng trên đỉnh cao nhìn xuống thường xem chúng sinh như loài sâu kiến nhỏ nhoi, tùy ý định đoạt vận mệnh của kẻ yếu.

Thế nhưng, chính trong những nghịch cảnh tăm tối nhất của cuộc đời, ý chí bất khuất của con người mới bộc lộ sức mạnh phi thường tiềm ẩn. Dù thể xác có phải chịu đựng đòn roi tra tấn, dù kinh mạch bế tắc hay bị toàn bộ thế giới quay lưng ruồng bỏ, một khi ngọn lửa niềm tin vào chính bản thân mình chưa từng tắt lụi, thì vẫn còn cơ hội để lội ngược dòng nước, nghịch chuyển càn khôn.

Bầu trời đêm sâu thẳm lấp lánh muôn ngàn vì tinh tú xa xôi, mỗi một đốm sáng tượng trưng cho một số phận, một linh hồn đang tranh đấu không ngừng nghỉ để tìm kiếm vị trí của mình trong vũ trụ vô tận. Gió đêm thổi qua những tán cây cổ thụ phát ra âm thanh rì rào như tiếng thở dài của thời gian. Những kỷ nguyên huy hoàng rồi cũng sẽ lụi tàn thành tro bụi, các bậc đế vương bá chủ rồi cũng phải quy về cát bụi thời gian, duy chỉ có những tâm hồn kiên định dũng cảm dám bước đi trên con đường của riêng mình mới có thể lưu danh vĩnh cửu.

Từng dòng suy nghĩ lắng đọng lại trong tâm trí, tựa như dòng suối trong vắt gột rửa đi mọi nỗi hoang mang sợ hãi. Hít một hơi thật sâu để lồng ngực tràn ngập sinh khí của đất trời, ánh mắt hướng về phía chân trời xa xăm nơi ánh bình minh sắp sửa ló rạng xua tan màn đêm tăm tối. Con đường phía trước còn muôn trùng chông gai thử thách, nhưng mỗi một bước chân kiên định được đặt xuống hôm nay sẽ là nền móng vững chắc cho sự quật khởi huy hoàng trong ngày mai.

Vạn vật trong càn khôn đều vận hành theo quy luật tuần hoàn sinh diệt không ngừng nghỉ. Ngày qua đêm tới, hoa nở hoa tàn, cát bụi lại hóa thành non cao biển rộng. Người đi trên con đường nghịch thiên cải mệnh phải giữ cho tâm cảnh phẳng lặng như mặt hồ không một gợn sóng, bất biến giữa vạn biến, lấy tĩnh chế động, lấy nhu thắng cương.

Sức mạnh chân chính không chỉ nằm ở nắm đấm hay vũ lực hủy thiên diệt địa, mà trước hết bắt nguồn từ một trái tim kiên định không gì lay chuyển nổi. Trước nghịch cảnh không bao giờ khuất phục, trước cám dỗ không đánh mất lương tri bản ngã, đó mới chính là bản lĩnh đích thực của bậc kiêu hùng giữa đất trời.

Những gian khổ hôm nay trải qua rồi cũng sẽ hóa thành ngọn đèn soi sáng cho tương lai mịt mùng phía trước. Con đường tu luyện trường sinh vốn dĩ cô độc, ít người thấu hiểu, đòi hỏi một sự nhẫn nại phi thường và khả năng chịu đựng nỗi cô đơn tột cùng. Nhưng khi đã vượt qua được muôn ngàn hiểm hách, đứng trên đỉnh cao ngắm nhìn thiên hạ thái bình, mọi hy sinh mất mát đều sẽ trở nên vô cùng xứng đáng.

Từng giọt mồ hôi rơi xuống thấm sâu vào lòng đất mẹ cằn cỗi, như nuôi dưỡng hạt mầm hy vọng đang âm thầm đâm chồi nảy lộc. Không có thành công nào đến sau một đêm, không có vinh quang nào không phải trả giá bằng máu và nước mắt. Thiếu niên mang trong mình trái tim nóng bỏng và hoài bão lớn lao, quyết tâm khắc ghi từng bài học đắt giá vào tâm khảm để không bao giờ lặp lại sai lầm trong quá khứ.

Thời gian là dòng chảy vô tình cuốn trôi tất cả, nhưng cũng là thước đo chuẩn xác nhất thử thách phẩm chất và nghị lực của con người. Những kẻ yếu đuối sẽ bị sóng gió cuộc đời vùi dập và đào thải không thương tiếc, chỉ có những bậc kiên cường mới có thể sừng sững đứng vững tựa như ngọn núi Thái Sơn trước phong ba bão táp.

Trời đất bao la, đạo pháp vô biên, mỗi một sinh mệnh sinh ra đều mang trong mình một sứ mệnh và một ý nghĩa thiêng liêng nhất định. Đừng bao giờ tự ti trước sự nhỏ bé của bản thân, bởi vì một đốm lửa nhỏ cũng đủ sức thiêu rụi cả thảo nguyên bao la khi gió đông nổi lên.

Bước chân vững chãi tiến về phía trước, không quay đầu lại, không hối tiếc những gì đã qua. Phía trước là chân trời rộng mở, là biển rộng trời cao đang vẫy gọi những cánh chim dũng cảm dám vỗ cánh bay ngược chiều bão táp.

Thiên địa này vốn dĩ là một ván cờ lớn vô biên vô tận, nơi chư thần cùng ma quỷ mượn chúng sinh làm quân cờ mua vui. Nhưng ta sinh ra ở đời, há chịu cam tâm làm một con tốt thí ngoan ngoãn bị kẻ khác giật dây? Dù là phàm nhân thân phận thấp hèn, ta cũng quyết chí cầm cờ, tự tay khắc ghi vận mệnh của chính mình lên vách đá thời gian.', 0, 'PUBLISHED', 3900, false),
(2, 'Bá Thể Tam Đan Công', 'Bên trong Tàn Lão Thôn, ngọn lửa trại bập bùng xua tan cái lạnh giá của màn đêm Đại Khư. Những bức tượng thần bốn góc làng phát ra luồng sáng màu vàng nhạt ấm áp, tạo thành một lồng bảo hộ ngăn cách hoàn toàn với những tiếng gầm rú ghê rợn của đám ma quái đang vây quanh bên ngoài ranh giới bóng tối.

Tối nay là một đêm trọng đại đối với Tần Mục: Thôn Trưởng và Dược Sư sẽ tiến hành lễ giác tỉnh Linh Thai cho hắn. Ở thế giới bên ngoài, bất kỳ phàm nhân nào muốn bước lên con đường tu luyện thần thông đều phải giác tỉnh được một trong bốn đại Linh Thai tự nhiên: Thanh Long, Bạch Hổ, Chu Tước hoặc Huyền Vũ. Người không giác tỉnh được Linh Thai thì suốt đời chỉ là một phàm nhân phế vật, không thể nào ngưng tụ thần thức tu luyện nguyên khí.

Thôn Trưởng – ngồi trên chiếc giỏ tre cũ kỹ – đặt bàn tay gầy gò cụt ngón lên đỉnh đầu Tần Mục, vận chuyển một đạo huyền khí ôn nhu truyền sâu vào đan điền của thiếu niên để dò xét linh căn thiên bẩm. Dược Sư và Tư Bà Bà cùng những người khác đứng xung quanh, ánh mắt ai nấy đều lộ rõ vẻ căng thẳng âu lo.

Thời gian trôi qua chầm chậm. Sau một hồi lâu trầm mặc, Thôn Trưởng từ từ thu tay về, thở dài một hơi đầy nặng nề. Ánh mắt của vị lão nhân già nua lộ rõ vẻ tiếc nuối tột cùng:

"Không có Thanh Long... Không có Bạch Hổ... Cũng không có Chu Tước hay Huyền Vũ. Đan điền của Mục nhi hỗn độn tối tăm, kinh mạch không thuộc bất kỳ hệ thống ngũ hành nào. Nó là... Phàm Thể. Một phàm thể bình thường nhất, vĩnh viễn không thể giác tỉnh Linh Thai."

Lời phán quyết của Thôn Trưởng như một gáo nước lạnh tạt thẳng vào hy vọng của mọi người. Tư Bà Bà nước mắt lưng tròng ôm chặt lấy thiếu niên vào lòng, Mã Gia lắc đầu thở dài quay mặt đi, còn Dược Sư thì bóp nát chén thuốc trong tay vì phẫn uất trước sự trớ trêu của tạo hóa.

Tần Mục đứng chết lặng giữa sân viện, hai mắt cay xè. Hắn đã khổ luyện suốt mười lăm năm qua, chịu đựng biết bao đòn roi và thử thách khắc nghiệt, chẳng lẽ cuối cùng lại chỉ là một phế vật không thể tu luyện nguyên khí sao?

Thế nhưng, nhìn thấy ánh mắt tuyệt vọng của thiếu niên, Thôn Trưởng bỗng nhiên cười lớn một tràng sảng khoái vang vọng khắp gian nhà:

"Các ngươi thật là hồ đồ! Ai bảo Phàm Thể là phế vật?! Năm xưa khi Thần Ma đại chiến khai thiên lập địa, trên đời vốn dĩ chưa từng tồn tại bốn đại Linh Thai! Bốn đại Linh Thai chỉ là chi lưu hạ phẩm, còn loại thể chất chí cao vô thượng nhất, thống ngự vạn vật càn khôn chính là... Bá Thể! Vạn cổ đệ nhất Bá Thể!"

Thôn Trưởng nhìn thẳng vào đôi mắt đang ngơ ngác của Tần Mục, giọng nói đanh thép đầy hào khí:

"Mục nhi! Đan điền của con không phải là phế thể, mà là Thần Ma Hỗn Độn Bá Thể ngàn vạn năm mới xuất hiện một lần! Bởi vì Bá Thể quá mức nghịch thiên hùng mạnh nên thiên địa mới không cho phép con giác tỉnh những Linh Thai tầm thường kia! Ta sẽ truyền cho con công pháp vô thượng của riêng Bá Thể: Bá Thể Tam Đan Công!"

Lời nói dối thiện ý nhưng vĩ đại của Thôn Trưởng như một tia sét xé toạc bóng tối tuyệt vọng trong tâm hồn Tần Mục. Niềm tự hào và ý chí quật cường một lần nữa bùng cháy mãnh liệt trong lồng ngực thiếu niên. Hắn tin tưởng tuyệt đối vào lời Thôn Trưởng: hắn không phải phế vật, hắn là vạn cổ đệ nhất Bá Thể!

Đêm hôm đó dưới ánh sáng rực rỡ của tượng thần, Tần Mục bắt đầu tu luyện Bá Thể Tam Đan Công theo sự hướng dẫn của Thôn Trưởng. Với niềm tin sắt đá rằng mình là Bá Thể vô song, ý chí tinh thần của hắn bộc phát mạnh mẽ đến mức phá vỡ xiềng xích của phàm nhân. Trong đan điền hỗn độn tối tăm, một vầng mặt trời nguyên khí rực rỡ bỗng nhiên bừng sáng, khai mở đệ nhất thần tàng: Linh Thai Thần Tàng!

Nguyên khí cuồn cuộn như sóng thần tràn ngập kinh mạch, Tần Mục nhảy vọt lên cao hơn mười trượng, một quyền giáng xuống làm nứt toác cả một tảng đá ngàn cân giữa sân viện! Kỳ tích đã xảy ra: một phàm nhân không có linh thai đã tự tay khai phá con đường tu luyện của riêng mình!

Thôn Trưởng cùng các dị nhân Tàn Lão Thôn sững sờ kinh ngạc nhìn cảnh tượng trước mắt, không ai ngờ rằng lời nói dối ngẫu hứng lại thực sự đúc kết nên một truyền kỳ vô tiền khoáng hậu.

Một thiếu niên mang danh "Bá Thể" bước ra từ Đại Khư hoang dã, từ giây phút này sẽ mang theo nắm đấm sắt và ý chí bất khuất xông pha vào thế giới rộng lớn bên ngoài, dẫm đạp lên các Thần Ma ngụy quân tử để chấn hưng nhân đạo!

Trong cõi nhân gian bao la vạn dặm, con đường nhân sinh và tu đạo vốn dĩ chưa từng bằng phẳng dễ dàng. Những bậc tiền bối đại năng trong truyền thuyết xưa kia khi đạp mây cưỡi gió, ngạo thị quần hùng đều đã từng phải trải qua vô số năm tháng cơ hàn, nếm trải trăm ngàn cay đắng tủi nhục giữa chốn hồng trần cuồn cuộn. Nhân tâm hiểm ác, thế sự khó lường, kẻ mạnh đứng trên đỉnh cao nhìn xuống thường xem chúng sinh như loài sâu kiến nhỏ nhoi, tùy ý định đoạt vận mệnh của kẻ yếu.

Thế nhưng, chính trong những nghịch cảnh tăm tối nhất của cuộc đời, ý chí bất khuất của con người mới bộc lộ sức mạnh phi thường tiềm ẩn. Dù thể xác có phải chịu đựng đòn roi tra tấn, dù kinh mạch bế tắc hay bị toàn bộ thế giới quay lưng ruồng bỏ, một khi ngọn lửa niềm tin vào chính bản thân mình chưa từng tắt lụi, thì vẫn còn cơ hội để lội ngược dòng nước, nghịch chuyển càn khôn.

Bầu trời đêm sâu thẳm lấp lánh muôn ngàn vì tinh tú xa xôi, mỗi một đốm sáng tượng trưng cho một số phận, một linh hồn đang tranh đấu không ngừng nghỉ để tìm kiếm vị trí của mình trong vũ trụ vô tận. Gió đêm thổi qua những tán cây cổ thụ phát ra âm thanh rì rào như tiếng thở dài của thời gian. Những kỷ nguyên huy hoàng rồi cũng sẽ lụi tàn thành tro bụi, các bậc đế vương bá chủ rồi cũng phải quy về cát bụi thời gian, duy chỉ có những tâm hồn kiên định dũng cảm dám bước đi trên con đường của riêng mình mới có thể lưu danh vĩnh cửu.

Từng dòng suy nghĩ lắng đọng lại trong tâm trí, tựa như dòng suối trong vắt gột rửa đi mọi nỗi hoang mang sợ hãi. Hít một hơi thật sâu để lồng ngực tràn ngập sinh khí của đất trời, ánh mắt hướng về phía chân trời xa xăm nơi ánh bình minh sắp sửa ló rạng xua tan màn đêm tăm tối. Con đường phía trước còn muôn trùng chông gai thử thách, nhưng mỗi một bước chân kiên định được đặt xuống hôm nay sẽ là nền móng vững chắc cho sự quật khởi huy hoàng trong ngày mai.

Vạn vật trong càn khôn đều vận hành theo quy luật tuần hoàn sinh diệt không ngừng nghỉ. Ngày qua đêm tới, hoa nở hoa tàn, cát bụi lại hóa thành non cao biển rộng. Người đi trên con đường nghịch thiên cải mệnh phải giữ cho tâm cảnh phẳng lặng như mặt hồ không một gợn sóng, bất biến giữa vạn biến, lấy tĩnh chế động, lấy nhu thắng cương.

Sức mạnh chân chính không chỉ nằm ở nắm đấm hay vũ lực hủy thiên diệt địa, mà trước hết bắt nguồn từ một trái tim kiên định không gì lay chuyển nổi. Trước nghịch cảnh không bao giờ khuất phục, trước cám dỗ không đánh mất lương tri bản ngã, đó mới chính là bản lĩnh đích thực của bậc kiêu hùng giữa đất trời.

Những gian khổ hôm nay trải qua rồi cũng sẽ hóa thành ngọn đèn soi sáng cho tương lai mịt mùng phía trước. Con đường tu luyện trường sinh vốn dĩ cô độc, ít người thấu hiểu, đòi hỏi một sự nhẫn nại phi thường và khả năng chịu đựng nỗi cô đơn tột cùng. Nhưng khi đã vượt qua được muôn ngàn hiểm hách, đứng trên đỉnh cao ngắm nhìn thiên hạ thái bình, mọi hy sinh mất mát đều sẽ trở nên vô cùng xứng đáng.

Từng giọt mồ hôi rơi xuống thấm sâu vào lòng đất mẹ cằn cỗi, như nuôi dưỡng hạt mầm hy vọng đang âm thầm đâm chồi nảy lộc. Không có thành công nào đến sau một đêm, không có vinh quang nào không phải trả giá bằng máu và nước mắt. Thiếu niên mang trong mình trái tim nóng bỏng và hoài bão lớn lao, quyết tâm khắc ghi từng bài học đắt giá vào tâm khảm để không bao giờ lặp lại sai lầm trong quá khứ.

Thời gian là dòng chảy vô tình cuốn trôi tất cả, nhưng cũng là thước đo chuẩn xác nhất thử thách phẩm chất và nghị lực của con người. Những kẻ yếu đuối sẽ bị sóng gió cuộc đời vùi dập và đào thải không thương tiếc, chỉ có những bậc kiên cường mới có thể sừng sững đứng vững tựa như ngọn núi Thái Sơn trước phong ba bão táp.

Trời đất bao la, đạo pháp vô biên, mỗi một sinh mệnh sinh ra đều mang trong mình một sứ mệnh và một ý nghĩa thiêng liêng nhất định. Đừng bao giờ tự ti trước sự nhỏ bé của bản thân, bởi vì một đốm lửa nhỏ cũng đủ sức thiêu rụi cả thảo nguyên bao la khi gió đông nổi lên.

Bước chân vững chãi tiến về phía trước, không quay đầu lại, không hối tiếc những gì đã qua. Phía trước là chân trời rộng mở, là biển rộng trời cao đang vẫy gọi những cánh chim dũng cảm dám vỗ cánh bay ngược chiều bão táp.

Thiên địa này vốn dĩ là một ván cờ lớn vô biên vô tận, nơi chư thần cùng ma quỷ mượn chúng sinh làm quân cờ mua vui. Nhưng ta sinh ra ở đời, há chịu cam tâm làm một con tốt thí ngoan ngoãn bị kẻ khác giật dây? Dù là phàm nhân thân phận thấp hèn, ta cũng quyết chí cầm cờ, tự tay khắc ghi vận mệnh của chính mình lên vách đá thời gian.

Máu trong huyết quản sôi trào mãnh liệt như dòng nham thạch ngầm dưới lòng đất sâu. Khí phách hiên ngang không sợ trời, không sợ đất, một lòng hướng đạo, thề đạp bằng muôn trùng cản trở để chạm tới đỉnh cao vinh quang muôn đời.

Một trang sử mới đã chính thức mở ra, và tên tuổi của người thiếu niên ấy sẽ đời đời được vạn thế ca tụng như một huyền thoại bất tử giữa cõi hồng trần.

Khúc ca bi tráng của thời đại diệt thế và quật khởi bắt đầu vang vọng khắp muôn trùng núi sông vĩ đại.', 0, 'PUBLISHED', 4100, false)
) AS c(chapter_number, title, content, price, status, views, is_deleted)
ON CONFLICT (novel_id, chapter_number) DO UPDATE
SET title = EXCLUDED.title, content = EXCLUDED.content, price = EXCLUDED.price, status = EXCLUDED.status;


-- 8. Tự động đồng bộ tất cả sequences về MAX(id) nếu có bất kỳ dữ liệu nào được chèn trước đó
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT 
            c.relname AS seq_name,
            t.relname AS tab_name,
            a.attname AS col_name
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        JOIN pg_depend d ON d.objid = c.oid AND d.deptype = 'a'
        JOIN pg_class t ON t.oid = d.refobjid
        JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = d.refobjsubid
        WHERE c.relkind = 'S' AND n.nspname = 'public'
    LOOP
        EXECUTE format(
            'SELECT CASE 
                WHEN (SELECT COUNT(*) FROM %I) > 0 
                THEN setval(%L, (SELECT MAX(%I) FROM %I), true)
                ELSE setval(%L, 1, false)
             END',
            r.tab_name, r.seq_name, r.col_name, r.tab_name, r.seq_name
        );
    END LOOP;
END $$;
