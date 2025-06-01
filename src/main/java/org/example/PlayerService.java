package org.example;

import java.util.Collection;

public class PlayerService {
    Player getPlayerById(int id);

    // получить список игроков
    Collection<Player> getPlayers();

    // создать игрока (возвращает id нового игрока)
    int createPlayer(String nickname);

    // удалить игрока по id'шнику, вернет удаленного игрока
    Player deletePlayer(int id);

    // добавить очков игроку. Возвращает обновленный счет
    int addPoints(int playerId, int points);


}
