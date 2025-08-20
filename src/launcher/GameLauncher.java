package launcher;

import battle.BattleSwing;
import players.Player;
import server.PokemonServer;
import gui.ConnectionFrame;
import gui.ServerFrame;
import network.NetworkConstants;
import javax.swing.*;
import java.awt.*;

public class GameLauncher extends JFrame {
    
    private static final String TITLE = "Pokémon Battle - Launcher";
    
    public GameLauncher() {
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Título
        JLabel titleLabel = new JLabel("Pokémon Battle", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 102, 153));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Painel de opções
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        // Botão Jogo Local (original)
        JButton localGameButton = createStyledButton("Jogo Local", 
            "Joga como o projeto original (2 jogadores local)");
        localGameButton.addActionListener(e -> startLocalGame());
        optionsPanel.add(localGameButton);
        
        // Botão Servidor
        JButton serverButton = createStyledButton("Iniciar Servidor", 
            "Inicia servidor para jogos em rede");
        serverButton.addActionListener(e -> startServer());
        optionsPanel.add(serverButton);
        
        // Botão Cliente
        JButton clientButton = createStyledButton("Conectar como Cliente", 
            "Conecta a um servidor existente");
        clientButton.addActionListener(e -> startClient());
        optionsPanel.add(clientButton);
        
        // Botão Sair
        JButton exitButton = createStyledButton("Sair", "Fechar aplicação");
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setBackground(new Color(220, 53, 69));
        optionsPanel.add(exitButton);
        
        mainPanel.add(optionsPanel, BorderLayout.CENTER);
        
        // Informações na parte inferior
        JLabel infoLabel = new JLabel("<html><center>Escolha o modo de jogo</center></html>", 
            SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setForeground(Color.GRAY);
        mainPanel.add(infoLabel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(40, 167, 69));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(button.getBackground().brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(button.getBackground().darker());
            }
        });
        
        return button;
    }
    
    private void startLocalGame() {
        this.dispose(); // Fechar launcher
        
        SwingUtilities.invokeLater(() -> {
            System.out.println("Iniciando jogo local...");
            
            // Executar exatamente como o Main.java original
            Player p1 = new Player("Ash");
            Player p2 = new Player("Gary");
            new BattleSwing(p1, p2);
        });
    }

    private void startServer() {
        this.dispose(); // Fechar launcher
        
        SwingUtilities.invokeLater(() -> {
            System.out.println("Iniciando servidor...");
            
            // Criar interface do servidor
            ServerFrame serverFrame = new ServerFrame();
            serverFrame.setVisible(true);
            
            // Criar e iniciar servidor
            PokemonServer server = new PokemonServer(NetworkConstants.DEFAULT_PORT);
            server.setServerFrame(serverFrame);
            server.start();
            
            // Conectar servidor à interface
            serverFrame.setServer(server);
        });
    }
    
    private void startClient() {
        this.dispose(); // Fechar launcher
        
        SwingUtilities.invokeLater(() -> {
            System.out.println("Abrindo tela de conexão...");
            
            // Abrir tela de conexão
            ConnectionFrame connectionFrame = new ConnectionFrame();
            connectionFrame.setVisible(true);
        });
    }
    

    public static void main(String[] args) {
        // Configurar Look and Feel
        
        // Executar launcher
        SwingUtilities.invokeLater(() -> {
            System.out.println("Iniciando Pokémon Battle Launcher...");
            new GameLauncher().setVisible(true);
        });
    }
}