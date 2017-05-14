package com.walmartlabs.concord.server.process.pipelines.processors;

import com.walmartlabs.concord.server.process.Payload;
import com.walmartlabs.concord.server.process.queue.ProcessQueueDao;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class EnqueueingProcessor implements PayloadProcessor {

    private final ProcessQueueDao queueDao;

    @Inject
    public EnqueueingProcessor(ProcessQueueDao queueDao) {
        this.queueDao = queueDao;
    }

    @Override
    public Payload process(Chain chain, Payload payload) {
        String instanceId = payload.getInstanceId();
        String projectName = payload.getHeader(Payload.PROJECT_NAME);
        String initiator = payload.getHeader(Payload.INITIATOR);
        queueDao.insertInitial(instanceId, projectName, initiator);
        return chain.process(payload);
    }
}
