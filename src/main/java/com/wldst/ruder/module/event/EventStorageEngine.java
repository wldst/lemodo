package com.wldst.ruder.module.event;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface EventStorageEngine {

    void appendEvents(List<? extends EventMessage<?>> events);

    void storeSnapshot(DomainEventMessage<?> snapshot);

    Stream<? extends TrackedEventMessage<?>> readEvents(TrackingToken trackingToken, boolean mayBlock);

    DomainEventStream readEvents(String aggregateIdentifier, long firstSequenceNumber);

    Optional<DomainEventMessage<?>> readSnapshot(String aggregateIdentifier);

}
