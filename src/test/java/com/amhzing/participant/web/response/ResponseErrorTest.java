package com.amhzing.participant.web.response;

import com.amhzing.participant.annotation.TestOffline;
import com.amhzing.participant.helper.JsonLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
@TestOffline
public class ResponseErrorTest {

    @Autowired
    private JacksonTester<ResponseError> json;

    @Autowired
    private ResourceLoader resourceLoader;

    private JsonLoader jsonLoader;

    @Before
    public void setUp() throws Exception {
        jsonLoader = new JsonLoader();
    }

    @Test
    public void should_serialize_response() throws Exception {
        final ResponseError response = ResponseError.create(code(), message(), correlationId());

        final JsonContent<ResponseError> jsonContent = this.json.write(response);

        assertThat(jsonContent).extractingJsonPathStringValue("@.code").isEqualTo(code());
        assertThat(jsonContent).extractingJsonPathStringValue("@.message").isEqualTo(message());
        assertThat(jsonContent).extractingJsonPathStringValue("@.correlationId").isEqualTo(correlationId());
    }

    private String code() {
        return "123";
    }

    private String message() {
        return "Message";
    }

    private String correlationId() {
        return "19837";
    }
}