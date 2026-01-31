package planeSimulation;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Arrays;

// add panel showing console output?
// settings for closeness / s.d / whatevs
// number of generations
// number of simulations
// option for worst sim

// maybe move plane settings back?

public class SimulationControls extends JPanel {
    JTextArea generation = new JTextArea();
    Plane plane;
    ArrayList<Component> allComponents = new ArrayList<>();


        private boolean programmaticUpdate = false; // stops recurive listener shenanigans

        JLabel capacity;
        JLabel price;

        Slider elitism;
        Slider tournament;
        Slider selectionPool;
        Slider mutation;
        Slider newSims;

        GridBagLayout gbl = new GridBagLayout();

        private class Slider {
            JSlider slider;
            JLabel reading;
            JPanel panel;
            JLabel label;

            Slider(String l, double d, int min, int max, Boolean defaultListener, Boolean defaultLabels){
                JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, (int) d);
                JLabel reading = new JLabel();
                JLabel label = new JLabel(l + " ");
                JPanel panel = new JPanel(gbl);

                if (defaultListener){
                    slider.addChangeListener(e -> {
                        reading.setText("" + slider.getValue());
                    });
                }   
                if (defaultLabels){
                        slider.setMajorTickSpacing((max-min)/5);
                    slider.setPaintLabels(true);
                }


                GridBagConstraints gbc = new GridBagConstraints();

                gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
                panel.add(label, gbc);
                gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
                panel.add(slider, gbc);
                gbc.gridx = 2; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
                panel.add(reading, gbc);

                for (Component c: new Component[]{label, slider, reading, panel}){
                    c.setBackground(Color.DARK_GRAY);
                    c.setForeground(Color.LIGHT_GRAY);
                }

                this.slider = slider;
                this.reading = reading;
                this.panel = panel;
                this.label = label;

            }

            void setLabels(String[] labels){
                Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
                // calculate label positions
                int min = slider.getMinimum();
                int max = slider.getMaximum();
                labelTable.put(min, new JLabel(labels[0]));
                labelTable.put(max, new JLabel(labels[labels.length - 1]));
                for (int i = 0; i < labels.length - 2; i++){
                    labelTable.put(min + (i+1)*((max - min)/(labels.length - 2)), new JLabel(labels[i+1]));
                }
                slider.setLabelTable(labelTable);
                slider.setMajorTickSpacing((max-min)/(labels.length - 2));
                slider.setPaintLabels(true);
            }
        }

    public SimulationControls(){setLayout(new BorderLayout());}

    public void populate(Plane plane){
        removeAll();
        this.plane = plane;

        JPanel content = new JPanel(new GridLayout(3, 2));
        content.setBackground(Color.LIGHT_GRAY);

        generation = new JTextArea("\n    Current generation: 0 / " + parameters.NUMBER_GENERATIONS + "\n    Best time score: 0\n\n\n");
        generation.setWrapStyleWord(true);
        generation.setBorder(new CompoundBorder(new EmptyBorder(new Insets(15, 15, 15, 15)), new LineBorder(Color.WHITE, 1)));
        generation.setFont(generation.getFont().deriveFont(Font.BOLD,16f));
        content.add(generation);

        JPanel mainInfo = new JPanel(new GridLayout(5, 1));

        Slider speed = new Slider("Speed", 50, 0, 100, false, false);
        speed.setLabels(new String[]{"0.1x", "1x", "Max"});
        speed.slider.addChangeListener(e -> {
            parameters.delay = (100-speed.slider.getValue()) * 0.01;
            speed.reading.setText("" + speed.slider.getValue());
        });
        mainInfo.add(speed.panel);

        JPanel buttonsPanel = new JPanel(new GridLayout());
        JButton pauseButton = new JButton("Play!");
        JButton skipButton = new JButton("New gene pool");
        JButton endButton = new JButton("End round");

        skipButton.addActionListener(e -> parameters.SKIP = true);
        endButton.addActionListener(e -> {parameters.END = true; parameters.STARTED = false;});
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(skipButton);
        buttonsPanel.add(endButton);

        allComponents.addAll(Arrays.asList(buttonsPanel.getComponents()));
        mainInfo.add(buttonsPanel);

        // Number of Generations slider

        Slider generations = new Slider("generations", parameters.NUMBER_GENERATIONS, 200, 10000, false, true);
        generations.slider.addChangeListener(e -> {
            generations.reading.setText("" + generations.slider.getValue());
            if (!parameters.STARTED && !generations.slider.getValueIsAdjusting()){
                parameters.NUMBER_GENERATIONS = generations.slider.getValue();
            }
        });

        Slider simulations = new Slider("sims/gen", parameters.NUMBER_SIMULATIONS, 100, 5000, false, false);
        simulations.slider.addChangeListener(e -> {
            simulations.reading.setText("" + simulations.slider.getValue());
            if (!parameters.STARTED && !simulations.slider.getValueIsAdjusting()){
                parameters.NUMBER_SIMULATIONS = simulations.slider.getValue();
            }
        });
        simulations.setLabels(new String[]{"200", "2500", "5000 (Heavy)"});

        // number of Simulations slider
        
        mainInfo.add(generations.panel);
        mainInfo.add(simulations.panel);

        // Max Groups slider 
        Slider groups = new Slider("max groups", parameters.MAX_GROUPS, 2, 12, false, true);
        groups.slider.addChangeListener(e -> {
            groups.reading.setText("" + groups.slider.getValue());
            if (!parameters.STARTED){
                parameters.MAX_GROUPS = groups.slider.getValue();
            }
        });

        mainInfo.add(groups.panel);

        mainInfo.setBorder(new EmptyBorder(0, 15, 0, 15));
        mainInfo.setMaximumSize(mainInfo.getPreferredSize());
        allComponents.addAll(Arrays.asList(mainInfo.getComponents()));

        content.add(mainInfo);


         //uhhh why did i make my life so difficult who cares what the plane structure is???? >:(
        // doing this manually LMAO
        int[][] blocks = new int[][]{
            {0}, {1}, {1,1}, {1,2}, {2,2}, {2,3}, {3,3}, {2,3,2}, {2,4,2}, {3,3,3},
            {3,4,3}, {4,3,4}, {4,4,4}, {4,5,4}, {3,4,4,3}
        };


        // plane settings

        JPanel planeSettings = new JPanel(new GridLayout(6, 2));
        planeSettings.setBorder(new EmptyBorder(0, 15, 0, 15));
        planeSettings.add(new JLabel("<HTML><u>Plane settings</u></HTML>"));

        Slider width = new Slider("Plane width", plane.getWidth()-plane.getAisles().length, 2, 14, false, true);
        width.slider.addChangeListener(e -> {
            width.reading.setText("" + width.slider.getValue());
            if (!width.slider.getValueIsAdjusting()){
                if (!parameters.STARTED){
                    parameters.plane = new Plane(plane.getLength(), plane.getBusinessRows(), plane.getLength() - plane.getBusinessRows(), 
                    width.slider.getValue(), blocks[width.slider.getValue()], plane.getExits());
                    capacity = new JLabel("" + plane.capacity);
                    price = new JLabel("" + Math.round(((50 + plane.capacity / 2) * 100)/100));
                    parameters.REDRAW = true;
                }
            }
        });
        planeSettings.add(width.panel);

        Slider length = new Slider("Plane length", plane.getLength(), 8, 32, false, true);
        length.slider.addChangeListener(e -> {
            length.reading.setText("" + length.slider.getValue());
            if (!length.slider.getValueIsAdjusting()){
                if (!parameters.STARTED){
                    parameters.plane = new Plane(length.slider.getValue(), plane.getBusinessRows(), length.slider.getValue() - plane.getBusinessRows(), 
                    plane.getWidth()-plane.getAisles().length, plane.getBlocks(), plane.getExits());
                    capacity = new JLabel("" + plane.capacity);
                    price = new JLabel("" + Math.round(((50 + plane.capacity / 2) * 100)/100));
                    parameters.REDRAW = true;
                }
            }
        });
        planeSettings.add(length.panel);

        JPanel bRow = new JPanel(new FlowLayout());


        bRow.add(new JLabel("Business rows: "));
        JTextField businessRowsField = new JTextField(6);
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
                    if (!(val > 0 && val <= length.slider.getValue())) {
                        businessRowsField.setBackground(new Color(237, 88, 71));
                        businessRowsField.setToolTipText("Number out of range: business rows must <= plane length.");
                    } else {
                        businessRowsField.setBackground(Color.WHITE);
                        businessRowsField.setToolTipText("Number of business rows (min: 0; max: plane length)");
                        if (!parameters.STARTED){
                            parameters.plane = new Plane(plane.getLength(), val, plane.getLength() - val, plane.getWidth()-plane.getAisles().length, plane.getBlocks(), plane.getExits());
                            capacity = new JLabel("" + plane.capacity);
                            price = new JLabel("" + Math.round(((50 + plane.capacity / 2) * 100)/100));
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
        bRow.setSize(width.panel.getSize());
        planeSettings.add(bRow);

        allComponents.addAll(Arrays.asList(bRow.getComponents()));


        JPanel capacityPanel = new JPanel(new GridLayout(1, 2));
        capacityPanel.add(new JLabel("Plane capacity: "));
        this.capacity = new JLabel("" + plane.capacity);
        capacityPanel.add(capacity);
        planeSettings.add(capacityPanel);

        this.price = new JLabel("" + Math.round(((50 + plane.capacity / 2) * 100)/100));
        JPanel pricePanel = new JPanel(new GridLayout(1, 2));
        pricePanel.add(new JLabel(" Price / minute delay (£)"));
        pricePanel.add(price);
        planeSettings.add(pricePanel);

        allComponents.addAll(Arrays.asList(capacityPanel.getComponents()));
        allComponents.addAll(Arrays.asList(pricePanel.getComponents()));
        allComponents.addAll(Arrays.asList(planeSettings.getComponents()));

        content.add(planeSettings);


        // population settings

        JPanel popSettings = new JPanel(new GridLayout(6, 1));
        popSettings.setBorder(new EmptyBorder(0, 15, 0, 15));
        JLabel top = new JLabel("<HTML><u>Population settings</u></HTML>");
        allComponents.add(top);
        popSettings.add(top);

        Slider disabled = new Slider("% disabled", parameters.PROBABILITY_DISABLED * 100, 0, 100, true, true);
        disabled.slider.addChangeListener(e -> {
            if (programmaticUpdate) return;
            disabled.reading.setText("" + disabled.slider.getValue());
            if (!disabled.slider.getValueIsAdjusting()) {
                parameters.PROBABILITY_DISABLED = disabled.slider.getValue() / 100.0;
            }
        });
        Slider families = new Slider("% families", parameters.PROBABILITY_FAMILIES * 100, 0, 100, true, true);
        families.slider.addChangeListener(e -> {
            if (programmaticUpdate) return;
            families.reading.setText("" + families.slider.getValue());
            if (!families.slider.getValueIsAdjusting()) {
                parameters.PROBABILITY_FAMILIES = families.slider.getValue() / 100.0;
            }
        });
        Slider old = new Slider("% old", parameters.PROBABILITY_OLD * 100, 0, 100, true, true);
        old.slider.addChangeListener(e -> {
            if (programmaticUpdate) return;
            old.reading.setText("" + old.slider.getValue());
            if (!old.slider.getValueIsAdjusting()) {
                parameters.PROBABILITY_OLD = old.slider.getValue() / 100.0;
            }
        });
        Slider children = new Slider("% children", parameters.PROBABILITY_CHILDREN * 100, 0, 100, true, true);
        children.slider.addChangeListener(e -> {
            if (programmaticUpdate) return;
            children.reading.setText("" + children.slider.getValue());
            if (!children.slider.getValueIsAdjusting()) {
                parameters.PROBABILITY_CHILDREN = children.slider.getValue() / 100.0;
            }
        });
        Slider bags = new Slider("% bags", parameters.PROBABILITY_BAGS * 100, 0, 100, true, true);
        popSettings.add(disabled.panel);
        popSettings.add(families.panel);
        popSettings.add(old.panel);
        popSettings.add(children.panel);
        popSettings.add(bags.panel);

        content.add(popSettings);

        // Genetic algorithm settings 
        JPanel gaSettings = new JPanel(new GridLayout(6, 1));
        gaSettings.setBorder(new EmptyBorder(0, 15, 0, 15));
        gaSettings.add(new JLabel("<HTML><u>Genetic algorithm settings</u></HTML>"));

        Slider elitism = new Slider("% elitism", parameters.ELITISM * 100, 0, 100, false, true);
        elitism.slider.addChangeListener(e -> {
            if (programmaticUpdate) return;
            elitism.reading.setText("" + elitism.slider.getValue());
            if (!elitism.slider.getValueIsAdjusting()) {
                parameters.ELITISM = elitism.slider.getValue() / 100.0;
            }
        });
        gaSettings.add(elitism.panel);

        Slider tournament = new Slider("tournament size", parameters.TOURNAMENT_SIZE, 2, 10, false, true);
        tournament.slider.addChangeListener(e -> {
            if (programmaticUpdate) return;
            tournament.reading.setText("" + tournament.slider.getValue());
            if (!tournament.slider.getValueIsAdjusting()) {
                parameters.TOURNAMENT_SIZE = tournament.slider.getValue();
            }
        });
        gaSettings.add(tournament.panel);

        Slider selection = new Slider("% selection pool", parameters.SELECTION_POOL * 100, 0, 100, false, true);
        selection.slider.addChangeListener(e -> {
            if (programmaticUpdate) return;
            selection.reading.setText("" + selection.slider.getValue());
            if (!selection.slider.getValueIsAdjusting()) {
                parameters.SELECTION_POOL = selection.slider.getValue() / 100.0;
            }
        });
        gaSettings.add(selection.panel);

        Slider mutation = new Slider("% mutation", parameters.MUTATION * 100, 0, 100, false, true);
        mutation.slider.addChangeListener(e -> {
            if (programmaticUpdate) return;
            mutation.reading.setText("" + mutation.slider.getValue());
            if (!mutation.slider.getValueIsAdjusting()) {
                parameters.MUTATION = mutation.slider.getValue() / 100.0;
            }
        });
        gaSettings.add(mutation.panel);

        Slider newSims = new Slider("% new sims", parameters.NEW_SIMULATIONS * 100, 0, 100, false, true);
        newSims.slider.addChangeListener(e -> {
            if (programmaticUpdate) return;
            newSims.reading.setText("" + newSims.slider.getValue());
            if (!newSims.slider.getValueIsAdjusting()) {
                parameters.NEW_SIMULATIONS = newSims.slider.getValue() / 100.0;
            }
        });
        gaSettings.add(newSims.panel);

        content.add(gaSettings);
        allComponents.addAll(Arrays.asList(gaSettings.getComponents()));

        this.elitism = elitism;
        this.tournament = tournament;
        this.selectionPool = selection;
        this.mutation = mutation;
        this.newSims = newSims;

        // bottom button

        JPanel bottomSpacer = new JPanel(new GridLayout(5, 1));
        bottomSpacer.setBorder(new EmptyBorder(0, 15, 0, 15));

        Slider quickness = new Slider("boarding speed", parameters.QUICKNESS * 100, 0, 100, false, true);
        quickness.slider.addChangeListener(e -> {
            quickness.reading.setText("" + quickness.slider.getValue());
            if (!quickness.slider.getValueIsAdjusting()) {
                parameters.QUICKNESS = quickness.slider.getValue() / 100.0;
            }
        });
        bottomSpacer.add(quickness.panel);

        Slider clustering = new Slider("clustering", parameters.CLUSTERING * 100, 0, 100, false, true);
        clustering.slider.addChangeListener(e -> {
            clustering.reading.setText("" + clustering.slider.getValue());
            if (!clustering.slider.getValueIsAdjusting()) {
                parameters.CLUSTERING = clustering.slider.getValue() / 100.0;
            }
        });
        bottomSpacer.add(clustering.panel);

        Slider orderliness = new Slider("orderliness", parameters.ORDERLINESS * 100, 0, 100, false, true);
        orderliness.slider.addChangeListener(e -> {
            orderliness.reading.setText("" + orderliness.slider.getValue());
            if (!orderliness.slider.getValueIsAdjusting()) {
                parameters.ORDERLINESS = orderliness.slider.getValue() / 100.0;
            }
        });
        bottomSpacer.add(orderliness.panel);

        JPanel worstPanel = new JPanel(new GridLayout(1, 2));
        JLabel worstLabel = new JLabel("Find worst solution: (hint)");
        worstLabel.setToolTipText("Best with a high max group numner");
        worstPanel.add(worstLabel);
        JCheckBox worst = new JCheckBox();
        worst.addActionListener(e -> {if (!parameters.STARTED){parameters.WORSTFIND = !parameters.WORSTFIND;}});
        worstPanel.add(worst);
        bottomSpacer.add(worstPanel);

        allComponents.addAll(Arrays.asList(worstPanel.getComponents()));


        JButton startButton = new JButton("Start simulation");
        java.util.function.BiConsumer<Integer, Integer> startThatThang = (num, num2) -> {

            // so just update all the parameters

            try { Integer.parseInt(businessRowsField.getText()); }
            catch (Exception x) { return; }
            int b = Integer.parseInt(businessRowsField.getText());
            if (b == 0 || b > length.slider.getValue()) { return; }

            // this is actually such a mess why did i try to be clever lmao
            // family ones
            try {
                parameters.PROBABILITY_DISABLED = disabled.slider.getValue() / 100.0;

                parameters.PROBABILITY_FAMILIES = families.slider.getValue() / 100.0;

                parameters.PROBABILITY_OLD = old.slider.getValue() / 100.0;
                
                parameters.PROBABILITY_CHILDREN = children.slider.getValue() / 100.0;
                
                parameters.PROBABILITY_BAGS = bags.slider.getValue() / 100.0;
            } catch (Exception xxx) {
            }

            // ga ones

            parameters.NUMBER_GENERATIONS = generations.slider.getValue();
            parameters.NUMBER_SIMULATIONS = simulations.slider.getValue();
            parameters.MAX_GROUPS = groups.slider.getValue();

            // rest of the plane ones

            int[] exitRows = new int[]{0, Math.max(0, length.slider.getValue() - 1)};
            parameters.plane = new Plane(length.slider.getValue(), b, length.slider.getValue() - b, width.slider.getValue(), blocks[width.slider.getValue()],
                exitRows);

            this.capacity = new JLabel("" + plane.capacity);
            this.price = new JLabel("" + Math.round(((50 + plane.capacity / 2) * 100)/100));

            if (worst.isSelected()){parameters.WORSTFIND = true;} else {parameters.WORSTFIND = false;}

            parameters.END = true;
            parameters.STARTED = true;
            parameters.PAUSED = false;
            if ("Play!".equals(pauseButton.getText())) {
                pauseButton.setText("Pause");
            }
        };
        startButton.addActionListener(e -> startThatThang.accept(69, 420)); // look i just needed it to work ok


        pauseButton.addActionListener(e -> {
            if (!parameters.STARTED){startThatThang.accept(67, 41);}
            else if (parameters.PAUSED) {
                pauseButton.setText("Pause");
                parameters.PAUSED = false;

            } else {
                pauseButton.setText("Play!");
                parameters.PAUSED = true;
            }

        });
        JButton resetButton = new JButton("Reset to default");
        resetButton.addActionListener(e -> {
            pauseButton.setText("Play!");
            parameters.PAUSED = false;
            parameters.END = true;
            parameters.STARTED = false;
            parameters.RESET = true;
            parameters.REDRAW = true;
            endButton.doClick(); // this is now the jankiest thing I have ever written
            // literally going back to the 80s with goto statements LMAO
        });
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        buttonPanel.add(resetButton);
        buttonPanel.add(startButton);
        bottomSpacer.add(buttonPanel);
        bottomSpacer.setMaximumSize(bottomSpacer.getPreferredSize());
        content.add(bottomSpacer);

        allComponents.addAll(Arrays.asList(bottomSpacer.getComponents()));
        allComponents.addAll(Arrays.asList(buttonPanel.getComponents()));
 
        removeAll();
        add(content, BorderLayout.CENTER);

        allComponents.addAll(Arrays.asList(content.getComponents()));
        for (Component c: allComponents){
            c.setBackground(Color.DARK_GRAY);
            c.setForeground(Color.LIGHT_GRAY);
            if (c instanceof JButton){
                c.setBackground(new Color(255, 105, 120));
                c.setForeground(Color.WHITE);
                ((JButton)c).setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
            }
        }


        revalidate();
        repaint();

    }

    public void updateGeneration(int gens, Simulation best, Simulation worst, int stats, long startTime){
        gens++;
        long timeElapsed = System.nanoTime()-startTime;
        // Don't swap best/worst; instead indicate which one is currently shown in the plane view
        String bestShownFlag = parameters.WORSTFIND ? "" : " (shown)";
        String worstShownFlag = parameters.WORSTFIND ? " (shown)" : "";

        generation.setText("\n    Current generation: " + gens + " / " + parameters.NUMBER_GENERATIONS + 
            "\n    Current pool: " + parameters.pool + 
            "\n    Best simulation has " + best.getNumberGroups() + " groups, taking " 
            +  best.getDuration() + "\n ticks." + " Fitness score: " + best.fitnessScore + bestShownFlag + 
            (worst != null ? "\n    Worst simulation has " + worst.getNumberGroups() + " groups, taking " 
            +  worst.getDuration() + "\n ticks." + " Fitness score: " +  worst.fitnessScore + worstShownFlag : "") + //arterial blockage inducing icl
            "\n    Static generations: " + stats + "\n\n       Time elapsed: " +
            String.format("%02d:%02d.%03d",
                timeElapsed / 60000000000L,
                (timeElapsed / 1000000000) % 60,
                (timeElapsed / 1000000) % 1000) +
            (best.splitFamilies()>0 ? "\n       This simulation contains " + best.splitFamilies() + " split family members.":"")
    ); if (gens == parameters.NUMBER_GENERATIONS){
            generation.setText("\n    Simulation finished! Showing winning simulation." + "\n" + generation.getText() + "\n    Trialled: "
            + parameters.NUMBER_GENERATIONS*parameters.NUMBER_SIMULATIONS + " simulations in " 
            +             String.format("%02d:%02d.%03d",
                timeElapsed / 60000000000L,
                (timeElapsed / 1000000000) % 60,
                (timeElapsed / 1000000) % 1000)); // half understand long numbers even after it was explained to me ;(
        }
    }

    public void generationText(String s){ // just fpr the tutorial lol
        generation.setText(s);
    }

    public void refreshGAControls() {
        programmaticUpdate = true; 
        try {
            elitism.slider.setValue((int)(parameters.ELITISM * 100));
            elitism.reading.setText("" + elitism.slider.getValue());
            elitism.slider.setEnabled(!parameters.STARTED);

            tournament.slider.setValue(parameters.TOURNAMENT_SIZE);
            tournament.reading.setText("" + tournament.slider.getValue());
            tournament.slider.setEnabled(!parameters.STARTED);

            selectionPool.slider.setValue((int)(parameters.SELECTION_POOL * 100));
            selectionPool.reading.setText("" + selectionPool.slider.getValue());
            selectionPool.slider.setEnabled(!parameters.STARTED);

            mutation.slider.setValue((int)(parameters.MUTATION * 100));
            mutation.reading.setText("" + mutation.slider.getValue());
            mutation.slider.setEnabled(!parameters.STARTED);

            newSims.slider.setValue((int)(parameters.NEW_SIMULATIONS * 100));
            newSims.reading.setText("" + newSims.slider.getValue());
            newSims.slider.setEnabled(!parameters.STARTED);

        } finally {
            programmaticUpdate = false;
        }
    }


    public void reset(){ // reset parameters to defaults and refresh UI
        // parameter defaults (same as before)
        parameters.NUMBER_GENERATIONS = 1000;
        parameters.NUMBER_SIMULATIONS = 500;
        parameters.plane =
            new Plane(20, 6, 14, 6, new int[]{3, 3}, new int[]{0, 7});
        parameters.MAX_GROUPS = 6;
        parameters.PROBABILITY_DISABLED = 0.03;
        parameters.PROBABILITY_FAMILIES = 0.20;
        parameters.PROBABILITY_OLD = 0.10;
        parameters.PROBABILITY_CHILDREN = 0.10;
        parameters.PROBABILITY_BAGS = 0.80;
        parameters.QUICKNESS = 1.0;
        parameters.CLUSTERING = 0.3;
        parameters.ORDERLINESS = 0.5;
        parameters.ELITISM = 0.03;
        parameters.TOURNAMENT_SIZE = 3;
        parameters.SELECTION_POOL = 0.5;
        parameters.MUTATION = 0.2;
        parameters.NEW_SIMULATIONS = 0.3;
        parameters.SPLIT_PENALTY = 30;

        
        parameters.PAUSED = false;
        parameters.WORSTFIND = false;

            
        parameters.SKIP = false;
        parameters.END = false;
        parameters.STARTED = false;
        parameters.REDRAW = false;
        parameters.delay = 0.0;

        // gotta make sure everything renders when it's ready
        SwingUtilities.invokeLater(() -> { 
            allComponents.clear();
            populate(parameters.plane);
            updateGeneration(0, new Simulation(10), new Simulation(10), 0, 0);
            revalidate();
            repaint();
        });
    }
}


