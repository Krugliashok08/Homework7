import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.Player;
import org.example.PlayerService;
import org.example.PlayerServiceJSON;
import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PlayerServiceJSONTest {
    private PlayerService playerService;
    private static final String TEST_JSON_FILE = "players.json";
    private static final String BACKUP_JSON_FILE = "players_backup.json";

    @BeforeEach
    void setUp() throws IOException {
        // Создаем резервную копию файла перед каждым тестом
        File original = new File(TEST_JSON_FILE);
        if (original.exists()) {
            Files.copy(original.toPath(), new File(BACKUP_JSON_FILE).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        playerService = new PlayerServiceJSON();
        // Очищаем данные перед каждым тестом
        new File(TEST_JSON_FILE).delete();
        playerService = new PlayerServiceJSON();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Восстанавливаем файл после каждого теста
        File backup = new File(BACKUP_JSON_FILE);
        if (backup.exists()) {
            Files.copy(backup.toPath(), new File(TEST_JSON_FILE).toPath(), StandardCopyOption.REPLACE_EXISTING);
            backup.delete();
        }
    }

    // Позитивные тесты

    @Test
    void testAddPlayerAndCheckInList() {
        // Получить список игроков
        Collection<Player> initialPlayers = playerService.getPlayers();
        int initialSize = initialPlayers.size();

        // Создать игрока
        int newPlayerId = playerService.createPlayer("testPlayer");

        // Запросить по нему инфо / получить список
        Player createdPlayer = playerService.getPlayerById(newPlayerId);
        Collection<Player> updatedPlayers = playerService.getPlayers();

        // Проверить, что отображается инфо (ник, id, 0 очков, online)
        assertNotNull(createdPlayer);
        assertEquals("testPlayer", createdPlayer.getNick());
        assertEquals(newPlayerId, createdPlayer.getId());
        assertEquals(0, createdPlayer.getPoints());
        assertTrue(createdPlayer.isOnline());
        assertEquals(initialSize + 1, updatedPlayers.size());
        assertTrue(updatedPlayers.contains(createdPlayer));
    }

    @Test
    void testAddAndDeletePlayer() {
        // Создать игрока
        int newPlayerId = playerService.createPlayer("playerToDelete");
        Player createdPlayer = playerService.getPlayerById(newPlayerId);
        assertNotNull(createdPlayer);

        // Удалить игрока
        Player deletedPlayer = playerService.deletePlayer(newPlayerId);
        assertEquals(createdPlayer, deletedPlayer);

        // Проверить отсутствие в списке
        assertNull(playerService.getPlayerById(newPlayerId));
        assertFalse(playerService.getPlayers().contains(deletedPlayer));
    }

    @Test
    void testAddPlayerWhenNoJsonFile() {
        // Убедимся, что файла нет
        new File(TEST_JSON_FILE).delete();

        // Создать игрока
        int newPlayerId = playerService.createPlayer("newPlayer");
        assertNotNull(playerService.getPlayerById(newPlayerId));
    }

    @Test
    void testAddPlayerWhenJsonFileExists() throws IOException {
        // Создадим предварительно файл с одним игроком
        Map<Integer, Player> initialPlayers = new HashMap<>();
        initialPlayers.put(1, new Player(1, "existingPlayer", 100, true));
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .writeValue(new File(TEST_JSON_FILE), initialPlayers.values());

        // Пересоздадим сервис для загрузки существующих данных
        playerService = new PlayerServiceJSON();

        // Создать нового игрока
        int newPlayerId = playerService.createPlayer("newPlayer");
        assertEquals(2, newPlayerId);
        assertNotNull(playerService.getPlayerById(newPlayerId));
        assertNotNull(playerService.getPlayerById(1));
    }

    @Test
    void testAddPointsToExistingPlayer() {
        int playerId = playerService.createPlayer("playerForPoints");
        int initialPoints = playerService.getPlayerById(playerId).getPoints();

        int newPoints = playerService.addPoints(playerId, 50);
        assertEquals(initialPoints + 50, newPoints);
        assertEquals(newPoints, playerService.getPlayerById(playerId).getPoints());
    }

    @Test
    void testAddPointsToExistingPoints() {
        int playerId = playerService.createPlayer("playerWithPoints");
        playerService.addPoints(playerId, 30);

        int newPoints = playerService.addPoints(playerId, 20);
        assertEquals(50, newPoints);
    }

    @Test
    void testGetPlayerByIdAfterCreation() {
        int playerId = playerService.createPlayer("playerForIdTest");
        Player player = playerService.getPlayerById(playerId);
        assertNotNull(player);
        assertEquals(playerId, player.getId());
    }

    @Test
    void testFileSavingCorrectness() throws IOException {
        int playerId = playerService.createPlayer("playerForFileTest");

        // Пересоздадим сервис для проверки загрузки из файла
        PlayerService newService = new PlayerServiceJSON();
        Player loadedPlayer = newService.getPlayerById(playerId);

        assertNotNull(loadedPlayer);
        assertEquals("playerForFileTest", loadedPlayer.getNick());
    }

    @Test
    void testJsonFileLoadingCorrectness() throws IOException {
        // Создадим тестовые данные
        Map<Integer, Player> testPlayers = new HashMap<>();
        testPlayers.put(1, new Player(1, "player1", 10, true));
        testPlayers.put(2, new Player(2, "player2", 20, false));

        // Сохраним в файл
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .writeValue(new File(TEST_JSON_FILE), testPlayers.values());

        // Пересоздадим сервис
        playerService = new PlayerServiceJSON();

        // Проверим загрузку
        Collection<Player> loadedPlayers = playerService.getPlayers();
        assertEquals(2, loadedPlayers.size());
        assertTrue(loadedPlayers.stream().anyMatch(p -> p.getId() == 1 && p.getNick().equals("player1")));
        assertTrue(loadedPlayers.stream().anyMatch(p -> p.getId() == 2 && p.getNick().equals("player2")));
    }

    @Test
    void testUniqueIdGeneration() {
        // Создадим 5 игроков
        for (int i = 1; i <= 5; i++) {
            playerService.createPlayer("player" + i);
        }

        // Удалим 3-го игрока
        playerService.deletePlayer(3);

        // Добавим нового
        int newPlayerId = playerService.createPlayer("newPlayerAfterDelete");

        // Должен быть id = 6, а не 3
        assertEquals(6, newPlayerId);
    }

    @Test
    void testGetPlayersWhenNoJsonFile() {
        // Убедимся, что файла нет
        new File(TEST_JSON_FILE).delete();

        Collection<Player> players = playerService.getPlayers();
        assertNotNull(players);
        assertTrue(players.isEmpty());
    }

    @Test
    void testCreatePlayerWith15CharactersNickname() {
        String nickname = "a".repeat(15);
        int playerId = playerService.createPlayer(nickname);
        Player player = playerService.getPlayerById(playerId);
        assertEquals(nickname, player.getNick());
    }

    // Негативные тесты

    @Test
    void testDeleteNonExistentPlayer() {
        // Создадим несколько игроков
        playerService.createPlayer("player1");
        playerService.createPlayer("player2");

        // Попробуем удалить несуществующего
        Player deleted = playerService.deletePlayer(10);
        assertNull(deleted);
    }

    @Test
    void testCreateDuplicateNickname() {
        String nickname = "duplicateTest";
        playerService.createPlayer(nickname);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            playerService.createPlayer(nickname);
        });

        assertTrue(exception.getMessage().contains("Nickname is already in use"));
    }

    @Test
    void testGetNonExistentPlayerById() {
        Player player = playerService.getPlayerById(999);
        assertNull(player);
    }

    @Test
    void testCreatePlayerWithEmptyNickname() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.createPlayer("");
        });

        assertTrue(exception.getMessage().contains("Nickname cannot be empty"));
    }

    @Test
    void testAddNegativePoints() {
        int playerId = playerService.createPlayer("playerForNegativePoints");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.addPoints(playerId, -10);
        });

        assertTrue(exception.getMessage().contains("Points cannot be negative"));
    }

    @Test
    void testAddPointsToNonExistentPlayer() {
        int result = playerService.addPoints(999, 10);
        assertEquals(-1, result);
    }

    @Test
    void testAddPointsWithoutId() {
        // Это тест на уровне вызова метода, так что просто проверяем обработку несуществующего ID
        int result = playerService.addPoints(0, 10);
        assertEquals(-1, result);
    }

    @Test
    void testLoadSystemWithInvalidJsonFile() throws IOException {
        // Создадим невалидный JSON файл
        Files.write(new File(TEST_JSON_FILE).toPath(), "invalid json".getBytes());

        // При создании сервиса не должно быть исключения, просто пустой список игроков
        PlayerService newService = new PlayerServiceJSON();
        assertTrue(newService.getPlayers().isEmpty());
    }

    @Test
    void testJsonLoadingWithDuplicates() throws IOException {
        // Создадим JSON с дубликатами
        String jsonWithDuplicates = "[{\"id\":1,\"nick\":\"duplicate\",\"points\":10,\"isOnline\":true}," +
                "{\"id\":1,\"nick\":\"duplicate\",\"points\":20,\"isOnline\":false}]";
        Files.write(new File(TEST_JSON_FILE).toPath(), jsonWithDuplicates.getBytes());

        // При загрузке должен остаться только один игрок (последний)
        PlayerService newService = new PlayerServiceJSON();
        assertEquals(1, newService.getPlayers().size());
        Player player = newService.getPlayerById(1);
        assertEquals(20, player.getPoints());
        assertFalse(player.isOnline());
    }

    @Test
    void testCreatePlayerWith16CharactersNickname() {
        String nickname = "a".repeat(16);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.createPlayer(nickname);
        });

        assertTrue(exception.getMessage().contains("Nickname is too long"));
    }
}