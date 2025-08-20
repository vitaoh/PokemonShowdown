package battle;

import battle.BattleSwing;
import players.Player;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Player p1 = new Player("Ash");  
            Player p2 = new Player("Gary");
            
            // Criar batalha local
            BattleSwing battle = new BattleSwing(p1, p2);
            
            // Iniciar seleção de times
//            p1.chooseTeam(battle);
//            p2.chooseTeam(battle);
        });
    }
}
