package com.shreeai.os.platform.legacy.skills;

import com.shreeai.os.platform.legacy.context.ConversationContext;
import com.shreeai.os.platform.legacy.llm.OllamaClient;
import org.springframework.stereotype.Component;

@Component
public class DefaultSkill implements Skill {

    private final OllamaClient llm;


    public DefaultSkill(OllamaClient llm) {
        this.llm = llm;
    }

    @Override
    public boolean supports(String intent) {
        return "DEFAULT".equals(intent);
    }



    @Override
    public String execute(String input,
                          ConversationContext context) {

        return llm.generateDirect(input);
    }

}



