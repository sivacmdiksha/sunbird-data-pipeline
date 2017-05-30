package org.ekstep.ep.samza.service;

import org.ekstep.ep.samza.dedup.DeDupEngine;
import org.ekstep.ep.samza.domain.Event;
import org.ekstep.ep.samza.logger.Logger;
import org.ekstep.ep.samza.task.PublicExhaustDeDuplicationConfig;
import org.ekstep.ep.samza.task.PublicExhaustDeDuplicationSink;
import org.ekstep.ep.samza.task.PublicExhaustDeDuplicationSource;

public class PublicExhaustDeDuplicationService {
    static Logger LOGGER = new Logger(PublicExhaustDeDuplicationService.class);
    private final DeDupEngine deDupEngine;


    public PublicExhaustDeDuplicationService(DeDupEngine deDupEngine) {
        this.deDupEngine = deDupEngine;
    }

    public void process(PublicExhaustDeDuplicationSource source, PublicExhaustDeDuplicationSink sink) {
        Event event = source.getEvent();

        try {
            String checksum = event.getChecksum();

            if (checksum == null) {
                LOGGER.info(event.id(), "EVENT WITHOUT CHECKSUM & MID, PASSING THROUGH : {}", event);
                sink.toSuccessTopic(event);
                event.updateMetadata("event_without_checksum");
            }

            if (!deDupEngine.isUniqueEvent(checksum)) {
                LOGGER.info(event.id(), "DUPLICATE EVENT, CHECKSUM: {}", checksum);
                sink.toDuplicateTopic(event);
                return;
            }

            LOGGER.info(event.id(), "ADDING EVENT CHECKSUM TO STORE");
            deDupEngine.storeChecksum(checksum);

            sink.toSuccessTopic(event);
        } catch (Exception e) {
            LOGGER.error(event.id(), "EXCEPTION. PASSING EVENT THROUGH AND ADDING IT TO FAILED TOPIC", e);
            e.printStackTrace();
            sink.toFailedTopic(event);
        }
    }
}