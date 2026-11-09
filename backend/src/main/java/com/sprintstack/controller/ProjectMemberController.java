package com.sprintstack.controller;

import com.sprintstack.dto.member.*;
import com.sprintstack.service.ProjectMemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {
    private final ProjectMemberService members;
    public ProjectMemberController(ProjectMemberService members){this.members=members;}
    @GetMapping public List<MemberResponse> list(Authentication auth,@PathVariable Long projectId){return members.list(auth.getName(),projectId);}
    @GetMapping("/search") public List<UserLookupResponse> search(Authentication auth,@PathVariable Long projectId,@RequestParam String q){return members.searchAvailable(auth.getName(),projectId,q);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MemberResponse add(Authentication auth,@PathVariable Long projectId,@Valid @RequestBody MemberRequest request){return members.add(auth.getName(),projectId,request);}
    @PutMapping("/{userId}") public MemberResponse changeRole(Authentication auth,@PathVariable Long projectId,@PathVariable Long userId,@Valid @RequestBody MemberRoleRequest request){return members.changeRole(auth.getName(),projectId,userId,request);}
    @PatchMapping("/{userId}/owner") public MemberResponse transferOwnership(Authentication auth,@PathVariable Long projectId,@PathVariable Long userId){return members.transferOwnership(auth.getName(),projectId,userId);}
    @DeleteMapping("/{userId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void remove(Authentication auth,@PathVariable Long projectId,@PathVariable Long userId){members.remove(auth.getName(),projectId,userId);}
}
