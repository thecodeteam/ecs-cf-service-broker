FROM golang:1.12-alpine

RUN apk add --no-cache git curl

RUN git clone https://github.com/spiegela/ecs-broker-test-app.git

WORKDIR ecs-broker-test-app

RUN go mod download

RUN go build -o /app/ecs-broker-test-app .

EXPOSE 8080

COPY ./docker-entrypoint.sh /

ENTRYPOINT ["/docker-entrypoint.sh"]