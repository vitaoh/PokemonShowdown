package network;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Classe para representar mensagens trocadas entre cliente e servidor
 * Implementa Serializable para permitir transmissão via ObjectOutputStream
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private String sender;
    private Object data;
    private LocalDateTime timestamp;
    private String sessionId;
    
    // Construtor padrão
    public Message() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Construtor principal
    public Message(MessageType type, String sender, Object data) {
        this();
        this.type = type;
        this.sender = sender;
        this.data = data;
    }
    
    // Construtor com sessão
    public Message(MessageType type, String sender, Object data, String sessionId) {
        this(type, sender, data);
        this.sessionId = sessionId;
    }
    
    // Getters e Setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    // Método toString para debug
    @Override
    public String toString() {
        return String.format("Message{type=%s, sender='%s', timestamp=%s, sessionId='%s'}",
                type, sender, timestamp, sessionId);
    }
    
    // Método de factory para criar mensagens rapidamente
    public static Message create(MessageType type, String sender, Object data) {
        return new Message(type, sender, data);
    }
    
    // Método para verificar se a mensagem é válida
    public boolean isValid() {
        return type != null && sender != null && !sender.trim().isEmpty();
    }
}