package MenuToolbar;

import imagecloning.Main;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import constants.Constants;
import imagecloning.ClonePanel;
import imagecloning.DrawPanel;
import imagecloning.DropPanel;
import imagecloning.GlassPanel;
import imagecloning.MainFrame;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import operations.Utilities;

public class ToolBox extends MouseAdapter {
    private static final ToolBox instance = new ToolBox();
    ActionPanel[] aps;
    JPanel[] panels;
    public static JFrame f;
    public static JFrame colorFrame = null;
	
    private ToolBox() {
        assembleActionPanels();
        assemblePanels();
        f = new JFrame();
        f.setBackground(Color.white);
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        JPanel p = getComponent();
        p.setBackground(Color.white);
        f.getContentPane().add(p);
        f.setLocation(200,100);
        
        for (int i = 0; i < aps.length; i++) {
            aps[i].setVisible(true);
            panels[i].setVisible(true);
            aps[i].getParent().validate();
        }

        f.pack(); 
        assignActionsToComponenets();
    }
	
    public static ToolBox getInstance(){
        return instance;
    }
	
    public void show(){
        f.setVisible(true);
    }

    public void hide() {
        f.setVisible(false);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        ActionPanel ap = (ActionPanel)e.getSource();
        if(ap.target.contains(e.getPoint()))
        {
            ap.toggleSelection();
            togglePanelVisibility(ap);
        }
    }
  
    private void togglePanelVisibility(ActionPanel ap) {
        int index = getPanelIndex(ap);
        if(panels[index].isShowing()) panels[index].setVisible(false);
            else panels[index].setVisible(true);
        ap.getParent().validate();
        f.pack();
    }
 
    private int getPanelIndex(ActionPanel ap) {
        for(int j = 0; j < aps.length; j++)
            if(ap == aps[j])
                return j;
        return -1;
    }
 
    private void assembleActionPanels() {
        String[] ids = { "Sketch & Drag", "Creative Clone", "Color", "Set Backgound Image", "Clone Mode"};
        aps = new ActionPanel[ids.length];
        for(int j = 0; j < aps.length; j++)
            aps[j] = new ActionPanel(ids[j], this);
    }
 
    private void assemblePanels() {
        JPanel p1 = firstPanel();		
        JPanel p2 = secondPanel();
        JPanel p3 = thirdPanel();
        JPanel p4 = fourthPanel();
        JPanel p5 = fifthPanel();
		       
        panels = new JPanel[] { p1, p2, p3, p4, p5};
    }
	
    private JPanel firstPanel() {

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel p1 = new JPanel(new GridBagLayout());
        p1.setBackground(Color.white);

        gbc.gridwidth = 7;
        gbc.gridy = 0;
        gbc.gridx = 0;

        Component topSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p1.add(topSeparator, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        
        Component vSeparator1 = Box.createRigidArea(new Dimension(5, 0)); 
        p1.add(vSeparator1, gbc);

        gbc.gridy = 1;
        gbc.gridx = 1;

        String[] imageFiles =
            { "images/sketch.png",  "images/selection.png", "images/eraser.png"};
        String[] imageNames =
            { "SKETCH", "BRUSH", "ERASER"};
        String[] toolbarLabels = 
            { "Sketch",  "Brush", "Eraser"};

        for (int i = 0; i < toolbarLabels.length; i++) {

            ToolBarButton button =  new ToolBarButton(imageFiles[i]);
            button.setName(imageNames[i]);
            Dimension dim = new Dimension(40, 40);
            button.setSize(dim);
            button.setMaximumSize(dim); 
            button.setMinimumSize(dim);

            button.setAlignmentY(Component.LEFT_ALIGNMENT);
            button.setToolTipText(toolbarLabels[i]);
            button.setAlignmentY(Component.CENTER_ALIGNMENT); 
            
            button.setFocusable(true);
               
            p1.add(button, gbc);
            
            gbc.gridx++;
			
            Component vSeparator2 = Box.createRigidArea(new Dimension(5, 0)); 
            p1.add(vSeparator2, gbc);
            
            gbc.gridx++;

        }
	
        gbc.gridwidth = 7;
        gbc.gridy ++;
	gbc.gridx = 0;
			
        Component middleSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p1.add(middleSeparator, gbc);
        
	gbc.gridwidth = 7;
        gbc.gridy ++;
	gbc.gridx = 0;
			
        // Slider for smoothness
        JSlider smoothSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 20);
//        smoothSlider.setMajorTickSpacing(20);
//        smoothSlider.setMinorTickSpacing(10);
//        smoothSlider.setPaintTicks(true);
//        smoothSlider.setPaintLabels(true);
        smoothSlider.setAlignmentY(Component.CENTER_ALIGNMENT);
        smoothSlider.setBackground(Color.white);
        smoothSlider.setBorder(BorderFactory.createTitledBorder("Stroke Smoothness"));
        smoothSlider.setName("SMOOTHNESS");
        smoothSlider.setFocusable(true);
	p1.add(smoothSlider, gbc);
        
         Component middleSeparator2 = Box.createRigidArea(new Dimension(0, 5)); 
        p1.add(middleSeparator2, gbc);
	
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 7;

        JSlider thresholdSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 2);
        thresholdSlider.setMajorTickSpacing(1);
        thresholdSlider.setMinorTickSpacing(1);
//        thresholdSlider.setPaintTicks(true);
//        thresholdSlider.setPaintLabels(true);
        thresholdSlider.setAlignmentY(Component.CENTER_ALIGNMENT);
        thresholdSlider.setBackground(Color.white);
        thresholdSlider.setBorder(BorderFactory.createTitledBorder("Gap between Strokes"));
        thresholdSlider.setName("THRESHOLD");
        thresholdSlider.setFocusable(true);
        p1.add(thresholdSlider, gbc);
        
        gbc.gridwidth = 7;
        gbc.gridy ++;
	gbc.gridx = 0;
			
        Component bottomSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p1.add(bottomSeparator, gbc);
	
	return p1;
    }
	
    private JPanel secondPanel() {
	
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel p2 = new JPanel(new GridBagLayout());
        p2.setBackground(Color.white);

        gbc.gridwidth = 6;
        gbc.gridy = 0;
		gbc.gridx = 0;
			
        Component topSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p2.add(topSeparator, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
		gbc.gridx = 0;
        
        Component vSeparator1 = Box.createRigidArea(new Dimension(5, 0)); 
        p2.add(vSeparator1, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
		gbc.gridx = 1;
      
     	ToolBarButton photoshopClone =  new ToolBarButton("images/photoshop.png");
        photoshopClone.setName("PHOTOSHOP");
        Dimension dim = new Dimension(40, 40);
        photoshopClone.setSize(dim);
        photoshopClone.setMaximumSize(dim); 
        photoshopClone.setMinimumSize(dim);
        photoshopClone.setAlignmentY(Component.LEFT_ALIGNMENT);
        photoshopClone.setToolTipText("Cloning");
        photoshopClone.setAlignmentY(Component.CENTER_ALIGNMENT); 
        photoshopClone.setFocusable(true);
        p2.add(photoshopClone, gbc);

        gbc.gridx++;

        Component vSeparator2 = Box.createRigidArea(new Dimension(5, 0)); 
        p2.add(vSeparator2, gbc);

        gbc.gridx++;

        ToolBarButton guidedStroke =  new ToolBarButton("images/guide.png");
        guidedStroke.setName("GUIDEDSTROKE");
        guidedStroke.setSize(dim);
        guidedStroke.setMaximumSize(dim); 
        guidedStroke.setMinimumSize(dim);
        guidedStroke.setAlignmentY(Component.LEFT_ALIGNMENT);
        guidedStroke.setToolTipText("Guided stroke");
        guidedStroke.setAlignmentY(Component.CENTER_ALIGNMENT); 
        guidedStroke.setFocusable(true);
        p2.add(guidedStroke, gbc);

        gbc.gridwidth = 6;
        gbc.gridy ++;
	gbc.gridx = 0;
			
        Component middleSeparator1 = Box.createRigidArea(new Dimension(0, 5)); 
        p2.add(middleSeparator1, gbc);
        	
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 6;

        JSlider frequencySlider = new JSlider(JSlider.HORIZONTAL, 10, 100, 50);
//        frequencySlider.setMajorTickSpacing(10);
//        frequencySlider.setMinorTickSpacing(1);
//        frequencySlider.setPaintTicks(true);
        frequencySlider.setPaintLabels(true);
        frequencySlider.setAlignmentY(Component.CENTER_ALIGNMENT);
        frequencySlider.setBackground(Color.white);
        frequencySlider.setBorder(BorderFactory.createTitledBorder("Stroke density"));
        frequencySlider.setName("FREQUENCY");
        frequencySlider.setFocusable(true);
        
         //Create the label table
        Hashtable labelTable = new Hashtable();
        labelTable.put(new Integer(100), new JLabel("Sparse"));
        labelTable.put(new Integer(10), new JLabel("Dense"));
        frequencySlider.setLabelTable(labelTable); 
        
        p2.add(frequencySlider, gbc);
		
	gbc.gridwidth = 6;
        gbc.gridy ++;
	gbc.gridx = 0;
			
        Component bottomSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p2.add(bottomSeparator, gbc);	
		
	return p2;
    }
		
    private JPanel thirdPanel() {
		
	GridBagConstraints gbc = new GridBagConstraints();
        JPanel p3 = new JPanel(new GridBagLayout());
        p3.setBackground(Color.white);

        gbc.gridwidth = 6;
        gbc.gridy = 0;
	gbc.gridx = 0;
			
        Component topSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p3.add(topSeparator, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
	gbc.gridx = 0;
			
        Component vSeparator = Box.createRigidArea(new Dimension(5, 0)); 
        p3.add(vSeparator, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
	gbc.gridx = 1;
		
        ToolBarButton photoshopClone =  new ToolBarButton("images/color_chooser.png");
        photoshopClone.setName("COLORCHOOSER");
        Dimension dim = new Dimension(40, 40);
        photoshopClone.setSize(dim);
        photoshopClone.setMaximumSize(dim); 
        photoshopClone.setMinimumSize(dim);
        photoshopClone.setAlignmentY(Component.LEFT_ALIGNMENT);
        photoshopClone.setToolTipText("Color Chooser");
        photoshopClone.setAlignmentY(Component.CENTER_ALIGNMENT); 
        photoshopClone.setFocusable(true);
        p3.add(photoshopClone, gbc);
        
        gbc.gridwidth = 6;
        gbc.gridy ++;
	gbc.gridx = 0;
			
        Component middleSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p3.add(middleSeparator, gbc);
				
	gbc.gridwidth = 6;
        gbc.gridy ++;
	gbc.gridx = 0;

        

        JSlider colorSlider = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
        colorSlider.setMajorTickSpacing(100);
//        colorSlider.setMinorTickSpacing(1);
        colorSlider.setPaintTicks(true);
        colorSlider.setPaintLabels(true);
        colorSlider.setAlignmentY(Component.CENTER_ALIGNMENT);
        colorSlider.setBackground(Color.white);
        colorSlider.setBorder(BorderFactory.createTitledBorder("Color Gradient"));
        colorSlider.setName("COLORGRADIENT");
        colorSlider.setFocusable(true);
        
        //Create the label table
        Hashtable labelTable = new Hashtable();
        labelTable.put(new Integer(-100), new JLabel("Dark"));
        labelTable.put(new Integer(0), new JLabel("0"));
        labelTable.put(new Integer(100), new JLabel("Bright"));
        colorSlider.setLabelTable(labelTable);        
        
        p3.add(colorSlider, gbc);
        
        gbc.gridwidth = 6;
        gbc.gridy ++;
	gbc.gridx = 0;
			
        Component bottomSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p3.add(bottomSeparator, gbc);

        return p3;
	}
	
    private JPanel fourthPanel(){
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel p4 = new JPanel(new GridBagLayout());
        p4.setBackground(Color.white);

        gbc.gridwidth = 6;
        gbc.gridy = 0;
	gbc.gridx = 0;
			
        Component topSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p4.add(topSeparator, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
	gbc.gridx = 0;
			
        Component vSeparator = Box.createRigidArea(new Dimension(5, 0)); 
        p4.add(vSeparator, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 1;	
		
        ToolBarButton photoshopClone =  new ToolBarButton("images/background.gif");
        photoshopClone.setName("BACKGROUND");
        Dimension dim = new Dimension(40, 40);
        photoshopClone.setSize(dim);
        photoshopClone.setMaximumSize(dim); 
        photoshopClone.setMinimumSize(dim);
        photoshopClone.setAlignmentY(Component.LEFT_ALIGNMENT);
        photoshopClone.setToolTipText("Set Background");
        photoshopClone.setAlignmentY(Component.CENTER_ALIGNMENT); 
        //photoshopClone.setFocusable(true);
        p4.add(photoshopClone, gbc);
        
        gbc.gridwidth = 6;
        gbc.gridy ++;
	gbc.gridx = 0;
			
        Component middleSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p4.add(middleSeparator, gbc);
				
        gbc.gridwidth = 6;
        gbc.gridy ++;
        gbc.gridx = 0;
 			
         // Slider for threshold
        JSlider alphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
        alphaSlider.setMajorTickSpacing(2);
        alphaSlider.setMinorTickSpacing(1);
//        alphaSlider.setPaintTicks(true);
//        alphaSlider.setPaintLabels(true);
        alphaSlider.setAlignmentY(Component.CENTER_ALIGNMENT);
        alphaSlider.setBackground(Color.white);
        alphaSlider.setBorder(BorderFactory.createTitledBorder("Image Transparency"));
        alphaSlider.setName("ALPHA");
        alphaSlider.setFocusable(true);

        p4.add(alphaSlider, gbc);	
		
        gbc.gridwidth = 6;
        gbc.gridy ++;
	gbc.gridx = 0;
			
        Component bottomSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p4.add(bottomSeparator, gbc);
        
	return p4;
	}
      
     private JPanel fifthPanel() {
	JPanel p5 = new JPanel();
        p5.setLayout(new BoxLayout(p5, BoxLayout.Y_AXIS));
        p5.setBackground(Color.white);
        
        Component topSeparator = Box.createRigidArea(new Dimension(0, 5));
        p5.add(topSeparator);
        
        JRadioButton ftfButton = new JRadioButton("Flexible to Flexible");
        ftfButton.setSelected(true);
        ftfButton.setBackground(Color.white);
        p5.add(ftfButton);
        
        JRadioButton ftsButton = new JRadioButton("Flexible to Solid");
        ftsButton.setBackground(Color.white);
        p5.add(ftsButton);
        
        JRadioButton stsButton = new JRadioButton("Solid to Solid");
        stsButton.setBackground(Color.white);
        p5.add(stsButton);
        
        Component bottomSeparator = Box.createRigidArea(new Dimension(0, 5)); 
        p5.add(bottomSeparator);
        
        ButtonGroup group = new ButtonGroup();
        group.add(ftfButton);
        group.add(ftsButton);
        group.add(stsButton);
        
        ftfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlassPanel.state = Constants.FLEXIBLE_TO_FLEXIBLE;
                ClonePanel.state = Constants.FLEXIBLE_TO_FLEXIBLE;
            }
        });
        
        ftsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlassPanel.state = Constants.FLEXIBLE_TO_SOLID;
                ClonePanel.state = Constants.FLEXIBLE_TO_SOLID;
            }
        });
        
        stsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlassPanel.state = Constants.SOLID_TO_SOLID;
                ClonePanel.state = Constants.SOLID_TO_SOLID;
            }
        });

        return p5;
    }

	private void assignActionsToComponenets() {
		
		ArrayList<Component> toolbarItems = new ArrayList<Component>();
		for (JPanel p:panels) {
			toolbarItems.addAll(Arrays.asList(p.getComponents()));
		}
		
        for (Component c:toolbarItems){
            
            String itemName = c.getName();
			if (itemName !=null) {
				if (itemName.equals("SKETCH")){
					((JButton) c).addActionListener(
						new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent ae) {
							try {
								Main.mainFrame.setCursor(Utilities.getCursor("pencil"));
                                                                f.setCursor(Utilities.getCursor("pencil"));
							} catch (IOException ex) {}
								DrawPanel.operation = Constants.DRAW;
								ClonePanel.operation = Constants.DRAW;
                                                                DropPanel.operation = Constants.NONE;
                                                                MainFrame.glassPanel.setVisible(false);
								GlassPanel.operation = Constants.NONE;
							}	
						}
					);
				} else if (itemName.equals("BRUSH")) {
					((JButton) c).addActionListener(
						new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent ae) {
                                Main.mainFrame.setCursor(Cursor.CROSSHAIR_CURSOR);
                                f.setCursor(Cursor.CROSSHAIR_CURSOR);
								DrawPanel.operation = Constants.SELECT;
								DropPanel.operation = Constants.NONE;
								MainFrame.dropPanel.clearSquare();
								MainFrame.dropPanel.repaint();
							}
						}
					);
				} else if (itemName.equals("CLONE")) {
					((JButton) c).addActionListener(
						new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent ae) {
								try {
									Main.mainFrame.setCursor(Utilities.getCursor("drag"));
                                    f.setCursor(Utilities.getCursor("drag"));
								} catch (IOException ex) {}
								if (DrawPanel.selectedStrokes.size() > 0) {
									GlassPanel.operation = Constants.DRAG;
                                                                        DropPanel.operation = Constants.NONE;
									MainFrame.glassPanel.setVisible(true);
								}
							}
						}
					);
				} else if (itemName.equals("BRUSHING_RECTANGLE")) {
					((JButton) c).addActionListener(
						new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent ae) {
								DrawPanel.operation = Constants.BRUSH_RECTANGLE;
                                                                
								MainFrame.dropPanel.clearSquare();
								MainFrame.dropPanel.repaint();
							}
						}
					);
				} else if (itemName.equals("THRESHOLD")) {
					((JSlider) c).addChangeListener(
						new ChangeListener(){
							@Override
							public void stateChanged(ChangeEvent e) {
								JSlider source = (JSlider)e.getSource();
								if (!source.getValueIsAdjusting()) {
									int newThreshold = (int)source.getValue();
									Constants.THRESHOLD = newThreshold;
									Constants.circleMap();
								}
							}
						}
					);
				} else if (itemName.equals("ALPHA")) {
					((JSlider) c).addChangeListener(
						new ChangeListener(){
							@Override
							public void stateChanged(ChangeEvent e) {
								JSlider source = (JSlider)e.getSource();
								int newthreshold = (int)source.getValue();
								MainFrame.clonePanel.imageAlpha = (float)newthreshold / 10;
								MainFrame.clonePanel.repaint();
							}
						}
					);
				} else if (itemName.equals("FREQUENCY")) {
					((JSlider) c).addChangeListener(
						new ChangeListener(){
							@Override
							public void stateChanged(ChangeEvent e) {
								JSlider source = (JSlider)e.getSource();
								if (!source.getValueIsAdjusting()) {
									int number = (int)source.getValue();
									ClonePanel.guidedOverlapFactor = number;
									if (ClonePanel.hiddenGuideStroke != null
											&& ClonePanel.selectedStrokesClone != null) {
										MainFrame.clonePanel.again_guide();
									}
								}
							}
						}
					);
				} else if (itemName.equals("SMOOTHNESS")) {
					((JSlider) c).addChangeListener(
						new ChangeListener(){
							@Override
							public void stateChanged(ChangeEvent e) {
								JSlider source = (JSlider)e.getSource();
								if (!source.getValueIsAdjusting()) {
									int smoothness = (int) source.getValue();
									Constants.SMOOTHNESS = smoothness / 100f;
								}
							}
						}
					);
				} else if (itemName.equals("COLORGRADIENT")) {
					((JSlider) c).addChangeListener(
						new ChangeListener(){
							@Override
							public void stateChanged(ChangeEvent e) {
								JSlider source = (JSlider)e.getSource();
								if (!source.getValueIsAdjusting()) {
									int gradient = (int) source.getValue();
									Constants.COLOR_GRADIENT = gradient;
								}
							}
						}
					);
				} else if (itemName.equals("ERASER")) {
					((JButton) c).addActionListener(
						new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent ae) {
							 	try {
									Main.mainFrame.setCursor(Utilities.getCursor("eraser"));
                                                                        f.setCursor(Utilities.getCursor("eraser"));
								} catch (IOException ex) {}
								DrawPanel.operation = Constants.ERASE;
								DropPanel.operation = Constants.ERASE;
								ClonePanel.operation = Constants.ERASE;
                                                                MainFrame.glassPanel.setVisible(false);
								GlassPanel.operation = Constants.NONE;
							}
						}
					);
				} else if (itemName.equals("GUIDEDSTROKE")) {
					((JButton) c).addActionListener(
						new ActionListener(){
								@Override
								public void actionPerformed(ActionEvent ae) {
                                                                    try {
									Main.mainFrame.setCursor(Utilities.getCursor("pen"));
                                                                        f.setCursor(Utilities.getCursor("pen"));
								} catch (IOException ex) {}
									ClonePanel.operation = Constants.DRAW_GUIDE;
                                                                        DropPanel.operation = Constants.SELECT;
                                                                        
									MainFrame.glassPanel.setVisible(false);
									GlassPanel.operation = Constants.NONE;
								}
							}
						);
				} else if (itemName.equals("PHOTOSHOP")) {
					((JButton) c).addActionListener(
							new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent ae) {
								Main.mainFrame.setCursor(Cursor.DEFAULT_CURSOR);
								f.setCursor(Cursor.DEFAULT_CURSOR);
								DrawPanel.operation = Constants.PHOTOSHOP_MODE;
                                                                DropPanel.operation = Constants.NONE;
								ClonePanel.operation = Constants.PHOTOSHOP_MODE;
								MainFrame.glassPanel.setVisible(false);
								GlassPanel.operation = Constants.NONE;
								
							}
							
						});
				}  else if (itemName.equals("COLORCHOOSER")) {
					((JButton) c).addActionListener(
						new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent ae) {
									
									try {
										Main.mainFrame.setCursor(Utilities.getCursor("paint"));
                                                                                f.setCursor(Utilities.getCursor("paint"));
									} catch (IOException ex) {}
									DrawPanel.operation = Constants.COLOR;
									DropPanel.operation = Constants.COLOR;
									ClonePanel.operation = Constants.COLOR;
                                                                        MainFrame.glassPanel.setVisible(false);
                                                                        GlassPanel.operation = Constants.NONE;
									if (colorFrame == null) {
										colorFrame = new JFrame("Color Chooser");
										colorFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
										JComponent colorChooser = ColorChooser.getInstance();
										// colorChooser must be opaque
										colorChooser.setOpaque(true);
										colorFrame.setContentPane(colorChooser);
										colorFrame.pack();
										colorFrame.setVisible(true);
									} else {
										colorFrame.setVisible(true);
									}
										}
									}
						);
				} else if (itemName.equals("BACKGROUND")) {
					((JButton) c).addActionListener(
						new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent ae) {
								JFileChooser fileChooser = new JFileChooser();
								int returnValue = fileChooser.showOpenDialog(null);
								if (returnValue == JFileChooser.APPROVE_OPTION) {
									File selectedFile = fileChooser.getSelectedFile();
									String imagePath = selectedFile.getAbsolutePath();
									try {
										MainFrame.clonePanel.image = ImageIO.read(new File(imagePath));
										MainFrame.clonePanel.repaint();
									} catch (IOException ex) {
										Logger.getLogger(MainFrame.class.getName())
												.log(Level.SEVERE, null, ex);
									}
								}
							}
						}
					);
				}
			}
		}		
	}
	
					
    private JPanel getComponent() 
	{
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1,3,0,3);
        gbc.weightx = 1.0;
        gbc.fill = gbc.HORIZONTAL;
        gbc.gridwidth = gbc.REMAINDER;
        for(int j = 0; j < aps.length; j++)
        {
            panel.add(aps[j], gbc);
            panel.add(panels[j], gbc);
            panels[j].setVisible(false);
        }
        JLabel padding = new JLabel();
        gbc.weighty = 1.0;
        panel.add(padding, gbc);
        return panel;
    }

   
	

}
class ActionPanel extends JPanel {
    String text;
    Font font;
    private boolean selected;
    BufferedImage open, closed;
    Rectangle target;
    final int
        OFFSET = 30,
        PAD    =  5;
 
    public ActionPanel(String text, MouseListener ml)
    {
        this.text = text;
        addMouseListener(ml);
        font = new Font("sans-serif", Font.PLAIN, 12);
        selected = false;
        setBackground(new Color(202, 214, 234));
        setPreferredSize(new Dimension(200,20));
        setBorder(BorderFactory.createRaisedBevelBorder());
        setPreferredSize(new Dimension(200,20));
        createImages();
        setRequestFocusEnabled(true);
    }
  
    public void toggleSelection()
    {
        selected = !selected;
        repaint();
    }
 
	@Override 
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        if (selected) g2.drawImage(open, PAD, 0, this);
		else g2.drawImage(closed, PAD, 0, this);
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics(text, frc);
        float height = lm.getAscent() + lm.getDescent();
        float x = OFFSET;
        float y = (h + height)/2 - lm.getDescent();
        g2.drawString(text, x, y);
    }
 
    private void createImages() 
    {
        int w = 20;
        int h = getPreferredSize().height;
        target = new Rectangle(2, 0, 20, 18);
        open = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = open.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getBackground());
        g2.fillRect(0,0,w,h);    
        int[] x = { 2, w/2, 18 };
        int[] y = { 4, 15,   4 };
        Polygon p = new Polygon(x, y, 3);
        g2.setPaint(Color.black);
        g2.fill(p);
        g2.setPaint(Color.black);
        g2.draw(p);
        g2.dispose();
        closed = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        g2 = closed.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getBackground());
        g2.fillRect(0,0,w,h);
        x = new int[] { 3, 13,   3 };
        y = new int[] { 4, h/2, 16 };
        p = new Polygon(x, y, 3);
        g2.setPaint(Color.black);
        g2.fill(p);
        g2.setPaint(Color.black);
        g2.draw(p);
        g2.dispose();
    }
}
