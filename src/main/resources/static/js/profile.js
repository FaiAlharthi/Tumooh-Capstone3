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
