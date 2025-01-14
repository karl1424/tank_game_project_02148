package dk.dtu;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class MouseHandler implements EventHandler<MouseEvent> {
    public boolean mouseReleased = false;
    public boolean mouseConsumed = false;
    public double mouseX = 0;
    public double mouseY = 0;

    @Override
    public void handle(MouseEvent event) {
        boolean released = event.getEventType() == MouseEvent.MOUSE_RELEASED;
        mouseX = event.getX();
        mouseY = event.getY();

        if (released) {
            mouseReleased = true;
            mouseConsumed = false;
        }
    }

    public boolean isClicked() {
        if (mouseReleased && !mouseConsumed) {
            mouseConsumed = true;
            return true;
        }
        return false;
    }
}
