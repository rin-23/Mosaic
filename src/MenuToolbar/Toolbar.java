package MenuToolbar;
import java.awt.*;
import javax.swing.*;
import operations.KeyDispatcher;

public class Toolbar extends JPanel {
    
    public Toolbar() {
	
        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        KeyDispatcher keyDispatcher = new KeyDispatcher();

        String[] imageFiles =
        { "sketch.png",  "brush.png", "clone.png", "brushingrect.png", "color_chooser.png" };
        String[] imageNames =
        { "SKETCH", "BRUSH", "CLONE", "BRUSHING_RECTANGLE", "COLOR_CHOOSER" };
        String[] toolbarLabels =
        { "Sketch",  "Brush", "Clone", "Brushing Rectangle", "Color Choooser"};
    
        for (int i = 0; i < toolbarLabels.length; i++) {
            ToolBarButton button =  new ToolBarButton(imageFiles[i]);
            button.setName(imageNames[i]);
            Dimension dim = new Dimension(40, 40);
            button.setSize(dim);
            button.setMaximumSize(dim);
            button.setMinimumSize(dim);

            button.setAlignmentY(Component.LEFT_ALIGNMENT);
            button.setToolTipText(toolbarLabels[i]);
            button.setAlignmentY(CENTER_ALIGNMENT);
            
            button.setFocusable(true);
            button.addKeyListener(keyDispatcher);
            
            this.add(button);
            
            Component smallSeparator = Box.createRigidArea(new Dimension(10, 0));
            smallSeparator.setName("SEPARATOR");
            this.add(smallSeparator);
        }
	
        // Slider for threshold
	JSlider thresholdSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
	thresholdSlider.setMajorTickSpacing(1);
	thresholdSlider.setMinorTickSpacing(1);
	thresholdSlider.setPaintTicks(true);
	thresholdSlider.setPaintLabels(true);
        thresholdSlider.setAlignmentY(CENTER_ALIGNMENT);
        thresholdSlider.setBackground(Color.white);
        thresholdSlider.setBorder(BorderFactory.createTitledBorder("Set Threshold"));
        thresholdSlider.setName("THRESHOLD");
        thresholdSlider.setFocusable(true);
        thresholdSlider.addKeyListener(keyDispatcher);
	this.add(thresholdSlider);
        Component firstSeparator = Box.createRigidArea(new Dimension(10, 0));
        firstSeparator.setName("SEPARATOR");
        this.add(firstSeparator);
        
        // Slider for threshold
	JSlider alphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
	alphaSlider.setMajorTickSpacing(2);
	alphaSlider.setMinorTickSpacing(1);
	alphaSlider.setPaintTicks(true);
	alphaSlider.setPaintLabels(true);
        alphaSlider.setAlignmentY(CENTER_ALIGNMENT);
        alphaSlider.setBackground(Color.white);
        alphaSlider.setBorder(BorderFactory.createTitledBorder("Set Alpha"));
        alphaSlider.setName("ALPHA");
        alphaSlider.setFocusable(true);
        alphaSlider.addKeyListener(keyDispatcher);
	this.add(alphaSlider);
        Component secondSeparator = Box.createRigidArea(new Dimension(10, 0));
        secondSeparator.setName("SEPARATOR");
        this.add(secondSeparator);
        
        // Slider for guided stroke frequency
        JSlider frequencySlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 5);
	frequencySlider.setMajorTickSpacing(3);
	frequencySlider.setMinorTickSpacing(1);
	frequencySlider.setPaintTicks(true);
	frequencySlider.setPaintLabels(true);
        frequencySlider.setAlignmentY(CENTER_ALIGNMENT);
        frequencySlider.setBackground(Color.white);
        frequencySlider.setBorder(BorderFactory.createTitledBorder("Set Frequency"));
        frequencySlider.setName("FREQUENCY");
        frequencySlider.setFocusable(true);
        frequencySlider.addKeyListener(keyDispatcher);
	this.add(frequencySlider);
        Component thirdSeparator = Box.createRigidArea(new Dimension(10, 0));
        thirdSeparator.setName("SEPARATOR");
        this.add(thirdSeparator);
        
        // Slider for smoothness
	JSlider smoothSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 20);
	smoothSlider.setMajorTickSpacing(20);
	smoothSlider.setMinorTickSpacing(10);
	smoothSlider.setPaintTicks(true);
	smoothSlider.setPaintLabels(true);
        smoothSlider.setAlignmentY(CENTER_ALIGNMENT);
        smoothSlider.setBackground(Color.white);
        smoothSlider.setBorder(BorderFactory.createTitledBorder("Set Smoothness"));
        smoothSlider.setName("SMOOTHNESS");
        smoothSlider.setFocusable(true);
        smoothSlider.addKeyListener(keyDispatcher);
	this.add(smoothSlider);
        Component fourthSeparator = Box.createRigidArea(new Dimension(10, 0));
        fourthSeparator.setName("SEPARATOR");
        this.add(fourthSeparator);
        
        // Slider for smoothness
	JSlider colorSlider = new JSlider(JSlider.HORIZONTAL, -10, 10, 0);
	colorSlider.setMajorTickSpacing(4);
	colorSlider.setMinorTickSpacing(1);
	colorSlider.setPaintTicks(true);
	colorSlider.setPaintLabels(true);
        colorSlider.setAlignmentY(CENTER_ALIGNMENT);
        colorSlider.setBackground(Color.white);
        colorSlider.setBorder(BorderFactory.createTitledBorder("Set Color Gradient"));
        colorSlider.setName("COLOR");
        colorSlider.setFocusable(true);
        colorSlider.addKeyListener(keyDispatcher);
	this.add(colorSlider);
        Component bigSeparator = Box.createRigidArea(new Dimension(10, 0));
        bigSeparator.setName("SEPARATOR");
        this.add(bigSeparator);
  }

}
