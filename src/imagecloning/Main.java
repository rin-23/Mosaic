package imagecloning;

import MenuToolbar.ToolBox;
import constants.Constants;
import javax.swing.JFrame;


public class Main {
    /**
     * @param args the command line arguments
     */
    public static JFrame mainFrame;
    public static void main(String[] args) {
        
        // Set up circleMap
        Constants.circleMap();
        
        // Set up window to display
        mainFrame = new MainFrame();
        mainFrame.setVisible(true);
	ToolBox tbox = ToolBox.getInstance();
        tbox.show();
    }
}
