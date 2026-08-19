package com.shreeai.os.platform.legacy.skills;

import com.shreeai.os.platform.legacy.context.ConversationContext;

public interface Skill {



    boolean supports(String intent);


    String execute(String input, ConversationContext context) throws Exception;
}
