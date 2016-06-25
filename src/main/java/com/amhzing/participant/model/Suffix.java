package com.amhzing.participant.model;

import com.amhzing.participant.api.event.ParticipantCreatedEvent;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;

public class Suffix extends AbstractAnnotatedEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(Suffix.class);

    static final int MAX_LENGTH = 10;

    private String value;

    private Suffix(final String value) {
        isValid(value);

        this.value = value.trim();
    }

    public static Suffix create(final String value) {
        return new Suffix(value);
    }

    @EventHandler
    public void on(final ParticipantCreatedEvent event) {
        final String suffix = event.getName().getMiddleName();
        if (isValid(suffix)) {
            this.value = suffix;
            LOGGER.debug("Applied ParticipantCreatedEvent [{}] for suffix '{}'", event.getId(), suffix);
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Suffix suffix = (Suffix) o;
        return Objects.equals(value, suffix.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Suffix{" +
                "value='" + value + '\'' +
                '}';
    }

    private boolean isValid(final String value) {
        notBlank(value);
        isTrue(value.length() <= MAX_LENGTH);

        return true;
    }
}
