package com.zest.toeic.practice.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "user_answers")
public class UserAnswer extends BaseDocument {

    private String userId;
    private String questionId;
    private String testId;
    private String selectedOption; // A, B, C, D
    private boolean correct;
    private int timeTaken; // seconds
}
