# IDEA QE Weather Sample

## API

| Method | Endpoint                                                   | Status      | Media Type       |
|--------|------------------------------------------------------------|-------------|------------------|
| POST   | http://{server}:{port}/weather/                            | 201 Created | application/json |
| GET    | http://{server}:{port}/weather/{stationId}/{observationId} | 200 OK      | application/json |
| GET    | http://{server}:{port}/weather/{stationId}                 | 200 OK      | application/json |

### Sample POST Payload
```json
{
  "temperature": 60,
  "humidity": 37.78,
  "precipitation": 5.27,
  "observationId": 12345,
  "stationId": 1,
  "timestamp": "2016-11-20T04:00:00.000+0000"
}
```

### Sample GET Responses

#### /weather/{stationId}
```json
[
  {
    "temperature": 60,
    "humidity": 37.78,
    "precipitation": 5.27,
    "observationId": 1,
    "stationId": 1,
    "timestamp": "2016-11-20T04:00:00.000+0000"
  },
  {
    "temperature": 60,
    "humidity": 37.93,
    "precipitation": 4,
    "observationId": 8,
    "stationId": 1,
    "timestamp": "2016-11-20T04:35:00.000+0000"
  },
  {
    "temperature": 60,
    "humidity": 37.78,
    "precipitation": 5.27,
    "observationId": 12345,
    "stationId": 1,
    "timestamp": "2016-11-20T04:00:00.000+0000"
  }
]
```

#### /weather/{stationId}/{observationId}
```json
{
  "temperature": 60,
  "humidity": 37.93,
  "precipitation": 4,
  "observationId": 8,
  "stationId": 1,
  "timestamp": "2016-11-20T04:35:00.000+0000"
}
```


## Implementations
We've got two implementations in place for you to. Feel free to either choose [Java](https://github.com/ideaqe/weather-java) or [Python](https://github.com/ideaqe/weather-python).
