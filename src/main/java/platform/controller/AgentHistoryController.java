package platform.controller;

import platform.memory.MemoryStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AgentHistoryController {

    private final MemoryStore memoryStore;

    public AgentHistoryController(MemoryStore memoryStore) {
        this.memoryStore = memoryStore;
    }

    @GetMapping("/history")
    public Object history() {
        return memoryStore.load().getHistory();
    }
}
