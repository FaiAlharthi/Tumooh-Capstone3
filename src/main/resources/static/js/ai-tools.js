/**
 * AI applicant tools — shared page engine.
 *
 * Each tool page sets:  window.TUMOOH_AI = { tool: '<toolName>' };
 * and provides:
 *   <form id="ai-form" data-endpoint="/ai/...">
 *   <p id="ai-error" class="hidden" role="alert"></p>
 *   <button id="ai-submit" type="submit">
 *   <div id="ai-loading" class="hidden">
 *   <div id="ai-result" class="hidden space-y-4"></div>
 */
(function () {
    'use strict';

    /* ------------------------------------------------------------------ *
     * Formatting helpers (all output is HTML-escaped)
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
        if (typeof value === 'string') {
            return value.trim() !== '';
        }
        return true;
    }

    function paragraphs(text) {
        if (!present(text)) {
            return '';
        }
        return String(text)
            .split(/\n+/)
            .filter(function (line) { return line.trim() !== ''; })
            .map(function (line) {
                return '<p class="mt-2 text-sm leading-relaxed text-tumooh-navy/80">' + esc(line.trim()) + '</p>';
            })
            .join('');
    }

    function list(items) {
        if (!Array.isArray(items)) {
            return '';
        }
        const usable = items.filter(present);
        if (!usable.length) {
            return '';
        }
        return '<ul class="mt-2 space-y-1.5">' + usable.map(function (item) {
            return '<li class="flex gap-2 text-sm text-tumooh-navy/80">' +
                '<span class="text-tumooh-accent" aria-hidden="true">•</span>' +
                '<span>' + esc(item) + '</span></li>';
        }).join('') + '</ul>';
    }

    function card(title, body) {
        if (!present(body)) {
            return '';
        }
        return '<section class="rounded-2xl bg-white p-6 shadow-soft border border-tumooh-navy/5">' +
            (present(title) ? '<h2 class="font-semibold">' + esc(title) + '</h2>' : '') +
            body +
            '</section>';
    }

    function score(label, value, max) {
        if (!present(value)) {
            return '';
        }
        return '<section class="rounded-2xl bg-white p-6 shadow-soft border border-tumooh-navy/5 flex items-center gap-5">' +
            '<div class="text-4xl font-bold text-tumooh-accent">' + esc(value) +
            '<span class="text-lg font-medium text-tumooh-navy/40">/' + esc(max || 100) + '</span></div>' +
            '<div class="text-sm font-medium text-tumooh-navy/70">' + esc(label) + '</div>' +
            '</section>';
    }

    function chip(value) {
        if (!present(value)) {
            return '';
        }
        const key = String(value).toUpperCase();
        let cls = 'bg-tumooh-navy/5 text-tumooh-navy/70 border-tumooh-navy/10';
        if (key === 'HIGH') {
            cls = 'bg-red-50 text-red-700 border-red-200';
        } else if (key === 'MEDIUM') {
            cls = 'bg-amber-50 text-amber-700 border-amber-200';
        } else if (key === 'LOW') {
            cls = 'bg-emerald-50 text-emerald-700 border-emerald-200';
        }
        return '<span class="inline-block rounded-full border px-2.5 py-0.5 text-xs font-semibold ' + cls + '">' + esc(value) + '</span>';
    }

    function keywordChips(items) {
        if (!Array.isArray(items)) {
            return '';
        }
        const usable = items.filter(present);
        if (!usable.length) {
            return '';
        }
        return '<div class="mt-3 flex flex-wrap gap-2">' + usable.map(function (item) {
            return '<span class="rounded-full border border-tumooh-navy/10 bg-tumooh-navy/5 px-3 py-1 text-xs font-medium text-tumooh-navy/70">' + esc(item) + '</span>';
        }).join('') + '</div>';
    }

    function money(value, currency) {
        if (!present(value)) {
            return '—';
        }
        const amount = Number(value).toLocaleString();
        return present(currency) ? amount + ' ' + esc(currency) : amount;
    }

    function header(title, subtitle, extra) {
        return card('', '<div class="flex flex-wrap items-center justify-between gap-3">' +
            '<div><h2 class="text-lg font-semibold">' + esc(title) + '</h2>' +
            (present(subtitle) ? '<p class="mt-1 text-sm text-tumooh-navy/60">' + esc(subtitle) + '</p>' : '') +
            '</div>' + (extra || '') + '</div>');
    }

    /* ------------------------------------------------------------------ *
     * Result renderers — one per tool, matching each response DTO
     * ------------------------------------------------------------------ */

    const renderers = {

        coverLetter: function (d) {
            let out = header(d.jobTitle || 'Cover letter', d.targetCompany, chip(d.tone));
            out += card('Subject line', present(d.subjectLine)
                ? '<p class="mt-2 font-medium text-tumooh-accent">' + esc(d.subjectLine) + '</p>' : '');
            out += card('Cover letter', paragraphs(d.coverLetter));
            out += card('Key points used', list(d.keyPointsUsed));
            return out;
        },

        cvRevise: function (d) {
            let out = header('CV revision', d.fileName, '');
            out += score('ATS score', d.atsScore);
            out += card('Key strengths', list(d.keyStrengths));
            out += card('Areas for improvement', list(d.areasForImprovement));
            out += card('Revised professional summary', paragraphs(d.revisedProfessionalSummary));
            out += card('Revised bullet points', list(d.revisedBulletPoints));
            out += card('Formatting and tone feedback', paragraphs(d.formattingAndToneFeedback));
            return out;
        },

        skillGap: function (d) {
            let out = header(d.targetRole || 'Skill gap analysis', '', '');
            out += score('Readiness for this role', d.readinessScore);
            out += card('Current strengths', list(d.currentStrengths));

            if (Array.isArray(d.gaps) && d.gaps.length) {
                out += card('Gaps to close', '<div class="mt-3 space-y-3">' + d.gaps.map(function (gap) {
                    return '<div class="rounded-xl border border-tumooh-navy/10 p-4">' +
                        '<div class="flex items-center justify-between gap-3">' +
                        '<span class="font-medium text-sm">' + esc(gap.skill) + '</span>' + chip(gap.priority) + '</div>' +
                        (present(gap.reason) ? '<p class="mt-1.5 text-sm text-tumooh-navy/70">' + esc(gap.reason) + '</p>' : '') +
                        '</div>';
                }).join('') + '</div>');
            }

            if (Array.isArray(d.learningPlan) && d.learningPlan.length) {
                out += card('Learning plan', '<div class="mt-3 space-y-3">' + d.learningPlan.map(function (item) {
                    return '<div class="rounded-xl border border-tumooh-navy/10 p-4">' +
                        '<p class="text-sm font-semibold">' + esc(item.skill) + '</p>' +
                        (present(item.suggestion) ? '<p class="mt-1 text-sm text-tumooh-navy/70">' + esc(item.suggestion) + '</p>' : '') +
                        '</div>';
                }).join('') + '</div>');
            }

            out += card('Overall assessment', paragraphs(d.overallAssessment));
            return out;
        },

        salaryBenchmark: function (d) {
            const currency = d.currency;
            let out = header(d.targetRole || 'Salary benchmark', d.city, chip(d.experienceLevel));

            if (d.estimatedRange) {
                out += card('Estimated range', '<div class="mt-3 grid grid-cols-3 gap-3 text-center">' +
                    '<div class="rounded-xl border border-tumooh-navy/10 p-4"><p class="text-xs text-tumooh-navy/60">Minimum</p>' +
                    '<p class="mt-1 font-semibold">' + money(d.estimatedRange.min, currency) + '</p></div>' +
                    '<div class="rounded-xl border border-tumooh-accent/40 bg-tumooh-accent/5 p-4"><p class="text-xs text-tumooh-navy/60">Typical</p>' +
                    '<p class="mt-1 font-semibold text-tumooh-accent">' + money(d.estimatedRange.typical, currency) + '</p></div>' +
                    '<div class="rounded-xl border border-tumooh-navy/10 p-4"><p class="text-xs text-tumooh-navy/60">Maximum</p>' +
                    '<p class="mt-1 font-semibold">' + money(d.estimatedRange.max, currency) + '</p></div>' +
                    '</div>');
            }

            if (d.percentileBands) {
                out += card('Percentile bands', '<div class="mt-3 grid grid-cols-3 gap-3 text-center">' +
                    '<div class="rounded-xl border border-tumooh-navy/10 p-4"><p class="text-xs text-tumooh-navy/60">25th</p>' +
                    '<p class="mt-1 font-semibold">' + money(d.percentileBands.p25, currency) + '</p></div>' +
                    '<div class="rounded-xl border border-tumooh-navy/10 p-4"><p class="text-xs text-tumooh-navy/60">50th</p>' +
                    '<p class="mt-1 font-semibold">' + money(d.percentileBands.p50, currency) + '</p></div>' +
                    '<div class="rounded-xl border border-tumooh-navy/10 p-4"><p class="text-xs text-tumooh-navy/60">75th</p>' +
                    '<p class="mt-1 font-semibold">' + money(d.percentileBands.p75, currency) + '</p></div>' +
                    '</div>');
            }

            out += card('Factors affecting pay', list(d.factorsAffectingPay));
            out += card('Negotiation tips', list(d.negotiationTips));
            out += card('Notes', paragraphs([d.confidence, d.disclaimer].filter(present).join('\n')));
            return out;
        },

        starAnswer: function (d) {
            let out = header('STAR answer', d.question, '');
            out += card('Situation', paragraphs(d.situation));
            out += card('Task', paragraphs(d.task));
            out += card('Action', paragraphs(d.action));
            out += card('Result', paragraphs(d.result));
            out += card('Full answer', paragraphs(d.starAnswer));
            out += card('Strengths revealed', list(d.strengthsRevealed));
            out += card('What to polish', list(d.whatToPolish));
            out += card('Tips', list(d.tips));
            return out;
        },

        interviewPrep: function (d) {
            let out = header(d.jobTitle || 'Interview prep', d.targetCompany, '');
            out += card('Overall strategy', paragraphs(d.overallStrategy));
            out += card('Preparation tips', list(d.keyPreparationTips));
            out += card('Common interview questions', list(d.commonInterviewQuestions));
            out += card('Skills to highlight', list(d.recommendedSkillsToHighlight));
            return out;
        },

        linkedin: function (d) {
            let out = header(d.candidateName || 'LinkedIn profile', '', '');
            out += card('Headline ideas', list(d.headlines));
            out += card('About section', paragraphs(d.aboutSection));
            out += card('Keywords', keywordChips(d.keywords));
            out += card('Tip on usage', paragraphs(d.tipOnUsage));
            return out;
        },

        careerPivot: function (d) {
            let out = header(d.targetRole || 'Career pivot', '', '');
            out += score('Feasibility score', d.feasibilityScore);
            out += card('Overall verdict', paragraphs(d.overallVerdict));
            out += card('Transferable skills', list(d.transferableSkills));
            out += card('Gaps', list(d.gaps));

            if (Array.isArray(d.pivotPaths) && d.pivotPaths.length) {
                out += card('Pivot paths', '<div class="mt-3 space-y-3">' + d.pivotPaths.map(function (path) {
                    return '<div class="rounded-xl border border-tumooh-navy/10 p-4">' +
                        '<div class="flex flex-wrap items-center justify-between gap-3">' +
                        '<span class="font-semibold text-sm">' + esc(path.path) + '</span>' +
                        '<span class="flex items-center gap-2">' + chip(path.difficulty) +
                        (present(path.estimatedTimeline)
                            ? '<span class="text-xs text-tumooh-navy/60">' + esc(path.estimatedTimeline) + '</span>' : '') +
                        '</span></div>' + list(path.steps) + '</div>';
                }).join('') + '</div>');
            }

            out += card('Risks', list(d.risks));
            return out;
        },

        companyBrief: function (d) {
            const subtitle = [d.industry, d.role].filter(present).join(' · ');
            let out = header(d.companyName || 'Company brief', subtitle, '');
            out += card('Culture highlights', list(d.cultureHighlights));
            out += card('Work environment', paragraphs(d.workEnvironment));
            out += card('Interview insights', list(d.interviewInsights));
            out += card('Common perks', list(d.commonPerks));
            out += card('Research tips', list(d.researchTips));
            out += card('Source', paragraphs(d.dataSourceNote));
            return out;
        }
    };

    // Exposed so the renderers can be inspected/tested in isolation
    window.TumoohAi = { renderers: renderers };

    /* ------------------------------------------------------------------ *
     * Page wiring
     * ------------------------------------------------------------------ */

    const config = window.TUMOOH_AI || {};
    const toolName = config.tool;
    const form = document.getElementById('ai-form');
    const errorEl = document.getElementById('ai-error');
    const loadingEl = document.getElementById('ai-loading');
    const resultEl = document.getElementById('ai-result');
    const submitBtn = document.getElementById('ai-submit');

    if (!form || !toolName || !renderers[toolName]) {
        return;
    }

    const isUpload = toolName === 'cvRevise';

    // Turns raw server messages into something a person can act on
    function friendlyMessage(message) {
        const generic = !message
            || message === 'Request failed'
            || message === 'Internal Server Error'
            || message === 'Not Found'
            || message === 'Bad Request';
        if (generic) {
            return 'The AI service is unavailable right now. Please try again in a moment.';
        }
        return message;
    }

    function showError(message) {
        if (errorEl) {
            errorEl.textContent = friendlyMessage(message);
            errorEl.classList.remove('hidden');
        }
    }

    function clearError() {
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.classList.add('hidden');
        }
    }

    function setLoading(on) {
        if (loadingEl) {
            loadingEl.classList.toggle('hidden', !on);
        }
        if (submitBtn) {
            submitBtn.disabled = on;
            submitBtn.classList.toggle('opacity-60', on);
            submitBtn.classList.toggle('cursor-wait', on);
        }
    }

    function buildPayload() {
        const payload = {};
        Array.prototype.forEach.call(form.querySelectorAll('[name]'), function (field) {
            if (field.type === 'file' || field.type === 'submit' || field.type === 'button') {
                return;
            }
            const value = (field.value || '').trim();
            if (value === '') {
                return;
            }
            if (field.type === 'number' || field.dataset.numeric === 'true') {
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

    // Company brief needs a companyId chosen from the existing companies list
    async function loadCompanies() {
        const select = form.querySelector('[name="companyId"]');
        if (!select) {
            return;
        }
        select.disabled = true;
        try {
            const companies = await window.TumoohApi.get('/companies/get');
            const options = (Array.isArray(companies) ? companies : [])
                .filter(function (company) { return company && company.id; })
                .map(function (company) {
                    const label = company.nameEn || company.nameAr || ('Company #' + company.id);
                    return '<option value="' + esc(company.id) + '">' + esc(label) + '</option>';
                })
                .join('');
            select.innerHTML = options || '<option value="">No companies available</option>';
        } catch (err) {
            showError('Could not load companies: ' + err.message);
        } finally {
            select.disabled = false;
        }
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        clearError();
        if (resultEl) {
            resultEl.innerHTML = '';
            resultEl.classList.add('hidden');
        }

        let data;
        setLoading(true);
        try {
            if (isUpload) {
                const fileInput = form.querySelector('input[type="file"]');
                const file = fileInput && fileInput.files.length ? fileInput.files[0] : null;
                if (!file) {
                    throw new Error('Please choose a PDF file first.');
                }
                const formData = new FormData();
                formData.append('file', file);
                data = await window.TumoohApi.postForm(form.dataset.endpoint, formData);
            } else {
                data = await window.TumoohApi.post(form.dataset.endpoint, buildPayload());
            }
        } catch (err) {
            showError(err && err.message ? err.message : '');
            return;
        } finally {
            setLoading(false);
        }

        if (resultEl) {
            resultEl.innerHTML = renderers[toolName](data || {});
            resultEl.classList.remove('hidden');
            resultEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    });

    if (toolName === 'companyBrief') {
        loadCompanies();
    }
})();
