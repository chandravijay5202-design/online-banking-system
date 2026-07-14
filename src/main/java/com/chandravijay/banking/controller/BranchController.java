package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.BranchResponse;
import com.chandravijay.banking.entity.Branch;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    @GetMapping
    public ResponseEntity<List<BranchResponse>> listBranches() {
        List<BranchResponse> branches = Arrays.stream(Branch.values())
                .map(BranchResponse::from)
                .toList();
        return ResponseEntity.ok(branches);
    }
}
