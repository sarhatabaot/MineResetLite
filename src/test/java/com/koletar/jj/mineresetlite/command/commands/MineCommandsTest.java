package com.koletar.jj.mineresetlite.command.commands;

import com.koletar.jj.mineresetlite.MineResetLite;
import com.koletar.jj.mineresetlite.data.MineData;
import com.koletar.jj.mineresetlite.mine.Mine;
import com.koletar.jj.mineresetlite.util.StringTools;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.koletar.jj.mineresetlite.util.Phrases.phrase;

import static org.junit.jupiter.api.Assertions.*;

class MineCommandsTest {
    private final List<Mine> mines = new ArrayList<>(createMines());

    private static List<Mine> createMines(){
        ArrayList<Mine> mines = new ArrayList<>();
        mines.add(MineData.getTestMine(1));
        mines.add(MineData.getTestMine(2));
        return mines;
    }

    // use mockito heavily here
    @Test
    void listMines() {
    }

    @Test
    void setPoint1() {
    }

    @Test
    void setPoint2() {
    }

    @Test
    void createMine() {
    }

    @Test
    void mineInfo() {
    }

    @Test
    void setComposition() {
    }

    @Test
    void unsetComposition() {
    }

    @Test
    void resetMine() {
    }

    @Test
    void flag() {
    }

    @Test
    void setTP() {
    }

    @Test
    void removeTP() {
    }
}