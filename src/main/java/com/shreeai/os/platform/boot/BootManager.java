package com.shreeai.os.platform.boot;

import com.shreeai.os.platform.runtime.AbstractRuntimeService;
import org.springframework.stereotype.Component;

@Component
public class BootManager extends AbstractRuntimeService {

    @Override
    public String getName() {
        return "Boot Manager";
    }

}