package operations;

import imagecloning.MainFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/* Customized KeyListener class */
public class KeyDispatcher implements KeyListener{

    private boolean ctrl = false;
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrl = true;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            if (ctrl == true) {
                if (MainFrame.file == null & MainFrame.type == null) {
                    Utilities.saveAs();	
                } else {
                    Utilities.save(MainFrame.file, MainFrame.type);
                }
            }
            ctrl = false;
        } else if (e.getKeyCode() == KeyEvent.VK_O) {
            Utilities.open();
            ctrl = false;
        } else {
            ctrl = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

}