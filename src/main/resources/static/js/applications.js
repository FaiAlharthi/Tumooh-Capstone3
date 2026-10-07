(function () {
    const userId = window.TUMOOH_APPS.userId;
    const PLACEHOLDER_LOGO = '/images/logo-placeholder.png';

    // Labels and colors for each status
    const STATUS = {
        Applied:    { label: 'Applied',     badge: 'bg-sky-50 text-sky-700 ring-sky-200' },
        InProgress: { label: 'In progress', badge: 'bg-amber-50 text-amber-700 ring-amber-200' },
        Offered:    { label: 'Offered',     badge: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
        Rejected:   { label: 'Rejected',    badge: 'bg-rose-50 text-rose-700 ring-rose-200' },
        Withdrawn:  { label: 'Withdrawn',   badge: 'bg-slate-100 text-slate-600 ring-slate-200' }
    };

    let allApplications = [];

    /* ---------- Small helpers ---------- */

    function $(id) {
        return document.getElementById(id);
    }

    function show(el) {
        el.classList.remove('hidden');
    }

    function hide(el) {
        el.classList.add('hidden');
    }

    function escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function formatDate(value) {
        if (!value) return '';
        const date = new Date(value);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    function statusBadge(status) {
        const info = STATUS[status] || { label: status, badge: 'bg-slate-100 text-slate-600 ring-slate-200' };
        return '<span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ring-1 ' + info.badge + '">'
            + escapeHtml(info.label) + '</span>';
    }

    function toast(message) {
        const el = $('toast');
        el.textContent = message;
        show(el);
        clearTimeout(toast.timer);
        toast.timer = setTimeout(function () { hide(el); }, 3000);
    }

    function showPageError(message) {
        $('page-error').textContent = message;
        show($('page-error'));
    }

    /* ---------- Stats ---------- */

    function updateStats() {
        $('stat-total').textContent = allApplications.length;
        Object.keys(STATUS).forEach(function (status) {
            const count = allApplications.filter(function (a) { return a.status === status; }).length;
            $('stat-' + status).textContent = count;
        });
    }

    /* ---------- List ---------- */

    function renderList(applications, emptyTitle, emptyText) {
        const list = $('applications-list');
        hide($('applications-loading'));
        list.innerHTML = '';

        if (!applications || applications.length === 0) {
            $('empty-title').textContent = emptyTitle || 'No applications yet';
            $('empty-text').textContent = emptyText || 'Add your first one, or connect Gmail and we will add them for you.';
            show($('applications-empty'));
            return;
        }
        hide($('applications-empty'));

        //newest first
        const sorted = applications.slice().sort(function (a, b) { return b.id - a.id; });

        sorted.forEach(function (app) {
            const company = app.job.company;
            const logo = company.companyLogoUrl || PLACEHOLDER_LOGO;

            const card = document.createElement('button');
            card.type = 'button';
            card.className = 'w-full text-left rounded-2xl bg-white p-5 shadow-soft border border-tumooh-navy/5 hover:border-tumooh-accent hover:-translate-y-0.5 transition-all flex items-center gap-4';
            card.innerHTML =
                '<img src="' + escapeHtml(logo) + '" alt="" class="h-12 w-12 shrink-0 rounded-xl border border-tumooh-navy/10 object-contain bg-white p-1" onerror="this.src=\'' + PLACEHOLDER_LOGO + '\'"/>' +
                '<div class="flex-1 min-w-0">' +
                '<p class="font-semibold truncate">' + escapeHtml(app.job.position) + '</p>' +
                '<p class="text-sm text-tumooh-navy/60 truncate">' + escapeHtml(company.nameEn) + '</p>' +
                '<p class="mt-1 text-xs text-tumooh-navy/50">Applied on ' + formatDate(app.createdAt) + '</p>' +
                '</div>' +
                '<div class="flex items-center gap-3 shrink-0">' +
                statusBadge(app.status) +
                '<svg class="h-5 w-5 text-tumooh-navy/30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>' +
                '</div>';
            card.addEventListener('click', function () { openDrawer(app); });
            list.appendChild(card);
        });
    }

    function showLoading() {
        $('applications-list').innerHTML = '';
        hide($('applications-empty'));
        show($('applications-loading'));
    }

    async function loadApplications() {
        showLoading();
        try {
            allApplications = await TumoohApi.get('/job-applications/myApplications/' + userId);
            updateStats();
            renderList(allApplications);
        } catch (e) {
            hide($('applications-loading'));
            showPageError(e.message);
        }
    }

    /* ---------- Filters ---------- */

    function setActiveChip(status) {
        document.querySelectorAll('.status-chip').forEach(function (chip) {
            const active = chip.dataset.status === status;
            chip.classList.toggle('bg-tumooh-navy', active);
            chip.classList.toggle('text-white', active);
            chip.classList.toggle('border-tumooh-navy', active);
            chip.classList.toggle('hover:border-tumooh-accent', !active);
        });
    }

    function showFilter(text) {
        $('filter-text').textContent = text;
        show($('filter-info'));
    }

    function clearFilter() {
        hide($('filter-info'));
        setActiveChip('');
        $('period-form').reset();
        $('company-form').reset();
        renderList(allApplications);
    }

    // Runs a filter request. If the backend says nothing was found, we show it as an empty list.
    async function runFilter(request, filterText) {
        showLoading();
        showFilter(filterText);
        try {
            const result = await request();
            renderList(result, 'Nothing found', 'Try another filter.');
        } catch (e) {
            renderList([], 'Nothing found', e.message);
        }
    }

    // #3 Filter by status
    document.querySelectorAll('.status-chip').forEach(function (chip) {
        chip.addEventListener('click', function () {
            const status = chip.dataset.status;
            $('period-form').reset();
            $('company-form').reset();
            if (!status) {
                clearFilter();
                return;
            }
            setActiveChip(status);
            runFilter(function () {
                return TumoohApi.post('/job-applications/status/' + userId, { status: status });
            }, 'Showing: ' + STATUS[status].label);
        });
    });

    // #9 Filter by date range
    $('period-form').addEventListener('submit', function (event) {
        event.preventDefault();
        const startDate = $('period-start').value;
        const endDate = $('period-end').value;
        setActiveChip('');
        $('company-form').reset();
        runFilter(function () {
            return TumoohApi.post('/job-applications/period/' + userId, { startDate: startDate, endDate: endDate });
        }, 'Applied between ' + formatDate(startDate) + ' and ' + formatDate(endDate));
    });

    // #5 History with a company
    $('company-form').addEventListener('submit', function (event) {
        event.preventDefault();
        const companyName = $('company-search').value.trim();
        if (!companyName) return;
        searchCompany(companyName);
    });

    function searchCompany(companyName) {
        setActiveChip('');
        $('period-form').reset();
        $('company-search').value = companyName;
        runFilter(function () {
            return TumoohApi.get('/job-applications/history/' + userId + '/' + encodeURIComponent(companyName));
        }, 'Your history with ' + companyName);
    }

    $('filter-clear').addEventListener('click', clearFilter);

    /* ---------- Add application (Step 3) ---------- */

    function openAddModal() {
        $('add-form').reset();
        hide($('add-error'));
        show($('add-modal'));
        $('add-company').focus();
    }

    function closeAddModal() {
        hide($('add-modal'));
    }

    $('add-btn').addEventListener('click', openAddModal);
    $('add-cancel').addEventListener('click', closeAddModal);

    // Click outside the form closes the modal
    $('add-modal').addEventListener('click', function (event) {
        if (event.target === $('add-modal')) closeAddModal();
    });

    $('add-form').addEventListener('submit', async function (event) {
        event.preventDefault();
        hide($('add-error'));

        const body = {
            companyName: $('add-company').value.trim(),
            position: $('add-position').value.trim(),
            status: $('add-status').value
        };
        const description = $('add-description').value.trim();
        if (description) {
            body.description = description;
        }

        const submit = $('add-submit');
        submit.disabled = true;
        submit.textContent = 'Saving...';
        try {
            await TumoohApi.post('/job-applications/add-manual/' + userId, body);
            closeAddModal();
            toast('Application added');
            hide($('filter-info'));
            setActiveChip('');
            $('period-form').reset();
            $('company-form').reset();
            await loadApplications();
        } catch (e) {
            $('add-error').textContent = e.message;
            show($('add-error'));
        } finally {
            submit.disabled = false;
            submit.textContent = 'Save';
        }
    });

    // Esc key closes the modal
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeAddModal();
        }
    });

    /* ---------- Drawer (Step 4) ---------- */

    let currentApp = null;

    // For PUT and DELETE (tumooh-api.js only has get, post and patch)
    async function apiSend(method, path, body) {
        const options = { method: method, headers: {} };
        if (body !== undefined) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(body);
        }
        const response = await fetch('/api/v1' + path, options);
        const text = await response.text();
        let data = null;
        if (text) {
            try { data = JSON.parse(text); } catch (e) { data = text; }
        }
        if (!response.ok) {
            throw new Error((data && data.message) ? data.message : 'Something went wrong');
        }
        return data;
    }

    function formatDateTime(value) {
        if (!value) return 'Date not set yet';
        const date = new Date(value);
        return date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })
            + ' at ' + date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
    }

    function openDrawer(app) {
        currentApp = app;
        const company = app.job.company;

        const logo = $('drawer-logo');
        logo.onerror = function () { logo.src = PLACEHOLDER_LOGO; };
        logo.src = company.companyLogoUrl || PLACEHOLDER_LOGO;
        $('drawer-company').textContent = company.nameEn;
        $('drawer-position').textContent = app.job.position;
        $('drawer-date').textContent = 'Applied on ' + formatDate(app.createdAt);
        $('drawer-status').value = app.status;

        // Clear old results from the last opened application
        $('next-step-result').innerHTML = '';
        hide($('next-step-result'));
        $('reply-result').innerHTML = '';
        hide($('reply-result'));
        hide($('delete-confirm'));
        show($('delete-btn'));
        updateReplySection(app.status);

        loadInterviews(app.id);

        show($('drawer-overlay'));
        $('drawer').classList.remove('translate-x-full');
        $('drawer').setAttribute('aria-hidden', 'false');
        document.body.classList.add('overflow-hidden');
    }

    function closeDrawer() {
        hide($('drawer-overlay'));
        $('drawer').classList.add('translate-x-full');
        $('drawer').setAttribute('aria-hidden', 'true');
        document.body.classList.remove('overflow-hidden');
        currentApp = null;
    }

    // The reply section only shows for Offered and Rejected (same rule as the backend)
    function updateReplySection(status) {
        if (status === 'Offered' || status === 'Rejected') {
            show($('reply-section'));
        } else {
            hide($('reply-section'));
        }
    }

    function resetFiltersUI() {
        hide($('filter-info'));
        setActiveChip('');
        $('period-form').reset();
        $('company-form').reset();
    }

    $('drawer-close').addEventListener('click', closeDrawer);
    $('drawer-overlay').addEventListener('click', closeDrawer);
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && currentApp) closeDrawer();
    });

    // #10 Interviews of this application
    async function loadInterviews(applicationId) {
        const box = $('drawer-interviews');
        box.innerHTML = '<p class="text-sm text-tumooh-navy/50">Loading interviews...</p>';
        try {
            const interviews = await TumoohApi.get('/job-applications/interviews/' + userId + '/' + applicationId);
            if (!currentApp || currentApp.id !== applicationId) return;

            const sorted = interviews.slice().sort(function (a, b) {
                if (!a.interviewDate) return 1;
                if (!b.interviewDate) return -1;
                return new Date(a.interviewDate) - new Date(b.interviewDate);
            });

            box.innerHTML = sorted.map(function (interview) {
                return '<div class="flex items-start gap-3 rounded-xl border border-tumooh-navy/10 p-4">' +
                    '<div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-tumooh-accent/15 text-tumooh-accent">' +
                    '<svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>' +
                    '</div>' +
                    '<div class="flex-1 min-w-0">' +
                    '<p class="text-sm font-semibold">' + escapeHtml(formatDateTime(interview.interviewDate)) + '</p>' +
                    '<p class="mt-0.5 text-xs text-tumooh-navy/60">' + escapeHtml(interview.status || 'PENDING') + '</p>' +
                    '</div>' +
                    '</div>';
            }).join('');
        } catch (e) {
            if (!currentApp || currentApp.id !== applicationId) return;
            box.innerHTML = '<p class="rounded-xl bg-tumooh-light px-4 py-3 text-sm text-tumooh-navy/60">' + escapeHtml(e.message) + '</p>';
        }
    }

    // #2 Update status
    $('drawer-save').addEventListener('click', async function () {
        if (!currentApp) return;
        const status = $('drawer-status').value;
        if (status === currentApp.status) {
            toast('Nothing to save');
            return;
        }
        const button = $('drawer-save');
        button.disabled = true;
        try {
            await apiSend('PUT', '/job-applications/update-status/' + userId + '/' + currentApp.id, { status: status });
            currentApp.status = status;
            updateReplySection(status);
            $('reply-result').innerHTML = '';
            hide($('reply-result'));
            toast('Status updated');
            resetFiltersUI();
            await loadApplications();
        } catch (e) {
            toast(e.message);
        } finally {
            button.disabled = false;
        }
    });

    // #6 Next step (AI)
    $('next-step-btn').addEventListener('click', async function () {
        if (!currentApp) return;
        const button = $('next-step-btn');
        const result = $('next-step-result');
        const applicationId = currentApp.id;

        button.disabled = true;
        button.textContent = 'Thinking...';
        result.innerHTML = '<p class="text-sm text-tumooh-navy/50">AI is reading your application...</p>';
        show(result);
        try {
            const data = await TumoohApi.get('/job-applications/next-step/' + userId + '/' + applicationId);
            if (!currentApp || currentApp.id !== applicationId) return;
            result.innerHTML =
                '<div class="rounded-xl bg-tumooh-accent/10 p-4">' +
                '<p class="font-semibold">' + escapeHtml(data.nextStep) + '</p>' +
                '<p class="mt-2 text-sm text-tumooh-navy/70">' + escapeHtml(data.reason) + '</p>' +
                '</div>';
        } catch (e) {
            result.innerHTML = '<p class="text-sm text-red-600">' + escapeHtml(e.message) + '</p>';
        } finally {
            button.disabled = false;
            button.textContent = 'Ask AI';
        }
    });

    // #8 Reply message (AI)
    let replies = [];

    $('reply-btn').addEventListener('click', async function () {
        if (!currentApp) return;
        const button = $('reply-btn');
        const result = $('reply-result');
        const applicationId = currentApp.id;

        button.disabled = true;
        button.textContent = 'Writing...';
        result.innerHTML = '<p class="text-sm text-tumooh-navy/50">AI is writing your email...</p>';
        show(result);
        try {
            replies = await TumoohApi.get('/job-applications/reply/' + userId + '/' + applicationId);
            if (!currentApp || currentApp.id !== applicationId) return;
            result.innerHTML = replies.map(function (reply, index) {
                const title = replies.length > 1 ? '<p class="text-xs font-semibold uppercase tracking-wide text-tumooh-accent">Option ' + (index + 1) + '</p>' : '';
                return '<div class="rounded-xl bg-tumooh-light p-4 space-y-2">' +
                    '<div class="flex items-start justify-between gap-3">' +
                    '<div class="min-w-0">' + title +
                    '<p class="text-sm font-semibold">' + escapeHtml(reply.subject) + '</p>' +
                    '</div>' +
                    '<button type="button" data-copy="' + index + '" class="copy-reply shrink-0 rounded-lg bg-white px-3 py-1.5 text-xs font-semibold text-tumooh-navy shadow-sm hover:text-tumooh-accent">Copy</button>' +
                    '</div>' +
                    '<p class="whitespace-pre-line text-sm text-tumooh-navy/80">' + escapeHtml(reply.reply) + '</p>' +
                    '</div>';
            }).join('');
        } catch (e) {
            result.innerHTML = '<p class="text-sm text-red-600">' + escapeHtml(e.message) + '</p>';
        } finally {
            button.disabled = false;
            button.textContent = 'Write it';
        }
    });

    $('reply-result').addEventListener('click', async function (event) {
        const button = event.target.closest('.copy-reply');
        if (!button) return;
        const reply = replies[Number(button.dataset.copy)];
        try {
            await navigator.clipboard.writeText('Subject: ' + reply.subject + '\n\n' + reply.reply);
            toast('Copied');
        } catch (e) {
            toast('Could not copy, please select the text and copy it');
        }
    });

    // #5 All applications at this company
    $('history-btn').addEventListener('click', function () {
        if (!currentApp) return;
        const companyName = currentApp.job.company.nameEn;
        closeDrawer();
        searchCompany(companyName);
        $('applications-list').scrollIntoView({ behavior: 'smooth', block: 'start' });
    });

    // Delete application
    $('delete-btn').addEventListener('click', function () {
        hide($('delete-btn'));
        show($('delete-confirm'));
    });

    $('delete-cancel').addEventListener('click', function () {
        hide($('delete-confirm'));
        show($('delete-btn'));
    });

    $('delete-yes').addEventListener('click', async function () {
        if (!currentApp) return;
        const button = $('delete-yes');
        button.disabled = true;
        try {
            await apiSend('DELETE', '/job-applications/delete/' + userId + '/' + currentApp.id);
            closeDrawer();
            toast('Application deleted');
            resetFiltersUI();
            await loadApplications();
        } catch (e) {
            toast(e.message);
        } finally {
            button.disabled = false;
        }
    });

    /* ---------- AI insights (Step 5) ---------- */

    const insightsLoadingHtml = $('insights-loading').innerHTML;

    function fillList(id, items) {
        $(id).innerHTML = (items || []).map(function (item) {
            return '<li class="flex gap-2"><span class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-current opacity-60"></span><span>' + escapeHtml(item) + '</span></li>';
        }).join('');
    }

    $('insights-btn').addEventListener('click', async function () {
        const button = $('insights-btn');
        const panel = $('insights-panel');
        show(panel);
        hide($('insights-content'));
        $('insights-loading').innerHTML = insightsLoadingHtml;
        show($('insights-loading'));
        panel.scrollIntoView({ behavior: 'smooth', block: 'start' });

        button.disabled = true;
        try {
            const data = await TumoohApi.get('/job-applications/insights/' + userId);

            const industries = data.industries || [];
            $('insights-industries').innerHTML = industries.length === 0 ? '' :
                '<span class="text-sm text-tumooh-navy/60 mr-1">Industries you applied to:</span>' +
                industries.map(function (industry) {
                    return '<span class="rounded-full bg-tumooh-light px-3 py-1 text-xs font-semibold">' + escapeHtml(industry) + '</span>';
                }).join('');

            fillList('insights-strengths', data.strengths);
            fillList('insights-concerns', data.concerns);
            fillList('insights-recommendations', data.recommendations);

            hide($('insights-loading'));
            show($('insights-content'));
        } catch (e) {
            $('insights-loading').innerHTML = '<p class="text-sm text-red-600">' + escapeHtml(e.message) + '</p>';
        } finally {
            button.disabled = false;
        }
    });

    $('insights-close').addEventListener('click', function () {
        hide($('insights-panel'));
    });

    /* ---------- Gmail (Step 5) ---------- */

    function setGmailBadge(connected) {
        const badge = $('gmail-badge');
        badge.className = 'rounded-full px-3 py-1 text-xs font-semibold inline-flex items-center gap-1.5 '
            + (connected ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600');
        badge.innerHTML = '<span class="h-1.5 w-1.5 rounded-full ' + (connected ? 'bg-emerald-500' : 'bg-slate-400') + '"></span>'
            + (connected ? 'Connected' : 'Not connected');
    }

    async function loadGmailStatus() {
        try {
            const status = await TumoohApi.get('/gmail/status/' + userId);
            hide($('gmail-loading'));
            setGmailBadge(status.connected);
            $('gmail-update-btn').textContent = 'Update App Password';

            if (status.connected) {
                $('gmail-address').textContent = status.gmailAddress;
                $('gmail-last-sync').textContent = status.lastSyncedAt ? formatDateTime(status.lastSyncedAt) : 'Not checked yet';
                show($('gmail-connected'));
                hide($('gmail-setup'));
            } else {
                hide($('gmail-connected'));
                show($('gmail-setup'));
            }
        } catch (e) {
            $('gmail-loading').textContent = e.message;
        }
    }

    // Show or hide the App Password
    $('toggle-password').addEventListener('click', function () {
        const input = $('app-password');
        const showing = input.type === 'text';
        input.type = showing ? 'password' : 'text';
        $('toggle-password').textContent = showing ? 'Show' : 'Hide';
    });

    // Connect Gmail
    $('gmail-form').addEventListener('submit', async function (event) {
        event.preventDefault();
        hide($('connect-error'));
        const button = $('connect-btn');
        button.disabled = true;
        button.textContent = 'Connecting...';
        try {
            await TumoohApi.post('/gmail/connect/' + userId, { appPassword: $('app-password').value.trim() });
            $('gmail-form').reset();
            toast('Gmail connected');
            await loadGmailStatus();
        } catch (e) {
            $('connect-error').textContent = e.message;
            show($('connect-error'));
        } finally {
            button.disabled = false;
            button.textContent = 'Connect Gmail';
        }
    });

    // Open or close the form to update the App Password
    $('gmail-update-btn').addEventListener('click', function () {
        const setup = $('gmail-setup');
        if (setup.classList.contains('hidden')) {
            show(setup);
            $('gmail-update-btn').textContent = 'Cancel';
        } else {
            hide(setup);
            $('gmail-update-btn').textContent = 'Update App Password';
        }
    });

    // Check now for new emails
    $('sync-btn').addEventListener('click', async function () {
        const button = $('sync-btn');
        const result = $('sync-result');
        button.disabled = true;
        button.textContent = 'Checking...';
        hide(result);
        try {
            const response = await TumoohApi.post('/gmail/sync/' + userId);
            result.className = 'rounded-xl bg-emerald-50 px-4 py-3 text-sm text-emerald-800';
            result.textContent = response.message;
            show(result);
            await loadGmailStatus();
            resetFiltersUI();
            await loadApplications();
        } catch (e) {
            result.className = 'rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700';
            result.textContent = e.message;
            show(result);
        } finally {
            button.disabled = false;
            button.textContent = 'Check now';
        }
    });

    /* ---------- Start ---------- */
    setActiveChip('');
    loadApplications();
    loadGmailStatus();
})();