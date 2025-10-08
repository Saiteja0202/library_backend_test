package com.cts.library.controller;

import com.cts.library.service.FineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @PostMapping("/pay/{fineId}")
    public ResponseEntity<String> payFine(@PathVariable Long fineId) {
        fineService.payFine(fineId);
        return ResponseEntity.ok("Fine paid successfully.");
    }
}
