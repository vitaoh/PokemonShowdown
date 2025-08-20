package players;

import pokemon.Species;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TeamSelectionFrame extends JFrame {
    private final Player player;
    private final ArrayList<Species> selected = new ArrayList<>();
    private Consumer<List<Species>> selectionListener;

    public TeamSelectionFrame(Player player) {
        this.player = player;
        setTitle("Select your Pokémon - " + player.getName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleWindowClosing();
            }
        });

        initializeGUI();
    }

    private void handleWindowClosing() {
        if (selected.size() < Player.TEAM_SIZE) {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Você deve selecionar " + Player.TEAM_SIZE + " Pokémon antes de continuar.\n" +
                "Tem certeza que deseja cancelar?",
                "Seleção Incompleta",
                JOptionPane.YES_NO_OPTION
            );

            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } else {
            dispose();
        }
    }

    private void initializeGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JLabel instructions = new JLabel(
            "<html><center>Selecione " + Player.TEAM_SIZE + " Pokémon para " + 
            player.getName() + "<br>Progresso: " + selected.size() + "/" + 
            Player.TEAM_SIZE + "</center></html>",
            SwingConstants.CENTER
        );
        instructions.setFont(new Font("Arial", Font.BOLD, 18));
        instructions.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(instructions, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 5, 10, 10));
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        for (Species species : Species.values()) {
            JButton button = createSpeciesButton(species, instructions);
            grid.add(button);
        }

        mainPanel.add(scroll, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        JLabel statusLabel = new JLabel("Selecione um Pokémon para começar");
        statusPanel.add(statusLabel);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JButton createSpeciesButton(Species species, JLabel instructions) {
        String imagePath = "/pokemonsFront/" + species.getName().toLowerCase() + ".png";
        ImageIcon icon = null;

        try {
            icon = new ImageIcon(getClass().getResource(imagePath));
            Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("Could not load image for: " + species.getName());
        }

        JButton button = new JButton(species.getName(), icon);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.addActionListener(e -> handleSpeciesSelection(species, button, instructions));
        return button;
    }

    private void handleSpeciesSelection(Species species, JButton button, JLabel instructions) {
        if (selected.contains(species)) {
            JOptionPane.showMessageDialog(this,
                "Você já selecionou este Pokémon!",
                "Pokémon Duplicado",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (selected.size() >= Player.TEAM_SIZE) {
            JOptionPane.showMessageDialog(this,
                "Time completo! Você só pode ter " + Player.TEAM_SIZE + " Pokémon.",
                "Time Completo",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        selected.add(species);
        button.setEnabled(false);
        button.setBackground(Color.GREEN);

        updateInstructions(instructions);

        if (selected.size() == Player.TEAM_SIZE) {
            completeTeamSelection();
        }
    }

    private void updateInstructions(JLabel instructions) {
        instructions.setText(
            "<html><center>Selecione " + Player.TEAM_SIZE + " Pokémon para " + 
            player.getName() + "<br>Progresso: " + selected.size() + "/" + 
            Player.TEAM_SIZE + "</center></html>"
        );
    }

    private void completeTeamSelection() {
        for (Species s : selected) {
            player.getTeamInternal().add(s);
        }

        StringBuilder message = new StringBuilder();
        message.append("Time do ").append(player.getName()).append(" completo!\n\nPokémon selecionados:\n");
        for (Species s : selected) {
            message.append("- ").append(s.getName()).append("\n");
        }

        JOptionPane.showMessageDialog(this,
            message.toString(),
            "Time Completo!",
            JOptionPane.INFORMATION_MESSAGE
        );

        if (selectionListener != null) {
            selectionListener.accept(new ArrayList<>(selected));
        }

        dispose();
    }

    /**
     * Registra um callback para quando a seleção estiver completa.
     */
    public void addSelectionListener(Consumer<List<Species>> listener) {
        this.selectionListener = listener;
    }

    /**
     * Retorna a lista de Pokémon selecionados.
     */
    private List<Species> collectSelectedTeam() {
        return new ArrayList<>(selected);
    }
}
