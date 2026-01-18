package org.example.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/upload")
    public String upload() {
        return "forward:/upload.html";
    }

    @GetMapping("/editor")
    public String editor() {
        return "forward:/editor.html";
    }
}