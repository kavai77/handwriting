package com.himadri.handwriting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.himadri.handwriting.model.Pixels;
import com.himadri.handwriting.model.Prediction;
import com.maxmind.geoip2.DatabaseReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class DynamoDbRepository {
    public static final String TABLE_NAME = "HandwritingPrediction";
    public static final String COLUMN_DATE = "createDateTime";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_IP = "ip";
    public static final String COLUMN_IP_COUNTRY = "ipCountry";
    public static final String COLUMN_PREDICTIONS = "predictions";
    public static final String COLUMN_PREFERRED_CONFIDENCE = "preferredConfidence";
    public static final String COLUMN_PREFERRED_PREDICTION = "preferredPrediction";

    @Value("${accessKeyId}")
    private String accessKeyId;

    @Value("${secretAccessKey}")
    private String secretAccessKey;

    private DynamoDbClient dynamoDbClient;

    private DatabaseReader dbReader;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws IOException {
        dynamoDbClient = DynamoDbClient.builder()
            .credentialsProvider(() -> AwsBasicCredentials.create(accessKeyId, secretAccessKey))
            .region(Region.EU_CENTRAL_1)
            .build();
        dbReader = new DatabaseReader.Builder(getClass().getResourceAsStream("/GeoLite2-Country.mmdb")).build();
    }

    public void storePrediction(String remoteAddress, Pixels input, List<Prediction> predictions) {
        Prediction preferredPrediction = predictions.stream()
            .filter(Prediction::isPreferred)
            .findFirst()
            .orElseThrow(RuntimeException::new);
        String country;
        try {
            country = dbReader.country(InetAddress.getByName(remoteAddress)).getCountry().getName();
        } catch (Exception e) {
            country = "unknown";
        }
        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(Map.of(
                    COLUMN_DATE, AttributeValue.builder().n(Long.toString(System.currentTimeMillis())).build(),
                    COLUMN_IP, AttributeValue.builder().s(remoteAddress).build(),
                    COLUMN_IP_COUNTRY, AttributeValue.builder().s(country).build(),
                    COLUMN_IMAGE, AttributeValue.builder().b(SdkBytes.fromByteArray(input.getPngEncoded())).build(),
                    COLUMN_PREFERRED_PREDICTION, AttributeValue.builder().n(Integer.toString(preferredPrediction.getPrediction())).build(),
                    COLUMN_PREFERRED_CONFIDENCE, AttributeValue.builder().n(String.format(Locale.ROOT, "%.2f", preferredPrediction.getConfidence())).build(),
                    COLUMN_PREDICTIONS, AttributeValue.builder().s(objectMapper.writeValueAsString(predictions)).build()
                ))
                .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
