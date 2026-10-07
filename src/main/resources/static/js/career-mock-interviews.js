(function () {
    const userId = window.TUMOOH_CAREER && window.TUMOOH_CAREER.userId;
    if (!userId) {
        return;
    }

    const summaryEl = document.getElementById('mock-summary');
    const listEl = document.getElementById('mock-list');
    const errorEl = document.getElementById('career-error');

    function formatDateTime(value) {
        if (value == null || value === '') {
            return '—';
        }
        var d;
        if (Array.isArray(value)) {
            d = new Date(value[0], value[1] - 1, value[2], value[3] || 0, value[4] || 0);
        } else {
            d = new Date(String(value).trim().replace(' ', 'T'));
        }
        if (Number.isNaN(d.getTime())) {
            return String(value);
        }
        return d.toLocaleString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: 'numeric',
            minute: '2-digit'
        });
    }

    function formatScore(score) {
        if (score == null) {
            return 'Not evaluated';
        }
        return Number(score).toFixed(1) + ' / 100';
    }

    function escapeHtml(text) {
        return String(text == null ? '' : text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    async function load() {
        try {
            const summary = await window.TumoohApi.get('/users/' + userId + '/mock-interviews/summary');
            if (summaryEl) {
                summaryEl.innerHTML =
                    '<p><strong>Sessions:</strong> ' + summary.totalSessions + '</p>' +
                    '<p><strong>Average score:</strong> ' +
                    (summary.averageAiScore != null ? Number(summary.averageAiScore).toFixed(1) : '—') + '</p>' +
                    '<p><strong>Last session:</strong> ' + escapeHtml(formatDateTime(summary.lastSessionAt)) + '</p>';
            }
            const sessions = await window.TumoohApi.get('/mock-interviews/get/user/' + userId);
            if (!listEl) {
                return;
            }
            if (!Array.isArray(sessions) || !sessions.length) {
                listEl.innerHTML = '<li class="text-tumooh-navy/60 py-4">No sessions yet.</li>';
                return;
            }
            listEl.innerHTML = sessions.map(function (s) {
                var title = escapeHtml(s.jobTitle || 'Mock session');
                var when = escapeHtml(formatDateTime(s.createdAt));
                var score = formatScore(s.aiScore);
                var feedbackLabel = s.aiScore != null ? 'View feedback' : 'Open session';
                return '<li class="rounded-xl border border-tumooh-navy/10 bg-white p-4 shadow-soft flex flex-wrap items-center justify-between gap-3">' +
                    '<div class="min-w-0">' +
                    '<p class="font-semibold text-tumooh-navy">' + title + '</p>' +
                    '<p class="text-sm text-tumooh-navy/60 mt-1">Session #' + s.id + ' · ' + when + '</p>' +
                    '<p class="text-sm text-tumooh-navy/80 mt-1"><span class="text-tumooh-navy/60">Score:</span> ' + escapeHtml(score) + '</p>' +
                    '</div>' +
                    '<a class="shrink-0 text-tumooh-accent font-medium hover:underline" href="/mock-interview/feedback/' + s.id + '">' +
                    feedbackLabel + '</a></li>';
            }).join('');
        } catch (err) {
            if (errorEl) {
                errorEl.textContent = err.message;
                errorEl.classList.remove('hidden');
            }
            if (listEl) {
                listEl.innerHTML = '<li class="text-red-600 text-sm">Could not load sessions.</li>';
            }
        }
    }

    load();
})();
