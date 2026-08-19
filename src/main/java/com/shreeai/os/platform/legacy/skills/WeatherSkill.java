package com.shreeai.os.platform.legacy.skills;

import com.shreeai.os.platform.legacy.context.ConversationContext;
import org.springframework.stereotype.Component;


@Component
public class WeatherSkill implements Skill {

    @Override
    public boolean supports(String intent) {
        return intent.equals("WEATHER");
    }

    @Override
    public String execute(String input, ConversationContext context) {

        String city;

        if (input.toLowerCase().contains("mumbai")) {
            city = "Mumbai";
            context.put("city", city);
        } else {
            city = (String) context.get("city");
        }

        context.setLastIntent("WEATHER");

        if (city == null) {
            return "Which city?";
        }

        return "Weather in " + city + " is sunny ☀️";
    }
}
