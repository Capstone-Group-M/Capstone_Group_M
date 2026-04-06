package com.notam.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.notam.model.NOTAM;

@RestController
@RequestMapping("/notams")
public class NotamController {

    @GetMapping
    public List<NOTAM> getNotams(@RequestParam String icaoLocation) {
        // we'll hook up real FAA logic here next
        return List.of(new NOTAM("1", "A1234/26", icaoLocation));
    }
}