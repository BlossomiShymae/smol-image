package io.github.blossomishymae.views;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.blossomishymae.viewmodels.MainWindowViewModel;

import javax.swing.*;

@Singleton
public class MainWindowView extends JFrame {
    private final MainWindowViewModel viewModel;

    @Inject
    public MainWindowView(MainWindowViewModel viewModel) {
        super();
        this.viewModel = viewModel;

        setTitle("smol-image");
        setSize(800,600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
