package com.shreeai.os.platform.runtime.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <b>SocketRedisConnection</b>
 *
 * <p>A minimal RESP (REdis Serialization Protocol) client over a plain TCP
 * socket. Used by {@link RedisConnectionProvider#defaultProvider()} when no
 * external Redis client library (Jedis/Lettuce) is on the classpath.</p>
 *
 * <p>Supports the commands required by {@link RedisConnection}: SET, SETEX,
 * GET, DEL, EXISTS, DBSIZE, KEYS, FLUSHDB.</p>
 *
 * <p><b>Ownership:</b> Runtime — Distributed State</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class SocketRedisConnection implements RedisConnection {

    private final Socket socket;
    private final OutputStream out;
    private final BufferedReader in;
    private final java.io.InputStream socketIn;
    private final byte[] readBuffer = new byte[8192];

    /**
     * Creates a connection to a Redis server.
     *
     * @param host the Redis host
     * @param port the Redis port
     * @throws IOException if the socket cannot be opened
     */
    public SocketRedisConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setSoTimeout(5000);
        this.out = socket.getOutputStream();
        this.socketIn = socket.getInputStream();
        this.in = new BufferedReader(new InputStreamReader(socketIn, StandardCharsets.UTF_8));
    }

    @Override
    public void set(String key, String value) {
        writeCommand("SET", key, value);
        readSimpleString();
    }

    @Override
    public void setex(String key, long seconds, String value) {
        writeCommand("SETEX", key, String.valueOf(seconds), value);
        readSimpleString();
    }

    @Override
    public String get(String key) {
        writeCommand("GET", key);
        return readBulkString();
    }

    @Override
    public long del(String key) {
        writeCommand("DEL", key);
        return readInteger();
    }

    @Override
    public boolean exists(String key) {
        writeCommand("EXISTS", key);
        return readInteger() > 0;
    }

    @Override
    public long dbSize() {
        writeCommand("DBSIZE");
        return readInteger();
    }

    @Override
    public Set<String> keys(String pattern) {
        writeCommand("KEYS", pattern);
        Set<String> result = new LinkedHashSet<>();
        int count = readArrayLength();
        for (int i = 0; i < count; i++) {
            String value = readBulkString();
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    @Override
    public void flushDB() {
        writeCommand("FLUSHDB");
        readSimpleString();
    }

    @Override
    public void close() {
        try {
            out.close();
        } catch (IOException ignored) {
            // ignore
        }
        try {
            in.close();
        } catch (IOException ignored) {
            // ignore
        }
        try {
            socketIn.close();
        } catch (IOException ignored) {
            // ignore
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            // ignore
        }
    }

    // ==========================================================
    // RESP protocol helpers
    // ==========================================================

    private void writeCommand(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append('*').append(args.length).append("\r\n");
        for (String arg : args) {
            byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
            sb.append('$').append(bytes.length).append("\r\n");
            sb.append(arg).append("\r\n");
        }
        try {
            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write Redis command: " + args[0], e);
        }
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read Redis response", e);
        }
    }

    private void readSimpleString() {
        String line = readLine();
        if (line == null || line.isEmpty() || line.charAt(0) != '+') {
            throw new IllegalStateException("Unexpected Redis response: " + line);
        }
    }

    private long readInteger() {
        String line = readLine();
        if (line == null || line.isEmpty() || line.charAt(0) != ':') {
            throw new IllegalStateException("Unexpected Redis integer response: " + line);
        }
        return Long.parseLong(line.substring(1));
    }

    private int readArrayLength() {
        String line = readLine();
        if (line == null || line.isEmpty() || line.charAt(0) != '*') {
            throw new IllegalStateException("Unexpected Redis array response: " + line);
        }
        return Integer.parseInt(line.substring(1));
    }

    private String readBulkString() {
        String line = readLine();
        if (line == null || line.isEmpty() || line.charAt(0) != '$') {
            throw new IllegalStateException("Unexpected Redis bulk response: " + line);
        }
        int length = Integer.parseInt(line.substring(1));
        if (length < 0) {
            return null; // $-1 means nil
        }
        try {
            int read = 0;
            int offset = 0;
            while (read < length + 2) {
                int n = socketIn.read(readBuffer, offset, readBuffer.length - offset);
                if (n < 0) {
                    throw new IllegalStateException("Redis stream closed mid-response");
                }
                offset += n;
                read += n;
            }
            // Strip the trailing CRLF
            return new String(readBuffer, 0, length, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read Redis bulk string", e);
        }
    }
}