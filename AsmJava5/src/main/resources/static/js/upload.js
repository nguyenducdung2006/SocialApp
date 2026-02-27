const dropZone = document.getElementById('dropZone');
const fileInput = document.getElementById('fileInput');
const previewImg = document.getElementById('previewImg');
const dropPlaceholder = document.getElementById('dropPlaceholder');
const tagsContainer = document.getElementById('tagsContainer');
const tagTyper = document.getElementById('tagTyper');
const tagsHidden = document.getElementById('tagsHidden');
const titleInput = document.querySelector('input[name="title"]');
const titleCount = document.getElementById('titleCount');
let tags = [];

// Drag & Drop
['dragenter','dragover'].forEach(e => dropZone.addEventListener(e, ev => { ev.preventDefault(); dropZone.classList.add('dragover'); }));
['dragleave','drop'].forEach(e => dropZone.addEventListener(e, ev => { ev.preventDefault(); dropZone.classList.remove('dragover'); }));
dropZone.addEventListener('drop', e => { const f = e.dataTransfer.files[0]; if(f) handleFile(f); });
fileInput.addEventListener('change', () => { if(fileInput.files[0]) handleFile(fileInput.files[0]); });

function handleFile(file) {
    if (!file.type.startsWith('image/')) { alert('Chỉ chấp nhận file ảnh!'); return; }
    if (file.size > 10*1024*1024) { alert('Ảnh tối đa 10MB!'); return; }
    const reader = new FileReader();
    reader.onload = e => {
        previewImg.src = e.target.result;
        previewImg.style.display = 'block';
        dropPlaceholder.style.display = 'none';
    };
    reader.readAsDataURL(file);
}

// Title counter
if (titleInput) {
    titleInput.addEventListener('input', function() {
        titleCount.textContent = this.value.length + '/255';
    });
}

// Tags
tagTyper.addEventListener('keydown', function(e) {
    if ((e.key === 'Enter' || e.key === ',') && this.value.trim()) {
        e.preventDefault();
        addTag(this.value.trim().replace(/,/g,''));
        this.value = '';
    }
    if (e.key === 'Backspace' && !this.value && tags.length) removeTag(tags.length - 1);
});

function addTag(text) {
    if (tags.includes(text) || tags.length >= 10) return;
    tags.push(text);
    renderTags();
}
function removeTag(i) {
    tags.splice(i, 1);
    renderTags();
}
function renderTags() {
    tagsContainer.innerHTML = tags.map((t,i) => `
        <div class="tag-chip">#${t}
            <button type="button" onclick="removeTag(${i})">×</button>
        </div>`).join('');
    tagsHidden.value = tags.join(',');
}

// Submit
document.getElementById('uploadForm').addEventListener('submit', function() {
    const btn = document.getElementById('submitBtn');
    btn.querySelector('.btn-text').style.display = 'none';
    btn.querySelector('.btn-loading').style.display = 'inline-flex';
    btn.disabled = true;
});
