(function () {
    const config = window.TUMOOH_FEEDBACK || {};
    const sessionId = config.sessionId;
    if (!sessionId) {
        return;
    }

    const loadingEl = document.getElementById('feedback-loading');
    const errorEl = document.getElementById('feedback-error');
    const contentEl = document.getElementById('feedback-content');
    function showError(message) {
        if (loadingEl) {
            loadingEl.classList.add('hidden');
        }
        if (contentEl) {
            contentEl.classList.add('hidden');
        }
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.classList.remove('hidden');
        }
    }

    function renderResults(data) {
        if (loadingEl) {
            loadingEl.classList.add('hidden');
        }
        if (!data.evaluationAvailable) {
            showError(data.message || 'AI feedback is not available yet.');
            return;
        }
        if (errorEl) {
            errorEl.classList.add('hidden');
        }
        if (contentEl) {
            contentEl.classList.remove('hidden');
        }
        setText('score-value', formatNum(data.aiScore));
        setText('clarity-value', data.speechClarity != null ? formatNum(data.speechClarity) : '—');
        setText('strengths-text', data.strengths);
        setText('weaknesses-text', data.weaknesses);
        setText('body-language-text', data.bodyLanguageTips);
        const scoreBar = document.getElementById('score-bar');
        if (scoreBar && data.aiScore != null) {
            scoreBar.style.width = Math.min(100, data.aiScore) + '%';
        }
    }

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value || '—';
        }
    }

    function formatNum(n) {
        return Number(n).toFixed(1);
    }

    async function loadFeedback() {
        let slowTimer = setTimeout(function () {
            if (loadingEl && !loadingEl.classList.contains('hidden')) {
                loadingEl.textContent = 'Still working… Gemini can take up to a minute, or fail fast if quota is exceeded.';
            }
        }, 20000);

        const controller = new AbortController();
        const abortTimer = setTimeout(function () { controller.abort(); }, 120000);

        try {
            const response = await fetch('/api/v1/mock-interviews/' + sessionId + '/evaluate', {
                method: 'POST',
                signal: controller.signal
            });
            clearTimeout(slowTimer);
            clearTimeout(abortTimer);
            const text = await response.text();
            let results = null;
            if (text) {
                try {
                    results = JSON.parse(text);
                } catch (e) {
                    throw new Error(text);
                }
            }
            if (!response.ok) {
                throw new Error((results && results.message) ? results.message : 'Evaluation failed');
            }
            renderResults(results);
        } catch (err) {
            clearTimeout(slowTimer);
            clearTimeout(abortTimer);
            if (err.name === 'AbortError') {
                showError('Evaluation timed out. Try again in Postman or refresh when Gemini is available.');
            } else {
                showError(err.message || 'Could not load feedback.');
            }
        }
    }

    loadFeedback();
})();
