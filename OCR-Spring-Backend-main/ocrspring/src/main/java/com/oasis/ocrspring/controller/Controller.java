package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.annotations.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class Controller
{
    @Protected
    @GetMapping("/hello")
    public String hello()
    {
        return "this end point is protected";
    }

    @GetMapping("/world")
    public String world()
    {
        return "this end point is not protected";
    }
}
