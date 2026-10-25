package com.bomb;

import com.bomb.server.GameService;
import com.bomb.ui.Launcher;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.SwingUtilities;

@SpringBootApplication
@ServletComponentScan
public class BombApp {

    private static ConfigurableApplicationContext server;

    public static void main(String[] args) {
        // The app always opens the start screen first.
        // Spring Boot only starts if this laptop is the technician.
        SwingUtilities.invokeLater(() -> new Launcher().setVisible(true));
    }

    // Starts the Spring Boot server (embedded Tomcat) once and returns the game service
    public static synchronized GameService startServer() {
        if (server == null) {
            server = new SpringApplicationBuilder(BombApp.class).headless(false).run();
        }
        return server.getBean(GameService.class);
    }
}
