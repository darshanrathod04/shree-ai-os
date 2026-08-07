package com.shreeai.os.platform.skills;

import com.shreeai.os.platform.context.ConversationContext;

public interface Skill {



    boolean supports(String intent);


    String execute(String input, ConversationContext context) throws Exception;
}
