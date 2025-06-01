package org.example;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PlayerServiceJSON implements PlayerService {
    private final Map<Integer, Player> players = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final File file = new File("players.json");

    public PlayerServiceJSON() {
        loadPlayers();
    }

    private void loadPlayers() {
        if (file.exists()) {
            try {
                Player[] playersArray = objectMapper.readValue(file, Player[].class);
                for (Player player : playersArray) {
                    players.put(player.getId(), player);
                }
            } catch (IOException e) {
                System.err.println("Error loading players from JSON: " + e.getMessage());
            }
        }
    }

    private void savePlayers() {
        try {
            objectMapper.writeValue(file, players.values());
        } catch (IOException e) {
            System.err.println("Error saving players to JSON: " + e.getMessage());
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
}
