package io.github.blossomishymae.views;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.blossomishymae.viewmodels.MainWindowViewModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

@Singleton
public class MainWindowView extends JFrame {
    private final MainWindowViewModel viewModel;

    @Inject
    public MainWindowView(MainWindowViewModel viewModel) {
        super();
        this.viewModel = viewModel;

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel scaleLabel = new JLabel("Scale down to ");
        JRadioButton widthButton = new JRadioButton("Width");
        JRadioButton heightButton = new JRadioButton("Height");
        ButtonGroup group = new ButtonGroup();
        group.add(widthButton);
        group.add(heightButton);
        JTextField dimensionField = new JTextField(5);
        JPanel dragDropPanel = new JPanel(new BorderLayout(10, 10));
        dragDropPanel.setBorder(BorderFactory.createTitledBorder("Drag and drop image"));
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("");

        controlPanel.add(scaleLabel);
        controlPanel.add(widthButton);
        controlPanel.add(heightButton);
        controlPanel.add(dimensionField);
        containerPanel.add(controlPanel, BorderLayout.NORTH);
        containerPanel.add(dragDropPanel, BorderLayout.CENTER);
        statusPanel.add(statusLabel);
        containerPanel.add(statusPanel, BorderLayout.SOUTH);

        add(containerPanel);
        setTitle("smol-image");
        setSize(800,600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
