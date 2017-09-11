package com.epam.dojo.expansion.model;

/*-
 * #%L
 * iCanCode - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2016 EPAM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.epam.dojo.expansion.model.lobby.PlayerLobby;
import com.epam.dojo.expansion.model.lobby.WaitForAllPlayerLobby;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.epam.dojo.expansion.model.AbstractSinglePlayersTest.*;
import static com.epam.dojo.expansion.model.AbstractSinglePlayersTest.PLAYER6;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by Sanja on 15.02.14.
 */
public class GameRunnerWithLobbyTest extends AbstractGameRunnerTest {

    @NotNull
    protected PlayerLobby getLobby() {
        return new WaitForAllPlayerLobby();
    }

    @Test
    public void shouldCreateSixPlayersInTwoDifferentRooms() {
        givenLevels();

        createNewGame();
        createNewGame();
        createNewGame();
        createNewGame();
        createNewGame();
        createNewGame();

        String levelLobby =
                "╔══════════════════┐\n" +
                "║..................│\n" +
                "║..................│\n" +
                "║..................│\n" +
                "║..B....BBB..BBB...│\n" +
                "║..B....B.B..B.B...│\n" +
                "║..B....B.B..BBB...│\n" +
                "║..B....B.B..B.B...│\n" +
                "║..BBB..BBB..BBB...│\n" +
                "║..................│\n" +
                "║..................│\n" +
                "║........BBB..B.B..│\n" +
                "║........B.B..B.B..│\n" +
                "║........BBB...B...│\n" +
                "║........B.B...B...│\n" +
                "║........BBB...B...│\n" +
                "║..................│\n" +
                "║..................│\n" +
                "║..................│\n" +
                "└──────────────────┘\n";
        String forcesLobby =
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n";
        assertL(levelLobby, PLAYER1);
        assertE(forcesLobby, PLAYER1);
        assertL(levelLobby, PLAYER2);
        assertE(forcesLobby, PLAYER2);
        assertL(levelLobby, PLAYER3);
        assertE(forcesLobby, PLAYER3);
        assertL(levelLobby, PLAYER4);
        assertE(forcesLobby, PLAYER4);
        assertL(levelLobby, PLAYER5);
        assertE(forcesLobby, PLAYER5);
        assertL(levelLobby, PLAYER6);
        assertE(forcesLobby, PLAYER6);

        // when
        levelOrFreeRoom(LEVEL1,  // LEVEL1
                0, // first free room
                0, // first free room
                0, // first free room
                LEVEL2,
                0); // first free room

        tickAll();

        // then
        String level1 =
                "╔════┐\n" +
                "║1..2│\n" +
                "║....│\n" +
                "║....│\n" +
                "║4..3│\n" +
                "└────┘\n";
        String forces1 =
                "------\n" +
                "-♥--♦-\n" +
                "------\n" +
                "------\n" +
                "-♠--♣-\n" +
                "------\n";
        assertL(level1, PLAYER1);
        assertE(forces1, PLAYER1);
        assertL(level1, PLAYER2);
        assertE(forces1, PLAYER2);
        assertL(level1, PLAYER3);
        assertE(forces1, PLAYER3);
        assertL(level1, PLAYER4);
        assertE(forces1, PLAYER4);

        String level2 =
                "╔════┐\n" +
                "║..1.│\n" +
                "║4...│\n" +
                "║...2│\n" +
                "║.3..│\n" +
                "└────┘\n";
        String forces2 =
                "------\n" +
                "---♥--\n" +
                "------\n" +
                "----♦-\n" +
                "------\n" +
                "------\n";
        assertL(level2, PLAYER5);
        assertE(forces2, PLAYER5);
        assertL(level2, PLAYER6);
        assertE(forces2, PLAYER6);
    }

}
