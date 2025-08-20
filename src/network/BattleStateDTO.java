package network;

import java.io.Serializable;

public class BattleStateDTO implements Serializable {
    public int hpP1;
    public int hpP2;
    public int idxP1;
    public int idxP2;
    public boolean p1Turn; // true se é o turno do player1, false se do player2

    // Construtor simples
    public BattleStateDTO(int hpP1, int hpP2, int idxP1, int idxP2, boolean p1Turn) {
        this.hpP1 = hpP1;
        this.hpP2 = hpP2;
        this.idxP1 = idxP1;
        this.idxP2 = idxP2;
        this.p1Turn = p1Turn;
    }
}
