package com.epam.gym_app_stats_mq.component;

import com.epam.gym_app_stats_mq.api.ActionTypeInStatApp;
import com.epam.gym_app_stats_mq.api.UpdateStatRequestInStatApp;
import com.epam.gym_app_stats_mq.stat.statMongoDb.StatModelMongoDb;
import com.epam.gym_app_stats_mq.stat.statMongoDb.StatRepoMongoDb;
import com.epam.gym_app_stats_mq.stat.statMongoDb.StatServiceMongoDb;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
@SpringBootTest
public class ComponentSteps {

    private final StatServiceMongoDb serviceMongoDb;
    private final StatRepoMongoDb repoMongoDb;
    private final ObjectMapper objectMapper;

    @Autowired
    public ComponentSteps(StatServiceMongoDb serviceMongoDb, StatRepoMongoDb repoMongoDb, ObjectMapper objectMapper) {
        this.serviceMongoDb = serviceMongoDb;
        this.repoMongoDb = repoMongoDb;
        this.objectMapper = objectMapper;
    }

    @Given("a stat-update-request payload {string}")
    public void aStatUpdateRequestPayloadTrainerIdYearMonthDurationActionTypeADDUserNameTimSmithFirstNameTimLastNameSmithStatusTrue(
            String statUpdateRequestPayload
    ) throws JsonProcessingException {

        Map<String, String> map = objectMapper.readValue(
                statUpdateRequestPayload,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));

        String username = map.get("userName");
        String firstName = map.get("firstName");
        String lastName = map.get("lastName");
        Boolean status = Boolean.parseBoolean(map.get("status"));
        Integer year = Integer.parseInt(map.get("year"));
        Integer month = Integer.parseInt(map.get("month"));
        Integer minutes = Integer.parseInt(map.get("duration"));
        ActionTypeInStatApp actionType = ActionTypeInStatApp.valueOf(map.get("actionType"));
        boolean actionTypeIsAdd = actionType == ActionTypeInStatApp.ADD;

        serviceMongoDb.letsMongo(
                username,
                lastName,
                firstName,
                status,
                year,
                month,
                minutes,
                actionTypeIsAdd
        );
    }


    @Then("the data should be persisted and the username {string} should be in the data base")
    public void theDataShouldBePersistedAndTheUsernameShouldBeInTheDataBase(String username) {
        StatModelMongoDb stat = repoMongoDb.findByUserName(username);
        assertNotNull("The stat object should not be null", stat);
        assertEquals("The userName should be Timmy.Smith", username, stat.getUserName());
        assertEquals("The firstName should be Timmy", "Timmy", stat.getFirstName());
        assertEquals("The lastName should be Smith", "Smith", stat.getLastName());
    }
}
