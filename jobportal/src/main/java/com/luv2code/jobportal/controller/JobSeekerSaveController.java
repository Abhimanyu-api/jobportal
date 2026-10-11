
package com.luv2code.jobportal.controller;

import com.luv2code.jobportal.entity.JobPostActivity;
import com.luv2code.jobportal.entity.JobSeekerProfile;
import com.luv2code.jobportal.entity.JobSeekerSave;
import com.luv2code.jobportal.entity.Users;
import com.luv2code.jobportal.services.JobPostActivityService;
import com.luv2code.jobportal.services.JobSeekerProfileService;
import com.luv2code.jobportal.services.JobSeekerSaveService;
import com.luv2code.jobportal.services.UsersService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class JobSeekerSaveController {

    private final UsersService usersService;
    private final JobSeekerProfileService jobSeekerProfileService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerSaveService jobSeekerSaveService;

    public JobSeekerSaveController(
            UsersService usersService,
            JobSeekerProfileService jobSeekerProfileService,
            JobPostActivityService jobPostActivityService,
            JobSeekerSaveService jobSeekerSaveService) {

        this.usersService = usersService;
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @PostMapping("job-details/save/{id}")
    public String save(@PathVariable("id") int id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String currentUsername = authentication.getName();

        Users users = usersService.findByEmail(currentUsername);

        if (users == null) {
            throw new RuntimeException("User not found");
        }

        Optional<JobSeekerProfile> seekerProfile =
                jobSeekerProfileService.getOne(users.getUserId());

        JobPostActivity jobPostActivity =
                jobPostActivityService.getOne(id);

        if (seekerProfile.isEmpty() || jobPostActivity == null) {
            throw new RuntimeException(
                    "User profile or job not found"
            );
        }

        // Create a new entity instead of reusing form-submitted data.
        JobSeekerSave jobSeekerSave = new JobSeekerSave();
        jobSeekerSave.setUserId(seekerProfile.get());
        jobSeekerSave.setJob(jobPostActivity);

        jobSeekerSaveService.addNew(jobSeekerSave);

        return "redirect:/dashboard/";
    }

    @GetMapping("saved-jobs/")
    public String savedJobs(Model model) {

        List<JobPostActivity> jobPost = new ArrayList<>();

        Object currentUserProfile =
                usersService.getCurrentUserProfile();

        List<JobSeekerSave> jobSeekerSaveList =
                jobSeekerSaveService.getCandidatesJob(
                        (JobSeekerProfile) currentUserProfile
                );

        for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
            jobPost.add(jobSeekerSave.getJob());
        }

        model.addAttribute("jobPost", jobPost);
        model.addAttribute("user", currentUserProfile);

        return "saved-jobs";
    }
}