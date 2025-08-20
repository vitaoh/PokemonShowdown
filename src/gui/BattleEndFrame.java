package gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class BattleEndFrame extends JFrame {

    private final String playerName;
    private final String opponentName;
    private final String result;
    private final boolean isWinner;
    private Consumer<Boolean> rematchRequestCallback;
    private boolean requestSent = false;

    public BattleEndFrame(String playerName, String opponentName, String result, boolean isWinner) {
        this.playerName = playerName;
        this.opponentName = opponentName;
        this.result = result;
        this.isWinner = isWinner;

        initializeGUI();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (rematchRequestCallback != null && !requestSent) {
                    rematchRequestCallback.accept(false);
                }
            }
        });
    }

    private void initializeGUI() {
        setTitle("Batalha Finalizada");
        setSize(450, 300);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Painel superior - Resultado
        JPanel resultPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        resultPanel.setBackground(Color.WHITE);

        // Título resultado
        JLabel titleLabel = new JLabel("Resultado da Batalha", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));

        // Resultado principal
        JLabel resultLabel = new JLabel(result, SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        if (isWinner) {
            resultLabel.setForeground(new Color(0, 150, 0));
            resultLabel.setText("🎉 " + result + " 🎉");
        } else {
            resultLabel.setForeground(new Color(200, 0, 0));
            resultLabel.setText("😔 " + result);
        }

        // Informação dos jogadores
        JLabel playersLabel = new JLabel(
                "<html><center>" + playerName + " vs " + opponentName + "</center></html>",
                SwingConstants.CENTER
        );
        playersLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        playersLabel.setForeground(new Color(100, 100, 100));

        resultPanel.add(titleLabel);
        resultPanel.add(resultLabel);
        resultPanel.add(playersLabel);

        // Painel central - Estatísticas (opcional)
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createTitledBorder("Estatísticas da Batalha"));

        JLabel statsLabel = new JLabel(
                "<html><center>Boa batalha!<br>Obrigado por jogar!</center></html>"
        );
        statsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statsPanel.add(statsLabel);

        // Painel inferior - Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton rematchButton = new JButton("🔄 Quero Revanche");
        rematchButton.setPreferredSize(new Dimension(180, 40));
        rematchButton.setBackground(new Color(52, 152, 219));
        rematchButton.setForeground(Color.WHITE);
        rematchButton.setFont(new Font("Arial", Font.BOLD, 14));
        rematchButton.addActionListener(e -> requestRematch());

        JButton exitButton = new JButton("🚪 Fechar");
        exitButton.setPreferredSize(new Dimension(150, 40));
        exitButton.setBackground(new Color(220, 53, 69));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Deseja realmente fechar o jogo?",
                    "Confirmar Saída",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Notificar servidor sobre saída
                if (rematchRequestCallback != null) {
                    rematchRequestCallback.accept(false); // Indica que não quer revanche
                }

                System.out.println("🚪 Fechando jogo por solicitação do jogador");
                System.exit(0); // Fechar programa completamente
            }
        });

        buttonPanel.add(rematchButton);
        buttonPanel.add(exitButton);

        mainPanel.add(resultPanel, BorderLayout.NORTH);
        mainPanel.add(statsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void requestRematch() {
        if (requestSent) {
            JOptionPane.showMessageDialog(this,
                    "Você já solicitou revanche!\nAguardando o oponente também solicitar.",
                    "Revanche",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Deseja uma revanche contra " + opponentName + "?\n\n"
                + "⚠️ A revanche só acontecerá se AMBOS os jogadores solicitarem!\n"
                + "Vocês irão escolher pokémon novamente.",
                "Confirmar Revanche",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            requestSent = true;

            // Desabilitar botão e alterar texto
            JButton rematchBtn = findRematchButton();
            if (rematchBtn != null) {
                rematchBtn.setText("⏳ Aguardando " + opponentName + "...");
                rematchBtn.setEnabled(false);
                rematchBtn.setBackground(new Color(149, 165, 166));
            }

            // Chamar callback
            if (rematchRequestCallback != null) {
                rematchRequestCallback.accept(true);
            }

            // Mostrar mensagem
            JOptionPane.showMessageDialog(this,
                    "Solicitação enviada!\n\n"
                    + "Aguardando " + opponentName + " também solicitar revanche.\n"
                    + "Quando ambos solicitarem, vocês escolherão pokémon novamente.",
                    "Revanche Solicitada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JButton findRematchButton() {
        return findButtonRecursive(this, "🔄 Solicitar Revanche");
    }

    private JButton findButtonRecursive(Container container, String buttonText) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getText().contains("Solicitar Revanche")) {
                    return button;
                }
            } else if (component instanceof Container) {
                JButton found = findButtonRecursive((Container) component, buttonText);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public void setRematchRequestCallback(Consumer<Boolean> callback) {
        this.rematchRequestCallback = callback;
    }

    public void handleRematchDeclined(String reason) {
        SwingUtilities.invokeLater(() -> {
            JButton rematchBtn = findRematchButton();
            if (rematchBtn != null) {
                rematchBtn.setText("🔄 Solicitar Revanche");
                rematchBtn.setEnabled(true);
                rematchBtn.setBackground(new Color(52, 152, 219));
            }

            requestSent = false;

            JOptionPane.showMessageDialog(this,
                    "Revanche recusada: " + reason,
                    "Revanche",
                    JOptionPane.INFORMATION_MESSAGE);

            System.exit(0); // Encerra o programa depois do clique em OK
        });
    }

    public void handleRematchAccepted() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Revanche aceita! Preparando nova batalha...",
                    "Revanche",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
    }
}
