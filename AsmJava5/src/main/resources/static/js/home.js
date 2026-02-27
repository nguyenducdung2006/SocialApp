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
            const isLiked = this.classList.toggle('liked');
            const parts = this.innerHTML.split(' ');
            const icon = '<i class="bi bi-heart' + (isLiked ? '-fill' : '') + '"></i>';
            let count = parseInt(parts[parts.length - 1]);
            count = isLiked ? count + 1 : count - 1;
            this.innerHTML = icon + ' ' + count;
        });
    });

    // ===== SAVE / BOOKMARK BUTTON =====
    document.querySelectorAll('.save-btn').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            const isSaved = this.classList.toggle('saved');
            this.innerHTML = isSaved
                ? '<i class="bi bi-bookmark-fill"></i>'
                : '<i class="bi bi-bookmark"></i>';
            showToast(isSaved ? '✅ Đã lưu tác phẩm!' : '🗑️ Đã bỏ lưu!');
        });
    });

    // ===== FOLLOW BUTTON =====
    document.querySelectorAll('.follow-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const isFollowing = this.classList.toggle('following');
            this.textContent = isFollowing ? 'Đang theo dõi' : 'Theo dõi';
            showToast(isFollowing ? '✅ Đã theo dõi!' : 'Đã hủy theo dõi');
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

    // ===== LOAD MORE =====
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const grid = document.getElementById('mainGrid');
    let page = 1;

    const extraCards = [
        { seed: 'art7',  title: 'Night City',      author: '@artist_g', views: '1.8k', color: '#e91e63' },
        { seed: 'art8',  title: 'Dragon Fly',       author: '@artist_h', views: '2.4k', color: '#ff9800' },
        { seed: 'art9',  title: 'Forest Spirit',    author: '@artist_i', views: '3.1k', color: '#009688' },
        { seed: 'art10', title: 'Cyber Girl',        author: '@artist_j', views: '5.2k', color: '#3f51b5' },
        { seed: 'art11', title: 'Sakura Season',    author: '@artist_k', views: '4.7k', color: '#e91e63' },
        { seed: 'art12', title: 'Mountain Peak',    author: '@artist_l', views: '900',  color: '#607d8b' },
    ];

    loadMoreBtn.addEventListener('click', function () {
        this.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang tải...';
        this.disabled = true;

        setTimeout(() => {
            extraCards.forEach(card => {
                const el = document.createElement('div');
                el.className = 'img-card';
                el.innerHTML = `
                    <img src="https://picsum.photos/seed/${card.seed}/300/300" class="img-thumb" alt="">
                    <div class="img-overlay">
                        <button class="overlay-btn like-btn"><i class="bi bi-heart"></i> ${Math.floor(Math.random()*400+50)}</button>
                        <button class="overlay-btn save-btn"><i class="bi bi-bookmark"></i></button>
                    </div>
                    <div class="img-info">
                        <div class="img-title">${card.title}</div>
                        <div class="img-meta">
                            <div class="img-author">
                                <div class="author-dot" style="background:${card.color};">${card.author[1].toUpperCase()}</div>
                                ${card.author}
                            </div>
                            <div class="img-stats"><i class="bi bi-eye"></i> ${card.views}</div>
                        </div>
                    </div>
                `;

                // Gắn lại events cho card mới
                el.querySelector('.like-btn').addEventListener('click', function(e) {
                    e.stopPropagation();
                    const isLiked = this.classList.toggle('liked');
                    const count = parseInt(this.textContent.trim()) + (isLiked ? 1 : -1);
                    this.innerHTML = `<i class="bi bi-heart${isLiked ? '-fill' : ''}"></i> ${count}`;
                });
                el.querySelector('.save-btn').addEventListener('click', function(e) {
                    e.stopPropagation();
                    const isSaved = this.classList.toggle('saved');
                    this.innerHTML = isSaved ? '<i class="bi bi-bookmark-fill"></i>' : '<i class="bi bi-bookmark"></i>';
                    showToast(isSaved ? '✅ Đã lưu tác phẩm!' : '🗑️ Đã bỏ lưu!');
                });
                el.addEventListener('click', function() {
                    const img = this.querySelector('.img-thumb');
                    if (img) {
                        lightboxImg.src = img.src;
                        lightbox.classList.add('show');
                        document.body.style.overflow = 'hidden';
                    }
                });

                grid.appendChild(el);
            });

            loadMoreBtn.innerHTML = '<i class="bi bi-arrow-clockwise me-2"></i>Tải thêm';
            loadMoreBtn.disabled = false;
            page++;

            if (page >= 3) {
                loadMoreBtn.textContent = 'Đã hết';
                loadMoreBtn.disabled = true;
            }
        }, 800);
    });

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
