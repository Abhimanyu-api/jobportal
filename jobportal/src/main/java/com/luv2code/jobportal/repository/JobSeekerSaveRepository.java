
package com.luv2code.jobportal.repository;

import com.luv2code.jobportal.entity.JobPostActivity;
import com.luv2code.jobportal.entity.JobSeekerProfile;
import com.luv2code.jobportal.entity.JobSeekerSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobSeekerSaveRepository
        extends JpaRepository<JobSeekerSave, Integer> {

    List<JobSeekerSave> findByUserId(JobSeekerProfile userId);

    List<JobSeekerSave> findByJob(JobPostActivity job);

    boolean existsByUserIdAndJob(
            JobSeekerProfile userId,
            JobPostActivity job
    );

    @Modifying
    @Query("DELETE FROM JobSeekerSave savedJob WHERE savedJob.job = :job")
    void deleteAllByJob(@Param("job") JobPostActivity job);
}