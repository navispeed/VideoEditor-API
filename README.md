# VideoEditor

## How to run it
Export following environment variable: 
```bash
export JDBC_URL=jdbc:postgresql://localhost:5432/youtube-extractor
export JDBC_USERNAME=postgres
export JDBC_USERPASSWORD=postgres
```

Then, run the app using: 
```bash
mvn spring-boot:run
```

## Features

- Download video from youtube
- Extract some part of video and export it as mp3/mp4
- Stream exported files