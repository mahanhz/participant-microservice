package com.amhzing.participant.command.domain;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.amhzing.participant.helper.JUnitParamHelper.invalidMatching;
import static com.amhzing.participant.helper.JUnitParamHelper.valid;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class ContactNumberTest {

    @Test
    @Parameters(method = "contactNumberValues")
    public void contact_number_is_valid(final Class<? extends Exception> exception, final String value)  {
        try {
            ContactNumber.create(value);
        } catch (Exception ex) {
            assertThat(ex.getClass()).isEqualTo(exception);
        }
    }

    @SuppressWarnings("unused")
    private Object contactNumberValues() {
        return new Object[][]{
                {valid(), "12345678"},
                {invalidMatching(IllegalArgumentException.class), ""},
                {invalidMatching(NullPointerException.class), null}
        };
    }
}