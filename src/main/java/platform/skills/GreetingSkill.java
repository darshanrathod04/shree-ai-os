package platform.skills;

import platform.context.ConversationContext;
import org.springframework.stereotype.Component;


@Component
public class GreetingSkill implements Skill {

    @Override
    public boolean supports(String intent) {
        return intent.equals("GREETING");
    }

    @Override
    public String execute(String input, ConversationContext context) {
        context.setLastIntent("GREETING");
        return "Hello 👋 I am Shree.";
    }
}


