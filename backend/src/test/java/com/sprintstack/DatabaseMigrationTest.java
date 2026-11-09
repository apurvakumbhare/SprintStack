package com.sprintstack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import com.sprintstack.dto.project.ProjectRequest;
import com.sprintstack.dto.task.TaskRequest;
import com.sprintstack.dto.issue.IssueRequest;
import com.sprintstack.dto.comment.CommentRequest;
import com.sprintstack.dto.member.MemberRequest;
import com.sprintstack.entity.Priority;
import com.sprintstack.entity.ProjectRole;
import com.sprintstack.entity.IssueStatus;
import com.sprintstack.entity.TaskStatus;
import com.sprintstack.entity.User;
import com.sprintstack.repository.NotificationRepository;
import com.sprintstack.repository.UserRepository;
import com.sprintstack.service.ProjectService;
import com.sprintstack.service.TaskService;
import com.sprintstack.service.DashboardService;
import com.sprintstack.service.ProjectAnalyticsService;
import com.sprintstack.service.ProjectMemberService;
import com.sprintstack.service.IssueService;
import com.sprintstack.service.CommentService;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sprintstack-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "app.jwt.secret=test-only-secret-that-is-long-enough-to-sign-hmac-tokens",
        "app.jwt.expiration-ms=60000"
})
class DatabaseMigrationTest {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository users;
    @Autowired private ProjectService projects;
    @Autowired private TaskService tasks;
    @Autowired private DashboardService dashboard;
    @Autowired private ProjectAnalyticsService analytics;
    @Autowired private ProjectMemberService projectMembers;
    @Autowired private IssueService issues;
    @Autowired private CommentService comments;
    @Autowired private NotificationRepository notifications;

    @Test
    void migrationsCreateTheCoreTablesAndVersionHistory() {
        Integer users = jdbc.queryForObject("select count(*) from information_schema.tables where table_schema='PUBLIC' and table_name = 'USERS'", Integer.class);
        Integer tasks = jdbc.queryForObject("select count(*) from information_schema.tables where table_schema='PUBLIC' and table_name = 'TASKS'", Integer.class);
        Integer migrations = jdbc.queryForObject("select count(*) from \"flyway_schema_history\" where \"success\" = true", Integer.class);
        assertThat(users).isEqualTo(1);
        assertThat(tasks).isEqualTo(1);
        assertThat(migrations).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Transactional
    void projectPermissionsTaskStatusAndDashboardUsePersistedRecords() {
        User owner=users.saveAndFlush(new User("Owner User","owner@example.test","already-hashed-password"));
        User outsider=users.saveAndFlush(new User("Outside User","outside@example.test","already-hashed-password"));
        var project=projects.create(owner.getEmail(),new ProjectRequest("Release plan","Initial project",Priority.HIGH,null,null));
        assertThat(project.memberCount()).isEqualTo(1);
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                ()->projects.get(outsider.getEmail(),project.id())).getStatusCode().value()).isEqualTo(403);
        var task=tasks.create(owner.getEmail(),project.id(),new TaskRequest("Ship feature","Work item",null,null,null,null,null,null,List.of("backend")));
        tasks.changeStatus(owner.getEmail(),task.id(),TaskStatus.COMPLETED);
        tasks.create(owner.getEmail(),project.id(),new TaskRequest("Review docs",null,owner.getId(),null,null,null,null,null,List.of()));
        var summary=dashboard.summary(owner.getEmail());
        assertThat(summary.totalProjects()).isEqualTo(1);
        assertThat(summary.totalTasks()).isEqualTo(2);
        assertThat(summary.completedTasks()).isEqualTo(1);
        assertThat(summary.taskStatusCounts().get("COMPLETED")).isEqualTo(1L);
        assertThat(summary.projectProgress().getFirst().completionPercent()).isEqualTo(50);
        assertThat(summary.recentActivity()).extracting("projectName").containsOnly("Release plan");
        assertThat(task.labels()).containsExactly("backend");
        var projectSummary=analytics.summary(owner.getEmail(),project.id());
        assertThat(projectSummary.totalTasks()).isEqualTo(2);
        assertThat(projectSummary.pendingTasks()).isEqualTo(1);
        assertThat(projectSummary.completionPercent()).isEqualTo(50);
        assertThat(projectSummary.tasksByPriority().get("MEDIUM")).isEqualTo(2L);
        assertThat(projectSummary.teamWorkload().getFirst().openAssignedTasks()).isEqualTo(1L);
    }

    @Test
    @Transactional
    void issueAssignmentResolutionCommentsAndNotificationsRespectMembership() {
        User owner=users.saveAndFlush(new User("Issue Owner","issue-owner@example.test","already-hashed-password"));
        User developer=users.saveAndFlush(new User("Issue Developer","issue-developer@example.test","already-hashed-password"));
        var project=projects.create(owner.getEmail(),new ProjectRequest("Issue flow","Issue integration",Priority.MEDIUM,null,null));
        projectMembers.add(owner.getEmail(),project.id(),new MemberRequest(developer.getEmail(),ProjectRole.DEVELOPER));

        var issue=issues.create(owner.getEmail(),project.id(),new IssueRequest("Login fails","Steps to reproduce",developer.getId(),
                com.sprintstack.entity.IssueSeverity.HIGH,com.sprintstack.entity.TaskPriority.HIGH,null));
        assertThat(notifications.countByUser_IdAndReadFalse(developer.getId())).isGreaterThanOrEqualTo(2);

        var comment=comments.addIssueComment(developer.getEmail(),issue.id(),new CommentRequest("I can reproduce this."));
        var denied=org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                ()->comments.editIssueComment(owner.getEmail(),issue.id(),comment.id(),new CommentRequest("Changed by another member")));
        assertThat(denied.getStatusCode().value()).isEqualTo(403);
        assertThat(comments.editIssueComment(developer.getEmail(),issue.id(),comment.id(),new CommentRequest("Confirmed on latest build.")).body())
                .isEqualTo("Confirmed on latest build.");

        issues.changeStatus(developer.getEmail(),issue.id(),IssueStatus.RESOLVED);
        assertThat(notifications.countByUser_IdAndReadFalse(owner.getId())).isGreaterThanOrEqualTo(1);
        assertThat(issues.list(owner.getEmail(),project.id(),IssueStatus.RESOLVED,null,null)).hasSize(1);
    }
}
