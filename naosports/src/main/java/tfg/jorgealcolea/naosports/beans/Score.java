package tfg.jorgealcolea.naosports.beans;

/**
 * Created by george on 12/08/16.
 */
public class Score {

    public String userName;
    public int score;

    public Score(String userName, int score) {
        this.userName = userName;
        this.score = score;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
