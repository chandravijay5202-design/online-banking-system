package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.Branch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchResponse {
    private String code;
    private String branchName;
    private String city;
    private String state;
    private String ifscCode;
    private String micrCode;

    public static BranchResponse from(Branch branch) {
        return BranchResponse.builder()
                .code(branch.name())
                .branchName(branch.getBranchName())
                .city(branch.getCity())
                .state(branch.getState())
                .ifscCode(branch.getIfscCode())
                .micrCode(branch.getMicrCode())
                .build();
    }
}
