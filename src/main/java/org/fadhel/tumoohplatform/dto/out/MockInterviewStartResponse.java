package org.fadhel.tumoohplatform.dto.out;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewStartResponse {
    private Long id;
    private String jobTitle;
    private List<String> questions;
}
