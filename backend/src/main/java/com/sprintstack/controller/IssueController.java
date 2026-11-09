package com.sprintstack.controller;

import com.sprintstack.dto.issue.IssueRequest;
import com.sprintstack.dto.issue.IssueResponse;
import com.sprintstack.dto.issue.IssueStatusRequest;
import com.sprintstack.entity.IssueSeverity;
import com.sprintstack.entity.IssueStatus;
import com.sprintstack.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class IssueController {
    private final IssueService issues;
    public IssueController(IssueService issues){this.issues=issues;}

    @GetMapping("/api/projects/{projectId}/issues")
    public List<IssueResponse> list(Authentication authentication,@PathVariable Long projectId,
                                    @RequestParam(required=false) IssueStatus status,
                                    @RequestParam(required=false) IssueSeverity severity,
                                    @RequestParam(required=false) String q){
        return issues.list(authentication.getName(),projectId,status,severity,q);
    }
    @PostMapping("/api/projects/{projectId}/issues")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponse create(Authentication authentication,@PathVariable Long projectId,@Valid @RequestBody IssueRequest request){
        return issues.create(authentication.getName(),projectId,request);
    }
    @PutMapping("/api/issues/{issueId}")
    public IssueResponse update(Authentication authentication,@PathVariable Long issueId,@Valid @RequestBody IssueRequest request){
        return issues.update(authentication.getName(),issueId,request);
    }
    @PatchMapping("/api/issues/{issueId}/status")
    public IssueResponse changeStatus(Authentication authentication,@PathVariable Long issueId,@Valid @RequestBody IssueStatusRequest request){
        return issues.changeStatus(authentication.getName(),issueId,request.status());
    }
}
