
package com.luv2code.jobportal.services;

import com.luv2code.jobportal.entity.JobPostActivity;
import com.luv2code.jobportal.entity.JobSeekerProfile;
import com.luv2code.jobportal.entity.JobSeekerSave;
import com.luv2code.jobportal.repository.JobSeekerSaveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobSeekerSaveService {

    private final JobSeekerSaveRepository jobSeekerSaveRepository;

    public JobSeekerSaveService(
            JobSeekerSaveRepository jobSeekerSaveRepository) {
        this.jobSeekerSaveRepository = jobSeekerSaveRepository;
    }

    public List<JobSeekerSave> getCandidatesJob(
            JobSeekerProfile userAccountId) {
        return jobSeekerSaveRepository.findByUserId(userAccountId);
    }

    public List<JobSeekerSave> getJobCandidates(
            JobPostActivity job) {
        return jobSeekerSaveRepository.findByJob(job);
    }

    @Transactional
    public void addNew(JobSeekerSave jobSeekerSave) {

        if (jobSeekerSave.getUserId() == null
                || jobSeekerSave.getJob() == null) {
            throw new IllegalArgumentException(
                    "User profile and job are required"
            );
        }

        boolean alreadySaved =
                jobSeekerSaveRepository.existsByUserIdAndJob(
                        jobSeekerSave.getUserId(),
                        jobSeekerSave.getJob()
                );

        if (!alreadySaved) {
            // A new saved-job record must have no assigned ID.
            jobSeekerSave.setId(null);
            jobSeekerSaveRepository.save(jobSeekerSave);
        }
    }
}
