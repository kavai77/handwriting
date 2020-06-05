package com.himadri.handwriting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

@Getter
@Builder
@ToString
public class PredictionDbEntity {
    private Long id;
    private String ip;
    private String ipCountry;
    private byte[] image;
    private int preferredPrediction;
    private double preferredConfidence;
    private String predictions;
    private Date createTime;
}
