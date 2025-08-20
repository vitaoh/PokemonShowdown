package network;

import java.io.Serializable;

public class RematchRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String requesterName;
    private final String targetName;
    private final long timestamp;
    
    public RematchRequest(String requesterName, String targetName) {
        this.requesterName = requesterName;
        this.targetName = targetName;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getRequesterName() { return requesterName; }
    public String getTargetName() { return targetName; }
    public long getTimestamp() { return timestamp; }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 30000; // 30 segundos
    }
}
