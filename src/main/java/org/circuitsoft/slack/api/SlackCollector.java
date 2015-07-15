package org.circuitsoft.slack.api;

import java.util.Map;

public interface SlackCollector {
    void onMessage(Map<String, String> message);
}
