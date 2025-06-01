package org.example;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PlayerServiceXML implements PlayerService {
    private final Map<Integer, Player> players = new HashMap<>();
    private final XmlMapper xmlMapper = new XmlMapper();
    private final File file = new File("players.xml");

    public PlayerServiceXML() {
        loadPlayers();
    }

    private void loadPlayers() {
        if (file.exists()) {
            try {
                PlayerListWrapper wrapper = xmlMapper.readValue(file, PlayerListWrapper.class);
                if (wrapper != null && wrapper.getPlayers() != null) {
                    for (Player player : wrapper.getPlayers()) {
                        players.put(player.getId(), player);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading players from XML: " + e.getMessage());
            }
        }
    }

    private void savePlayers() {
        try {
            PlayerListWrapper wrapper = new PlayerListWrapper();
            wrapper.setPlayers(players.values());
            xmlMapper.writeValue(file, wrapper);
        } catch (IOException e) {
            System.err.println("Error saving players to XML: " + e.getMessage());
        }
    }

    @Override
    public Player getPlayerById(int id) {
        return players.get(id);
    }

    @Override
    public Collection<Player> getPlayers() {
        return players.values();
    }

    @Override
    public int createPlayer(String nickname) {
        int newId = players.isEmpty() ? 1 : players.keySet().stream().max(Integer::compare).get() + 1;
        Player player = new Player(newId, nickname, 0, true);
        players.put(newId, player);
        savePlayers();
        return newId;
    }

    @Override
    public Player deletePlayer(int id) {
        Player removed = players.remove(id);
        if (removed != null) {
            savePlayers();
        }
        return removed;
    }

    @Override
    public int addPoints(int playerId, int points) {
        Player player = players.get(playerId);
        if (player != null) {
            player.setPoints(player.getPoints() + points);
            savePlayers();
            return player.getPoints();
        }
        return -1;
    }

    // Вспомогательный класс для правильной сериализации XML
    public static class PlayerListWrapper {
        private Collection<Player> players;

        public Collection<Player> getPlayers() {
            return players;
        }

        public void setPlayers(Collection<Player> players) {
            this.players = players;
        }
    }
}