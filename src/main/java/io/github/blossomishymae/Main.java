package io.github.blossomishymae;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.blossomishymae.configuration.ApplicationInjectorConfiguration;
import io.github.blossomishymae.views.MainWindowView;
import org.pushingpixels.radiance.theming.api.skin.RadianceModerateLookAndFeel;
import org.pushingpixels.radiance.theming.api.skin.RadianceNightShadeLookAndFeel;

import javax.swing.*;

public class Main implements Runnable {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Main());
    }

    @Override
    public void run() {
        try {
            UIManager.setLookAndFeel(new RadianceModerateLookAndFeel());
        } catch (Exception exception) {
            exception.printStackTrace(System.out);
        }
        Injector injector = Guice.createInjector(new ApplicationInjectorConfiguration());
        JFrame view = injector.getInstance(MainWindowView.class);
    }
}