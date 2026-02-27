const tabs   = document.querySelectorAll('.filter-tab');
const cards  = document.querySelectorAll('.shop-card');
const empty  = document.getElementById('emptyState');
const count  = document.getElementById('visibleCount');

function updateCount() {
    const v = [...cards].filter(c => c.style.display !== 'none').length;
    if (count) count.textContent = v;
    if (empty) empty.style.display = v === 0 ? 'block' : 'none';
}

tabs.forEach(tab => {
    tab.addEventListener('click', () => {
        tabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        const type = tab.dataset.type;
        cards.forEach(card => {
            card.style.display =
                (type === 'ALL' || card.dataset.type === type) ? '' : 'none';
        });
        updateCount();
    });
});

// Auto close alerts
setTimeout(() => {
    document.querySelectorAll('.alert-box').forEach(el => {
        el.style.transition = 'opacity .5s';
        el.style.opacity = '0';
        setTimeout(() => el.remove(), 500);
    });
}, 3000);
