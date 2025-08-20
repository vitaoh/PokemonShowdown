package network;

import java.io.Serializable;

public class BattleEndData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String playerName;
    private final String opponentName;
    private final String result;
    private final boolean isWinner;
    
    public BattleEndData(String playerName, String opponentName, String result, boolean isWinner) {
        this.playerName = playerName;
        this.opponentName = opponentName;
        this.result = result;
        this.isWinner = isWinner;
    }
    
    public String getPlayerName() { return playerName; }
    public String getOpponentName() { return opponentName; }
    public String getResult() { return result; }
    public boolean isWinner() { return isWinner; }
}
