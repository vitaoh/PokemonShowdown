package gui;

import server.PokemonServer;
import network.NetworkConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerFrame extends JFrame {
    
    private PokemonServer server;
    
    // Componentes da interface
    private JLabel statusLabel;
    private JLabel portLabel;
    private JLabel clientCountLabel;
    private JLabel sessionCountLabel;
    private JLabel uptimeLabel;
    private JTextArea logArea;
    private JButton startStopButton;
    private JProgressBar clientProgressBar;
    
    // Informações do servidor
    private long startTime;
    private Timer uptimeTimer;
    private boolean serverRunning = false;
    
    public ServerFrame() {
        initializeGUI();
        startTime = System.currentTimeMillis();
        startUptimeTimer();
    }
    
    private void initializeGUI() {
        setTitle("Servidor Pokémon - Console");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Handler para fechar servidor ao fechar janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownServer();
            }
        });
        
        // Layout principal
        setLayout(new BorderLayout());
        
        // Painel superior - Status
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.NORTH);
        
        // Painel central - Log
        JPanel logPanel = createLogPanel();
        add(logPanel, BorderLayout.CENTER);
        
        // Painel inferior - Controles
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
        
        // Valores iniciais
        updateDisplay();
    }
    
    /**
     * Cria painel de status do servidor
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Status do Servidor"));
        panel.setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Status
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusLabel = new JLabel("Parado");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(statusLabel, gbc);
        
        // Porta
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Porta:"), gbc);
        gbc.gridx = 3;
        portLabel = new JLabel(String.valueOf(NetworkConstants.DEFAULT_PORT));
        panel.add(portLabel, gbc);
        
        // Clientes conectados
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Clientes:"), gbc);
        gbc.gridx = 1;
        clientCountLabel = new JLabel("0 / " + NetworkConstants.MAX_CLIENTS);
        panel.add(clientCountLabel, gbc);
        
        // Barra de progresso de clientes
        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        clientProgressBar = new JProgressBar(0, NetworkConstants.MAX_CLIENTS);
        clientProgressBar.setValue(0);
        clientProgressBar.setStringPainted(true);
        clientProgressBar.setString("0%");
        panel.add(clientProgressBar, gbc);
        
        // Sessões ativas
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Sessões:"), gbc);
        gbc.gridx = 1;
        sessionCountLabel = new JLabel("0");
        panel.add(sessionCountLabel, gbc);
        
        // Tempo de atividade
        gbc.gridx = 2; gbc.gridy = 2;
        panel.add(new JLabel("Uptime:"), gbc);
        gbc.gridx = 3;
        uptimeLabel = new JLabel("00:00:00");
        panel.add(uptimeLabel, gbc);
        
        return panel;
    }
    
    /**
     * Cria painel de log
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log do Servidor"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(580, 200));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Botão para limpar log
        JButton clearLogButton = new JButton("🗑️ Limpar Log");
        clearLogButton.addActionListener(e -> logArea.setText(""));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearLogButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Log inicial
        addLog("📋 Console do servidor iniciado");
        addLog("⚙️ Porta configurada: " + NetworkConstants.DEFAULT_PORT);
        addLog("👥 Máximo de clientes: " + NetworkConstants.MAX_CLIENTS);
        
        return panel;
    }
    
    /**
     * Cria painel de controles
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        startStopButton = new JButton("🚀 Iniciar Servidor");
        startStopButton.setFont(new Font("Arial", Font.BOLD, 14));
        startStopButton.setBackground(new Color(40, 167, 69));
        startStopButton.setForeground(Color.WHITE);
        startStopButton.addActionListener(e -> toggleServer());
        
        JButton aboutButton = new JButton("ℹ️ Sobre");
        aboutButton.addActionListener(e -> showAbout());
        
        panel.add(startStopButton);
        panel.add(aboutButton);
        
        return panel;
    }
    
    /**
     * Inicia/para o servidor
     */
    private void toggleServer() {
        if (!serverRunning) {
            // Iniciar servidor
            if (server != null && server.isRunning()) {
                addLog("⚠️ Servidor já está rodando");
                return;
            }
            
            addLog("🚀 Iniciando servidor...");
            server = new PokemonServer(NetworkConstants.DEFAULT_PORT);
            server.setServerFrame(this);
            server.start();
            
            serverRunning = true;
            startStopButton.setText("🛑 Parar Servidor");
            startStopButton.setBackground(new Color(220, 53, 69));
            
            setServerStatus("Rodando");
            addLog("✅ Servidor iniciado com sucesso na porta " + NetworkConstants.DEFAULT_PORT);
            
        } else {
            // Parar servidor
            addLog("🛑 Parando servidor...");
            
            if (server != null) {
                server.stopServer();
            }
            
            serverRunning = false;
            startStopButton.setText("🚀 Iniciar Servidor");
            startStopButton.setBackground(new Color(40, 167, 69));
            
            setServerStatus("Parado");
            addLog("🔴 Servidor parado");
            
            // Resetar contadores
            updateClientCount(0);
            updateSessionCount(0);
        }
    }
    
    /**
     * Timer para atualizar tempo de atividade
     */
    private void startUptimeTimer() {
        uptimeTimer = new Timer(1000, e -> updateUptime());
        uptimeTimer.start();
    }
    
    /**
     * Atualiza tempo de atividade
     */
    private void updateUptime() {
        if (serverRunning && startTime > 0) {
            long uptime = System.currentTimeMillis() - startTime;
            long hours = uptime / 3600000;
            long minutes = (uptime % 3600000) / 60000;
            long seconds = (uptime % 60000) / 1000;
            
            uptimeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
    }
    
    /**
     * Adiciona linha ao log
     */
    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * Atualiza display
     */
    private void updateDisplay() {
        if (!serverRunning) {
            statusLabel.setText("Parado");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    /**
     * Shutdown do servidor
     */
    private void shutdownServer() {
        if (server != null && serverRunning) {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Servidor está rodando. Deseja realmente fechar?",
                "Confirmar Fechamento",
                JOptionPane.YES_NO_OPTION
            );
            
            if (option == JOptionPane.YES_OPTION) {
                addLog("🔧 Finalizando servidor...");
                server.stopServer();
                if (uptimeTimer != null) {
                    uptimeTimer.stop();
                }
                System.exit(0);
            }
        } else {
            if (uptimeTimer != null) {
                uptimeTimer.stop();
            }
            System.exit(0);
        }
    }
    
    /**
     * Exibe informações sobre o servidor
     */
    private void showAbout() {
        String info = "<html><body style='padding: 10px;'>" +
                     "<h3>Servidor Pokémon Battle</h3>" +
                     "<p><b>Versão:</b> 1.0</p>" +
                     "<p><b>Porta Padrão:</b> " + NetworkConstants.DEFAULT_PORT + "</p>" +
                     "<p><b>Máx. Clientes:</b> " + NetworkConstants.MAX_CLIENTS + "</p>" +
                     "<p><b>Timeout:</b> " + (NetworkConstants.CONNECTION_TIMEOUT / 1000) + "s</p>" +
                     "<br><p>Sistema de rede adicionado ao projeto original,<br>" +
                     "mantendo todas as funcionalidades intactas.</p>" +
                     "</body></html>";
        
        JOptionPane.showMessageDialog(this, info, "Sobre o Servidor", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Métodos públicos para atualização do servidor
    public void setServerStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            if (status.equals("Rodando")) {
                statusLabel.setForeground(new Color(40, 167, 69));
                startTime = System.currentTimeMillis();
            } else {
                statusLabel.setForeground(Color.RED);
            }
        });
    }
    
    public void updateClientCount(int count) {
        SwingUtilities.invokeLater(() -> {
            clientCountLabel.setText(count + " / " + NetworkConstants.MAX_CLIENTS);
            clientProgressBar.setValue(count);
            int percentage = (count * 100) / NetworkConstants.MAX_CLIENTS;
            clientProgressBar.setString(percentage + "%");
            
            addLog("👥 Clientes conectados: " + count);
        });
    }
    
    public void updateSessionCount(int count) {
        SwingUtilities.invokeLater(() -> {
            sessionCountLabel.setText(String.valueOf(count));
            addLog("🎲 Sessões ativas: " + count);
        });
    }
    
    public void setServer(PokemonServer server) {
        this.server = server;
    }
    
    public static void main(String[] args) {

            
            new ServerFrame().setVisible(true);
        
    }
}