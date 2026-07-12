package platform.skills;

import platform.context.ConversationContext;

public interface Skill {



    boolean supports(String intent);


    String execute(String input, ConversationContext context) throws Exception;
}
