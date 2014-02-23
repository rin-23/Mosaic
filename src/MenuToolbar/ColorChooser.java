package MenuToolbar;

import constants.Constants;
import imagecloning.ClonePanel;
import imagecloning.DrawPanel;
import imagecloning.DropPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import operations.Utilities;

public class ColorChooser extends JPanel {
    
    private static final ColorChooser instance = new ColorChooser();
    protected JColorChooser tcc;
    private static Color currentColor;
    
    private ChangeListener changeListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            DrawPanel.operation = Constants.COLOR;
            DropPanel.operation = Constants.COLOR;
            ClonePanel.operation = Constants.COLOR;
            currentColor = tcc.getColor();
            try {
                setCursor(Utilities.getCursor("paint"));
            } catch (IOException ex) {}
        }
    };

    private ColorChooser() {
        super(new BorderLayout());

        // Set up color chooser for setting text color
        tcc = new JColorChooser(Color.red);
        tcc.getSelectionModel().addChangeListener(changeListener);
        // Remove Preveiw Panel from Color Chooser
        tcc.setPreviewPanel(new JPanel());
        add(tcc, BorderLayout.PAGE_END);
    }

    public static ColorChooser getInstance(){
        return instance;
    }

    public Color getCurrentColor() {
        return currentColor;				
    }

}
