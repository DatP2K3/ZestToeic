package com.zest.toeic.practice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateFlashcardRequest {

    @NotBlank(message = "Front content is required")
    private String front;

    @NotBlank(message = "Back content is required")
    private String back;

    private List<String> tags;
    private Integer part;
    private String difficulty;
}
