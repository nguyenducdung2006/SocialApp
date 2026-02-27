// ===== SET BACKGROUND ẢNH BÌA =====
const header = document.querySelector('.profile-header');
if (header) {
    const bg = header.getAttribute('data-bg');
    if (bg && bg !== '') {
        header.style.backgroundImage = 'url(' + bg + ')';
        header.style.backgroundSize = 'cover';
        header.style.backgroundPosition = 'center';
    }
}

// ===== TABS =====
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const tab = btn.dataset.tab;
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById('tab-' + tab).classList.add('active');
    });
});

// ===== MODAL EDIT PROFILE =====
const editModal  = document.getElementById('editModal');
const openEdit   = document.getElementById('editProfileBtn');
const closeEdit  = document.getElementById('closeModal');
const cancelEdit = document.getElementById('cancelEdit');
if (openEdit)   openEdit.addEventListener('click',   () => editModal.classList.add('open'));
if (closeEdit)  closeEdit.addEventListener('click',  () => editModal.classList.remove('open'));
if (cancelEdit) cancelEdit.addEventListener('click', () => editModal.classList.remove('open'));
if (editModal)  editModal.addEventListener('click',  e => {
    if (e.target === editModal) editModal.classList.remove('open');
});

// ===== MODAL AVATAR =====
const avatarModal  = document.getElementById('avatarModal');
const openAvatar   = document.getElementById('openAvatarModal');
const closeAvatar  = document.getElementById('closeAvatarModal');
const cancelAvatar = document.getElementById('cancelAvatar');
if (openAvatar)   openAvatar.addEventListener('click',   () => avatarModal.classList.add('open'));
if (closeAvatar)  closeAvatar.addEventListener('click',  () => avatarModal.classList.remove('open'));
if (cancelAvatar) cancelAvatar.addEventListener('click', () => avatarModal.classList.remove('open'));
if (avatarModal)  avatarModal.addEventListener('click',  e => {
    if (e.target === avatarModal) avatarModal.classList.remove('open');
});

// ===== AVATAR FILE PREVIEW =====
const avatarFileInput = document.getElementById('avatarFileInput');
const saveAvatarBtn   = document.getElementById('saveAvatarBtn');
const avatarFileName  = document.getElementById('avatarFileName');

if (avatarFileInput) {
    avatarFileInput.addEventListener('change', function () {
        const file = this.files[0];
        if (!file) return;

        if (file.size > 5 * 1024 * 1024) {
            alert('Ảnh quá lớn! Tối đa 5MB.');
            this.value = '';
            return;
        }

        const reader = new FileReader();
        reader.onload = e => {
            const preview     = document.getElementById('avatarPreviewImg');
            const placeholder = document.getElementById('avatarPreviewPlaceholder');
            preview.src = e.target.result;
            preview.style.display = 'block';
            if (placeholder) placeholder.style.display = 'none';
        };
        reader.readAsDataURL(file);

        avatarFileName.textContent = '📎 ' + file.name;
        avatarFileName.style.display = 'block';
        saveAvatarBtn.removeAttribute('disabled');
    });
}

// ===== UPLOAD ẢNH BÌA =====
const coverInput = document.getElementById('coverFileInput');
if (coverInput) {
    coverInput.addEventListener('change', function () {
        if (!this.files[0]) return;
        const reader = new FileReader();
        reader.onload = e => {
            if (header) {
                header.style.backgroundImage = 'url(' + e.target.result + ')';
                header.style.backgroundSize = 'cover';
                header.style.backgroundPosition = 'center';
                header.classList.add('has-bg');
            }
        };
        reader.readAsDataURL(this.files[0]);
        document.getElementById('coverForm').submit();
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
