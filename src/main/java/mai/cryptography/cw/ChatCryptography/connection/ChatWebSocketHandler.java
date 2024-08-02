package mai.cryptography.cw.ChatCryptography.connection;

import elemental.json.Json;
import elemental.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("New WebSocket connection established: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            logger.info("Received message: {}", message.getPayload());
            synchronized (sessions) {
                for (WebSocketSession webSocketSession : sessions) {
                    if (webSocketSession.isOpen()) {
                        String payload = message.getPayload();
                        logger.info("Processing message payload: {}", payload);
                        JsonObject jsonMessage = Json.parse(payload);
                        jsonMessage.put("timestamp", LocalDateTime.now(MOSCOW_ZONE).format(FORMATTER));
                        webSocketSession.sendMessage(new TextMessage(jsonMessage.toString()));
                        logger.info("Sent message: {}", jsonMessage.toString());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handling message: ", e);
            throw e;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("WebSocket connection closed: {}", session.getId());
    }
}
