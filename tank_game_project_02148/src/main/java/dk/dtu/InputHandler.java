package dk.dtu;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class InputHandler implements EventHandler <KeyEvent>{
    public boolean upPressed, downPressed, leftPressed, rightPressed = false;

    @Override
    public void handle(KeyEvent event) {
        boolean pressed = event.getEventType() == KeyEvent.KEY_PRESSED;
        KeyCode keyCode = event.getCode();
        switch (keyCode) {
            case W:
                upPressed = pressed;
                break;
            case S:
                downPressed = pressed;
                break;
            case A:
                leftPressed = pressed;
                break;
            case D:
                rightPressed = pressed;
                break;
            default:
                break;
        }
    }


}
