package com.bomb.server;

import com.bomb.game.Bomb;
import org.springframework.stereotype.Service;

// Holds the bomb that is being played right now
@Service
public class GameService {

    private Bomb bomb;

    public synchronized Bomb newBomb(int minutes, int puzzles) {
        bomb = new Bomb(minutes, puzzles);
        return bomb;
    }

    public synchronized Bomb getBomb() {
        return bomb;
    }
}
