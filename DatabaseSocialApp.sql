-- Tạo database
CREATE DATABASE social_app
COLLATE Vietnamese_CI_AS;
GO

USE social_app;
GO

-- 1. Bảng Users
CREATE TABLE users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) UNIQUE NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    password NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(100),
    bio NVARCHAR(MAX),
    avatar_url NVARCHAR(255),
    wallet_balance DECIMAL(10, 2) DEFAULT 0.00,
    is_banned BIT DEFAULT 0,
    ban_until DATETIME2 NULL,
    role NVARCHAR(20) DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);
GO

-- 2. Bảng Posts
CREATE TABLE posts (
    post_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title NVARCHAR(255) NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    description NVARCHAR(MAX),
    is_deleted BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
GO

-- 3. Bảng Comments
CREATE TABLE comments (
    comment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT NULL,
    content NVARCHAR(MAX) NOT NULL,
    is_deleted BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id)
);
GO

-- 4. Bảng Reactions
CREATE TABLE reactions (
    reaction_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction_type NVARCHAR(20) NOT NULL CHECK (reaction_type IN ('LIKE', 'LOVE', 'HAHA', 'WOW', 'SAD', 'ANGRY')),
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT unique_user_post_reaction UNIQUE (user_id, post_id)
);
GO

-- 5. Bảng Shares
CREATE TABLE shares (
    share_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
GO

-- 6. Bảng Reports
CREATE TABLE reports (
    report_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    post_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason NVARCHAR(MAX) NOT NULL,
    status NVARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REVIEWED', 'RESOLVED', 'REJECTED')),
    admin_note NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    reviewed_at DATETIME2 NULL,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES users(user_id)
);
GO

-- 7. Bảng Chat Messages
CREATE TABLE chat_messages (
    message_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    is_read BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (sender_id) REFERENCES users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);
GO

-- 8. Bảng Shop Items
CREATE TABLE shop_items (
    item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    item_name NVARCHAR(100) NOT NULL,
    description NVARCHAR(MAX),
    price DECIMAL(10, 2) NOT NULL,
    item_type NVARCHAR(30) NOT NULL CHECK (item_type IN ('PROFILE_THEME', 'NEWSFEED_THEME', 'STICKER', 'BADGE', 'FRAME')),
    image_url NVARCHAR(255),
    is_available BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE()
);
GO

-- 9. Bảng User Purchases
CREATE TABLE user_purchases (
    purchase_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL,
    is_active BIT DEFAULT 1,
    purchased_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES shop_items(item_id)
);
GO

-- 10. Bảng Wallet Transactions
CREATE TABLE wallet_transactions (
    transaction_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transaction_type NVARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'PURCHASE', 'REFUND')),
    description NVARCHAR(255),
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
GO

-- 11. Bảng Follows
CREATE TABLE follows (
    follow_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (follower_id) REFERENCES users(user_id),
    FOREIGN KEY (following_id) REFERENCES users(user_id),
    CONSTRAINT unique_follow UNIQUE (follower_id, following_id)
);
GO

-- 12. Bảng Ban History
CREATE TABLE ban_history (
    ban_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    reason NVARCHAR(MAX) NOT NULL,
    ban_type NVARCHAR(20) NOT NULL CHECK (ban_type IN ('POST', 'COMMENT', 'FULL')),
    banned_at DATETIME2 DEFAULT GETDATE(),
    ban_until DATETIME2 NOT NULL,
    is_active BIT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (admin_id) REFERENCES users(user_id)
);
GO

-- Tạo indexes
CREATE INDEX idx_posts_user ON posts(user_id);
CREATE INDEX idx_comments_post ON comments(post_id);
CREATE INDEX idx_comments_user ON comments(user_id);
CREATE INDEX idx_reactions_post ON reactions(post_id);
CREATE INDEX idx_chat_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_receiver ON chat_messages(receiver_id);
CREATE INDEX idx_reports_status ON reports(status);
GO

USE social_app;

-- Thêm cột cho posts (lưu ảnh binary + tags)
ALTER TABLE posts ADD image_data VARBINARY(MAX) NULL;
ALTER TABLE posts ADD image_name NVARCHAR(255) NULL;
ALTER TABLE posts ADD tags NVARCHAR(500) NULL;
ALTER TABLE posts ADD views INT DEFAULT 0;
ALTER TABLE posts ADD likes_count INT DEFAULT 0;

-- Thêm cột equipped decoration cho users
ALTER TABLE posts ALTER COLUMN image_url NVARCHAR(500) NULL; -- cho phép NULL vì lưu binary

-- Thêm cột trang trí đang dùng cho user
ALTER TABLE users ADD equipped_frame    NVARCHAR(100) NULL;
ALTER TABLE users ADD equipped_bg       NVARCHAR(100) NULL;
ALTER TABLE users ADD equipped_name_fx  NVARCHAR(100) NULL;
ALTER TABLE users ADD equipped_chat_fx  NVARCHAR(100) NULL;
ALTER TABLE users ADD equipped_home_bg  NVARCHAR(100) NULL;

-- Thêm is_equipped vào user_purchases
ALTER TABLE user_purchases ADD is_equipped BIT DEFAULT 0;

-- Insert dữ liệu mẫu shop
INSERT INTO shop_items (item_name, description, price, item_type, image_url) VALUES
(N'Frame Galaxy',       N'Khung avatar hiệu ứng dải ngân hà',  50000, 'FRAME',          'https://picsum.photos/seed/frame1/200/200'),
(N'Frame Sakura',       N'Khung avatar hoa anh đào',            30000, 'FRAME',          'https://picsum.photos/seed/frame2/200/200'),
(N'Frame Gold',         N'Khung avatar vàng sang trọng',        80000, 'FRAME',          'https://picsum.photos/seed/frame3/200/200'),
(N'Profile Aurora',     N'Nền profile hiệu ứng cực quang',      60000, 'PROFILE_THEME',  'https://picsum.photos/seed/bg1/300/150'),
(N'Profile Ocean',      N'Nền profile đại dương xanh',          40000, 'PROFILE_THEME',  'https://picsum.photos/seed/bg2/300/150'),
(N'Newsfeed Space',     N'Nền trang chủ không gian',            70000, 'NEWSFEED_THEME', 'https://picsum.photos/seed/home1/300/150'),
(N'Newsfeed Neon City', N'Nền trang chủ thành phố neon',        70000, 'NEWSFEED_THEME', 'https://picsum.photos/seed/home2/300/150'),
(N'Badge Artist',       N'Huy hiệu nghệ sĩ chuyên nghiệp',     25000, 'BADGE',          'https://picsum.photos/seed/badge1/100/100'),
(N'Badge VIP',          N'Huy hiệu thành viên VIP',             99000, 'BADGE',          'https://picsum.photos/seed/badge2/100/100'),
(N'Sticker Cute Pack',  N'Bộ sticker dễ thương',                20000, 'STICKER',        'https://picsum.photos/seed/sticker1/100/100');

ALTER TABLE users ADD avatar_data VARBINARY(MAX) NULL;
ALTER TABLE users ADD equipped_item_name NVARCHAR(255) NULL;
ALTER TABLE users ALTER COLUMN equipped_bg NVARCHAR(MAX);
ALTER TABLE users ALTER COLUMN equipped_frame NVARCHAR(MAX);
ALTER TABLE users ALTER COLUMN equipped_name_fx NVARCHAR(MAX);
ALTER TABLE users ALTER COLUMN equipped_chat_fx NVARCHAR(MAX);
ALTER TABLE users ALTER COLUMN equipped_home_bg NVARCHAR(MAX);
-- Xem item nào đang là nền profile
SELECT * FROM shop_items WHERE item_type IN ('PROFILE_BG', 'NEWSFEED_THEME');

-- Thay tên constraint thực tế vào đây
ALTER TABLE shop_items DROP CONSTRAINT [tên_constraint];

-- Tạo lại constraint mới không có PROFILE_THEME
ALTER TABLE shop_items ADD CONSTRAINT CK_shop_items_item_type 
CHECK (item_type IN ('NEWSFEED_THEME', 'STICKER', 'BADGE', 'FRAME'));


USE social_app;

-- Xóa purchase NEWSFEED_THEME và PROFILE_THEME
DELETE FROM user_purchases 
WHERE item_id IN (
    SELECT item_id FROM shop_items 
    WHERE item_type IN ('NEWSFEED_THEME', 'PROFILE_THEME')
);

-- Xóa items khỏi shop
DELETE FROM shop_items 
WHERE item_type IN ('NEWSFEED_THEME', 'PROFILE_THEME');

-- Reset equipped_home_bg cho tất cả user
UPDATE users SET equipped_home_bg = NULL;

-- Kiểm tra lại
SELECT * FROM shop_items;

