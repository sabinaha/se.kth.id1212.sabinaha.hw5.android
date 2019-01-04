package shared;

import java.io.Serializable;

/**
 * A class containing the public information of a hangman game at a point in time.
 */
public class GameInfo implements Serializable {

    private GameState gameState;
    private char[] wordProgress;
    private char[] guessedChars;
    private int remainingAttempts;
    private int score;
    private String secretWord;

    /**
     * Creates a public knowledge snapshot of the hangman game.
     * @param wordProgress The word-guessing progress.
     * @param remainingAttempts The remaining guess attempts.
     * @param score The session score.
     * @param word The actual word, revealed
     * @param gs The game-state, such as Ongoing, Won or Lost.
     */
    public GameInfo(char[] wordProgress, char[] guessedChars, int remainingAttempts, int score, String word, GameState gs) {
        this.secretWord = word;
        this.wordProgress = wordProgress;
        this.guessedChars = guessedChars;
        this.remainingAttempts = remainingAttempts;
        this.score = score;
        this.gameState = gs;
    }

    public char[] getWordProgress() {
        return this.wordProgress;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public int getScore() {
        return score;
    }

    public String getSecretWord() {
        return secretWord;
    }

    public GameState getGameState() {
        return gameState;
    }

    /*
     * FORMAT FOR THE INFORMATION SENDING:
     * GAME_STATE|W _ R _|A,B,C,D|(rem_attempts:int)|(score:int)[|secretWord:String]
     *           CORRECT  GUESSED
     * */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(gameState);
        sb.append("|");

        for (char progress : wordProgress) {
            if (progress == '\u0000')
                sb.append("_ ");
            else
                sb.append(progress + " ");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("|");

        if (guessedChars.length == 0) {
            sb.append("_");
        } else {
            for (char guessedChar : guessedChars) {
                sb.append(guessedChar);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append("|");


        sb.append(remainingAttempts);
        sb.append("|");

        sb.append(score);
        sb.append("|");

        if (gameState != GameState.GAME_ONGOING)
            sb.append(secretWord);
        return sb.toString();
    }
}