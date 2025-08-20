package network;

import java.io.Serializable;
import java.util.List;
import pokemon.Species;

public class BattleInitPayload implements Serializable {
    public final List<Species> teamA;
    public final List<Species> teamB;
    public final boolean youStart;

    public BattleInitPayload(List<Species> me, List<Species> foe, boolean youStart){
        this.teamA = me;
        this.teamB = foe;
        this.youStart = youStart;
    }
    public BattleInitPayload swap(){
        return new BattleInitPayload(teamB, teamA, !youStart);
    }
}

