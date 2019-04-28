package com.himadri.handwriting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.himadri.handwriting.model.Pixels;
import com.himadri.handwriting.model.Prediction;
import com.maxmind.geoip2.DatabaseReader;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

@Component
public class StorageService {
    private DynamoDbClient client;
    private DatabaseReader dbReader;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws IOException {
        client = DynamoDbClient.builder()
            .region(Region.US_EAST_2)
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build();
        dbReader = new DatabaseReader.Builder(getClass().getResourceAsStream("/GeoLite2-Country.mmdb")).build();
    }

    public void storePrediction(String remoteAddress, Pixels pixels, List<Prediction> predictions) {
        Prediction preferredPrediction = predictions.stream().filter(Prediction::isPreferred).findFirst().orElseThrow(RuntimeException::new);
        String country;
        try {
            country = dbReader.country(InetAddress.getByName(remoteAddress)).getCountry().getName();
        } catch (Exception e) {
            country = "unknown";
        }
        try {
            PutItemRequest request = PutItemRequest.builder()
                .tableName("predictions")
                .item(
                    ImmutableMap.<String, AttributeValue>builder()
                        .put("timestamp", AttributeValue.builder().n(Long.toString(System.currentTimeMillis())).build())
                        .put("ip", AttributeValue.builder().s(remoteAddress).build())
                        .put("ipCountry", AttributeValue.builder().s(country).build())
                        .put("image", AttributeValue.builder().b(SdkBytes.fromByteArray(pixels.getPngEncoded())).build())
                        .put("preferredPrediction", AttributeValue.builder().n(Integer.toString(preferredPrediction.getPrediction())).build())
                        .put("preferredConfidence", AttributeValue.builder().n(String.format("%.2f", preferredPrediction.getConfidence())).build())
                        .put("predictions", AttributeValue.builder().s(objectMapper.writeValueAsString(predictions)).build())
                    .build()
                )
                .build();
            client.putItem(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
