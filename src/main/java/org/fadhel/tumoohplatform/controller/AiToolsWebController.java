package org.fadhel.tumoohplatform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ai-tools")
public class AiToolsWebController {

    @GetMapping
    public String hub() {
        return "ai/hub";
    }

    @GetMapping("/cover-letter")
    public String coverLetter() {
        return "ai/cover-letter";
    }

    @GetMapping("/cv-revise")
    public String cvRevise() {
        return "ai/cv-revise";
    }

    @GetMapping("/skill-gap")
    public String skillGap() {
        return "ai/skill-gap";
    }

    @GetMapping("/salary-benchmark")
    public String salaryBenchmark() {
        return "ai/salary-benchmark";
    }

    @GetMapping("/star-answer")
    public String starAnswer() {
        return "ai/star-answer";
    }

    @GetMapping("/interview-prep")
    public String interviewPrep() {
        return "ai/interview-prep";
    }

    @GetMapping("/linkedin")
    public String linkedin() {
        return "ai/linkedin";
    }

    @GetMapping("/career-pivot")
    public String careerPivot() {
        return "ai/career-pivot";
    }

    @GetMapping("/company-brief")
    public String companyBrief() {
        return "ai/company-brief";
    }
}
