package com.himadri.handwriting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Prediction {
    private final String method;
    private final int prediction;
    private final Double confidence;
}
