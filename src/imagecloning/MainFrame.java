package imagecloning;

import MenuToolbar.ColorChooser;
import MenuToolbar.ToolBox;
import MenuToolbar.Toolbar;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import constants.Constants;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import operations.KeyDispatcher;
import operations.UndoManager;
import operations.Utilities;

public class MainFrame extends JFrame {
    
    private Toolbar toolbar;
    public static DrawPanel drawPanel;
    public static DropPanel dropPanel;
    public static ClonePanel clonePanel;
    public static GlassPanel glassPanel;
    
    public static JFrame colorFrame;
    
    public static File file = null;
    public static String type = null;
    
    public MainFrame() {
        super("Image Clone Demo");
        
        colorFrame = null;
        
        try {
            setCursor(Utilities.getCursor("pencil"));
            MenuToolbar.ToolBox.f.setCursor(Utilities.getCursor("pencil"));
        } catch (IOException ex) {}
        
        KeyDispatcher keyDispatcher = new KeyDispatcher();
        super.setFocusable(true);
        super.addKeyListener(keyDispatcher);
        super.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    if (drawPanel != null && DrawPanel.operation == Constants.COLOR) {
                        setCursor(Utilities.getCursor("paint"));
                    }
                } catch (IOException ex) {}
            }
            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }
    
    @Override
    protected void frameInit() {
        super.frameInit();
        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        super.setResizable(false);
        super.setSize(1080, 600);
        
	Container contentPane = super.getContentPane();
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints gBC = new GridBagConstraints();
        
        toolbar = new Toolbar();
        toolbar.setBackground(Color.white);
        toolbar.setSize(190, 40);
        toolbar.setBorder(BorderFactory.createLineBorder(Color.white, 6));
            
        gBC.gridx = 0;
        gBC.gridy = 0;
        gBC.gridwidth = 3;
        gBC.fill = GridBagConstraints.HORIZONTAL;
        //contentPane.add(toolbar, gBC);
        
        drawPanel = new DrawPanel();
        drawPanel.setBackground(Color.white);
        drawPanel.setSize(400, 600);
        drawPanel.setBorder(BorderFactory.createTitledBorder("Draw Panel"));
        
        gBC.gridx = 0;
        gBC.gridy = 1;
        gBC.weightx = 0.4;
        gBC.gridwidth = 1;
        gBC.fill = GridBagConstraints.BOTH;
        contentPane.add(drawPanel, gBC);
        
        dropPanel = new DropPanel();
        dropPanel.setBackground(Color.white);
        dropPanel.setSize(80, 600);
        dropPanel.setBorder(BorderFactory.createTitledBorder("Drop Panel"));
        
        gBC.gridx = 1;
        gBC.gridy = 1;
        gBC.weightx = 0.08;
        gBC.gridwidth = 1;
        gBC.fill = GridBagConstraints.BOTH;
        contentPane.add(dropPanel, gBC);
        
        clonePanel = new ClonePanel();
        clonePanel.setBackground(Color.white);
        clonePanel.setSize(600, 600);
        clonePanel.setBorder(BorderFactory.createTitledBorder("Clone Panel"));
        
        gBC.gridx = 2;
        gBC.gridy = 1;
        gBC.weightx = 0.6;
        gBC.weighty = 1.0;
        gBC.gridwidth = 1;
        gBC.fill = GridBagConstraints.BOTH;
        contentPane.add(clonePanel, gBC);
    
        glassPanel = new GlassPanel();
        super.setGlassPane(glassPanel);
        modeMenuInit();
        toolbarInit();
    }
    
    private void modeMenuInit() {
        
        JMenuBar menuBar = new JMenuBar();
		
		//menuBar.setForeground(new Color(202, 214, 234));
		//menuBar.setBackground(Color.yellow);
		menuBar.setOpaque(false);
		menuBar.setCursor(Cursor.getDefaultCursor());
		
        JMenu fileMenu = new JMenu("File");
        JMenu modeMenu = new JMenu("Mode");
        JMenu editMenu = new JMenu("Edit");
        JMenu settingsMenu = new JMenu("ToolBox");
        
        JMenuItem save = new JMenuItem("Save                          (Ctrl+S)");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem open = new JMenuItem("Open                         (Ctrl+O)");
        JMenuItem chooseBG = new JMenuItem("Select Background Image");
        
        JMenuItem photoshopMode = new JMenuItem("Photoshop Clone Mode");
        JMenuItem guideMode = new JMenuItem("Guided Line Mode");
        JMenuItem eraseMode = new JMenuItem("Erase Mode");
        
        JMenuItem solidToSolid = new JMenuItem("Solid To Solid");
        JMenuItem flexToFlex = new JMenuItem("Flexible To Flexible");
        JMenuItem flexToSolid = new JMenuItem("Flexible To Solid");
        
        JMenuItem undo = new JMenuItem("Undo");
        JMenuItem showExpansionLines = new JMenuItem("Toggle Expansion Progress");
        JMenuItem toolbox = new JMenuItem("ToolBox");
        settingsMenu.addMenuListener(
            new MenuListener() {
                @Override
                public void menuSelected(MenuEvent e) {
                    //System.out.println("Menu Selected");
					ToolBox tbox = ToolBox.getInstance();
					tbox.show();
                }

                @Override
                public void menuDeselected(MenuEvent e) {
                    System.out.println("Menu Deselected");
                }

                @Override
                public void menuCanceled(MenuEvent e) {
                    System.out.println("Menu Canceled");
                }  
            }
        );
           
	
        open.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae) {
                    Utilities.open();
                }
            }
        );
        
        save.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (file == null & type == null) {
                        Utilities.saveAs();	
                    } else { 
                       Utilities.save(file, type);
                    }
                }
            } 
       );
        
        saveAs.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae) {
                    Utilities.saveAs();			
                }
            }
        );
        
        chooseBG.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JFileChooser fileChooser = new JFileChooser();
                    int returnValue = fileChooser.showOpenDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        String imagePath = selectedFile.getAbsolutePath();
                        try {
                            clonePanel.image = ImageIO.read(new File(imagePath));
                            clonePanel.repaint();
                        } catch (IOException ex) {
                            Logger.getLogger(MainFrame.class.getName())
                                    .log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        );
        	
	photoshopMode.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae) {
                    DrawPanel.operation = Constants.PHOTOSHOP_MODE;
                    ClonePanel.operation = Constants.PHOTOSHOP_MODE;
                }
            }
        );
        
        guideMode.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ClonePanel.operation = Constants.DRAW_GUIDE;
                }
            }
        );
        
        eraseMode.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae) {
                    try {
                        setCursor(Utilities.getCursor("eraser"));
                    } catch (IOException ex) {}
                    DrawPanel.operation = Constants.ERASE;
                    DropPanel.operation = Constants.ERASE;
                    ClonePanel.operation = Constants.ERASE;
                }
            }
        );
	
			//SETTINGS
		toolbox.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae) {
					ToolBox toolbox = ToolBox.getInstance();
					toolbox.show();
			    }
            }
        );
		   
		
		
//        solidToSolid.addActionListener(
//            new ActionListener(){
//                @Override
//                public void actionPerformed(ActionEvent ae) {
//                    GlassPanel.cloningMode = Constants.SOLID_TO_SOLID;
//                }
//            }
//        );
//        
//        flexToFlex.addActionListener(
//            new ActionListener(){
//                @Override
//                public void actionPerformed(ActionEvent ae) {
//                    GlassPanel.cloningMode = Constants.FLEX_TO_FLEX;
//                }
//            }
//        );
//        
//        flexToSolid.addActionListener(
//            new ActionListener(){
//                @Override
//                public void actionPerformed(ActionEvent ae) {
//                    GlassPanel.cloningMode = Constants.FLEX_TO_SOLID;
//                }
//            }
//        );
          
	undo.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ae) {
                        UndoManager.undo();    
                        clonePanel.repaint();
                }
            }
        );
        
//	showExpansionLines.addActionListener(
//            new ActionListener(){
//                @Override
//                public void actionPerformed(ActionEvent ae) {
//                    clonePanel.showExpansionProgress = 
//                            !clonePanel.showExpansionProgress;
//                    clonePanel.repaint();
//                }
//            }
//        );
        
        fileMenu.add(save);
        fileMenu.add(saveAs);
        fileMenu.add(open);
        fileMenu.addSeparator();
        fileMenu.add(chooseBG);

       // modeMenu.add(photoshopMode);
       // modeMenu.add(guideMode);
       // modeMenu.add(eraseMode);
       // modeMenu.addSeparator();
        
        //modeMenu.add(solidToSolid);
        //modeMenu.add(flexToFlex);
        //modeMenu.add(flexToSolid);
        
        editMenu.add(undo);
       // settingsMenu.add(showExpansionLines);
		//settingsMenu.add(toolbox);
		
        menuBar.add(fileMenu);
       // menuBar.add(modeMenu);
        menuBar.add(editMenu);
        //menuBar.add(settingsMenu);
		menuBar.add(toolbox);
        super.setJMenuBar(menuBar);
		
        
    }
		
    private void toolbarInit() {
        
        Component toolbarItems[] = toolbar.getComponents();
        
        for (int i = 0; i < toolbar.getComponentCount(); i++){
            
            String itemName = toolbarItems[i].getName();

            if (itemName.equals("SKETCH")){
                ((JButton) toolbarItems[i]).addActionListener(
                    new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                        try {
                            setCursor(Utilities.getCursor("pencil"));
                        } catch (IOException ex) {}
                        DrawPanel.operation = Constants.DRAW;
                        ClonePanel.operation = Constants.DRAW;
                        }	
                    }
                );
            } 
            
            if (itemName.equals("BRUSH")) {
                ((JButton) toolbarItems[i]).addActionListener(
                    new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            try {
                                setCursor(Utilities.getCursor("select"));
                            } catch (IOException ex) {}
                            DrawPanel.operation = Constants.SELECT;
                            DropPanel.operation = Constants.SELECT;
                            MainFrame.dropPanel.clearSquare();
                            MainFrame.dropPanel.repaint();
                        }
                    }
                );
            }
            
            if (itemName.equals("CLONE")) {
                ((JButton) toolbarItems[i]).addActionListener(
                    new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            try {
                                setCursor(Utilities.getCursor("drag"));
                            } catch (IOException ex) {}
                            if (DrawPanel.selectedStrokes.size() > 0) {
                                GlassPanel.operation = Constants.DRAG;
                                glassPanel.setVisible(true);
                            }
//                            DrawPanel.setSelectedStrokes();
//                            if (DrawPanel.selectedStrokes.size() > 0) {
//                                GlassPanel.operation = Constants.DRAG;
//                                glassPanel.setVisible(true);
//                            } else {
//                                DrawPanel.operation = Constants.DRAG;
//                            }
                        }
                    }
                );
            }
            
            if (itemName.equals("BRUSHING_RECTANGLE")) {
                ((JButton) toolbarItems[i]).addActionListener(
                    new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            DrawPanel.operation = Constants.BRUSH_RECTANGLE;
                            MainFrame.dropPanel.clearSquare();
                            MainFrame.dropPanel.repaint();
                        }
                    }
                );
            }
			
            if (itemName.equals("COLOR_CHOOSER")) {
                ((JButton) toolbarItems[i]).addActionListener(
                    new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            try {
                                setCursor(Utilities.getCursor("paint"));
                            } catch (IOException ex) {}
                            DrawPanel.operation = Constants.COLOR;
                            DropPanel.operation = Constants.COLOR;
                            ClonePanel.operation = Constants.COLOR;
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
            }
            
            if (itemName.equals("THRESHOLD")) {
                ((JSlider) toolbarItems[i]).addChangeListener(
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
            }
            
            if (itemName.equals("ALPHA")) {
                ((JSlider) toolbarItems[i]).addChangeListener(
                    new ChangeListener(){
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            JSlider source = (JSlider)e.getSource();
                            int newthreshold = (int)source.getValue();
                            clonePanel.imageAlpha =
                                    (float)newthreshold / 10;
                            clonePanel.repaint();
                        }
                    }
                );
            }
            
            if (itemName.equals("FREQUENCY")) {
                ((JSlider) toolbarItems[i]).addChangeListener(
                    new ChangeListener(){
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            JSlider source = (JSlider)e.getSource();
                            if (!source.getValueIsAdjusting()) {
                                int number = (int)source.getValue();
                                ClonePanel.guidedOverlapFactor = number;
                                if (ClonePanel.hiddenGuideStroke != null
                                        && ClonePanel.selectedStrokesClone != null) {
                                    clonePanel.again_guide();
                                }
                            }
                        }
                    }
                );
            }
            
            if (itemName.equals("SMOOTHNESS")) {
                ((JSlider) toolbarItems[i]).addChangeListener(
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
            }
            
            if (itemName.equals("COLOR")) {
                ((JSlider) toolbarItems[i]).addChangeListener(
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
            }
            
        }
    }
    
}
