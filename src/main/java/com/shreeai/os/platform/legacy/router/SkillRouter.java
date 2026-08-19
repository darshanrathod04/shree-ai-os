package com.shreeai.os.platform.legacy.router;

import com.shreeai.os.platform.legacy.skills.Skill;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkillRouter {

    private final List<Skill> skills;

    public SkillRouter(List<Skill> skills) {
        this.skills = skills;
    }

    public Skill route(String intent) {

        for (Skill skill : skills) {
            if (skill.supports(intent)) {
                return skill;
            }
        }

        // fallback → CHAT
        for (Skill skill : skills) {
            if (skill.supports("CHAT")) {
                return skill;
            }
        }

        throw new RuntimeException("No skill found");
    }
}
