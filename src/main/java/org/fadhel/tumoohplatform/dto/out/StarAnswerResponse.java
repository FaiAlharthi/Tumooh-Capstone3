package org.fadhel.tumoohplatform.dto.out;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StarAnswerResponse {

    private String question;
    private String starAnswer;
    private String situation;
    private String task;
    private String action;
    private String result;
    private List<String> strengthsRevealed;
    private List<String> whatToPolish;
    private List<String> tips;
}