package org.fadhel.tumoohplatform.service;

import jakarta.mail.*;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.ReceivedDateTerm;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.ai.EmailClassification;
import org.fadhel.tumoohplatform.model.*;
import org.fadhel.tumoohplatform.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class GmailSyncService {

    private final GmailConnectionRepository gmailConnectionRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewRepository interviewRepository;
    private final GeminiService geminiService;
    private final WhatsAppService whatsAppService;
    private final ObjectMapper objectMapper;

    private static final int MAX_EMAILS = 20;

    //Runs automatically every day at 11:59PM riyadh
    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Riyadh")
    public void syncAllUsers() {
        List<GmailConnection> connections = gmailConnectionRepository.findAll();
        for (GmailConnection connection : connections) {
            try {
                syncConnection(connection);
            } catch (Exception e) {
                //If one user fails check the others
                System.out.println("Gmail sync failed for " + connection.getGmailAddress() + ": " + e.getMessage());
            }
        }
    }

    //this is for manual endpoint >> just to try the endpoint anytime
    public String syncUser(Long userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        GmailConnection connection = gmailConnectionRepository.findGmailConnectionByUser(user);
        if (connection == null) {
            throw new ApiException("Please connect your Gmail first");
        }
        return syncConnection(connection);
    }

    private String syncConnection(GmailConnection connection) {
        User user = connection.getUser();
        LocalDateTime syncStartedAt = LocalDateTime.now();
        LocalDateTime lastSynced = (connection.getLastSyncedAt() != null)
                ? connection.getLastSyncedAt()
                : syncStartedAt.minusDays(1);
        Date since = Date.from(lastSynced.atZone(ZoneId.systemDefault()).toInstant());

        int checked = 0;
        int added = 0;
        int updated = 0;

        Store store = null;
        try {
            //log in to Gmail and open the inbox (read only)
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props);
            store = session.getStore("imaps");
            store.connect("imap.gmail.com", connection.getGmailAddress(), connection.getAppPassword());

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            //get new emails received since the last sync
            Message[] messages = inbox.search(new ReceivedDateTerm(ComparisonTerm.GE, since));

            //the new MAX_EMAILS emails
            int start = Math.max(0, messages.length - MAX_EMAILS);
            for (int i = start; i < messages.length; i++) {
                Message message = messages[i];

                //searches by day only, so we check the exact time here
                if (message.getReceivedDate() == null || !message.getReceivedDate().after(since)) {
                    continue;
                }
                checked++;

                try {
                    //Ask Gemini what this email is about
                    EmailClassification result = classifyEmail(message);
                    if (result == null || result.getJobRelated() == null || !result.getJobRelated()) {
                        continue;
                    }

                    //Apply the result
                    if ("Applied".equals(result.getType())) {
                        if (addApplicationFromEmail(user, result)) {
                            added++;
                        }
                    } else {
                        if (updateApplicationFromEmail(user, result)) {
                            updated++;
                        }
                    }
                } catch (Exception e) {
                    //If one email fails check next
                    System.out.println("Could not process email: " + e.getMessage());
                }
            }
            inbox.close(false);
        } catch (MessagingException e) {
            throw new ApiException("Could not read Gmail. Please reconnect your Gmail");
        } finally {
            try {
                if (store != null) store.close();
            } catch (MessagingException ignored) {
            }
        }

        //Save the sync time, so next time we only read newer emails
        connection.setLastSyncedAt(syncStartedAt);
        gmailConnectionRepository.save(connection);

        return "Checked " + checked + " emails: " + added + " applications added, " + updated + " applications updated";
    }

    private EmailClassification classifyEmail(Message message) throws Exception {
        String from = (message.getFrom() != null && message.getFrom().length > 0) ? message.getFrom()[0].toString() : "Unknown";
        String subject = (message.getSubject() != null) ? message.getSubject() : "";
        String body = getText(message);
        if (body.length() > 2000) {
            body = body.substring(0, 2000);
        }

        String prompt = String.format("""
            You are an assistant that reads emails for a job seeker.
            Decide if this email is about one of the candidate's job applications, and extract the details.

            Email received at: %s
            From: %s
            Subject: %s
            Body:
            %s

            Rules for "type":
            - "Applied": the company confirms they received the application
            - "Interview": the company invites the candidate to an interview
            - "InProgress": the application moved forward (assessment, under review, next stage) without an interview invitation
            - "Offered": the company offers the job
            - "Rejected": the company rejects the candidate
            Newsletters, job alerts, ads and anything not about the candidate's own application are NOT job related.
            "interviewDate" is only for "Interview" and only if a clear date and time are written, in format yyyy-MM-ddTHH:mm, otherwise null.

            Strictly return ONLY a valid JSON object matching this exact schema (no markdown formatting, no text outside JSON):
            {
              "jobRelated": true,
              "type": "Applied",
              "companyName": "Company name",
              "position": "Job title, or null if not mentioned",
              "interviewDate": null
            }
            """, message.getReceivedDate(), from, subject, body);

        String rawAiOutput = geminiService.generateText(prompt);
        String cleanJson = rawAiOutput.replaceAll("```json|```", "").trim();
        return objectMapper.readValue(cleanJson, EmailClassification.class);
    }

    //new application from a confirmation email
    private boolean addApplicationFromEmail(User user, EmailClassification result) {
        if (result.getCompanyName() == null || result.getCompanyName().isBlank()) {
            return false;
        }
        String companyName = result.getCompanyName().trim();
        String position = (result.getPosition() == null || result.getPosition().isBlank())
                ? "Unknown position"
                : result.getPosition().trim();

        Company company = companyRepository.findByAnyName(companyName);
        if (company == null) {
            company = new Company();
            company.setNameEn(companyName);
            company.setIndustryEn("Other");
            company.setCompanyLogoUrl("/images/logo-placeholder.png");
            company.setVerified(false);
            companyRepository.save(company);
        }

        //skip if the user already has an active application for the same position at this company
        List<JobApplication> existing = jobApplicationRepository.findJobApplicationsByUserAndJob_Company(user, company);
        for (JobApplication application : existing) {
            boolean samePosition = application.getJob().getPosition().equalsIgnoreCase(position);
            boolean active = !application.getStatus().equals("Rejected") && !application.getStatus().equals("Withdrawn");
            if (samePosition && active) {
                return false;
            }
        }

        String description = "Added from Gmail";
        Job job = jobRepository.findJobByCompanyAndPositionIgnoreCaseAndDescriptionIgnoreCase(company, position, description);
        if (job == null) {
            job = new Job();
            job.setCompany(company);
            job.setPosition(position);
            job.setDescription(description);
            jobRepository.save(job);
        }

        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setJob(job);
        application.setStatus("Applied");
        application.setCreatedAt(LocalDate.now());
        jobApplicationRepository.save(application);
        return true;
    }

    //update the status of an existing application
    private boolean updateApplicationFromEmail(User user, EmailClassification result) {
        if (result.getCompanyName() == null || result.getCompanyName().isBlank()) {
            return false;
        }

        //If we don't have the company or an application with it, ignore the email
        Company company = companyRepository.findByAnyName(result.getCompanyName().trim());
        if (company == null) {
            return false;
        }
        List<JobApplication> applications = jobApplicationRepository.findJobApplicationsByUserAndJob_Company(user, company);
        if (applications.isEmpty()) {
            return false;
        }

        //find the right application: same position first, otherwise the first active one
        JobApplication target = null;
        if (result.getPosition() != null) {
            for (JobApplication application : applications) {
                if (application.getJob().getPosition().equalsIgnoreCase(result.getPosition().trim())) {
                    target = application;
                    break;
                }
            }
        }
        if (target == null) {
            for (JobApplication application : applications) {
                if (!application.getStatus().equals("Rejected") && !application.getStatus().equals("Withdrawn")) {
                    target = application;
                    break;
                }
            }
        }
        if (target == null) {
            return false;
        }

        //if there's any interview mentioned in the email
        // Interview invitation = InProgress + add an interview
        String newStatus = result.getType().equals("Interview") ? "InProgress" : result.getType();
        if (!newStatus.equals("InProgress") && !newStatus.equals("Offered") && !newStatus.equals("Rejected")) {
            return false;
        }

        if (result.getType().equals("Interview")) {
            Interview interview = new Interview();
            interview.setUser(user);
            interview.setJobApplication(target);
            interview.setReminderSent(false);
            LocalDateTime interviewDate = parseDate(result.getInterviewDate());
            interview.setInterviewDate(interviewDate);
            interview.setStatus(interviewDate != null ? "SCHEDULED" : "PENDING");
            interviewRepository.save(interview);
        }

        target.setStatus(newStatus);
        jobApplicationRepository.save(target);

        sendWhatsApp(user, target, result.getType().equals("Interview") ? "invited to an interview" : newStatus);
        return true;
    }

    private void sendWhatsApp(User user, JobApplication application, String update) {
        Profile profile = user.getProfile();
        if (profile == null || profile.getPhoneNumber() == null) {
            return;
        }
        try {
            String message = "Tumooh update: your application for " + application.getJob().getPosition()
                    + " at " + application.getJob().getCompany().getNameEn() + " is now: " + update;
            whatsAppService.sendMessage(profile.getPhoneNumber(), message);
        } catch (Exception e) {
            // WhatsApp failing should not stop the sync
            System.out.println("WhatsApp message failed: " + e.getMessage());
        }
    }

    private LocalDateTime parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    //extract the text of the email, whether it is plain text, HTML, or multipart
    private String getText(Part part) throws Exception {
        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        }
        if (part.isMimeType("text/html")) {
            return ((String) part.getContent()).replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ");
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                String text = getText(multipart.getBodyPart(i));
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return "";
    }
}