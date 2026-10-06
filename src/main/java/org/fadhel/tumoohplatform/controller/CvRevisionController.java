package org.fadhel.tumoohplatform.controller;


import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.dto.out.CvRevisionResponse;
import org.fadhel.tumoohplatform.service.CvRevisionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ai/cv")
@RequiredArgsConstructor
public class CvRevisionController {

    private final CvRevisionService cvRevisionService;

    @PostMapping(value = "/revise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvRevisionResponse> reviseCv(@RequestParam("file") MultipartFile file) {
        CvRevisionResponse response = cvRevisionService.reviseCvPdf(file);
        return ResponseEntity.ok(response);
    }
}
