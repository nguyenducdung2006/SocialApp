/**
 * notif-ws.js — Real-time notification system (Facebook-style)
 * Được nhúng vào TẤT CẢ các trang có navbar.
 * Yêu cầu: window.NOTIF_USER_ID phải được set trước khi load file này.
 *
 * Tính năng:
 *  - Kết nối WebSocket (SockJS + STOMP) khi trang load
 *  - Hiển thị badge số thông báo chưa đọc trên chuông bell
 *  - Hiển thị toast popup real-time khi nhận thông báo mới
 *  - Cập nhật badge ngay lập tức, không cần F5 hay đăng xuất
 */
(function () {
    'use strict';

    // ─── Cấu hình icon theo loại thông báo ───────────────────────────────
    const ICONS = {
        FOLLOW: '👤',
        NEW_POST: '🖼️',
        COMMENT: '💬',
        POST_DELETED: '🗑️',
        POST_REPORTED: '🚩'
    };

    // ─── Badge helper ────────────────────────────────────────────────────
    function getBadge() {
        return document.getElementById('notifBadge');
    }

    function updateBadge(count) {
        const badge = getBadge();
        if (!badge) return;
        if (count > 0) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.style.display = '';
        } else {
            badge.style.display = 'none';
        }
    }

    function incrementBadge() {
        const badge = getBadge();
        if (!badge) return;
        const cur = parseInt(badge.textContent) || 0;
        updateBadge(cur + 1);
    }

    // ─── Toast notification (Facebook-style) ────────────────────────────
    function showToast(notif) {
        // Xóa toast cũ nếu có
        const old = document.querySelector('.nws-toast');
        if (old) old.remove();

        const icon = ICONS[notif.type] || '🔔';
        const toast = document.createElement('div');
        toast.className = 'nws-toast';
        toast.style.cssText = [
            'position:fixed',
            'bottom:24px',
            'right:24px',
            'z-index:99999',
            'background:#1e1e2e',
            'border:1.5px solid #e91e63',
            'border-radius:16px',
            'padding:14px 18px',
            'max-width:340px',
            'min-width:260px',
            'cursor:pointer',
            'box-shadow:0 8px 32px rgba(0,0,0,0.55)',
            'display:flex',
            'gap:12px',
            'align-items:flex-start',
            'animation:nwsSlideIn 0.35s cubic-bezier(0.34,1.56,0.64,1)',
            'font-family:inherit'
        ].join(';');

        toast.innerHTML = `
            <span style="font-size:1.5rem;flex-shrink:0;margin-top:1px">${icon}</span>
            <div style="flex:1;min-width:0">
                <div style="color:#fff;font-size:0.82rem;font-weight:700;margin-bottom:3px;letter-spacing:0.3px">
                    Thông báo mới
                </div>
                <div style="color:#ccc;font-size:0.82rem;line-height:1.45;word-break:break-word">
                    ${notif.message || ''}
                </div>
            </div>
            <button onclick="this.parentElement.remove()" style="
                background:none;border:none;color:#666;font-size:1rem;
                cursor:pointer;padding:0;line-height:1;flex-shrink:0;margin-top:-2px
            ">✕</button>
        `;

        // Click vào toast → đến trang thông báo
        toast.addEventListener('click', function (e) {
            if (e.target.tagName === 'BUTTON') return;
            window.location.href = notif.link || '/notifications';
        });

        document.body.appendChild(toast);

        // Tự động ẩn sau 6 giây
        const hideTimer = setTimeout(() => {
            toast.style.animation = 'nwsSlideOut 0.3s ease forwards';
            setTimeout(() => toast.remove(), 300);
        }, 6000);

        // Dừng timer nếu hover
        toast.addEventListener('mouseenter', () => clearTimeout(hideTimer));
    }

    // ─── Inject keyframe CSS (chỉ một lần) ──────────────────────────────
    if (!document.getElementById('nws-style')) {
        const s = document.createElement('style');
        s.id = 'nws-style';
        s.textContent = `
            @keyframes nwsSlideIn {
                from { transform: translateX(120%) scale(0.8); opacity:0; }
                to   { transform: translateX(0)   scale(1);   opacity:1; }
            }
            @keyframes nwsSlideOut {
                from { transform: translateX(0);    opacity:1; }
                to   { transform: translateX(120%); opacity:0; }
            }
            .nws-toast:hover { border-color: #ff4081 !important; }
        `;
        document.head.appendChild(s);
    }

    // ─── Tải badge count lần đầu ────────────────────────────────────────
    function loadInitialCount() {
        fetch('/api/notifications/count')
            .then(r => r.ok ? r.json() : { count: 0 })
            .then(d => updateBadge(d.count || 0))
            .catch(() => { });
    }

    // ─── Kết nối WebSocket ───────────────────────────────────────────────
    function connectWS(userId) {
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.warn('[notif-ws] SockJS hoặc Stomp chưa load');
            return;
        }

        let retryCount = 0;
        const MAX_RETRIES = 5;

        function connect() {
            try {
                const sock = new SockJS('/ws');
                const client = Stomp.over(sock);
                client.debug = null; // Tắt log spam

                client.connect({}, function () {
                    retryCount = 0; // Reset khi kết nối thành công
                    client.subscribe('/topic/user-' + userId, function (frame) {
                        try {
                            const notif = JSON.parse(frame.body);
                            incrementBadge();
                            showToast(notif);
                        } catch (e) {
                            console.error('[notif-ws] Parse error:', e);
                        }
                    });
                }, function (err) {
                    // Thử kết nối lại sau lỗi
                    if (retryCount < MAX_RETRIES) {
                        retryCount++;
                        const delay = Math.min(1000 * Math.pow(2, retryCount), 30000);
                        setTimeout(connect, delay);
                    }
                });
            } catch (e) {
                console.error('[notif-ws] connect error:', e);
            }
        }

        connect();
    }

    // ─── Khởi động ──────────────────────────────────────────────────────
    function init() {
        const userId = window.NOTIF_USER_ID;
        if (!userId || userId <= 0) return; // Chưa đăng nhập

        loadInitialCount();
        connectWS(userId);
    }

    // Chờ DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
