package com.amhzing.participant.command.domain;

import org.junit.Test;

import static com.amhzing.participant.command.domain.MiddleName.MAX_LENGTH;
import static org.apache.commons.lang.StringUtils.repeat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MiddleNameTest {

    @Test
    public void pass_if_value_is_not_blank() {
        final MiddleName middleName = MiddleName.create("John");

        assertEquals(middleName.getValue(), "John");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_if_value_is_blank() {
        MiddleName.create("");

        fail(); // Fail if we got this far
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_if_value_is_greater_than_max_length() {
        MiddleName.create("J" + repeat("x", MAX_LENGTH));

        fail(); // Fail if we got this far
    }
}