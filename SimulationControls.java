import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
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

    // GA controls (exposed so they can be updated
        private JSlider elitism; private JLabel elitismReading;
        private JSlider tournament;  private JLabel tournamentReading;
        private JSlider selectionPool; private JLabel selectionReading;
        private JSlider mutation; private JLabel mutationReading;
        private JSlider newSims; private JLabel newSimsReading;
        private boolean programmaticUpdate = false; // stops recurive listener shenanigans

        JLabel capacity;
        JLabel price;

        private JSlider quickness; private JLabel quicknessReading;
        private JSlider orderliness;  private JLabel orderlinessReading;
        private JSlider clustering; private JLabel clusteringReading;
        
        
        GridBagLayout gbl = new GridBagLayout();

    public SimulationControls(){setLayout(new BorderLayout());}

    public void populate(Plane plane){
        removeAll();
        this.plane = plane;

        JPanel content = new JPanel(new GridLayout(4, 2));
        content.setBackground(Color.LIGHT_GRAY);

        generation = new JTextArea("\n    Current generation: 0 / " + parameters.NUMBER_GENERATIONS + "\n    Best time score: 0\n\n\n");
        generation.setBorder(new CompoundBorder(new EmptyBorder(new Insets(15, 15, 15, 15)), new LineBorder(Color.WHITE, 1)));
        generation.setFont(generation.getFont().deriveFont(Font.BOLD,16f));
        generation.setLineWrap(true);
        content.add(generation);

        JPanel mainInfo = new JPanel(new GridLayout(5, 1));

        JPanel speedpanel = new JPanel(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        speedpanel.add(new JLabel("Speed  "));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        JSlider speed = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        //Create the label table
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        JLabel[] flabels = new JLabel[]{ new JLabel("0.1x"), new JLabel("1x"), new JLabel("Max")};
        allComponents.addAll(Arrays.asList(flabels));
        labelTable.put( 0, flabels[0] );
        labelTable.put( 50, flabels[1]);
        labelTable.put( 100, flabels[2] );
        speed.setLabelTable( labelTable );
        speed.setPaintLabels(true);

        speed.addChangeListener(e -> {
            parameters.delay = (100-speed.getValue()) * 0.01;
        }
        );
        speedpanel.add(speed);
        mainInfo.add(speedpanel);

        allComponents.addAll(Arrays.asList(speedpanel.getComponents()));

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

        JPanel nG = new JPanel(gbl);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        nG.add(new JLabel("generations: "), gbc);

        JLabel nGReading = new JLabel();

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        JSlider genSlider = new JSlider(200, 10000, parameters.NUMBER_GENERATIONS);
        genSlider.addChangeListener(e -> {
            nGReading.setText("" + genSlider.getValue());
            if (!parameters.STARTED){
                parameters.NUMBER_GENERATIONS = genSlider.getValue();
            }
        });
        genSlider.setMajorTickSpacing((10000-200)/4);
        genSlider.setPaintLabels(true);
        nG.add(genSlider, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        nG.add(nGReading, gbc);
        allComponents.addAll(Arrays.asList(nG.getComponents()));

        // number of Simulations slider
        JPanel nS = new JPanel(gbl);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        nS.add(new JLabel("sims/gen: "), gbc);

        JLabel nSReading = new JLabel();

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        JSlider simSlider = new JSlider(100, 5000, parameters.NUMBER_SIMULATIONS);
        simSlider.addChangeListener(e -> {
            nSReading.setText("" + simSlider.getValue());
            if (!parameters.STARTED){
                parameters.NUMBER_SIMULATIONS = simSlider.getValue();
            }
        });
        Hashtable<Integer, JLabel> simsTable = new Hashtable<>();
        JLabel[] slabels = new JLabel[]{new JLabel("200"), new JLabel("2500"), new JLabel("5000 (Heavy)")};
        for (JLabel label: slabels){allComponents.add(label);}
        simsTable.put( 200, slabels[0]);
        simsTable.put( 2500, slabels[1] );
        simsTable.put( 5000, slabels[2] );
        simSlider.setLabelTable(simsTable);
        simSlider.setPaintLabels(true);
        nS.add(simSlider, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        nS.add(nSReading, gbc);
        allComponents.addAll(Arrays.asList(nS.getComponents()));

        mainInfo.add(nG);
        mainInfo.add(nS);

        // Max Groups slider 
        // ITS SO UGLY BUT IDEC ANYMORE GET IT DOOONNNNNNEEE    

        JPanel groups = new JPanel(gbl);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        groups.add(new JLabel("max groups: "), gbc);

        JLabel groupsR = new JLabel();

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        JSlider groupsSlider = new JSlider(2, 12, parameters.MAX_GROUPS);
        genSlider.addChangeListener(e -> {
            groupsR.setText("" + groupsSlider.getValue());
            if (!parameters.STARTED){
                parameters.MAX_GROUPS = groupsSlider.getValue();
            }
        });
        groupsSlider.setMajorTickSpacing(2);
        groupsSlider.setPaintLabels(true);
        groups.add(groupsSlider, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        groups.add(groupsR, gbc);
        mainInfo.add(groups);

        allComponents.addAll(Arrays.asList(groups.getComponents()));
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


        JPanel widthRow = new JPanel(new FlowLayout()); // this is called laziness
        JPanel lengthRow = new JPanel(new FlowLayout());
        JPanel bRow = new JPanel(new FlowLayout());

        widthRow.add(new JLabel("Plane width: "));
        JSlider width = new JSlider(JSlider.HORIZONTAL, 2, 14, plane.getWidth()-plane.getAisles().length);
        width.setMajorTickSpacing(4);
        width.setPaintLabels(true);

        JLabel widthReading = new JLabel("" + width.getValue());
        width.addChangeListener(e -> {
            widthReading.setText("" + width.getValue());
            if (!width.getValueIsAdjusting()){
                if (!parameters.STARTED){
                    parameters.plane = new Plane(plane.getLength(), plane.getBusinessRows(), plane.getLength() - plane.getBusinessRows(), 
                    width.getValue(), blocks[width.getValue()], plane.getExits(), "Custom aircraft");
                    capacity = new JLabel("" + plane.capacity);
                    price = new JLabel("" + Math.round(((50 + plane.capacity / 2) * 100)/100));
                    parameters.REDRAW = true;
                }
            }
        });
        widthReading.setMaximumSize(widthReading.getPreferredSize());
        widthRow.add(width);
        widthRow.add(widthReading);
        planeSettings.add(widthRow);

        lengthRow.add(new JLabel("Plane length: "));
        JSlider length = new JSlider(JSlider.HORIZONTAL, 8, 32, plane.getLength());
        length.setMajorTickSpacing(8);
        length.setMinorTickSpacing(8);
        length.setPaintLabels(true);
        length.setPaintTicks(true);

        JLabel lengthReading = new JLabel("" + length.getValue());
        length.addChangeListener(e -> {
            lengthReading.setText("" + length.getValue());
            if (!length.getValueIsAdjusting()){
            if (!parameters.STARTED){
                parameters.plane = new Plane(length.getValue(), plane.getBusinessRows(), plane.getLength() - plane.getBusinessRows(), 
                plane.getWidth()-plane.getAisles().length, plane.getBlocks(), new int[]{0, Math.max(0, length.getValue() - 1)}, "Custom aircraft");
                capacity = new JLabel("" + plane.capacity);
                price = new JLabel("" + Math.round(((50 + plane.capacity / 2) * 100)/100));
                parameters.REDRAW = true;
            }
        }
        });
        lengthReading.setMaximumSize(lengthReading.getPreferredSize());
        lengthRow.add(length);
        lengthRow.add(lengthReading);
        planeSettings.add(lengthRow);

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
                    if (!(val > 0 && val <= length.getValue())) {
                        businessRowsField.setBackground(new Color(237, 88, 71));
                        businessRowsField.setToolTipText("Number out of range: business rows must <= plane length.");
                    } else {
                        businessRowsField.setBackground(Color.WHITE);
                        businessRowsField.setToolTipText("Number of business rows (min: 0; max: plane length)");
                        if (!parameters.STARTED){
                            parameters.plane = new Plane(plane.getLength(), val, plane.getLength() - val, plane.getWidth()-plane.getAisles().length, plane.getBlocks(), plane.getExits(), plane.getType());
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
        bRow.setSize(widthRow.getSize());
        planeSettings.add(bRow);

        allComponents.addAll(Arrays.asList(widthRow.getComponents()));
        allComponents.addAll(Arrays.asList(lengthRow.getComponents()));
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
        popSettings.add(top);
        //popSettings.add(new JLabel("Total passengers:"));
        //popSettings.add(new JLabel("" + plane.getCapacity()));

        popSettings.add(slider(parameters.PROBABILITY_DISABLED * 100, 0, 0, null, "% disabled: "));
        popSettings.add(slider(parameters.PROBABILITY_FAMILIES* 100, 0, 0, null, "% families: "));
        popSettings.add(slider(parameters.PROBABILITY_OLD* 100, 0, 0, null, "% old: "));
        popSettings.add(slider(parameters.PROBABILITY_CHILDREN * 100, 0, 0, null, "% children: "));
        popSettings.add(slider(parameters.PROBABILITY_BAGS * 100, 0, 0, null, "% bags: "));

        allComponents.addAll(Arrays.asList(popSettings.getComponents()));
        content.add(popSettings);

        // Genetic algorithm settings 
        JPanel gaSettings = new JPanel(new GridLayout(6, 1));
        gaSettings.setBorder(new EmptyBorder(0, 15, 0, 15));
        gaSettings.add(new JLabel("<HTML><u>Genetic algorithm settings</u></HTML>"));

        JPanel elitismPanel = slider(parameters.ELITISM*100, 0, 0, e -> {
            if (programmaticUpdate) return;
            elitismReading.setText("" + elitism.getValue());
            if (!elitism.getValueIsAdjusting()) {
                parameters.ELITISM = elitism.getValue() / 100.0;
            }
        }, "% elitism");
        // extract internal components (label, slider, reading) and keep references
        this.elitism = (JSlider) elitismPanel.getComponent(1);
        this.elitismReading = (JLabel) elitismPanel.getComponent(2);
        gaSettings.add(elitismPanel);

        JPanel tournamentPanel = slider(parameters.TOURNAMENT_SIZE, 2, 10, e -> {
            if (programmaticUpdate) return;
            tournamentReading.setText("" + tournament.getValue());
            if (!tournament.getValueIsAdjusting()) parameters.TOURNAMENT_SIZE = tournament.getValue();
        }, "tournament size:");
        this.tournament = (JSlider) tournamentPanel.getComponent(1);
        this.tournamentReading = (JLabel) tournamentPanel.getComponent(2);
        gaSettings.add(tournamentPanel);

        JPanel selectionPanel = slider(parameters.SELECTION_POOL * 100, 0, 100, e -> {
            if (programmaticUpdate) return;
            selectionReading.setText("" + selectionPool.getValue());
            if (!selectionPool.getValueIsAdjusting()) parameters.SELECTION_POOL = selectionPool.getValue() / 100.0;
        }, " % selection pool :");
        this.selectionPool = (JSlider) selectionPanel.getComponent(1);
        this.selectionReading = (JLabel) selectionPanel.getComponent(2);
        gaSettings.add(selectionPanel);

        JPanel mutationPanel = slider(parameters.MUTATION*100, 0, 100, e -> {
            if (programmaticUpdate) return;
            mutationReading.setText("" + mutation.getValue());
            if (!mutation.getValueIsAdjusting()) parameters.MUTATION = mutation.getValue() / 100.0;
        }, "% mutation:");
        this.mutation = (JSlider) mutationPanel.getComponent(1);
        this.mutationReading = (JLabel) mutationPanel.getComponent(2);
        gaSettings.add(mutationPanel);

        JPanel newSimsPanel = slider(parameters.NEW_SIMULATIONS*100, 0, 100, e -> {
            if (programmaticUpdate) return;
            newSimsReading.setText("" + newSims.getValue());
            if (!newSims.getValueIsAdjusting()) parameters.NEW_SIMULATIONS = newSims.getValue() / 100.0;
        }, "% new sims:");
        this.newSims = (JSlider) newSimsPanel.getComponent(1);
        this.newSimsReading = (JLabel) newSimsPanel.getComponent(2);
        gaSettings.add(newSimsPanel);

        content.add(gaSettings);

        allComponents.addAll(Arrays.asList(elitismPanel.getComponents()));
        allComponents.addAll(Arrays.asList(tournamentPanel.getComponents()));
        allComponents.addAll(Arrays.asList(selectionPanel.getComponents()));
        allComponents.addAll(Arrays.asList(mutationPanel.getComponents()));
        allComponents.addAll(Arrays.asList(newSimsPanel.getComponents()));

        allComponents.addAll(Arrays.asList(gaSettings.getComponents()));





        //out of sight, out of mind.
        /* */
        JPanel disabledPanel = (JPanel) popSettings.getComponent(1);
        JSlider disabledSlider = (JSlider) disabledPanel.getComponent(1);
        allComponents.addAll(Arrays.asList(disabledPanel.getComponents()));

        JPanel famPanel = (JPanel) popSettings.getComponent(2);
        JSlider famSlider = (JSlider) famPanel.getComponent(1);
        allComponents.addAll(Arrays.asList(famPanel.getComponents()));

        JPanel oldPanel = (JPanel) popSettings.getComponent(3);
        JSlider oldSlider = (JSlider) oldPanel.getComponent(1);
        allComponents.addAll(Arrays.asList(oldPanel.getComponents()));

        JPanel childPanel = (JPanel) popSettings.getComponent(4);
        JSlider childSlider = (JSlider) childPanel.getComponent(1);
        allComponents.addAll(Arrays.asList(childPanel.getComponents()));

        JPanel bagsPanel = (JPanel) popSettings.getComponent(5);
        JSlider bagsSlider = (JSlider) bagsPanel.getComponent(1);
        allComponents.addAll(Arrays.asList(bagsPanel.getComponents()));

        allComponents.addAll(Arrays.asList(new Component[]{disabledPanel, disabledSlider, famPanel, famSlider, oldPanel, 
            oldSlider, childPanel, childSlider, bagsPanel, bagsSlider}));





        // bottom button

        JPanel bottomSpacer = new JPanel(new GridLayout(5, 1));
        bottomSpacer.setBorder(new EmptyBorder(0, 15, 0, 15));

        JPanel quicknessPanel = slider(parameters.QUICKNESS*100, 0, 0, e -> {
            quicknessReading.setText("" + quickness.getValue());
            if (!quickness.getValueIsAdjusting()) parameters.QUICKNESS = quickness.getValue();
        }, "boarding speed: ");
        this.quickness = (JSlider) quicknessPanel.getComponent(1);
        this.quicknessReading = (JLabel) quicknessPanel.getComponent(2);
        bottomSpacer.add(quicknessPanel);

        JPanel clusteringPanel = slider(parameters.CLUSTERING*100, 0, 0, e -> {
            clusteringReading.setText("" + clustering.getValue());
            if (!clustering.getValueIsAdjusting()) parameters.CLUSTERING = clustering.getValue();
        }, "clustering: ");
        this.clustering = (JSlider) clusteringPanel.getComponent(1);
        this.clusteringReading = (JLabel) clusteringPanel.getComponent(2);
        bottomSpacer.add(clusteringPanel);

        JPanel orderlinessPanel = slider(parameters.ORDERLINESS*100, 0, 0, e -> {
            orderlinessReading.setText("" + orderliness.getValue());
            if (!orderliness.getValueIsAdjusting()) parameters.ORDERLINESS = orderliness.getValue();
        }, "orderliness: ");
        this.orderliness = (JSlider) orderlinessPanel.getComponent(1);
        this.orderlinessReading = (JLabel) orderlinessPanel.getComponent(2);
        bottomSpacer.add(orderlinessPanel);

        JPanel worstPanel = new JPanel(new GridLayout(1, 2));
        JLabel worstLabel = new JLabel("Find worst solution: (hint)");
        worstLabel.setToolTipText("Best with a high max group numner");
        worstPanel.add(worstLabel);
        JCheckBox worst = new JCheckBox();
        worst.addActionListener(e -> {if (!parameters.STARTED){parameters.WORSTFIND = !parameters.WORSTFIND;}});
        worstPanel.add(worst);
        bottomSpacer.add(worstPanel);

        Component[] lovelyJubblyComponents = new Component[]{quickness, quicknessReading, orderliness, orderlinessReading, clustering, clusteringReading};
        allComponents.addAll(Arrays.asList(lovelyJubblyComponents));
        allComponents.addAll(Arrays.asList(worstPanel.getComponents()));


        JButton startButton = new JButton("Start simulation");
        java.util.function.BiConsumer<Integer, Integer> startThatThang = (num, num2) -> {

            // so just update all the parameters

            try { Integer.parseInt(businessRowsField.getText()); }
            catch (Exception x) { return; }
            int b = Integer.parseInt(businessRowsField.getText());
            if (b == 0 || b > length.getValue()) { return; }

            // this is actually such a mess why did i try to be clever lmao
            // family ones
            try {
                parameters.PROBABILITY_DISABLED = disabledSlider.getValue() / 100.0;

                parameters.PROBABILITY_FAMILIES = famSlider.getValue() / 100.0;

                parameters.PROBABILITY_OLD = oldSlider.getValue() / 100.0;
                
                parameters.PROBABILITY_CHILDREN = childSlider.getValue() / 100.0;
                
                parameters.PROBABILITY_BAGS = bagsSlider.getValue() / 100.0;
            } catch (Exception xxx) {
            }

            // ga ones

            parameters.NUMBER_GENERATIONS = genSlider.getValue();
            parameters.NUMBER_SIMULATIONS = simSlider.getValue();
            parameters.MAX_GROUPS = groupsSlider.getValue();

            // rest of the plane ones

            int[] exitRows = new int[]{0, Math.max(0, length.getValue() - 1)};
            parameters.plane = new Plane(length.getValue(), b, length.getValue() - b, width.getValue(), blocks[width.getValue()],
                exitRows, "Custom Plane");

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
            parameters.RESET = true;
            parameters.REDRAW = true;
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

    public void updateGeneration(int gens, int time, int stats, long startTime){
        gens++;
        long timeElapsed = System.nanoTime()-startTime;
        generation.setText("\n    Current generation: " + gens + " / " + parameters.NUMBER_GENERATIONS + 
            "\n    Best time score: " + time + "\n    Static generations: " + stats);
        if (gens == parameters.NUMBER_GENERATIONS){
            generation.setText("\n    Simulation finished! Showing winning simulation." + "\n" + generation.getText() + "\n    Trialled: "
            + parameters.NUMBER_GENERATIONS*parameters.NUMBER_SIMULATIONS + " simulations in " 
            + String.format("%02d:%02d.%03d", timeElapsed/(1000000000*60), (timeElapsed/1000000000)%60, timeElapsed%1000000000));
        }
    }

    public void refreshGAControls() {
        programmaticUpdate = true;
        try {
            elitism.setValue((int)(parameters.ELITISM * 100));
            elitismReading.setText("" + elitism.getValue());
            elitism.setEnabled(!parameters.STARTED);

            tournament.setValue(parameters.TOURNAMENT_SIZE);
            tournamentReading.setText("" + tournament.getValue());
            tournament.setEnabled(!parameters.STARTED);

            selectionPool.setValue((int)(parameters.SELECTION_POOL * 100));
            selectionReading.setText("" + selectionPool.getValue());
            selectionPool.setEnabled(!parameters.STARTED);

            mutation.setValue((int)(parameters.MUTATION * 100));
            mutationReading.setText("" + mutation.getValue());
            mutation.setEnabled(!parameters.STARTED);

            newSims.setValue((int)(parameters.NEW_SIMULATIONS * 100));
            newSimsReading.setText("" + newSims.getValue());
            newSims.setEnabled(!parameters.STARTED);

        } finally {
            programmaticUpdate = false;
        }
    }

    public JPanel slider(double defval, int min, int max, ChangeListener c, String s){
        if (max == 0){ max = 100; }
        JPanel holder = new JPanel(gbl);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // label at column 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        JLabel label = new JLabel(s);
        label.setPreferredSize(new Dimension(100, 30));
        holder.add(label, gbc);
        allComponents.add(label);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, (int) defval);
        slider.setMajorTickSpacing((max-min)/4);
        if (max-min <= 10) slider.setMinorTickSpacing(1);
        slider.setPaintLabels(true);

        Dimension pref = slider.getPreferredSize();
        pref.width = 120;
        slider.setPreferredSize(pref);
        holder.add(slider, gbc);
        allComponents.add(slider);

        JLabel sliderReading = new JLabel("" + slider.getValue());
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        if (c == null) {
            slider.addChangeListener(e -> sliderReading.setText("" + slider.getValue()));
        } else {
            slider.addChangeListener(c);
        }
        holder.add(sliderReading, gbc);
        allComponents.add(sliderReading);

        return holder;
    }

    public void reset(){ // reset parameters to defaults and refresh UI
        // parameter defaults (same as before)
        parameters.NUMBER_GENERATIONS = 1000;
        parameters.NUMBER_SIMULATIONS = 500;
        parameters.plane =
            new Plane(20, 6, 14, 6, new int[]{3, 3}, new int[]{0, 7}, "Boeing 737");
        parameters.MAX_GROUPS = 4;
        parameters.PROBABILITY_DISABLED = 0.03;
        parameters.PROBABILITY_FAMILIES = 0.20;
        parameters.PROBABILITY_OLD = 0.10;
        parameters.PROBABILITY_CHILDREN = 0.10;
        parameters.PROBABILITY_BAGS = 0.80;
        parameters.ELITISM = 0.03;
        parameters.TOURNAMENT_SIZE = 3;
        parameters.SELECTION_POOL = 0.5;
        parameters.MUTATION = 0.2;
        parameters.NEW_SIMULATIONS = 0.3;
        parameters.SPLIT_PENALTY = 700;
        parameters.PAUSED = false;
        parameters.SKIP = false;
        parameters.END = false;
        parameters.STARTED = false;
        parameters.REDRAW = false;
        parameters.delay = 0.0;

        // update UI on the EDT so components are manipulated safely
        SwingUtilities.invokeLater(() -> {
            if (this.plane != null) {
            
                populate(parameters.plane);
            } else {
                refreshGAControls();
                updateGeneration(0, 1000, 0, 0);
                revalidate();
                repaint();
            }
        });
    }
}


