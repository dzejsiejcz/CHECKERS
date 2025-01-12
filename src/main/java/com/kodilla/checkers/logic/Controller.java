package com.kodilla.checkers.logic;

import static com.kodilla.checkers.Checkers.*;

import static com.kodilla.checkers.model.Texts.*;
import static java.lang.Math.abs;
import static com.kodilla.checkers.utils.Constants.COLOR_WHITE;
import static com.kodilla.checkers.utils.Constants.HEIGHT;
import static com.kodilla.checkers.utils.Constants.WIDTH;
import static com.kodilla.checkers.utils.MoveType.FORBIDDEN;
import static com.kodilla.checkers.utils.MoveType.KILLING;
import static com.kodilla.checkers.utils.MoveType.NORMAL;
import static com.kodilla.checkers.utils.PawnType.RED;
import static com.kodilla.checkers.utils.PawnType.WHITE;

import com.kodilla.checkers.model.Coordinates;
import com.kodilla.checkers.model.Field;
import com.kodilla.checkers.model.Pawn;
import com.kodilla.checkers.utils.MoveType;
import com.kodilla.checkers.utils.PawnType;

import java.util.ArrayList;
import java.util.List;

public class Controller {

    public static Controller INSTANCE = new Controller();

    private Controller() {
    }

    /**
     * returning type of pawn's movement which depends on new pawn's position
     */

    public MoveType checkMove(Pawn pawn, int newCol, int newRow) {
        int oldCol = pawn.getCol();
        int oldRow = pawn.getRow();

        /*
         * whose turn?
         */
        if (rightToMove.getType() != pawn.getType()) {
            return FORBIDDEN;
        }

        /*
         * don't move out of board
         */
        if (newCol < 0 || newRow < 0 || newCol >= WIDTH || newRow >= HEIGHT) {
            return FORBIDDEN;
        }
        /*
         * don't move to white field or occupied field
         */
        Field newField = table[newCol][newRow];
        if (newField.getColor().equals(COLOR_WHITE) || newField.hasPawn()) {
            return FORBIDDEN;
        }

        /*
         *  check possibility of beating, if possible, user must beat
         */
        Coordinates checkingFieldAfterBeat = new Coordinates(newCol, newRow);
        //System.out.println("beating?: " + pawn.canKill());
        if (checkBeatingForGivenPawn(pawn)) {
            for (Coordinates currentField : pawn.getFieldsAfterBeats()) {
                if (currentField.getColNumber() == checkingFieldAfterBeat.getColNumber() &&
                        currentField.getRowNumber() == checkingFieldAfterBeat.getRowNumber()) {
                    return KILLING;
                }
            }
            return FORBIDDEN;
        }

        /*
         *  check right direction of move, except beating
         */
        int deltaY = newRow - oldRow;
        if (pawn.getType() == WHITE && deltaY > 0 && !checkBeatingForGivenPawn(pawn)) {
            return FORBIDDEN;
        }
        if (pawn.getType() == RED && deltaY < 0 && !checkBeatingForGivenPawn(pawn)) {
            return FORBIDDEN;
        }

        int deltaX = newCol - oldCol;

        /*
         *   normal movement if no beating, beating is obligatory
         */
        if (abs(deltaX) == 1 && abs(deltaY) == 1 && !checkBeatingOnEntireBoard(pawn.getType()) && game.isGame()) {
            System.out.println("first checking");
            return NORMAL;
        }

        return FORBIDDEN;
    }

    /*
     * check if given Type of Pawns has the possibility of hitting and set this
     * info into each Pawn and return true if any Pawn can hit
     */
    public static boolean checkBeatingOnEntireBoard(PawnType color) {
        List<Pawn> pawns = getListOfOneTypeOfPawns(color);
        boolean response = false;
        for (Pawn checkingPawn : pawns) {
            if (checkBeatingForGivenPawn(checkingPawn)) {
                //System.out.println("Pawn have sth to kill");
                response = true;
                //wiedziałem, że coś nie tak było jak mi skracałeś Macieju tę metodę, nie chciałem returna gry true
                //aby przeszedl wszystkie pionki i ustawił kazdemu pionkowi aktualny boolean canKill,
                // pojawił sie problem bijących dwóch różnych pionków, co naprawiłem,
                // teraz jest mniej liczenia, zrezygnowałem z właściwości canKill i wyszło na Twoje :)
            }
        }
        //System.out.println("Pawn doesn't have anything to kill");
        return response;
    }

    public static boolean checkBeatingForGivenPawn(Pawn checkingPawn) {
        boolean possible = false;
        Field neighbor;
        Field positionAfterBeating;
        checkingPawn.clearFieldsAfterBeats();
        PawnType pawnType = checkingPawn.getType();
        int x = checkingPawn.getCol();
        int y = checkingPawn.getRow();

        /*
         * checking if in neighborhood of given pawn is a possibility to hit
         * local array 5x5: (a -the field after beating, o-an opponent, p-the given pawn)
         * iteration of each of the arms of X shown below
         *
         *    -2 -1 0 +1 +2
         * +2  a         a
         * +1     o   o
         *  0       p
         * -1     o   o
         * -2  a         a
         */

        for (int i = -1; i < 2; i = i + 2) {
            for (int j = -1; j < 2; j = j + 2) {
                int adjacentOpponentCol = x + i;
                int adjacentOpponentRow = y + j;
                int locationAfterBeatCol = adjacentOpponentCol + i;
                int locationAfterBeatRow = adjacentOpponentRow + j;
                /*
                 * checking if position after beating is out of board?
                 */
                if (locationAfterBeatCol >= 0 && locationAfterBeatRow >= 0 &&
                        locationAfterBeatRow < HEIGHT && locationAfterBeatCol < WIDTH) {
                    neighbor = table[adjacentOpponentCol][adjacentOpponentRow];
                    positionAfterBeating = table[locationAfterBeatCol][locationAfterBeatRow];
                    /*
                     * is there a Pawn to hit? is there a free field to move in after hit? is there an opponent pawn?
                     */
                    if (neighbor.hasPawn() && !positionAfterBeating.hasPawn()) {
                        PawnType neighborType = neighbor.getPawn().getType();
                        if (neighborType != pawnType) {
                            checkingPawn.setPossiblePositionAfterBeating(locationAfterBeatCol, locationAfterBeatRow);
                            //System.out.println("to kill: " + neighbor.getPawn().getPawnNumber());
                            possible = true;
                        }
                    }
                }
            }
        }
        //System.out.println(pawnType + " " + checkingPawn.getPawnNumber()
        //        + " has to kill? " + possible);
        //System.out.println("sprawdzany field, x: " + x + " ,y: " + y + table[x][y].toString());
        return possible;
    }

    /*
     * End of movement, changing turn and set new number of pawns
     *
     * @param pawn     used Pawn
     * @param isKilled is that Pawn killed?
     * @return Text to display above the board
     */
    public static String doesMovementSummary(Pawn pawn, boolean isKilled) {
        //checkBeatingOnEntireBoard(RED);
        //checkBeatingOnEntireBoard(WHITE);
        PawnType type = pawn.getType();

        if (isKilled) {
            if (type == WHITE) {
                userRed.subtractOnePawn();
            } else {
                userWhite.subtractOnePawn();
            }
            if (checkBeatingForGivenPawn(pawn)) {
                return additional;
            }
        }

        String endResponse = game.whoWon();

        if (endResponse != null) {
            return endResponse;
        }

        return rightToMove.switchTurn();
    }

    public static List<Field> getListOfFields() {
        List<Field> fields = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                fields.add(table[x][y]);
                System.out.println(table[x][y]);
            }
        }
        return fields;
    }


    public static List<Pawn> getListOfOneTypeOfPawns(PawnType color) {
        List<Pawn> pawns = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Field checkingField = table[x][y];
                if (checkingField.hasPawn() && checkingField.getPawn().getType() == color) {
                    pawns.add(checkingField.getPawn());
                }
            }
        }
        return pawns;
    }


    public static Pawn getPawnByNumber(int number) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Field checkingField = table[x][y];
                if (checkingField.hasPawn() && checkingField.getPawn().getPawnNumber() == number) {
                    return checkingField.getPawn();
                }
            }
        }
        return null;
    }

    public static List<Integer> getListOfRowsOfOneTypeOfPawns(PawnType color) {
        List<Integer> rowsOfPawns = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Field checkingField = table[x][y];
                if (checkingField.hasPawn() && checkingField.getPawn().getType() == color) {
                    rowsOfPawns.add(checkingField.getPawn().getRow());
                }
            }
        }
        return rowsOfPawns;
    }

}
