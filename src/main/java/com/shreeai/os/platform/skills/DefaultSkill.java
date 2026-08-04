package com.shreeai.os.platform.skills;

import com.shreeai.os.platform.context.ConversationContext;
import com.shreeai.os.platform.llm.OllamaClient;
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



