package gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class RematchDialog extends JFrame {
    private final String requesterName;
    private Consumer<Boolean> responseCallback;
    private boolean responded = false;
    
    public RematchDialog(String requesterName) {
        this.requesterName = requesterName;
        
        initializeGUI();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Auto-decline após 30 segundos
        Timer timeoutTimer = new Timer(30000, e -> {
            if (!responded) {
                declineRematch("Tempo limite excedido");
            }
        });
        timeoutTimer.setRepeats(false);
        timeoutTimer.start();
    }
    
    private void initializeGUI() {
        setTitle("Solicitação de Revanche");
        setSize(400, 250);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Painel superior - Ícone e título
        JPanel headerPanel = new JPanel(new FlowLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        
        JLabel iconLabel = new JLabel("⚔️");
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        
        JLabel titleLabel = new JLabel("Solicitação de Revanche");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        
        // Painel central - Mensagem
        JPanel messagePanel = new JPanel(new GridLayout(3, 1, 5, 5));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        messagePanel.setBackground(Color.WHITE);
        
        JLabel requestLabel = new JLabel(
            "<html><center><b>" + requesterName + "</b><br>quer uma revanche!</center></html>", 
            SwingConstants.CENTER
        );
        requestLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JLabel questionLabel = new JLabel(
            "Você aceita lutar novamente?", 
            SwingConstants.CENTER
        );
        questionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        questionLabel.setForeground(new Color(52, 73, 94));
        
        JLabel timeLabel = new JLabel(
            "⏰ Tempo limite: 30 segundos", 
            SwingConstants.CENTER
        );
        timeLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        timeLabel.setForeground(Color.GRAY);
        
        messagePanel.add(requestLabel);
        messagePanel.add(questionLabel);
        messagePanel.add(timeLabel);
        
        // Painel inferior - Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton acceptButton = new JButton("✅ Aceitar");
        acceptButton.setPreferredSize(new Dimension(120, 35));
        acceptButton.setBackground(new Color(46, 204, 113));
        acceptButton.setForeground(Color.WHITE);
        acceptButton.setFont(new Font("Arial", Font.BOLD, 14));
        acceptButton.addActionListener(e -> acceptRematch());
        
        JButton declineButton = new JButton("❌ Recusar");
        declineButton.setPreferredSize(new Dimension(120, 35));
        declineButton.setBackground(new Color(231, 76, 60));
        declineButton.setForeground(Color.WHITE);
        declineButton.setFont(new Font("Arial", Font.BOLD, 14));
        declineButton.addActionListener(e -> declineRematch("Recusado pelo jogador"));
        
        buttonPanel.add(acceptButton);
        buttonPanel.add(declineButton);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void acceptRematch() {
        if (!responded) {
            responded = true;
            if (responseCallback != null) {
                responseCallback.accept(true);
            }
            dispose();
        }
    }
    
    private void declineRematch(String reason) {
        if (!responded) {
            responded = true;
            if (responseCallback != null) {
                responseCallback.accept(false);
            }
            dispose();
        }
    }
    
    public void setResponseCallback(Consumer<Boolean> callback) {
        this.responseCallback = callback;
    }
}
