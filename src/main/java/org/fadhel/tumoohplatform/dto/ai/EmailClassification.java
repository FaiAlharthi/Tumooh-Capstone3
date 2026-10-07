package org.fadhel.tumoohplatform.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailClassification {

    private Boolean jobRelated;
    private String type;//job application status: Applied,Interview, InProgress,Offered or Rejected
    private String companyName;
    private String position;
    private String interviewDate;//2026-10-20T10:00 or null (if there's no interview mentioned in the mail)
}