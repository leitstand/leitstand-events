# Leitstand Events

Leitstand Events encompasses a persistent message queue to store domain events and webhooks to forward domain events to a HTTP endpoint.

A _domain event_ informs about a change that has taken place.
For example, the `ElementAddedEvent` event informs about an element that has been added to the resource inventory.

The [leitstand-queue](./leitstand-queue/README.md) project contains the persistent [message queue](./doc/datamodel.md).

The [leitstand-webhooks](./leitstand-webhooks/README.md) project contains webhooks to forward messages to HTTP endpoint.

The [leitstand-events-ui](./leitstand-events-ui/README.md) project contributes the webhook management UI to the Leitstand administration console. 