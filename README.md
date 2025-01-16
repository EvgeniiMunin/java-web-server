# java-web-server

## Bid Prediction Service
[Spring Boot](https://spring.io/projects/spring-boot) application that provides predictions of bids on the bid requests. The project is built using [Maven](https://maven.apache.org/) and Java 17.

### Dependency Injection
`ServiceConfiguration` configures Spring dependencies as beans for the following classes:
- `InferenceDataService` - service that parses the bid request `.json` and flattens it to `InferenceMessage` feature set
- `OnnxModelRunner` - service that fetches and launches the trained [ONNX](https://onnx.ai/) model
- `TelemetryService` - service that defines `OtlpHttpMetricExporter`
- `PredictionService` - service that executes predictions using `OnnxModelRunner` and processes output probabilities

The dependency injection is done at the `Application` start up. At this moment the ONNX model is loaded into the memory. 
The above services are also initialized and ready to use.


### BidRequest schema
The `BidRequest` class represents the schema after the deserialization of the standard OpenRTB bid request `.json` payload. The example of this schema can be seen as follows:

```json
{
  "id": "1",
  "imp": [
    {
      "id": "pub_banniere_haute",
      "ext": {
        "prebid": {
          "bidder": {
            "rubicon": {
              "accountId": 1001,
              "siteId": 267318,
              "zoneId": 1861698
            }
          }
        }
      },
      "banner": {
        "format": [
          {
            "w": 300,
            "h": 250
          },
          {
            "w": 300,
            "h": 600
          }
        ]
      }
    }
  ],
  "site": {
    "publisher": {
      "id": "1001"
    },
    "page": "http://example.com/prebid_server_test.html"
  }
}
```

The DTO classes are defined using [Lombok](https://projectlombok.org/) annotations. It is a good practice to use them to avoid boilerplate with getters/ setters. Also the `@Builder` annotation makes the class fields immutable.


### Run Application
The java-wev-server application is available as a docker container and its image is hosted on [Docker Hub](https://hub.docker.com/repository/docker/evgeniimunin/java-web-server/general). To pull the image and run the application, use the following commands:
```bash
cd java-web-server
docker compose up -d
```

To send the bid request to the application, use the following command:
```bash
curl -X POST http://localhost:8080/bid -H "Content-Type: application/json" -d @src/main/resources/bid_request.json
```

The output on the client side will represent the map of probabilites of bid per bidder per adUnit:
```
{"pub_banniere_haute":{"rubicon":0.3}} 
```


### Metrics export
The application uses `OtlpHttpMetricExporter` to export model outputs (probabilities) to the Open Telemetry collector to `http://otel-collector:4318/v1/traces` endpoint. The metrics are then available in `otel-collector` at `http://localhost:8889/metrics` endpoint.

Example of the exported metrics:
```
rtb_rtb_predictions_sum{bidder="appnexus",impId="pub_banniere_haute",job="rtb-prediction-service",service="prediction-service"} 0.3
```


### Unit Testing
The project includes unit tests using [JUnit4](https://junit.org/junit4/) and [Mockito](https://site.mockito.org/) for the following services:
- `InferenceDataService`
- `PredictionService`

To run the tests, use the following command:
```bash
mvn test
```

