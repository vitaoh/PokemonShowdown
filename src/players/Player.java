package players;

import pokemon.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;

public class Player {
    private final String name;
    private final List<Species> team;
    public static final int TEAM_SIZE = 3;

    // Callback para notificar quando seleção completa
    private TeamSelectionCallback selectionCallback;

    public Player(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Trainer name cannot be null or empty.");
        }
        this.name = name.trim();
        this.team = new ArrayList<>(TEAM_SIZE);
    }

    
    // Inicia seleção de time com callback para notificação
    // @param callback Callback chamado quando seleção completa
    
    public void chooseTeam(TeamSelectionCallback callback) {
        this.selectionCallback = callback;

        SwingUtilities.invokeLater(() -> {
            TeamSelectionFrame frame = new TeamSelectionFrame(this);
            frame.setVisible(true);
        });
    }

    
    // Método chamado pelo TeamSelectionFrame quando seleção completa
    
    public void notifyTeamSelectionComplete() {
        System.out.println("Player " + name + " team selection complete. Team size: " + team.size());

        if (selectionCallback != null) {
            // Notificar no EDT (Event Dispatch Thread)
            SwingUtilities.invokeLater(() -> {
                selectionCallback.onTeamSelectionComplete(this);
            });
        }
    }

    // Método para uso interno do TeamSelectionFrame
    public List<Species> getTeamInternal() {
        return team;
    }

    public List<Species> getTeam() {
        return Collections.unmodifiableList(team);
    }

    public String getName() {
        return name;
    }

    public void displayTeam() {
        System.out.printf("%nTrainer %s's Team:%n", name);
        for (Species p : team) {
            System.out.printf("- %s (Dex #%s; Types: %s)%n",
                p.getName(),
                p.getDexNumber(),
                formatTypes(p.getTypes()));
        }
    }

    private String formatTypes(Type[] types) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            sb.append(types[i].name());
            if (i < types.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private Species findSpeciesByDex(String dex) {
        for (Species s : Species.values()) {
            if (s.getDexNumber().equals(dex)) {
                return s;
            }
        }
        return null;
    }
}