# Leitstand Event Queue

The Leitstand Event Queue is a _transactional_ persistent message queue.
A _domain event_ gets persisted in the queue if the transaction that issued the event was committed.
Consequently no event is stored if the transaction roles back.

## DomainEvent
The `DomainEvent` is merely an envelope to convey the actual payload.
The payload must be provided in JSON format.

An event contains the following properties:

- _event ID_, a unique message ID in UUIDv4 format
- _event name_, a descriptive event name (e.g. `ElementAddedEvent`, `ElementRemovedEvent`)
- _correlation ID_, an optional ID to correlate a message with a running process. 
  This allows a process to wait for a certain message before it proceeds.
- _topic_, the topic to which the message is assigned
- _message_, the JSON payload
- _creation date_, the timestamp when the event was created

## Topic
Messages are grouped by topics. 
Each message is associated to exactly one topic.

## EventQueueService
The `EventQueueService` provides the API to send domain events and query the event queue.