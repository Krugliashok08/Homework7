package org.example;

import java.util.Collection;

public class Main {
    public static void main(String[] args) {
        // Тестирование JSON реализации
        System.out.println("Testing JSON implementation:");
        testService(new PlayerServiceJSON());

        // Тестирование XML реализации
        System.out.println("\nTesting XML implementation:");
        testService(new PlayerServiceXML());
    }

    private static void testService(PlayerService service) {
        // Создаем игрока
        int playerId = service.createPlayer("WinMaster_777");
        System.out.println("Created player with ID: " + playerId);

        // Добавляем очки
        int newPoints = service.addPoints(playerId, 100);
        System.out.println("Player now has " + newPoints + " points");

        // Получаем список игроков
        Collection<Player> players = service.getPlayers();
        System.out.println("Current players:");
        for (Player player : players) {
            System.out.println(player);
        }

        // Удаляем игрока
        Player removed = service.deletePlayer(playerId);
        System.out.println("Removed player: " + removed);

        // Проверяем, что игрок удален
        System.out.println("Players after removal:");
        for (Player player : service.getPlayers()) {
            System.out.println(player);
        }
    }
}