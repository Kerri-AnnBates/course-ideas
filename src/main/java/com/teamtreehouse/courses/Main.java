package com.teamtreehouse.courses;

import com.teamtreehouse.courses.model.CourseIdea;
import com.teamtreehouse.courses.model.CourseIdeaDAO;
import com.teamtreehouse.courses.model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        staticFileLocation("/public"); // Link the css
        CourseIdeaDAO dao = new SimpleCourseIdeaDAO();

        // Middleware: Make cookie available via request attribute. Do for all paths.
        before((req, res) -> {
            if(req.cookie("username") != null) {
                req.attribute("username", req.cookie("username"));
            }
        });

        // Middleware: check if username cookie is set on the ideas path.
        before("/ideas", (req, res) -> {
            //TODO: send message about redirect.
            if(req.attribute("username") == null) {
                res.redirect("/");
                halt();
            }
        });

        // Get and display the home page.
        get("/", (req, res) -> {
            Map<String, String> model = new HashMap<>();
            model.put("username", req.attribute("username"));

            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        // Sign someone in.
        post("/sign-in", (req, res) -> {
            Map<String, String> model = new HashMap<>();
            String username = req.queryParams("username");
            res.cookie("username", username); // set the cookie
            model.put("username", username);
            res.redirect("/");

            return null;
        });

        // List all ideas
        get("/ideas", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("ideas", dao.findAll());

            return new ModelAndView(model, "ideas.hbs");
        }, new HandlebarsTemplateEngine());

        // Get single idea details.
        get("/ideas/:slug", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            CourseIdea idea = dao.findBySlug(req.params("slug"));
            model.put("idea", idea);

            return new ModelAndView(model, "idea.hbs");
        }, new HandlebarsTemplateEngine());

        // Add an idea
        post("/ideas", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String title = req.queryParams("title");
            CourseIdea courseIdea = new CourseIdea(title, req.attribute("username"));
            dao.add(courseIdea);
            res.redirect("/ideas");

            return null;
        }, new HandlebarsTemplateEngine());

        // Add a vote count to an idea.
        post("/ideas/:slug/vote", (req, res) -> {
            CourseIdea idea = dao.findBySlug(req.params("slug"));
            idea.addVoter(req.attribute("username"));
            res.redirect("/ideas");
            return null;
        });
    }
}
