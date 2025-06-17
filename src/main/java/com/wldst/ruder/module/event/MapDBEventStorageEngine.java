package com.wldst.ruder.module.event;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.IndexTreeList;

public class MapDBEventStorageEngine implements EventStorageEngine {
    private final DB db = DBMaker.memoryDB().make();
    private static final String ORDER_LINK_LIST = "ORDER_LINK_LIST";
    private final Lock lock = new ReentrantLock();
    private final Condition dataAvailableCondition = lock.newCondition();
 
    @Override
    public void appendEvents(List<? extends EventMessage<?>> events) {
 
        lock.lock();
        try {
            IndexTreeList<Object> hs = db.indexTreeList(ORDER_LINK_LIST).createOrOpen();
            hs.addAll(events);
        } finally {
            lock.unlock();
        }
    }
 
    @Override
    public void storeSnapshot(DomainEventMessage<?> snapshot) {
 
    }
 
    @Override
    public Stream<? extends TrackedEventMessage<?>> readEvents(TrackingToken trackingToken, boolean mayBlock) {
        return null;
    }
 
    @Override
    public DomainEventStream readEvents(String aggregateIdentifier, long firstSequenceNumber) {
        return null;
    }
 
    @Override
    public Optional<DomainEventMessage<?>> readSnapshot(String aggregateIdentifier) {
        return null;
    }
}
