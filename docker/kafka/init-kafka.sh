#!/bin/bash

# Ожидание готовности Kafka
echo "Waiting for Kafka to be ready..."
while ! kafka-topics.sh --bootstrap-server localhost:9092 --list >/dev/null 2>&1; do
  sleep 2
done

# Создание топиков
echo "Creating topics..."

# Основные топики
kafka-topics.sh --bootstrap-server localhost:9092 --create --if-not-exists \
  --topic booking-status-events \
  --partitions 3 \
  --replication-factor 1 \

kafka-topics.sh --bootstrap-server localhost:9092 --create --if-not-exists \
  --topic payment-events \
  --partitions 3 \
  --replication-factor 1

echo "Topics created:"
kafka-topics.sh --bootstrap-server localhost:9092 --list