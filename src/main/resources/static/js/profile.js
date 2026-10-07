/**
 * Profile page — view, create, or edit the user's profile.
 * Expects: window.TUMOOH_PROFILE = { userId: <id> }
 */
(function () {
    'use strict';

    const config = window.TUMOOH_PROFILE || {};
    const userId = config.userId;

    const loadingEl = document.getElementById('profile-loading');
    const errorEl = document.getElementById('profile-error');
    const viewEl = document.getElementById('profile-view');
    const createEl = document.getElementById('profile-create');
    const form = document.getElementById('profile-form');
    const formErrorEl = document.getElementById('profile-form-error');
    const submitBtn = document.getElementById('profile-submit');
    const cancelBtn = document.getElementById('profile-cancel');
    const titleEl = document.getElementById('profile-create-title');
    const subtitleEl = document.getElementById('profile-create-subtitle');

    const photoField = document.getElementById('profile-photo-field');
    const photoPreview = document.getElementById('profile-photo-preview');
    const photoAddBtn = document.getElementById('profile-photo-add');
    const photoRemoveBtn = document.getElementById('profile-photo-remove');
    const photoFileInput = document.getElementById('profile-photo-file');
    const photoValueInput = document.getElementById('profile-photo-value');
    const photoHint = document.getElementById('profile-photo-hint');
    const photoError = document.getElementById('profile-photo-error');

    const cvField = document.getElementById('profile-cv-field');
    const cvName = document.getElementById('profile-cv-name');
    const cvAddBtn = document.getElementById('profile-cv-add');
    const cvViewLink = document.getElementById('profile-cv-view');
    const cvRemoveBtn = document.getElementById('profile-cv-remove');
    const cvFileInput = document.getElementById('profile-cv-file');
    const cvValueInput = document.getElementById('profile-cv-value');
    const cvHint = document.getElementById('profile-cv-hint');
    const cvError = document.getElementById('profile-cv-error');

    const uploadsHint = document.getElementById('profile-uploads-hint');

    if (!userId || !form || !viewEl || !createEl) {
        return;
    }

    const CREATE_TITLE = 'Create your profile';
    const CREATE_SUBTITLE = "You don't have a profile yet — it takes a minute.";
    const PROFILE_PATH = '/profiles/user/' + userId;

    let mode = 'view';        // 'view' | 'create' | 'edit'
    let currentProfile = null;
    let currentEmail = '';

    /* ------------------------------------------------------------------ *
     * Helpers
     * ------------------------------------------------------------------ */

    function esc(value) {
        if (value === null || value === undefined) {
            return '';
        }
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function present(value) {
        if (value === null || value === undefined) {
            return false;
        }
        return String(value).trim() !== '';
    }

    // Turns raw server messages into something a person can act on
    function friendly(message) {
        const generic = !message
            || message === 'Request failed'
            || message === 'Internal Server Error'
            || message === 'Not Found'
            || message === 'Bad Request';
        return generic ? 'Something went wrong. Please try again.' : message;
    }

    function normalizeUrl(url) {
        const value = String(url).trim();
        if (/^(https?:)?\/\//i.test(value) || value.indexOf('/') === 0 || /^data:/i.test(value)) {
            return value;
        }
        return '/' + value;
    }

    function initialsOf(name) {
        const parts = String(name).trim().split(/\s+/).filter(Boolean);
        if (!parts.length) {
            return '?';
        }
        return (parts[0][0] + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase();
    }

    function skillsChips(skills) {
        if (!present(skills)) {
            return '';
        }
        const items = String(skills).split(',').map(function (item) {
            return item.trim();
        }).filter(Boolean);
        if (!items.length) {
            return '';
        }
        return '<div class="mt-2 flex flex-wrap gap-2">' + items.map(function (item) {
            return '<span class="rounded-full border border-tumooh-navy/10 bg-tumooh-navy/5 px-3 py-1 text-xs font-medium text-tumooh-navy/70">' + esc(item) + '</span>';
        }).join('') + '</div>';
    }

    function linkButton(label, url) {
        if (!present(url)) {
            return '';
        }
        return '<a href="' + esc(normalizeUrl(url)) + '" target="_blank" rel="noopener noreferrer" ' +
            'class="inline-flex items-center gap-2 rounded-2xl border border-tumooh-navy/15 px-4 py-2.5 text-sm font-medium ' +
            'text-tumooh-navy/80 hover:border-tumooh-accent hover:text-tumooh-accent transition-colors">' +
            esc(label) + '</a>';
    }

    function detailRow(label, value) {
        if (!present(value)) {
            return '';
        }
        return '<div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between sm:gap-4 py-3 border-b border-tumooh-navy/5 last:border-0">' +
            '<span class="text-sm text-tumooh-navy/60">' + esc(label) + '</span>' +
            '<span class="text-sm font-medium text-tumooh-navy/90 break-words sm:text-right">' + esc(value) + '</span></div>';
    }

    function showError(message) {
        if (errorEl) {
            errorEl.textContent = friendly(message);
            errorEl.classList.remove('hidden');
        }
    }

    function clearError() {
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.classList.add('hidden');
        }
    }

    function showFormError(message) {
        if (formErrorEl) {
            formErrorEl.textContent = friendly(message);
            formErrorEl.classList.remove('hidden');
        }
    }

    function clearFormError() {
        if (formErrorEl) {
            formErrorEl.textContent = '';
            formErrorEl.classList.add('hidden');
        }
    }

    function setLoading(on) {
        if (loadingEl) {
            loadingEl.classList.toggle('hidden', !on);
        }
    }

    /* ------------------------------------------------------------------ *
     * View state
     * ------------------------------------------------------------------ */

    function renderView(profile, email) {
        mode = 'view';
        currentProfile = profile || null;
        currentEmail = email || '';

        const fullName = present(profile.fullName) ? profile.fullName : 'Unnamed profile';

        const meta = [];
        if (present(profile.major)) {
            meta.push(profile.major);
        }
        if (present(profile.graduationYear)) {
            meta.push('Class of ' + profile.graduationYear);
        }
        if (present(email)) {
            meta.push(email);
        }

        let avatar = '<div class="h-16 w-16 shrink-0 rounded-full bg-tumooh-accent/15 text-tumooh-accent flex items-center justify-center text-xl font-semibold" aria-hidden="true">' +
            esc(initialsOf(fullName)) + '</div>';
        if (present(profile.profileImage)) {
            avatar = '<div class="relative h-16 w-16 shrink-0">' + avatar +
                '<img src="' + esc(normalizeUrl(profile.profileImage)) + '" alt="" ' +
                'class="absolute inset-0 h-16 w-16 rounded-full object-cover" onerror="this.remove()"/></div>';
        }

        let html = '<section class="rounded-2xl bg-white p-6 sm:p-8 shadow-soft border border-tumooh-navy/5">' +
            '<div class="flex items-start justify-between gap-4">' +
            '<div class="flex items-start gap-5 min-w-0">' + avatar +
            '<div class="min-w-0"><h2 class="text-xl font-semibold break-words">' + esc(fullName) + '</h2>' +
            (meta.length ? '<p class="mt-1 text-sm text-tumooh-navy/60 break-words">' + esc(meta.join(' · ')) + '</p>' : '') +
            (present(profile.bio) ? '<p class="mt-3 text-sm leading-relaxed text-tumooh-navy/80">' + esc(profile.bio) + '</p>' : '') +
            '</div></div>' +
            '<button type="button" id="profile-edit" ' +
            'class="shrink-0 rounded-2xl border border-tumooh-navy/15 px-4 py-2 text-sm font-medium ' +
            'text-tumooh-navy/80 hover:border-tumooh-accent hover:text-tumooh-accent transition-colors">Edit</button>' +
            '</div></section>';

        const rows = detailRow('Phone', profile.phoneNumber)
            + (present(profile.skills)
                ? '<div class="py-3 border-b border-tumooh-navy/5 last:border-0"><span class="text-sm text-tumooh-navy/60">Skills</span>' +
                  skillsChips(profile.skills) + '</div>'
                : '');

        const links = linkButton('LinkedIn', profile.linkedinUrl)
            + linkButton('GitHub', profile.githubUrl)
            + linkButton('View CV', profile.cvUrl);

        const details = rows + (links
            ? '<div class="flex flex-wrap gap-3 pt-4">' + links + '</div>'
            : '');

        if (details.trim()) {
            html += '<section class="rounded-2xl bg-white p-6 shadow-soft border border-tumooh-navy/5">' +
                '<h2 class="font-semibold">Details</h2><div class="mt-2">' + details + '</div></section>';
        } else {
            html += '<section class="rounded-2xl bg-white p-6 shadow-soft border border-tumooh-navy/5">' +
                '<p class="text-sm text-tumooh-navy/60">No details added yet.</p></section>';
        }

        viewEl.innerHTML = html;
        viewEl.classList.remove('hidden');
        createEl.classList.add('hidden');
        if (cancelBtn) {
            cancelBtn.classList.add('hidden');
        }
    }

    /* ------------------------------------------------------------------ *
     * Create / edit form state
     * ------------------------------------------------------------------ */

    function setFormHeading(title, subtitle) {
        if (titleEl) {
            titleEl.textContent = title;
        }
        if (subtitleEl) {
            subtitleEl.textContent = subtitle;
        }
        if (submitBtn) {
            submitBtn.textContent = mode === 'edit' ? 'Save changes' : 'Save profile';
        }
    }

    function showCreateForm() {
        mode = 'create';
        setFormHeading(CREATE_TITLE, CREATE_SUBTITLE);
        if (cancelBtn) {
            cancelBtn.classList.add('hidden');
        }
        // Uploads need an existing profile (the endpoints 400 otherwise)
        setUploadsVisibility(false);
        clearUploadErrors();
        viewEl.classList.add('hidden');
        createEl.classList.remove('hidden');
    }

    function fillForm(profile) {
        Array.prototype.forEach.call(form.querySelectorAll('[name]'), function (field) {
            const value = profile[field.name];
            field.value = (value === null || value === undefined) ? '' : String(value);
        });
    }

    function startEdit() {
        if (!currentProfile) {
            return;
        }
        mode = 'edit';
        clearError();
        clearFormError();
        fillForm(currentProfile);
        setUploadsVisibility(true);
        clearUploadErrors();
        refreshUploadWidgets();
        setFormHeading('Edit profile', 'Make your changes and save.');
        if (cancelBtn) {
            cancelBtn.classList.remove('hidden');
        }
        viewEl.classList.add('hidden');
        createEl.classList.remove('hidden');
        createEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
        const first = form.querySelector('[name]');
        if (first) {
            first.focus();
        }
    }

    function backToView() {
        clearFormError();
        if (currentProfile) {
            renderView(currentProfile, currentEmail);
        } else {
            showCreateForm();
        }
    }

    /* ------------------------------------------------------------------ *
     * Photo / CV uploads — edit mode only, uploaded immediately
     * ------------------------------------------------------------------ */

    const PHOTO_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    const MAX_PHOTO_BYTES = 5 * 1024 * 1024;
    const MAX_CV_BYTES = 10 * 1024 * 1024;

    function setUploadsVisibility(canUpload) {
        if (photoField) {
            photoField.classList.toggle('hidden', !canUpload);
        }
        if (cvField) {
            cvField.classList.toggle('hidden', !canUpload);
        }
        if (uploadsHint) {
            uploadsHint.classList.toggle('hidden', canUpload);
        }
    }

    function showUploadError(kind, message) {
        const el = kind === 'photo' ? photoError : cvError;
        if (el) {
            el.textContent = friendly(message);
            el.classList.remove('hidden');
        }
    }

    function clearUploadErrors() {
        [photoError, cvError].forEach(function (el) {
            if (el) {
                el.textContent = '';
                el.classList.add('hidden');
            }
        });
    }

    function fileNameFromUrl(url) {
        const last = String(url).split('/').pop() || '';
        try {
            return decodeURIComponent(last);
        } catch (err) {
            return last;
        }
    }

    // Hidden inputs are the source of truth for what will be saved
    function refreshUploadWidgets() {
        const photo = photoValueInput ? photoValueInput.value.trim() : '';
        const cv = cvValueInput ? cvValueInput.value.trim() : '';

        if (photoPreview) {
            if (photo) {
                photoPreview.innerHTML = '<img src="' + esc(normalizeUrl(photo)) + '" alt="" ' +
                    'class="h-full w-full object-cover" onerror="this.remove()"/>';
                photoPreview.classList.remove('hidden');
            } else {
                photoPreview.innerHTML = '';
                photoPreview.classList.add('hidden');
            }
        }
        if (photoAddBtn) {
            photoAddBtn.textContent = photo ? 'Change' : 'Add photo';
        }
        if (photoRemoveBtn) {
            photoRemoveBtn.classList.toggle('hidden', !photo);
        }

        if (cvName) {
            cvName.textContent = cv ? fileNameFromUrl(cv) : 'No CV yet';
        }
        if (cvAddBtn) {
            cvAddBtn.textContent = cv ? 'Change' : 'Add CV';
        }
        if (cvViewLink) {
            if (cv) {
                cvViewLink.href = normalizeUrl(cv);
                cvViewLink.classList.remove('hidden');
            } else {
                cvViewLink.classList.add('hidden');
            }
        }
        if (cvRemoveBtn) {
            cvRemoveBtn.classList.toggle('hidden', !cv);
        }
    }

    function setUploading(kind, busy) {
        const add = kind === 'photo' ? photoAddBtn : cvAddBtn;
        const remove = kind === 'photo' ? photoRemoveBtn : cvRemoveBtn;
        [add, remove].forEach(function (btn) {
            if (btn) {
                btn.disabled = busy;
                btn.classList.toggle('opacity-60', busy);
            }
        });
    }

    function uploadSelected(kind) {
        const input = kind === 'photo' ? photoFileInput : cvFileInput;
        if (!input || !input.files || !input.files.length) {
            return;
        }
        const file = input.files[0];
        clearUploadErrors();

        if (kind === 'photo') {
            if (PHOTO_TYPES.indexOf(file.type) === -1) {
                showUploadError('photo', 'Please choose a JPG, PNG, WebP or GIF image.');
                input.value = '';
                return;
            }
            if (file.size > MAX_PHOTO_BYTES) {
                showUploadError('photo', 'Image must be 5 MB or smaller.');
                input.value = '';
                return;
            }
        } else {
            const isPdf = file.type === 'application/pdf' || /\.pdf$/i.test(file.name);
            if (!isPdf) {
                showUploadError('cv', 'Please choose a PDF file.');
                input.value = '';
                return;
            }
            if (file.size > MAX_CV_BYTES) {
                showUploadError('cv', 'CV must be 10 MB or smaller.');
                input.value = '';
                return;
            }
        }

        const hint = kind === 'photo' ? photoHint : cvHint;
        const originalHint = hint ? hint.textContent : '';
        setUploading(kind, true);
        if (hint) {
            hint.textContent = 'Uploading…';
        }

        const formData = new FormData();
        formData.append('file', file);
        const endpoint = '/profiles/user/' + userId + (kind === 'photo' ? '/upload-image' : '/upload-cv');

        window.TumoohApi.postForm(endpoint, formData).then(function (saved) {
            if (saved) {
                currentProfile = saved;
                const target = kind === 'photo' ? photoValueInput : cvValueInput;
                if (target) {
                    target.value = saved[kind === 'photo' ? 'profileImage' : 'cvUrl'] || '';
                }
            }
            refreshUploadWidgets();
        }).catch(function (err) {
            showUploadError(kind, err && err.message ? err.message : '');
        }).finally(function () {
            input.value = '';
            if (hint) {
                hint.textContent = originalHint;
            }
            setUploading(kind, false);
        });
    }

    function wireUploads() {
        if (photoAddBtn && photoFileInput) {
            photoAddBtn.addEventListener('click', function () { photoFileInput.click(); });
        }
        if (cvAddBtn && cvFileInput) {
            cvAddBtn.addEventListener('click', function () { cvFileInput.click(); });
        }
        if (photoFileInput) {
            photoFileInput.addEventListener('change', function () { uploadSelected('photo'); });
        }
        if (cvFileInput) {
            cvFileInput.addEventListener('change', function () { uploadSelected('cv'); });
        }
        if (photoRemoveBtn) {
            photoRemoveBtn.addEventListener('click', function () {
                if (photoValueInput) {
                    photoValueInput.value = '';
                }
                refreshUploadWidgets();
            });
        }
        if (cvRemoveBtn) {
            cvRemoveBtn.addEventListener('click', function () {
                if (cvValueInput) {
                    cvValueInput.value = '';
                }
                refreshUploadWidgets();
            });
        }
    }

    wireUploads();

    // The view card is re-rendered as HTML, so listen by delegation
    viewEl.addEventListener('click', function (event) {
        const target = (event.target && event.target.closest)
            ? event.target.closest('#profile-edit')
            : (event.target && event.target.id === 'profile-edit' ? event.target : null);
        if (target) {
            startEdit();
        }
    });

    if (cancelBtn) {
        cancelBtn.addEventListener('click', backToView);
    }

    /* ------------------------------------------------------------------ *
     * Loading / saving
     * ------------------------------------------------------------------ */

    // Email lives on the user record, not the profile — best effort only
    async function loadEmail() {
        try {
            const user = await window.TumoohApi.get('/users/get/' + userId);
            return user && present(user.email) ? user.email : '';
        } catch (err) {
            return '';
        }
    }

    function notifyNav() {
        if (window.TumoohNavProfile && typeof window.TumoohNavProfile.refresh === 'function') {
            window.TumoohNavProfile.refresh();
        }
    }

    async function load() {
        clearError();
        setLoading(true);
        try {
            const profile = await window.TumoohApi.get(PROFILE_PATH);
            const email = await loadEmail();
            renderView(profile || {}, email);
        } catch (err) {
            const message = err && err.message ? err.message : '';
            if (message.indexOf('Profile not found') !== -1) {
                showCreateForm();
            } else {
                showError(message);
            }
        } finally {
            setLoading(false);
        }
    }

    function buildPayload() {
        const payload = {};
        Array.prototype.forEach.call(form.querySelectorAll('[name]'), function (field) {
            const value = (field.value || '').trim();
            if (value === '') {
                return;
            }
            if (field.type === 'number') {
                const parsed = Number(value);
                if (!Number.isNaN(parsed)) {
                    payload[field.name] = parsed;
                }
            } else {
                payload[field.name] = value;
            }
        });
        // The photo/CV hidden inputs are the source of truth in edit mode:
        // always send them, even when empty, so a Remove actually clears
        // the stored value server-side.
        if (mode === 'edit') {
            if (photoValueInput) {
                payload.profileImage = photoValueInput.value.trim();
            }
            if (cvValueInput) {
                payload.cvUrl = cvValueInput.value.trim();
            }
        }
        return payload;
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        clearFormError();
        const savingMode = mode;
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.classList.add('opacity-60', 'cursor-wait');
        }
        setLoading(true);
        try {
            const payload = buildPayload();
            const saved = savingMode === 'edit'
                ? await window.TumoohApi.put(PROFILE_PATH, payload)
                : await window.TumoohApi.post(PROFILE_PATH, payload);
            const email = await loadEmail();
            renderView(saved || {}, email);
            notifyNav();
            viewEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } catch (err) {
            showFormError(err && err.message ? err.message : '');
        } finally {
            setLoading(false);
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.classList.remove('opacity-60', 'cursor-wait');
            }
        }
    });

    load();
})();
