// ===== QUICK AMOUNT BUTTONS =====
const quickBtns   = document.querySelectorAll('.quick-btn');
const amountInput = document.getElementById('amountInput');

quickBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        quickBtns.forEach(b => b.classList.remove('selected'));
        btn.classList.add('selected');
        if (amountInput) amountInput.value = btn.dataset.amount;
    });
});

// Bỏ chọn quick khi user tự nhập
if (amountInput) {
    amountInput.addEventListener('input', () => {
        quickBtns.forEach(b => b.classList.remove('selected'));
    });
}

// ===== AUTO CLOSE ALERT =====
setTimeout(() => {
    document.querySelectorAll('.alert-box').forEach(el => {
        el.style.transition = 'opacity .5s';
        el.style.opacity = '0';
        setTimeout(() => el.remove(), 500);
    });
}, 3000);
