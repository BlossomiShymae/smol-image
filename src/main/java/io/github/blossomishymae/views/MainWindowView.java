package io.github.blossomishymae.views;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.blossomishymae.componentmodel.PropertyChangedEventArgs;
import io.github.blossomishymae.viewmodels.MainWindowViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
        JLabel scaleLabel = new JLabel("Scale down to ");
        JRadioButton widthButton = new JRadioButton("Width");
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
                viewModel.setWidth(tryParseOrDefault(dimensionField.getText(), viewModel.getWidth()));
                viewModel.setHeight(tryParseOrDefault(dimensionField.getText(), viewModel.getHeight()));
            }
        });
        JPanel dragDropPanel = new JPanel(new BorderLayout(10, 10));
        dragDropPanel.setBorder(BorderFactory.createTitledBorder("Drag and drop image"));
        dragDropPanel.setDropTarget(viewModel.getDropTarget());
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("");

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
        setIconImage(viewModel.getApplicationIcon());
        setSize(360,240);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static int tryParseOrDefault(final String string, final int defaultValue) {
        try {
            return Integer.parseInt(string, 10);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private void onPropertyChanged(PropertyChangedEventArgs propertyChangedEventArgs) {
        statusLabel.setText(viewModel.getStatusText());
    }
}
