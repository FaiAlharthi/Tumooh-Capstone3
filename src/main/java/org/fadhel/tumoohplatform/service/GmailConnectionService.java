package org.fadhel.tumoohplatform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.in.GmailConnectRequest;
import org.fadhel.tumoohplatform.model.GmailConnection;
import org.fadhel.tumoohplatform.model.User;
import org.fadhel.tumoohplatform.repository.GmailConnectionRepository;
import org.fadhel.tumoohplatform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class GmailConnectionService {

    private final UserRepository userRepository;
    private final GmailConnectionRepository gmailConnectionRepository;

    public void connectGmail(Long userId, GmailConnectRequest request) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        String gmailAddress = user.getEmail().trim();
        //google shows the App Password with spaces we need to remove them
        String appPassword = request.getAppPassword().replace(" ", "");

        //Try to log in to Gmail first, to make sure the email and App Password are correct
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", gmailAddress, appPassword);
            store.close();
        } catch (MessagingException e) {
            throw new ApiException("Could not connect to Gmail. Check your email and App Password");
        }

        //If the user already connected before update it or create a new one
        GmailConnection connection = gmailConnectionRepository.findGmailConnectionByUser(user);
        if (connection == null) {
            connection = new GmailConnection();
            connection.setUser(user);
        }
        connection.setGmailAddress(gmailAddress);
        connection.setAppPassword(appPassword);
        connection.setLastSyncedAt(LocalDateTime.now());
        gmailConnectionRepository.save(connection);
    }
}