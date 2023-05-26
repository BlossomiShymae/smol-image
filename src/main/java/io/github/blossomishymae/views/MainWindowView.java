package io.github.blossomishymae.views;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.blossomishymae.componentmodel.PropertyChangedEventArgs;
import io.github.blossomishymae.viewmodels.MainWindowViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Singleton
public class MainWindowView extends JFrame {
    private final MainWindowViewModel viewModel;
    private final JLabel statusLabel;

    @Inject
    public MainWindowView(MainWindowViewModel viewModel) {
        super();
        this.viewModel = viewModel;
        viewModel.propertyChanged.subscribe(this::onPropertyChanged);

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel scalePanel = new JPanel(new BorderLayout());
        scalePanel.setBorder(BorderFactory.createTitledBorder("Scale down to..."));
        JRadioButton widthButton = new JRadioButton("Width");
        widthButton.setSelected(true);
        widthButton.addActionListener(e -> viewModel.setDimension(MainWindowViewModel.Dimension.WIDTH));
        JRadioButton heightButton = new JRadioButton("Height");
        heightButton.addActionListener(e -> viewModel.setDimension(MainWindowViewModel.Dimension.HEIGHT));
        ButtonGroup group = new ButtonGroup();
        group.add(widthButton);
        group.add(heightButton);

        JTextField dimensionField = new JTextField(5);
        dimensionField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                viewModel.setWidth(dimensionField.getText());
                viewModel.setHeight(dimensionField.getText());
            }
        });
        JPanel dragDropPanel = new JPanel(new BorderLayout(10, 10));
        dragDropPanel.setBorder(BorderFactory.createTitledBorder("Drag and drop image"));
        dragDropPanel.setDropTarget(viewModel.getDropTarget());
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.PAGE_AXIS));
        statusLabel = new JLabel(" ");
        JSeparator separator = new JSeparator();
        JLabel aboutLabel = new JLabel("© 2023 BlossomiShymae 🌸💔");
        JLabel repositoryLabel = new JLabel("GitHub Repository");
        repositoryLabel.setForeground(Color.BLUE.darker());
        repositoryLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        repositoryLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/BlossomiShymae/smol-image"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        });

        controlPanel.add(dimensionField);
        controlPanel.add(widthButton);
        controlPanel.add(heightButton);
        scalePanel.add(controlPanel);
        containerPanel.add(scalePanel, BorderLayout.NORTH);
        containerPanel.add(dragDropPanel, BorderLayout.CENTER);
        statusPanel.add(statusLabel);
        statusPanel.add(separator);
        statusPanel.add(aboutLabel);
        statusPanel.add(repositoryLabel);
        containerPanel.add(statusPanel, BorderLayout.SOUTH);

        add(containerPanel);
        setTitle("smol-image");
        setIconImage(viewModel.getApplicationIcon());
        setSize(360,240);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void onPropertyChanged(PropertyChangedEventArgs propertyChangedEventArgs) {
        statusLabel.setText(viewModel.getStatusText());
    }
}
