package com.zest.toeic.shared.ai.model;

import com.zest.toeic.shared.model.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "ai_explanations")
@CompoundIndex(name = "question_answer_idx", def = "{'questionId': 1, 'userAnswer': 1}", unique = true)
public class AIExplanation extends BaseDocument {

    private String questionId;
    private String userAnswer;       // A, B, C, D
    private String explanation;      // Vietnamese explanation
    private String provider;         // GEMINI, CLAUDE, GPT, STATIC
    private int tokenCount;
}
