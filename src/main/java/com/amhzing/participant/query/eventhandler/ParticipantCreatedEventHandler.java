package com.amhzing.participant.query.eventhandler;

import com.amhzing.participant.annotation.Online;
import com.amhzing.participant.api.event.ParticipantCreatedEvent;
import com.amhzing.participant.query.data.cassandra.mapping.ParticipantDetails;
import com.amhzing.participant.query.data.cassandra.mapping.ParticipantDetailsBuilder;
import com.amhzing.participant.query.data.cassandra.mapping.ParticipantPrimaryKeyBuilder;
import com.amhzing.participant.query.exception.QueryInsertException;
import org.apache.commons.collections.MapUtils;
import org.axonframework.domain.MetaData;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.apache.commons.lang.Validate.notNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Online
public class ParticipantCreatedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantCreatedEventHandler.class);

    // This is auto-configured by Spring Boot
    @Autowired
    CassandraTemplate cassandraTemplate;

    @EventHandler
    public void handleEvent(final ParticipantCreatedEvent event, final MetaData metadata) {
        notNull(event);
        notNull(metadata);

        try {
            LOGGER.info("Inserting {} details for participant {}", ParticipantCreatedEvent.class.getSimpleName(), event.getId());
            cassandraTemplate.insert(participantDetails(event, metadata));
        } catch (final Exception ex) {
            throw new QueryInsertException(ex);
        }
    }

    private ParticipantDetails participantDetails(final ParticipantCreatedEvent event, final MetaData metadata) {
        final ParticipantPrimaryKeyBuilder participantPrimaryKeyBuilder = new ParticipantPrimaryKeyBuilder();
        participantPrimaryKeyBuilder.setCountry(event.getAddress().getCountry().getCode());
        participantPrimaryKeyBuilder.setCity(event.getAddress().getCity());
        participantPrimaryKeyBuilder.setAddressLine1(event.getAddress().getAddressLine1());
        participantPrimaryKeyBuilder.setLastName(event.getName().getLastName());
        participantPrimaryKeyBuilder.setParticipantId(event.getId());

        final ParticipantDetailsBuilder participantDetailsBuilder = new ParticipantDetailsBuilder();
        participantDetailsBuilder.setPrimaryKey(participantPrimaryKeyBuilder.create());
        participantDetailsBuilder.setFirstName(event.getName().getFirstName());
        participantDetailsBuilder.setMiddleName(event.getName().getMiddleName());
        participantDetailsBuilder.setSuffix(event.getName().getSuffix());
        participantDetailsBuilder.setAddressLine2(event.getAddress().getAddressLine2());
        participantDetailsBuilder.setPostalCode(event.getAddress().getPostalCode());
        participantDetailsBuilder.setEmail(email(event));
        participantDetailsBuilder.setContactNumber(contactNumber(event));
        participantDetailsBuilder.setAddedDate(currentTime());
        participantDetailsBuilder.setAddedBy(userId(metadata));
        participantDetailsBuilder.setUpdatedDate(currentTime());
        participantDetailsBuilder.setUpdatedBy(userId(metadata));

        return participantDetailsBuilder.create();
    }

    private Timestamp currentTime() {
        final ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        return Timestamp.from(utc.toInstant());
    }

    public static String contactNumber(final ParticipantCreatedEvent event) {
        if (event.getContactNumber() != null && isNotBlank(event.getContactNumber().getValue())) {
            return event.getContactNumber().getValue();
        }
        return null;
    }

    public static String email(final ParticipantCreatedEvent event) {
        if (event.getEmail() != null && isNotBlank(event.getEmail().getValue())) {
            return event.getEmail().getValue();
        }
        return null;
    }

    private String userId(final MetaData metadata) {
        return MapUtils.getString(metadata, "userId", "UNKNOWN");
    }
}
