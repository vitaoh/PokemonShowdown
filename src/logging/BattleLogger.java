package logging;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import pokemon.Species;

/**
 * Sistema de logging XML para batalhas Pokémon
 * Registra informações detalhadas sobre cada batalha incluindo:
 * - IPs dos jogadores
 * - Ataques utilizados
 * - Dano causado 
 * - Vencedor da batalha
 * - Estatísticas completas
 */
public class BattleLogger {
    
    private static final String LOG_DIRECTORY = "battle_logs";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    // Dados da batalha atual
    private String battleId;
    private Document document;
    private Element rootElement;
    private Element battleMovesElement;
    private Element battleInfoElement;
    private String logFilePath;
    
    // Informações dos jogadores
    private PlayerInfo player1;
    private PlayerInfo player2;
    
    // Estatísticas da batalha
    private long battleStartTime;
    private int totalDamageP1 = 0;
    private int totalDamageP2 = 0;
    private int turnCounter = 0;
    
    /**
     * Classe interna para armazenar informações do jogador
     */
    public static class PlayerInfo {
        public String name;
        public String ip;
        public List<String> team;
        
        public PlayerInfo(String name, String ip, List<Species> pokemonTeam) {
            this.name = name;
            this.ip = ip;
            this.team = new ArrayList<>();
            for (Species pokemon : pokemonTeam) {
                this.team.add(pokemon.getName());
            }
        }
    }
    
    /**
     * Classe interna para representar um movimento de batalha
     */
    public static class BattleMove {
        public int turn;
        public String playerName;
        public String pokemonName;
        public String moveName;
        public int damage;
        public int targetHp;
        public String timestamp;
        public String target;
        
        public BattleMove(int turn, String playerName, String pokemonName, 
                         String moveName, int damage, int targetHp, String target) {
            this.turn = turn;
            this.playerName = playerName;
            this.pokemonName = pokemonName;
            this.moveName = moveName;
            this.damage = damage;
            this.targetHp = targetHp;
            this.target = target;
            this.timestamp = DATE_FORMAT.format(new Date());
        }
    }
    
    /**
     * Inicializa um novo log de batalha
     */
    public BattleLogger() {
        try {
            // Criar diretório de logs se não existir
            File logDir = new File(LOG_DIRECTORY);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // Gerar ID único para a batalha
            this.battleId = UUID.randomUUID().toString();
            this.battleStartTime = System.currentTimeMillis();
            
            // Criar nome do arquivo com timestamp
            String fileName = "battle_" + FILE_DATE_FORMAT.format(new Date()) + "_" + 
                             battleId.substring(0, 8) + ".xml";
            this.logFilePath = LOG_DIRECTORY + File.separator + fileName;
            
            // Inicializar documento XML
            initializeXMLDocument();
            
            System.out.println("BattleLogger inicializado: " + fileName);
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar BattleLogger: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Inicializa a estrutura básica do documento XML
     */
    private void initializeXMLDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
        
        // Elemento raiz
        rootElement = document.createElement("battleLog");
        rootElement.setAttribute("battleId", battleId);
        rootElement.setAttribute("version", "1.0");
        document.appendChild(rootElement);
        
        // Elemento de informações da batalha
        battleInfoElement = document.createElement("battleInfo");
        rootElement.appendChild(battleInfoElement);
        
        // Elemento para movimentos
        battleMovesElement = document.createElement("battleMoves");
        rootElement.appendChild(battleMovesElement);
        
        // Adicionar timestamp de início
        Element timestampElement = document.createElement("startTimestamp");
        timestampElement.setTextContent(DATE_FORMAT.format(new Date()));
        battleInfoElement.appendChild(timestampElement);
    }
    
    /**
     * Define as informações dos jogadores participantes
     */
    public void setBattleParticipants(PlayerInfo player1, PlayerInfo player2) {
        this.player1 = player1;
        this.player2 = player2;
        
        try {
            // Adicionar informações do jogador 1
            Element p1Element = document.createElement("player1");
            battleInfoElement.appendChild(p1Element);
            
            Element p1Name = document.createElement("name");
            p1Name.setTextContent(player1.name);
            p1Element.appendChild(p1Name);
            
            Element p1Ip = document.createElement("ip");
            p1Ip.setTextContent(player1.ip);
            p1Element.appendChild(p1Ip);
            
            Element p1Team = document.createElement("team");
            p1Element.appendChild(p1Team);
            for (String pokemon : player1.team) {
                Element pokemonElement = document.createElement("pokemon");
                pokemonElement.setTextContent(pokemon);
                p1Team.appendChild(pokemonElement);
            }
            
            // Adicionar informações do jogador 2
            Element p2Element = document.createElement("player2");
            battleInfoElement.appendChild(p2Element);
            
            Element p2Name = document.createElement("name");
            p2Name.setTextContent(player2.name);
            p2Element.appendChild(p2Name);
            
            Element p2Ip = document.createElement("ip");
            p2Ip.setTextContent(player2.ip);
            p2Element.appendChild(p2Ip);
            
            Element p2Team = document.createElement("team");
            p2Element.appendChild(p2Team);
            for (String pokemon : player2.team) {
                Element pokemonElement = document.createElement("pokemon");
                pokemonElement.setTextContent(pokemon);
                p2Team.appendChild(pokemonElement);
            }
            
            System.out.println("Participantes registrados: " + player1.name + " vs " + player2.name);
            
        } catch (Exception e) {
            System.err.println("Erro ao registrar participantes: " + e.getMessage());
        }
    }
    
    /**
     * Registra um movimento de batalha
     */
    public void logBattleMove(BattleMove move) {
        try {
            turnCounter++;
            
            Element moveElement = document.createElement("move");
            moveElement.setAttribute("id", String.valueOf(turnCounter));
            battleMovesElement.appendChild(moveElement);
            
            // Dados básicos do movimento
            createElement(moveElement, "turn", String.valueOf(move.turn));
            createElement(moveElement, "player", move.playerName);
            createElement(moveElement, "pokemon", move.pokemonName);
            createElement(moveElement, "moveName", move.moveName);
            createElement(moveElement, "damage", String.valueOf(move.damage));
            createElement(moveElement, "targetHp", String.valueOf(move.targetHp));
            createElement(moveElement, "target", move.target);
            createElement(moveElement, "timestamp", move.timestamp);
            
            // Atualizar estatísticas de dano
            if (move.playerName.equals(player1.name)) {
                totalDamageP1 += move.damage;
            } else {
                totalDamageP2 += move.damage;
            }
            
            System.out.println("Movimento registrado: " + move.playerName + " usou " + 
                             move.moveName + " causando " + move.damage + " de dano");
            
        } catch (Exception e) {
            System.err.println("Erro ao registrar movimento: " + e.getMessage());
        }
    }
    
    /**
     * Finaliza o log da batalha com resultado e estatísticas
     */
    public void finalizeBattle(String winner) {
        try {
            long battleDuration = (System.currentTimeMillis() - battleStartTime) / 1000;
            
            // Elemento de resultado da batalha
            Element resultElement = document.createElement("battleResult");
            rootElement.appendChild(resultElement);
            
            createElement(resultElement, "winner", winner);
            createElement(resultElement, "duration", String.valueOf(battleDuration));
            createElement(resultElement, "totalTurns", String.valueOf(turnCounter));
            createElement(resultElement, "totalDamageP1", String.valueOf(totalDamageP1));
            createElement(resultElement, "totalDamageP2", String.valueOf(totalDamageP2));
            createElement(resultElement, "endTimestamp", DATE_FORMAT.format(new Date()));
            
            // Salvar arquivo XML
            saveToFile();
            
            System.out.println("Batalha finalizada e salva em: " + logFilePath);
            System.out.println("Vencedor: " + winner);
            System.out.println("Duração: " + battleDuration + " segundos");
            System.out.println("Turnos: " + turnCounter);
            System.out.println("Dano total " + player1.name + ": " + totalDamageP1);
            System.out.println("Dano total " + player2.name + ": " + totalDamageP2);
            
        } catch (Exception e) {
            System.err.println("Erro ao finalizar batalha: " + e.getMessage());
        }
    }
    
    /**
     * Método auxiliar para criar elementos XML com texto
     */
    private void createElement(Element parent, String tagName, String textContent) {
        Element element = document.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }
    
    /**
     * Salva o documento XML no arquivo
     */
    private void saveToFile() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        // Configurar formatação do XML
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
        
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(logFilePath));
        
        transformer.transform(source, result);
    }
    
    /**
     * Lê um arquivo de log XML e retorna informações da batalha
     */
    public static BattleLogData readBattleLog(String filePath) {
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            
            BattleLogData logData = new BattleLogData();
            
            // Informações básicas
            logData.battleId = doc.getDocumentElement().getAttribute("battleId");
            
            // Informações dos jogadores
            logData.player1Name = getElementText(doc, "player1", "name");
            logData.player1Ip = getElementText(doc, "player1", "ip");
            logData.player2Name = getElementText(doc, "player2", "name");
            logData.player2Ip = getElementText(doc, "player2", "ip");
            
            // Times dos jogadores
            logData.player1Team = getPokemonTeam(doc, "player1");
            logData.player2Team = getPokemonTeam(doc, "player2");
            
            // Resultado da batalha
            logData.winner = getElementText(doc, "battleResult", "winner");
            logData.duration = Integer.parseInt(getElementText(doc, "battleResult", "duration"));
            logData.totalTurns = Integer.parseInt(getElementText(doc, "battleResult", "totalTurns"));
            logData.totalDamageP1 = Integer.parseInt(getElementText(doc, "battleResult", "totalDamageP1"));
            logData.totalDamageP2 = Integer.parseInt(getElementText(doc, "battleResult", "totalDamageP2"));
            
            // Movimentos da batalha
            logData.moves = getBattleMoves(doc);
            
            System.out.println("Log lido com sucesso: " + filePath);
            return logData;
            
        } catch (Exception e) {
            System.err.println("Erro ao ler log XML: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Método auxiliar para extrair texto de elementos XML
     */
    private static String getElementText(Document doc, String parentTag, String childTag) {
        NodeList parentNodes = doc.getElementsByTagName(parentTag);
        if (parentNodes.getLength() > 0) {
            Element parentElement = (Element) parentNodes.item(0);
            NodeList childNodes = parentElement.getElementsByTagName(childTag);
            if (childNodes.getLength() > 0) {
                return childNodes.item(0).getTextContent();
            }
        }
        return "";
    }
    
    /**
     * Extrai time de pokémon de um jogador
     */
    private static List<String> getPokemonTeam(Document doc, String playerTag) {
        List<String> team = new ArrayList<>();
        NodeList playerNodes = doc.getElementsByTagName(playerTag);
        if (playerNodes.getLength() > 0) {
            Element playerElement = (Element) playerNodes.item(0);
            NodeList teamNodes = playerElement.getElementsByTagName("team");
            if (teamNodes.getLength() > 0) {
                Element teamElement = (Element) teamNodes.item(0);
                NodeList pokemonNodes = teamElement.getElementsByTagName("pokemon");
                for (int i = 0; i < pokemonNodes.getLength(); i++) {
                    team.add(pokemonNodes.item(i).getTextContent());
                }
            }
        }
        return team;
    }
    
    /**
     * Extrai todos os movimentos da batalha
     */
    private static List<BattleMove> getBattleMoves(Document doc) {
        List<BattleMove> moves = new ArrayList<>();
        NodeList moveNodes = doc.getElementsByTagName("move");
        
        for (int i = 0; i < moveNodes.getLength(); i++) {
            Element moveElement = (Element) moveNodes.item(i);
            
            BattleMove move = new BattleMove(
                Integer.parseInt(getChildElementText(moveElement, "turn")),
                getChildElementText(moveElement, "player"),
                getChildElementText(moveElement, "pokemon"),
                getChildElementText(moveElement, "moveName"),
                Integer.parseInt(getChildElementText(moveElement, "damage")),
                Integer.parseInt(getChildElementText(moveElement, "targetHp")),
                getChildElementText(moveElement, "target")
            );
            move.timestamp = getChildElementText(moveElement, "timestamp");
            moves.add(move);
        }
        
        return moves;
    }
    
    /**
     * Método auxiliar para extrair texto de elementos filhos
     */
    private static String getChildElementText(Element parent, String childTag) {
        NodeList childNodes = parent.getElementsByTagName(childTag);
        if (childNodes.getLength() > 0) {
            return childNodes.item(0).getTextContent();
        }
        return "";
    }
    
    // Getters
    public String getBattleId() { return battleId; }
    public String getLogFilePath() { return logFilePath; }
    public int getTotalDamageP1() { return totalDamageP1; }
    public int getTotalDamageP2() { return totalDamageP2; }
    public int getTurnCounter() { return turnCounter; }
}