package com.luv2code.jobportal.controller;

import com.luv2code.jobportal.entity.*;
import com.luv2code.jobportal.services.JobPostActivityService;
import com.luv2code.jobportal.services.JobSeekerApplyService;
import com.luv2code.jobportal.services.JobSeekerSaveService;
import com.luv2code.jobportal.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Security;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
public class JobPostActivityController {

    private final UsersService usersService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    public JobPostActivityController(UsersService usersService, JobPostActivityService jobPostActivityService, JobSeekerApplyService jobSeekerApplyService, JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @GetMapping("/dashboard/")
    public String searchJobs(Model model,
                             @RequestParam(value = "job", required = false) String job,
                             @RequestParam(value = "location", required = false) String location,
                             @RequestParam(value = "partTime", required = false) String partTime,
                             @RequestParam(value = "fullTime", required = false) String fullTime,
                             @RequestParam(value = "freelance", required = false) String freelance,
                             @RequestParam(value = "internship", required = false) String internship,
                             @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                             @RequestParam(value = "officeOnly", required = false) String officeOnly,
                             @RequestParam(value = "partialRemote", required = false) String partialRemote,
                             @RequestParam(value = "today", required = false) String today,
                             @RequestParam(value = "days7", required = false) String days7,
                             @RequestParam(value = "days30", required = false) String days30
                             ){

        model.addAttribute("partTime", partTime != null);
        model.addAttribute("fullTime", fullTime != null);
        model.addAttribute("freelance", freelance != null);
        model.addAttribute("internship", internship != null);
        model.addAttribute("remoteOnly", remoteOnly != null);
        model.addAttribute("officeOnly", officeOnly != null);
        model.addAttribute("partialRemote", partialRemote != null);
        model.addAttribute("today", today != null);
        model.addAttribute("days7", days7 != null);
        model.addAttribute("days30", days30 != null);

        model.addAttribute("job", job);
        model.addAttribute("location", location);

        LocalDate searchDate = null;
        List<JobPostActivity> jobPost = null;
        if (days30 != null) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (days7 != null) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (today != null) {
            searchDate = LocalDate.now();
        }

        List<String> types = new ArrayList<>();
        if (partTime != null) types.add("Part-time");
        if (fullTime != null) types.add("Full-time");
        if (freelance != null) types.add("Freelance");
        if (internship != null) types.add("Internship");
        if (types.isEmpty()) {
            types = List.of("Part-time", "Full-time", "Freelance", "Internship");
        }

        List<String> remoteTypes = new ArrayList<>();
        if (remoteOnly != null) remoteTypes.add("Remote-Only");
        if (officeOnly != null) remoteTypes.add("Office-Only");
        if (partialRemote != null) remoteTypes.add("Partial-Remote");
        if (remoteTypes.isEmpty()) {
            remoteTypes = List.of("Remote-Only", "Office-Only", "Partial-Remote");
        }

        jobPost = jobPostActivityService.search(
                StringUtils.hasText(job) ? job.trim() : null,
                StringUtils.hasText(location) ? location.trim() : null,
                types, remoteTypes, searchDate);

        Object currentUserProfile = usersService.getCurrentUserProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)){
            String currentUsername = authentication.getName();
            model.addAttribute("username", currentUsername);
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))){
                List<RecruiterJobsDto> recruiterJobs = jobPostActivityService.getRecruiterJobs(((RecruiterProfile)currentUserProfile).getUserAccountId());
                model.addAttribute("jobPost",recruiterJobs);
            } else {
                List<JobSeekerApply> jobSeekerApplyList =jobSeekerApplyService.getCandidatesJobs((JobSeekerProfile) currentUserProfile);
                List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);

                boolean exist;
                boolean saved;

                for (JobPostActivity jobActivity : jobPost){
                    exist = false;
                    saved = false;
                    for (JobSeekerApply jobSeekerApply : jobSeekerApplyList){
                        if (Objects.equals(jobActivity.getJobPostId(), jobSeekerApply.getJob().getJobPostId())){
                            jobActivity.setIsActive(true);
                            exist = true;
                            break;
                        }
                    }

                    for (JobSeekerSave jobSeekerSave : jobSeekerSaveList){
                        if (Objects.equals(jobActivity.getJobPostId(), jobSeekerSave.getJob().getPostedById())){
                            jobActivity.setIsSaved(true);
                            break;
                        }
                    }
                    if (!exist){
                        jobActivity.setIsActive(false);
                    }
                    if (!saved){
                        jobActivity.setIsSaved(false);
                    }

                    model.addAttribute("jobPost", jobPost);
                }
            }
        }
        model.addAttribute("user", currentUserProfile);
        return "dashboard";
    }

    @GetMapping("global-search/")
    public String globalSearch(Model model,
                               @RequestParam(value = "job", required = false) String job,
                               @RequestParam(value = "location", required = false) String location,
                               @RequestParam(value = "partTime", required = false) String partTime,
                               @RequestParam(value = "fullTime", required = false) String fullTime,
                               @RequestParam(value = "freelance", required = false) String freelance,
                               @RequestParam(value = "internship", required = false) String internship,
                               @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                               @RequestParam(value = "officeOnly", required = false) String officeOnly,
                               @RequestParam(value = "partialRemote", required = false) String partialRemote,
                               @RequestParam(value = "today", required = false) String today,
                               @RequestParam(value = "days7", required = false) String days7,
                               @RequestParam(value = "days30", required = false) String days30){

        model.addAttribute("partTime", partTime != null);
        model.addAttribute("fullTime", fullTime != null);
        model.addAttribute("freelance", freelance != null);
        model.addAttribute("internship", internship != null);
        model.addAttribute("remoteOnly", remoteOnly != null);
        model.addAttribute("officeOnly", officeOnly != null);
        model.addAttribute("partialRemote", partialRemote != null);
        model.addAttribute("today", today != null);
        model.addAttribute("days7", days7 != null);
        model.addAttribute("days30", days30 != null);

        model.addAttribute("job", job);
        model.addAttribute("location", location);

        LocalDate searchDate = null;
        List<JobPostActivity> jobPost = null;
        if (days30 != null) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (days7 != null) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (today != null) {
            searchDate = LocalDate.now();
        }

        List<String> types = new ArrayList<>();
        if (partTime != null) types.add("Part-time");
        if (fullTime != null) types.add("Full-time");
        if (freelance != null) types.add("Freelance");
        if (internship != null) types.add("Internship");
        if (types.isEmpty()) {
            types = List.of("Part-time", "Full-time", "Freelance", "Internship");
        }

        List<String> remoteTypes = new ArrayList<>();
        if (remoteOnly != null) remoteTypes.add("Remote-Only");
        if (officeOnly != null) remoteTypes.add("Office-Only");
        if (partialRemote != null) remoteTypes.add("Partial-Remote");
        if (remoteTypes.isEmpty()) {
            remoteTypes = List.of("Remote-Only", "Office-Only", "Partial-Remote");
        }

        jobPost = jobPostActivityService.search(
                StringUtils.hasText(job) ? job.trim() : null,
                StringUtils.hasText(location) ? location.trim() : null,
                types, remoteTypes, searchDate);

        model.addAttribute("jobPost", jobPost);
        return "global-search";

    }

    @GetMapping("/dashboard/add")
    public String addJobs(Model model){
        model.addAttribute("jobPostActivity", new JobPostActivity());
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @PostMapping("/dashboard/addNew")
    public String addNew(JobPostActivity jobPostActivity,Model model){
        Users user = usersService.getCurrentUser();
        if (user != null){
            jobPostActivity.setPostedById(user);
        }
        jobPostActivity.setPostedDate(new Date());
        model.addAttribute("jobPostActivity", jobPostActivity);
        JobPostActivity saved = jobPostActivityService.addNew(jobPostActivity);
        return "redirect:/dashboard/";
    }

    @PostMapping("dashboard/edit/{id}")
    public String editJob(@PathVariable("id") int id, Model model){
        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
        model.addAttribute("jobPostActivity", jobPostActivity);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @PostMapping("/dashboard/deleteJob/{id}")
    public String deleteJob(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
        Users currentUser = usersService.getCurrentUser();
        if (currentUser == null ||
                !Objects.equals(
                        jobPostActivity.getPostedById() == null ? null :
                                jobPostActivity.getPostedById().getUserId(),
                        currentUser.getUserId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not allowed to delete this job.");
        }

        jobPostActivityService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Job deleted successfully.");
        return "redirect:/dashboard/";
    }
}
