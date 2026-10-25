package com.bomb.server;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// A plain Java servlet running on Spring Boot's Tomcat.
// Open http://<technician-ip>:8080/ping in a browser to check the connection.
@WebServlet("/ping")
public class PingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().println("Bomb server is running");
    }
}
