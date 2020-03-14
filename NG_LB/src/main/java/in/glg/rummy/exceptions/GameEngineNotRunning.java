package in.glg.rummy.exceptions;

public class GameEngineNotRunning extends Exception {
    public GameEngineNotRunning(String message) {
        super(message);
    }

    public GameEngineNotRunning(Throwable cause) {
        super(cause);
    }
}
