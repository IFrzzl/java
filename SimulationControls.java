import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

public class SimulationControls extends JPanel {
    JTextArea generation = new JTextArea();
    Plane plane;
    public SimulationControls(){setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));}

    public void populate(Plane plane){
        removeAll();
        setBackground(Color.LIGHT_GRAY);
        add(new JLabel("Simulation Controls"));

        JPanel mainInfo = new JPanel(new GridLayout(4, 1));
        generation = new JTextArea("Current generation: 0 / " + parameters.NUMBER_GENERATIONS + "\nBest time score: 0\n\n\n");
        mainInfo.add(generation);
        mainInfo.add(new JLabel("Speed"));
        JSlider speed = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        //Create the label table
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put( 0, new JLabel("0.1x") );
        labelTable.put( 50, new JLabel("1x") );
        labelTable.put( 100, new JLabel("Max") );
        speed.setLabelTable( labelTable );
        speed.setPaintLabels(true);
        mainInfo.add(speed);

        JPanel buttonsPanel = new JPanel(new GridLayout());
        JButton pauseButton = new JButton("Play!");
        JButton skipButton = new JButton("Skip");

        pauseButton.addActionListener(e -> {
            if (!parameters.STARTED){parameters.STARTED = true; pauseButton.setText("Pause");}
            else if (parameters.PAUSED) {
                pauseButton.setText("Pause");
                parameters.PAUSED = false;

            } else {
                pauseButton.setText("Play!");
                parameters.PAUSED = true;
            }

        });
        skipButton.addActionListener(e -> parameters.SKIP = true);
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(skipButton);
        mainInfo.add(buttonsPanel);

        mainInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainInfo.setMaximumSize(mainInfo.getPreferredSize());
        add(mainInfo);

         //uhhh why did i make my life so difficult who cares what the plane structure is???? >:(
        // doing this manually LMAO
        int[][] blocks = new int[][]{
            {0}, {1}, {1,1}, {1,2}, {2,2}, {2,3}, {3,3}, {2,3,2}, {2,4,2}, {3,3,3},
            {3,4,3}, {4,3,4}, {4,4,4}, {4,5,4}, {3,4,4,3}
        };


        // plane settings
        JPanel planeSettings = new JPanel(new GridLayout(3,1));

        JPanel widthRow = new JPanel(new FlowLayout());
        JPanel lengthRow = new JPanel(new FlowLayout());
        JPanel bRow = new JPanel(new FlowLayout());

        widthRow.add(new JLabel("Plane width: "));
        JSlider width = new JSlider(JSlider.HORIZONTAL, 2, 14, plane.getWidth()-plane.getAisles().length);
        width.setMinorTickSpacing(4);
        width.setMajorTickSpacing(4);
        width.setPaintLabels(true);
        width.setPaintTicks(true);

        JTextArea widthReading = new JTextArea("" + width.getValue());
        width.addChangeListener(e -> {
            widthReading.setText("" + width.getValue());
            if (!width.getValueIsAdjusting()){
                if (!parameters.STARTED){
                    parameters.plane = new Plane(plane.getLength(), plane.getBusinessRows(), plane.getLength() - plane.getBusinessRows(), 
                width.getValue(), blocks[width.getValue()], plane.getExits(), "Custom aircraft");
                    parameters.REDRAW = true;
                }
            }
        });
        widthReading.setMaximumSize(widthReading.getPreferredSize());
        widthRow.add(width);
        widthRow.add(widthReading);

        lengthRow.add(new JLabel("Plane length: "));
        JSlider length = new JSlider(JSlider.HORIZONTAL, 8, 32, plane.getLength());
        length.setMajorTickSpacing(8);
        length.setMinorTickSpacing(8);
        length.setPaintLabels(true);
        length.setPaintTicks(true);

        JTextArea lengthReading = new JTextArea("" + length.getValue());
        length.addChangeListener(e -> {
            lengthReading.setText("" + length.getValue());
            if (!length.getValueIsAdjusting()){
            if (!parameters.STARTED){
                parameters.plane = new Plane(length.getValue(), plane.getBusinessRows(), plane.getLength() - plane.getBusinessRows(), 
                plane.getWidth()-plane.getAisles().length, plane.getBlocks(), new int[]{0, Math.max(0, length.getValue() - 1)}, "Custom aircraft");
                parameters.REDRAW = true;
            }
        }
        });
        lengthReading.setMaximumSize(lengthReading.getPreferredSize());
        lengthRow.add(length);
        lengthRow.add(lengthReading);

        bRow.add(new JLabel("Business rows: "));
        JTextField businessRowsField = new JTextField(3);
        businessRowsField.setText(""+ plane.getBusinessRows());
        businessRowsField.setToolTipText("Number of business rows (min: 0; max: plane length)");
        businessRowsField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { changed(); } // make the error messages go away
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { changed(); }
            private void changed() {
                String txt = businessRowsField.getText().trim();
                try {
                    int val = Integer.parseInt(txt);
                    if (!(val > 0 && val <= length.getValue())) {
                        businessRowsField.setBackground(new Color(237, 88, 71));
                        businessRowsField.setToolTipText("Number out of range: business rows must <= plane length.");
                    } else {
                        businessRowsField.setBackground(Color.WHITE);
                        businessRowsField.setToolTipText("Number of business rows (min: 0; max: plane length)");
                        if (!parameters.STARTED){
                            parameters.plane = new Plane(plane.getLength(), val, plane.getLength() - val, plane.getWidth()-plane.getAisles().length, plane.getBlocks(), plane.getExits(), plane.getType());
                            parameters.REDRAW = true;
                        }
                    }
                } catch (Exception e) {
                    businessRowsField.setBackground(new Color(237, 88, 71));
                    businessRowsField.setToolTipText("Number must not include non-numerical characters.");
                }
            }
        });
        bRow.add(businessRowsField);
        bRow.setSize(widthRow.getSize());
        
        planeSettings.add(widthRow);
        planeSettings.add(lengthRow);
        planeSettings.add(bRow);

        add(planeSettings);
        planeSettings.setMaximumSize(planeSettings.getPreferredSize());

        JPanel bottomSpacer = new JPanel(new GridLayout(2, 2));
        bottomSpacer.add(new JLabel());
        bottomSpacer.add(new JLabel());
        bottomSpacer.add(new JLabel());
        JButton startButton = new JButton("Start simulation");
        startButton.addActionListener(e -> {

            try {Integer.parseInt(businessRowsField.getText());}
            catch (Exception x) {return;}
            int b = Integer.parseInt(businessRowsField.getText());
            if (b == 0 || b > length.getValue()) {return;}

            int[] exitRows = new int[]{0, Math.max(0, length.getValue() - 1)};
            parameters.plane = new Plane(length.getValue(), b, length.getValue() - b, width.getValue(), blocks[width.getValue()],
             exitRows, "Custom Plane");
            parameters.END = true;
            parameters.STARTED = true;
            if (pauseButton.getText() == "Play!"){
                pauseButton.setText("Pause");
            }
        });
        bottomSpacer.add(startButton);
        bottomSpacer.setMaximumSize(bottomSpacer.getPreferredSize());
        add(bottomSpacer);

    }

    public void updateGeneration(int gens, int time, int stats){
        gens++;
        generation.setText("Current generation: " + gens + " / " + parameters.NUMBER_GENERATIONS + 
            "\nBest time score: " + time + "\nStatic generations: " + stats);
        if (gens == parameters.NUMBER_GENERATIONS){
            generation.setText("Simulation finished! Showing winning simulation." + "\n" + generation.getText());
        }
    }
}