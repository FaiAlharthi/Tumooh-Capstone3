(function () {
    const userId = window.TUMOOH_CAREER && window.TUMOOH_CAREER.userId;
    const listEl = document.getElementById('company-list');
    const errorEl = document.getElementById('career-error');

    if (!listEl || !userId) {
        return;
    }

    function renderRecommendations(recommendations) {
        listEl.innerHTML = (recommendations || []).map(function (c) {
            return '<article class="rounded-2xl bg-white p-5 border border-tumooh-navy/10 shadow-soft">' +
                '<h3 class="font-semibold text-lg">' + c.name + '</h3>' +
                '<p class="text-sm text-tumooh-navy/60 mt-1">' + (c.industry || '') + '</p>' +
                '<p class="mt-3 text-tumooh-navy/80">' + (c.reason || '') + '</p></article>';
        }).join('') || '<p class="text-tumooh-navy/60">No recommendations returned.</p>';
    }

    async function loadRecommendations() {
        if (errorEl) {
            errorEl.classList.add('hidden');
        }
        listEl.innerHTML = '<p class="text-tumooh-navy/60">Loading recommendations…</p>';
        try {
            const data = await window.TumoohApi.post('/ai/users/' + userId + '/company-recommendations');
            renderRecommendations(data.recommendations);
        } catch (err) {
            listEl.innerHTML = '';
            if (errorEl) {
                errorEl.textContent = err.message;
                errorEl.classList.remove('hidden');
            }
        }
    }

    loadRecommendations();
})();
