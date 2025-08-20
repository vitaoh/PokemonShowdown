package gui;

import client.PokemonClient;
import network.NetworkConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ConnectionFrame extends JFrame {

    private JTextField hostField;
    private JTextField portField;
    private JTextField playerNameField;
    private JButton connectButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    public ConnectionFrame() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Conectar ao Servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);

        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titleLabel = new JLabel("Conectar ao Servidor Pokémon", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(51, 102, 153));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Painel de formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Nome do jogador
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Nome do Jogador:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        playerNameField = new JTextField("Player", 15);
        formPanel.add(playerNameField, gbc);

        // Host do servidor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Servidor:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        hostField = new JTextField(NetworkConstants.DEFAULT_HOST, 15);
        formPanel.add(hostField, gbc);

        // Porta do servidor
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Porta:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        portField = new JTextField(String.valueOf(NetworkConstants.DEFAULT_PORT), 15);
        formPanel.add(portField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout());

        connectButton = new JButton("Conectar");
        connectButton.setFont(new Font("Arial", Font.BOLD, 12));
        connectButton.setBackground(new Color(40, 167, 69));
        connectButton.setForeground(Color.WHITE);
        connectButton.addActionListener(this::connectToServer);
        buttonPanel.add(connectButton);

        cancelButton = new JButton("Cancelar");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Status label
        statusLabel = new JLabel("Digite as informações e clique em Conectar", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);

        JPanel statusPanel = new JPanel();
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Enter para conectar
        getRootPane().setDefaultButton(connectButton);
    }

    /**
     * Conecta ao servidor
     */
    private void connectToServer(ActionEvent e) {
        String playerName = playerNameField.getText().trim();
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();

        // Validações
        if (playerName.isEmpty()) {
            showError("Nome do jogador não pode estar vazio!");
            playerNameField.requestFocus();
            return;
        }

        if (host.isEmpty()) {
            showError("Servidor não pode estar vazio!");
            hostField.requestFocus();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            showError("Porta deve ser um número entre 1 e 65535!");
            portField.requestFocus();
            return;
        }

        // Desabilitar botão durante conexão
        connectButton.setEnabled(false);
        connectButton.setText("Conectando...");
        statusLabel.setText("Conectando ao servidor...");
        statusLabel.setForeground(Color.BLUE);

        // Conectar em thread separada para não travar a UI
        SwingWorker<Boolean, Void> connectionWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                PokemonClient client = new PokemonClient(playerName, host, port);
                return client.connect();
            }

            @Override
            protected void done() {
                try {
                    boolean connected = get();

                    if (connected) {
                        statusLabel.setText("Conectado com sucesso!");
                        statusLabel.setForeground(new Color(40, 167, 69));

                        // Fechar tela de conexão após sucesso
                        Timer timer = new Timer(1500, event -> {
                            ConnectionFrame.this.dispose();
                        });
                        timer.setRepeats(false);
                        timer.start();

                    } else {
                        connectionFailed("Falha ao conectar ao servidor");
                    }

                } catch (Exception ex) {
                    connectionFailed("Erro durante conexão: " + ex.getMessage());
                }
            }
        };

        connectionWorker.execute();
    }

    private void connectionFailed(String message) {
        connectButton.setEnabled(true);
        connectButton.setText("Conectar");
        statusLabel.setText("Falha na conexão");
        statusLabel.setForeground(Color.RED);

        showError(message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {

        new ConnectionFrame().setVisible(true);

    }
}
