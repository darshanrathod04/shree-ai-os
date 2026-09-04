package com.shreeai.os.platform.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <b>InMemoryStore</b>
 *
 * <p>The default {@link PersistenceStore} implementation. Uses an
 * in-memory {@link ConcurrentHashMap} for storage. Default store
 * when no persistent backend is configured.</p>
 *
 * <p>Thread-safe via {@link ConcurrentHashMap}.</p>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
public class InMemoryStore implements PersistenceStore {

    private final Map<String, String> map = new ConcurrentHashMap<>();

    public InMemoryStore() {}

    @Override
    public void put(String key, String value) {
        map.put(key, value);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(map.get(key));
    }

    @Override
    public void delete(String key) {
        map.remove(key);
    }

    @Override
    public boolean exists(String key) {
        return map.containsKey(key);
    }

    @Override
    public List<String> keys(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return List.copyOf(map.keySet());
        }
        return map.keySet().stream()
                .filter(k -> k.startsWith(prefix))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> entries(String prefix) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (prefix == null || prefix.isEmpty() || e.getKey().startsWith(prefix)) {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }

    @Override
    public long size(String prefix) {
        if (prefix == null || prefix.isEmpty()) return map.size();
        return map.keySet().stream().filter(k -> k.startsWith(prefix)).count();
    }

    @Override
    public void clear(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            map.clear();
        } else {
            map.keySet().stream()
                    .filter(k -> k.startsWith(prefix))
                    .forEach(map::remove);
        }
    }

    @Override
    public String name() { return "in-memory"; }
}
