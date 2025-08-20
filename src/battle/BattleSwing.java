package battle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import players.Player;
import players.TeamSelectionCallback;
import pokemon.Move;
import pokemon.Species;
import client.PokemonClient;
import network.BattleStateDTO;

public class BattleSwing extends JFrame implements TeamSelectionCallback {

    private Player player1;
    private Player player2;
    private List<PokemonInstance> team1;
    private List<PokemonInstance> team2;
    private final Random random = new Random();

    private boolean networked = false;
    private client.PokemonClient clientRef = null;

    // GUI components
    private BackgroundPanel backgroundPanel;
    private JLabel player1Sprite, player2Sprite;
    private JLabel player1Name, player2Name;
    private JProgressBar hpBar1, hpBar2;
    private JLabel hpLabel1, hpLabel2;
    private JProgressBar hpBarPlayer;
    private JProgressBar hpBarOpponent;
    private JLabel lblSpritePlayer;
    private JLabel lblSpriteOpponent;
    private JTextArea battleLog;
    private JButton[] moveButtons = new JButton[4];
    private List<Species> yourTeam;
    private List<Species> opponentTeam;
    private JLabel statusLabel;
    private JLabel extraImage1, extraImage2;

    // Battle state
    private PokemonInstance active1, active2;
    private boolean p1Turn, waitingInput;

    // Track readiness
    private boolean ready1 = false, ready2 = false;

    public BattleSwing(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        setTitle("Batalha Pokémon");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);

         //Start team selection
        System.out.println("=== Iniciando seleção de times ===");
        p1.chooseTeam(this);
        p2.chooseTeam(this);
        System.out.println("Selecione os times nas janelas abertas...");
    }

    public BattleSwing(Player me, Player foe, boolean networked) {
        this(me, foe);                       // chama construtor antigo
        this.networked = networked;
    }

    public BattleSwing(Player me, Player foe, boolean networked, PokemonClient clientRef) {
        this(me, foe, networked);
        this.clientRef = clientRef;
    }
    
    @Override
    public synchronized void onTeamSelectionComplete(Player player) {
        System.out.println("Time de " + player.getName() + " pronto.");
        if (player == player1) {
            ready1 = true;
        } else if (player == player2) {
            ready2 = true;
        }

        if (ready1 && ready2) {
            SwingUtilities.invokeLater(this::setupAndShow);
        } else {
            System.out.println("Aguardando o outro jogador...");
        }
    }
    
    private void setupAndShow() {
        // build teams
        team1 = createInstances(player1);
        team2 = createInstances(player2);

        // painel de fundo
        backgroundPanel = new BackgroundPanel();
        setContentPane(backgroundPanel);
        backgroundPanel.setLayout(new BorderLayout());

        // cria JLayeredPane
        JLayeredPane layered = new JLayeredPane();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        layered.setPreferredSize(screen);
        backgroundPanel.add(layered, BorderLayout.CENTER);

        // Coordenadas centrais anotadas
        int cx2 = 1520, cy2 = 500;
        int cx1 = 600, cy1 = 640;

        int wSprite = 400, hSprite = 400;
        int wInfo = 120, hInfo = 50;
        int margin = 10;

        // Sprite Player 2
        player2Sprite = new JLabel();
        player2Sprite.setBounds(
                cx2 - wSprite / 2,
                cy2 - hSprite / 2,
                wSprite, hSprite
        );
        layered.add(player2Sprite, JLayeredPane.PALETTE_LAYER);

        // Info Player 2 (acima)
        JPanel info2 = new JPanel();
        info2.setLayout(new BoxLayout(info2, BoxLayout.Y_AXIS));
        info2.setOpaque(false);
        player2Name = labelStyled();
        hpLabel2 = labelStyledSmall();
        hpBar2 = hpBarStyled();
        info2.add(player2Name);
        info2.add(hpLabel2);
        info2.add(hpBar2);
        info2.setBounds(
                cx2 - wInfo / 2,
                cy2 - hSprite / 2 - hInfo - margin,
                wInfo, hInfo
        );
        layered.add(info2, JLayeredPane.PALETTE_LAYER);

        // Sprite Player 1
        player1Sprite = new JLabel();
        player1Sprite.setBounds(
                cx1 - wSprite / 2,
                cy1 - hSprite / 2,
                wSprite, hSprite
        );
        layered.add(player1Sprite, JLayeredPane.PALETTE_LAYER);

        // Info Player 1 (abaixo)
        JPanel info1 = new JPanel();
        info1.setLayout(new BoxLayout(info1, BoxLayout.Y_AXIS));
        info1.setOpaque(false);
        player1Name = labelStyled();
        hpLabel1 = labelStyledSmall();
        hpBar1 = hpBarStyled();
        info1.add(player1Name);
        info1.add(hpLabel1);
        info1.add(hpBar1);
        info1.setBounds(
                cx1 - wInfo / 2,
                cy1 + hSprite / 2 + margin,
                wInfo, hInfo
        );
        layered.add(info1, JLayeredPane.PALETTE_LAYER);

        // Controle e log (sem alterações)
        JPanel control = new JPanel(new BorderLayout());
        control.setOpaque(false);
        statusLabel = new JLabel("Preparando...", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        control.add(statusLabel, BorderLayout.NORTH);

        moveButtons = new JButton[4];
        JPanel moves = new JPanel(new GridLayout(2, 2, 10, 10));
        moves.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            JButton b = new JButton();
            b.setVisible(false);
            final int idx = i;
            b.addActionListener(e -> choose(idx));
            moveButtons[i] = b;
            moves.add(b);
        }
        control.add(moves, BorderLayout.CENTER);

        battleLog = new JTextArea(6, 30);
        battleLog.setEditable(false);
        battleLog.setForeground(Color.WHITE);
        battleLog.setBackground(new Color(0, 0, 0, 150));
        JScrollPane scroll = new JScrollPane(battleLog);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(control, BorderLayout.CENTER);
        south.add(scroll, BorderLayout.EAST);
        backgroundPanel.add(south, BorderLayout.SOUTH);

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        initBattle();
    }

    private void initBattle() {
        System.out.println("=== Iniciando batalha ===");
        active1 = nextActive(team1);
        active2 = nextActive(team2);
        p1Turn = random.nextBoolean();
        updateDisplay();
        log("Batalha começa! " + (p1Turn ? player1.getName() : player2.getName()) + " ataca primeiro.");
        nextTurn();
    }

    private void nextTurn() {

        if (active1 == null || active2 == null) {
            Player win = (active1 == null) ? player2 : player1;
            log("=== " + win.getName() + " venceu! ===");
            statusLabel.setText("Fim! " + win.getName() + " venceu!");
            return;
        }

        if (active1.isFainted() || active2.isFainted()) {
            handleFaint();
            return;
        }
        PokemonInstance atk = p1Turn ? active1 : active2;
        PokemonInstance def = p1Turn ? active2 : active1;
        statusLabel.setText(atk.getSpecies().getName() + " faz sua jogada");
        if (p1Turn) {
            showMoves(atk);
        } else {
            Timer t = new Timer(800, e -> {
                execute(atk, def, atk.getNextMove());
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private void showMoves(PokemonInstance atk) {
        Move[] m = atk.getSpecies().getMoves();
        for (int i = 0; i < 4; i++) {
            if (i < m.length) {
                moveButtons[i].setText(m[i].getName() + " (" + m[i].getPower() + ")");
                moveButtons[i].setVisible(true);
                moveButtons[i].setEnabled(true);
            } else {
                moveButtons[i].setVisible(false);
            }
        }
    }

    public void enableMoveSelection() {
        if (networked) {
            SwingUtilities.invokeLater(() -> {
                PokemonInstance atk = p1Turn ? active1 : active2;
                if (atk != null) {
                    showMoves(atk);
                }
            });
        }
    }

    public void refreshHpBars(BattleStateDTO state) {
        if (networked) {
            SwingUtilities.invokeLater(() -> {
                // Atualizar HP baseado no estado recebido do servidor
                if (hpBar1 != null && hpBar2 != null) {
                    // Assumir que state.hpP1 e hpP2 são valores percentuais (0-100)
                    hpBar1.setValue(state.hpP1);
                    hpBar2.setValue(state.hpP2);

                    // Atualizar labels também
                    if (hpLabel1 != null && hpLabel2 != null) {
                        hpLabel1.setText(state.hpP1 + "/100");
                        hpLabel2.setText(state.hpP2 + "/100");
                    }
                }
            });
        }
    }

    // Método para receber atualizações de movimento do servidor
    public void updateBattleFromServer(String moveResult) {
        if (networked) {
            SwingUtilities.invokeLater(() -> {
                log(moveResult);
                updateDisplay();
            });
        }
    }

    private void choose(int idx) {
        hideButtons();
        if (networked && clientRef != null) {
            clientRef.sendMove(idx);     // Comunicação client-servidor (online)
        } else {
            // Modo local - usar lógica existente
            PokemonInstance atk = p1Turn ? active1 : active2;
            PokemonInstance def = p1Turn ? active2 : active1;
            Move mv = atk.getSpecies().getMoves()[idx];
            execute(atk, def, mv);
        }
    }

    private void hideButtons() {
        for (JButton b : moveButtons) {
            b.setVisible(false);
        }
    }

    private void execute(PokemonInstance atk, PokemonInstance def, Move mv) {
        int dmg = mv.getPower();
        def.receiveDamage(dmg);
        log(atk.getSpecies().getName() + " usou " + mv.getName() + " e causou " + dmg + " de dano.");
        updateDisplay();
        p1Turn = !p1Turn;
        Timer t = new Timer(800, e -> nextTurn());
        t.setRepeats(false);
        t.start();
    }

    private void handleFaint() {
        PokemonInstance faint = active1.isFainted() ? active1 : active2;
        log(faint.getSpecies().getName() + " desmaiou!");

        if (active1.isFainted()) {
            active1 = nextActive(team1);
        }
        if (active2.isFainted()) {
            active2 = nextActive(team2);
        }

        // Se um dos times acabou, termina sem chamar updateDisplay()
        if (active1 == null || active2 == null) {
            Player win = (active1 == null) ? player2 : player1;
            log("=== " + win.getName() + " venceu! ===");
            statusLabel.setText("Fim! " + win.getName() + " venceu!");

            // Exibe a tela de vitória
            SwingUtilities.invokeLater(() -> showVictoryScreen(win));
            return;
        }

        // Caso ainda haja Pokémon, continua...
        updateDisplay();
        new Timer(800, e -> nextTurn()).start();
    }

    private void showVictoryScreen(Player winner) {
        // Janela principal
        JFrame victoryFrame = new JFrame("Vitória!");
        victoryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        victoryFrame.setSize(400, 250);
        victoryFrame.setLocationRelativeTo(this); // centraliza em relação à janela principal

        // Conteúdo
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 128, 0)); // verde suave

        JLabel title = new JLabel("Parabéns, " + winner.getName() + "!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Você venceu a batalha!", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 16));
        sub.setForeground(Color.WHITE);

        // Botão OK
        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.addActionListener(e -> {
            victoryFrame.dispose();   // fecha a janela de vitória
            System.exit(0);           // encerra a aplicação
        });

        // Painel para alinhar botão na parte inferior
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0, 128, 0));
        buttonPanel.add(okButton);

        // Montagem da janela
        panel.add(title, BorderLayout.NORTH);
        panel.add(sub, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        victoryFrame.setContentPane(panel);
        victoryFrame.setVisible(true);
    }

    // Helpers
    private List<PokemonInstance> createInstances(Player p) {
        List<PokemonInstance> list = new ArrayList<>();
        for (Species s : p.getTeam()) {
            list.add(new PokemonInstance(s));
        }
        return list;
    }

    private PokemonInstance nextActive(List<PokemonInstance> team) {
        return team.stream().filter(pk -> !pk.isFainted()).findFirst().orElse(null);
    }

    private void updateDisplay() {
        // Player 1: pokemonBack (de costas, embaixo na direita)
        if (active1 != null) {
            updateSprite(player1Sprite, active1, "pokemonsBack", 400, 400);
            player1Name.setText(active1.getSpecies().getName());
            updateHp(hpBar1, hpLabel1, active1);
        } else {
            player1Sprite.setIcon(null);
            player1Name.setText("");
            hpBar1.setValue(0);
            hpLabel1.setText("0/0");
        }

        // Player 2: pokemonFront (de frente, em cima na esquerda)
        if (active2 != null) {
            updateSprite(player2Sprite, active2, "pokemonsFront", 240, 240);
            player2Name.setText(active2.getSpecies().getName());
            updateHp(hpBar2, hpLabel2, active2);
        } else {
            player2Sprite.setIcon(null);
            player2Name.setText("");
            hpBar2.setValue(0);
            hpLabel2.setText("0/0");
        }
    }

    private void updateSprite(JLabel lbl, PokemonInstance pi, String folder, int x, int y) {
        if (pi == null) {
            lbl.setIcon(null);
            return;
        }
        String path = "/" + folder + "/" + pi.getSpecies().getName().toLowerCase() + ".png";
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                lbl.setIcon(new ImageIcon(img.getScaledInstance(x, y, Image.SCALE_SMOOTH)));
            } else {
                lbl.setIcon(null); // not found
            }
        } catch (Exception e) {
            lbl.setIcon(null);
        }
    }

    private void updateHp(JProgressBar bar, JLabel txt, PokemonInstance pi) {
        int cur = pi.getCurrentHp(), max = pi.getSpecies().getBaseStats()[0];
        int pct = cur * 100 / max;
        bar.setValue(pct);
        txt.setText(cur + "/" + max);
    }

    private void log(String msg) {
        battleLog.append(msg + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    private JLabel labelStyled() {
        JLabel l = new JLabel("", SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        return l;
    }

    private JLabel labelStyledSmall() {
        JLabel l = new JLabel("", SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        return l;
    }

    private JProgressBar hpBarStyled() {
        JProgressBar b = new JProgressBar(0, 100);
        b.setStringPainted(false);
        b.setPreferredSize(new Dimension(100, 12));
        return b;
    }

    private JPanel panelInfo(JLabel name, JLabel hpTxt, JProgressBar bar) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(name);
        p.add(hpTxt);
        p.add(bar);
        return p;
    }

    // Custom background
    private class BackgroundPanel extends JPanel {

        private BufferedImage bg;

        public BackgroundPanel() {
            setLayout(new BorderLayout());
            try (InputStream in = getClass().getResourceAsStream("/background/background.png")) {
                if (in != null) {
                    bg = ImageIO.read(in);
                }
            } catch (IOException e) {
                bg = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            } else {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Color.DARK_GRAY, 0, getHeight(), Color.LIGHT_GRAY));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    // Pokémon wrapper
    private static class PokemonInstance {

        private final Species species;
        private int currentHp;

        public PokemonInstance(Species s) {
            this.species = s;
            this.currentHp = s.getBaseStats()[0];
        }

        public Species getSpecies() {
            return species;
        }

        public int getCurrentHp() {
            return currentHp;
        }

        public boolean isFainted() {
            return currentHp <= 0;
        }

        public void receiveDamage(int d) {
            currentHp = Math.max(0, currentHp - d);
        }

        public Move getNextMove() {
            Move[] m = species.getMoves();
            return m[new Random().nextInt(m.length)];
        }
    }
}
