package com.amhzing.participant.domain;

import com.amhzing.participant.api.event.ParticipantCreatedEvent;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;

public class City extends AbstractAnnotatedEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(City.class);

    static final int MAX_LENGTH = 25;

    private String value;

    private City(final String value) {
        isValid(value);

        this.value = value.trim();
    }

    public static City create(final String value) {
        return new City(value);
    }

    @EventHandler
    public void handleEvent(final ParticipantCreatedEvent event) {
        final String city = event.getAddress().getCity();
        if (isValid(city)) {
            this.value = city;
        } else {
            LOGGER.info("Invalid city >{}<", city);
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final City city = (City) o;
        return Objects.equals(value, city.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "City{" +
                "value='" + value + '\'' +
                '}';
    }

    private boolean isValid(final String value) {
        notBlank(value);
        isTrue(value.length() <= MAX_LENGTH);

        return true;
    }
}