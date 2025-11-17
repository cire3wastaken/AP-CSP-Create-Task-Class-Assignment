/*
 * Copyright (c) 2025 cire3. All Rights Reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package me.cire3.apcsp.create;

import com.formdev.flatlaf.util.SystemInfo;
import me.cire3.apcsp.create.noise.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Window extends JFrame {
    private static final Dimension FRAME_SIZE = new Dimension(1100, 700);

    private File selectedFile;
    private final List<WeightedGenerator> generators = new ArrayList<>();

    public Window(String windowTitle) {
        super(windowTitle);
        final Dimension screenSize = getToolkit().getScreenSize();

        setSize(FRAME_SIZE);
        setResizable(false);
        setLocation((screenSize.width / 2) - (FRAME_SIZE.width / 2),
                (screenSize.height / 2) - (FRAME_SIZE.height / 2));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        if (SystemInfo.isMacFullWindowContentSupported) {
            getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
            getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
            getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
        }

        final JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(new Color(0,0,0,0));

        BufferedImage tempBgImage = null;
        try {
            tempBgImage = ImageIO.read(getClass().getResourceAsStream("/background.jpg"));
        } catch (Exception e) {
        }
        final BufferedImage bgImage = tempBgImage;

        final JPanel bgPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
                    g2.dispose();
                }
            }
        };
//        bgPanel.setMaximumSize(new Dimension(FRAME_SIZE.width, FRAME_SIZE.height));
        bgPanel.setLayout(new BoxLayout(bgPanel, BoxLayout.Y_AXIS));
        bgPanel.setOpaque(true);
        bgPanel.setPreferredSize(FRAME_SIZE);
        panel.add(bgPanel);

        final JPanel selectFilePanel = new JPanel();
        selectFilePanel.setLayout(new BoxLayout(selectFilePanel, BoxLayout.X_AXIS));
        selectFilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectFilePanel.setOpaque(false);
        bgPanel.add(selectFilePanel);

        final JButton loadFileButton = new JButton("Choose Image");
        loadFileButton.setFont(Main.NUNITO_FONT_20);
        loadFileButton.setAlignmentY(0.05F);
        loadFileButton.setMaximumSize(new Dimension(200, 75));
        loadFileButton.setOpaque(false);
        loadFileButton.addActionListener((e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                            name.endsWith(".png") || name.endsWith(".gif");
                }

                @Override
                public String getDescription() {
                    return "Image Files (*.jpg, *.jpeg, *.png, *.gif)";
                }
            });

            int result = chooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();

                System.out.println("Selected file: " + selectedFile);
            }
        });
        selectFilePanel.add(loadFileButton);

        JPanel generatorPanel = new JPanel();
        generatorPanel.setOpaque(false);
        generatorPanel.setLayout(new BoxLayout(generatorPanel, BoxLayout.Y_AXIS));
        generatorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel genLabel = new JLabel("Noise Generators:");
        genLabel.setFont(Main.NUNITO_FONT_20);
        genLabel.setForeground(Color.BLACK);
        genLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        genLabel.setOpaque(false);
        generatorPanel.add(genLabel);

        generatorPanel.add(Box.createVerticalStrut(10));

        JPanel generatorContainer = new JPanel();
        generatorContainer.setOpaque(false);
        generatorContainer.setLayout(new BoxLayout(generatorContainer, BoxLayout.Y_AXIS));
        generatorPanel.add(generatorContainer);

        JButton addGeneratorButton = new JButton("Add Generator");
        addGeneratorButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addGeneratorButton.setMaximumSize(new Dimension(200, 40));
        generatorPanel.add(addGeneratorButton);
        generatorPanel.add(Box.createVerticalStrut(15));

        addGeneratorButton.addActionListener(e -> {
            showAddGeneratorDialog(generatorContainer, scrollPane);
            generatorPanel.revalidate();
            generatorPanel.repaint();
        });

        bgPanel.add(generatorPanel);

        final JPanel launchAlignmentPanel = new JPanel();
        launchAlignmentPanel.setLayout(new BoxLayout(launchAlignmentPanel, BoxLayout.X_AXIS));
        launchAlignmentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        launchAlignmentPanel.setMaximumSize(new Dimension(150, FRAME_SIZE.height));
        launchAlignmentPanel.setOpaque(false);
        bgPanel.add(launchAlignmentPanel);

        final JButton launchButton = new JButton("Apply Noise");
        launchButton.setFont(Main.NUNITO_FONT_20);
        launchButton.setAlignmentY(0.85F);
        launchButton.setMaximumSize(new Dimension(150, 75));
        launchButton.setBackground(new Color(63, 199, 63));
        launchButton.setOpaque(false);
        launchButton.addActionListener((e) -> {
            (new Processor(selectedFile, generators)).run();
        });
        launchAlignmentPanel.add(launchButton);

        setContentPane(scrollPane);
    }

    private void showAddGeneratorDialog(JPanel container, JScrollPane scrollPane) {
        String[] choices = {
                "Gaussian Noise",
                "Perlin Noise",
                "Salt & Pepper Noise",
                "Value Noise",
                "White Noise"
        };

        String result = (String) JOptionPane.showInputDialog(
                this,
                "Select noise type:",
                "Add Generator",
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                choices[0]
        );

        if (result == null) return;

        JPanel panel = createGeneratorPanel(container, result);
        container.add(panel);
        container.revalidate();
        container.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private JPanel createGeneratorPanel(JPanel generatorContainer, String type) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setForeground(Color.BLACK);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel title = new JLabel(type);
        title.setFont(Main.NUNITO_FONT_12);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.BLACK);
        title.setBackground(Color.GRAY);
        p.add(title);

        JButton removeBtn = new JButton("Remove");
        removeBtn.setMaximumSize(new Dimension(100, 30));
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(removeBtn);

        JSlider weight = new JSlider(0, 100, 100);
        weight.setOpaque(false);
        weight.setMaximumSize(new Dimension(300, 40));
        weight.setForeground(Color.BLACK);
        weight.setAlignmentX(Component.CENTER_ALIGNMENT);
        weight.setToolTipText("Weight");
        weight.setFont(Main.NUNITO_FONT_12);

        JLabel weightLabel = new JLabel("Weight");
        weightLabel.setFont(Main.NUNITO_FONT_12);
        weightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        weightLabel.setForeground(Color.BLACK);

        p.add(weightLabel);
        p.add(weight);

        NoiseGenerator gen = switch (type) {
            case "Gaussian Noise" -> new GaussianNoiseGenerator(0, 1);
            case "Perlin Noise" -> new PerlinNoiseGenerator(1F);
            case "Salt & Pepper Noise" -> new SaltAndPepperNoiseGenerator(0.2F);
            case "Value Noise" -> new ValueNoiseGenerator(256, 256, 0.2F);
            default -> new WhiteNoiseGenerator();
        };

        WeightedGenerator wg = new WeightedGenerator(gen, 1f);
        generators.add(wg);

        removeBtn.addActionListener(e -> {
            generators.remove(wg);
            generatorContainer.remove(p);
            generatorContainer.revalidate();
            generatorContainer.repaint();
        });

        weight.addChangeListener(e -> {
            wg.setWeight(weight.getValue() / 100f);
        });

        switch (type) {
            case "Gaussian Noise" -> addGaussianParams(p, (GaussianNoiseGenerator) gen);
            case "Perlin Noise" -> addScaleParam(p, gen, "Scale");
            case "Salt & Pepper Noise" -> addScaleParam(p, gen, "Probability");
            case "Value Noise" -> addScaleParam(p, gen, "Scale");
            default -> {}
        }

        return p;
    }

    private void addGaussianParams(JPanel panel, GaussianNoiseGenerator g) {
        JSlider meanSlider = new JSlider(-100, 100, 0);
        meanSlider.setMaximumSize(new Dimension(300, 40));
        meanSlider.setForeground(Color.BLACK);
        meanSlider.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel muLabel = new JLabel("Mu:");
        muLabel.setFont(Main.NUNITO_FONT_12);
        muLabel.setForeground(Color.BLACK);
        muLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(muLabel);
        panel.add(meanSlider);

        meanSlider.addChangeListener(e ->
                g.setMu(meanSlider.getValue() / 10f)
        );

        JSlider stdSlider = new JSlider(1, 300, 100);
        stdSlider.setMaximumSize(new Dimension(300, 40));
        stdSlider.setForeground(Color.BLACK);
        stdSlider.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sigmaLabel = new JLabel("Sigma:");
        sigmaLabel.setFont(Main.NUNITO_FONT_12);
        sigmaLabel.setForeground(Color.BLACK);
        sigmaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(sigmaLabel);
        panel.add(stdSlider);

        stdSlider.addChangeListener(e ->
                g.setSigma(stdSlider.getValue() / 100f)
        );
    }

    private void addScaleParam(JPanel panel, NoiseGenerator gen, String name) {
        JSlider slider = new JSlider(1, 200, 10);
        slider.setMaximumSize(new Dimension(300, 40));
        slider.setToolTipText(name);
        slider.setForeground(Color.BLACK);
        slider.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scaleLabel = new JLabel(name + ":");
        scaleLabel.setFont(Main.NUNITO_FONT_12);
        scaleLabel.setForeground(Color.BLACK);
        scaleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(scaleLabel);
        panel.add(slider);

        slider.addChangeListener(e -> {
            if (gen instanceof PerlinNoiseGenerator p) p.setScale(slider.getValue() / 100f);
            if (gen instanceof ValueNoiseGenerator v) v.setScale(slider.getValue() / 100f);
            if (gen instanceof SaltAndPepperNoiseGenerator s) s.setProbability(slider.getValue() / 200f);
        });
    }
}
