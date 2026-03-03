document.addEventListener('DOMContentLoaded', function () {
    (function () {
        const homeBg = document.body.getAttribute('data-homebg');
        if (homeBg && homeBg !== '') {
            document.body.style.backgroundImage = 'url(' + homeBg + ')';
            document.body.style.backgroundSize = 'cover';
            document.body.style.backgroundPosition = 'center';
            document.body.style.backgroundAttachment = 'fixed';
            // Bật overlay
            const overlay = document.getElementById('homeBgOverlay');
            if (overlay) overlay.style.display = 'block';
        }
    })();
    // ===== TAB SWITCHING =====
    document.querySelectorAll('.tab-link').forEach(tab => {
        tab.addEventListener('click', function (e) {
            e.preventDefault();
            document.querySelectorAll('.tab-link').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // ===== LIKE BUTTON =====
    document.querySelectorAll('.like-btn').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            e.preventDefault();
            const postId = this.getAttribute('data-id');
            if (!postId) return;
            fetch(`/post/${postId}/react`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            })
                .then(res => { if (res.status === 401) { window.location.href = '/auth/login'; throw new Error(); } return res.json(); })
                .then(data => {
                    const icon = this.querySelector('i');
                    const countSpan = this.querySelector('span');
                    if (data.reacted) {
                        this.classList.add('liked');
                        if (icon) { icon.classList.remove('bi-heart'); icon.classList.add('bi-heart-fill'); }
                    } else {
                        this.classList.remove('liked');
                        if (icon) { icon.classList.remove('bi-heart-fill'); icon.classList.add('bi-heart'); }
                    }
                    if (countSpan && data.count !== undefined) countSpan.textContent = data.count;
                })
                .catch(() => { });
        });
    });

    // ===== SAVE / BOOKMARK BUTTON =====
    document.querySelectorAll('.save-btn').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            e.preventDefault();
            const postId = this.getAttribute('data-id');
            if (!postId) return;
            fetch(`/post/${postId}/save`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            })
                .then(res => { if (res.status === 401) { window.location.href = '/auth/login'; throw new Error(); } return res.json(); })
                .then(data => {
                    const icon = this.querySelector('i');
                    if (data.saved) {
                        this.classList.add('saved');
                        if (icon) { icon.classList.remove('bi-bookmark'); icon.classList.add('bi-bookmark-fill'); }
                        showToast('\u2705 Đã lưu tác phẩm!');
                    } else {
                        this.classList.remove('saved');
                        if (icon) { icon.classList.remove('bi-bookmark-fill'); icon.classList.add('bi-bookmark'); }
                        showToast('\uD83D\uDDD1️ Đã bỏ lưu!');
                    }
                })
                .catch(() => { });
        });
    });

    // ===== FOLLOW BUTTON =====
    document.querySelectorAll('.follow-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const userId = this.getAttribute('data-id');
            if (!userId) return;
            fetch('/profile/follow/' + userId, { method: 'POST' })
                .then(res => res.json())
                .then(data => {
                    if (data.error) { showToast('⚠️ ' + data.error); return; }
                    if (data.followed) {
                        this.classList.add('following');
                        this.textContent = 'Đang theo dõi';
                        showToast('✅ Đã theo dõi!');
                    } else {
                        this.classList.remove('following');
                        this.textContent = 'Theo dõi';
                        showToast('🗑️ Đã hủy theo dõi');
                    }
                })
                .catch(e => console.error(e));
        });
    });

    // ===== LIGHTBOX =====
    const lightbox = document.getElementById('lightbox');
    const lightboxImg = document.getElementById('lightboxImg');
    const lightboxClose = document.getElementById('lightboxClose');

    document.querySelectorAll('.img-card').forEach(card => {
        card.addEventListener('click', function () {
            const img = this.querySelector('.img-thumb');
            if (img) {
                lightboxImg.src = img.src;
                lightbox.classList.add('show');
                document.body.style.overflow = 'hidden';
            }
        });
    });

    lightboxClose.addEventListener('click', closeLightbox);
    lightbox.addEventListener('click', function (e) {
        if (e.target === lightbox) closeLightbox();
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeLightbox();
    });

    function closeLightbox() {
        lightbox.classList.remove('show');
        document.body.style.overflow = '';
    }

    // ===== AJAX FEED =====
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const grid = document.getElementById('mainGrid');
    const feedTabs = document.querySelectorAll('.feed-tab');
    const secTitle = document.getElementById('secTitle');
    const secAll = document.getElementById('secAll');
    let page = 1;
    let currentTab = 'foryou'; // Mặc định: Dành cho bạn

    // Map tiêu đề section theo tab
    const TAB_TITLES = {
        'foryou': '🎯 Dành cho bạn',
        'new': '✨ Mới nhất hôm nay',
        'following': '👥 Đang theo dõi',
        'popular': '🔥 Phổ biến',
        'ranking': '🏆 Ranking hôm nay'
    };

    function updateSectionTitle(tab) {
        if (secTitle) secTitle.textContent = TAB_TITLES[tab] || '✨ Mới nhất hôm nay';
        if (secAll) {
            if (tab === 'foryou') {
                secAll.style.display = 'none';
            } else {
                secAll.style.display = '';
            }
        }
    }

    // Bắt sự kiện chuyển Tab
    feedTabs.forEach(t => {
        t.addEventListener('click', function (e) {
            e.preventDefault();
            feedTabs.forEach(f => f.classList.remove('active'));
            this.classList.add('active');

            currentTab = this.getAttribute('data-target');
            page = 1; // Reset trang
            if (grid) grid.innerHTML = ''; // Clear Feed

            updateSectionTitle(currentTab);

            // Hiện chữ tải...
            if (loadMoreBtn) {
                loadMoreBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang tải...';
                loadMoreBtn.disabled = true;
                loadMoreBtn.style.display = 'flex'; // Hiện lại nếu trước đó bị Tắt
            }

            fetchData();
        });
    });

    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', function (e) {
            e.preventDefault();
            this.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang tải...';
            this.disabled = true;
            fetchData();
        });
    }

    // Tải bài viết lần đầu khi trang được load
    fetchData();

    function fetchData() {
        if (!loadMoreBtn || !grid) return;

        let url;
        if (currentTab === 'foryou') {
            // Lấy top tags từ localStorage
            const tagMap = JSON.parse(localStorage.getItem('tagViews') || '{}');
            const topTags = Object.entries(tagMap)
                .sort((a, b) => b[1] - a[1])
                .slice(0, 5)
                .map(([tag]) => tag);
            if (topTags.length === 0) {
                // Chưa có lịch sử: fallback về new
                url = `/api/posts?page=${page}&tab=new`;
            } else {
                url = `/api/posts?page=${page}&tab=foryou&tags=${encodeURIComponent(topTags.join(','))}`;
            }
        } else {
            url = `/api/posts?page=${page}&tab=${currentTab}`;
        }

        fetch(url)
            .then(res => {
                if (res.status === 401) {
                    window.location.href = '/auth/login';
                    throw new Error("Yêu cầu đăng nhập");
                }
                return res.json();
            })
            .then(data => {
                if (data.length === 0) {
                    loadMoreBtn.textContent = 'Đã hết bài viết';
                    loadMoreBtn.disabled = true;
                    return;
                }

                data.forEach(post => {
                    const el = document.createElement('div');
                    el.className = 'img-card';

                    let imgSource = '';
                    if (post.imageData) {
                        imgSource = `/upload/image/${post.id}`;
                    } else if (post.imageUrl) {
                        imgSource = post.imageUrl;
                    }

                    // Avatar logic
                    let avatarSection = '';
                    if (post.user && post.user.avatarData) {
                        avatarSection = `<img src="/profile/avatar/${post.user.id}" style="width:24px;height:24px;border-radius:50%;object-fit:cover;margin-right:6px;" alt=""/>`;
                    } else if (post.user) {
                        avatarSection = `<div class="author-dot d-flex align-items-center justify-content-center text-white" style="background:#e91e63;">${post.user.username.substring(0, 1).toUpperCase()}</div>`;
                    }

                    el.innerHTML = `
                        <a href="/post/${post.id}">
                            <img src="${imgSource}" class="img-thumb" alt="">
                        </a>
                        <div class="img-overlay">
                            <button class="overlay-btn like-btn" data-id="${post.id}">
                                <i class="bi bi-heart-fill"></i> <span>${post.likesCount || 0}</span>
                            </button>
                        </div>
                        <div class="img-info">
                            <a href="/post/${post.id}" class="text-decoration-none text-light">
                                <div class="img-title">${post.title}</div>
                            </a>
                            <div class="img-meta">
                                <a href="/profile/view/${post.user.id}" class="text-decoration-none text-muted w-100">
                                    <div class="img-author d-flex align-items-center">
                                        ${avatarSection}
                                        <span class="ms-1">@${post.user.username}</span>
                                    </div>
                                </a>
                            </div>
                        </div>
                    `;

                    // Attach like listener
                    const likeBtn = el.querySelector('.like-btn');
                    if (likeBtn) {
                        likeBtn.addEventListener('click', function (e) {
                            e.stopPropagation(); e.preventDefault();
                            const pid = this.getAttribute('data-id');
                            fetch(`/post/${pid}/react`, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                                .then(r => { if (r.status === 401) { window.location.href = '/auth/login'; throw new Error(); } return r.json(); })
                                .then(d => {
                                    const ic = this.querySelector('i');
                                    const sp = this.querySelector('span');
                                    if (d.reacted) { this.classList.add('liked'); if (ic) { ic.classList.remove('bi-heart'); ic.classList.add('bi-heart-fill'); } }
                                    else { this.classList.remove('liked'); if (ic) { ic.classList.remove('bi-heart-fill'); ic.classList.add('bi-heart'); } }
                                    if (sp && d.count !== undefined) sp.textContent = d.count;
                                }).catch(() => { });
                        });
                    }


                    grid.appendChild(el);
                });

                page++;
                loadMoreBtn.innerHTML = '<i class="bi bi-arrow-clockwise me-2"></i>Tải thêm';
                loadMoreBtn.disabled = false;

                if (data.length < 6) {
                    loadMoreBtn.textContent = 'Đã hết';
                    loadMoreBtn.disabled = true;
                }
            })
            .catch(err => {
                console.error('Lỗi khi tải bài viết:', err);
                loadMoreBtn.innerHTML = '<i class="bi bi-arrow-clockwise me-2"></i>Tải thêm';
                loadMoreBtn.disabled = false;
            });
    }

    // ===== TOAST NOTIFICATION =====
    function showToast(message) {
        const existing = document.querySelector('.custom-toast');
        if (existing) existing.remove();

        const toast = document.createElement('div');
        toast.className = 'custom-toast';
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%);
            background: #21262d; color: #e6edf3;
            padding: 10px 20px; border-radius: 20px;
            font-size: 0.875rem; z-index: 9999;
            border: 1px solid rgba(255,255,255,0.1);
            box-shadow: 0 4px 20px rgba(0,0,0,0.4);
            animation: fadeInUp 0.3s ease;
        `;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 2500);
    }

    // ===== SEARCH INPUT =====
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && this.value.trim()) {
                showToast('🔍 Tìm kiếm: ' + this.value.trim());
            }
        });
    }

    // ===== TAG CLICK =====
    document.querySelectorAll('.tag').forEach(tag => {
        tag.addEventListener('click', function () {
            showToast('🏷️ Tag: ' + this.textContent);
        });
    });
});
