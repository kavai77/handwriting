package com.himadri.handwriting;

import com.himadri.handwriting.model.Pixels;
import com.himadri.handwriting.model.Prediction;
import com.himadri.handwriting.predictionengine.PredictionEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service")
public class PredictionService {

    private final List<PredictionEngine> predictionEngineList;

    @Autowired
    public PredictionService(List<PredictionEngine> predictionEngineList) {
        this.predictionEngineList = predictionEngineList;
    }

    @PostMapping(value = "/prediction", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Prediction> prediction(@RequestBody Pixels input) {
        List<Prediction> predictions = predictionEngineList
            .stream()
            .map(a -> a.classify(input))
            .sorted(Comparator.comparing(Prediction::getMethod))
            .collect(Collectors.toList());

        return predictions;
    }
}
