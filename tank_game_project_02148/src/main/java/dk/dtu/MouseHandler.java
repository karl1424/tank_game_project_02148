package dk.dtu;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class MouseHandler implements EventHandler<MouseEvent> {
    private boolean mouseClicked = false;
    private double mouseX = 0;
    private double mouseY = 0;

    @Override
    public void handle(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
        if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            mouseClicked = true;
        }
    }

    public boolean wasMouseClicked() {
        if (mouseClicked) {
            mouseClicked = false;
            return true;
        }
        return false;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }
}
