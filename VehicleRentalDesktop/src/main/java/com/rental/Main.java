package com.rental;

import com.formdev.flatlaf.FlatLightLaf;
import com.rental.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point of the desktop application.
 * Run with:  java -jar target/VehicleRentalDesktop.jar
 */
public class Main {

    public static void main(String[] args) {
        // modern look and feel for Swing (one line, from the FlatLaf library)
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);

        // Swing components must be created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
