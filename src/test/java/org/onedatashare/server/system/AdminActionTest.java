package org.onedatashare.server.system;

import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.core.Mail;
import org.onedatashare.server.model.core.Role;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.core.UserDetails;
import org.onedatashare.server.model.request.PageRequest;
import org.onedatashare.server.model.useraction.NotificationBody;
import org.onedatashare.server.model.util.MailUUID;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.repository.MailRepository;
import org.onedatashare.server.system.mockuser.WithMockCustomUser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Long.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.just;

@SuppressWarnings("unchecked")
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AdminActionTest extends SystemTest {

    private static final String ADMIN_BASE_URL = "/api/stork/admin/";
    private static final String GET_USERS_URL = ADMIN_BASE_URL + "get-users";
    private static final String GET_ADMINS_URL = ADMIN_BASE_URL + "get-admins";
    private static final String GET_ALL_USERS_URL = ADMIN_BASE_URL + "getAllUsers";
    private static final String SEND_NOTIFICATION_URL = ADMIN_BASE_URL + "sendNotifications";
    private static final String DELETE_MAIL_URL = ADMIN_BASE_URL + "deleteMail";

    private static final String MAIL_DELETED_STATUS = "deleted";
    private static final int RESPONSE_STATUS_OK = 200;
    private static final int RESPONSE_STATUS_ERROR = 401;

    private Map<UUID, Mail> sentMail = new HashMap<>();

    @MockBean
    MailRepository mailRepo;

    @Before
    public void setup() {
        when(userRepository.insert((User) any())).thenAnswer(addToUsers());
        when(userRepository.save(any())).thenAnswer(addToUsers());

        when(userRepository.findAllUsers(any())).thenAnswer(getUsersSortedByEmail());
        when(userRepository.findAllAdministrators(any())).thenAnswer(getAdminsSortedByEmail());
        when(userRepository.findAll()).thenAnswer(getAllUsers());
        when(userRepository.countUsers()).thenAnswer(getUsersSize());
        when(userRepository.countAdministrators()).thenAnswer(getAdminsSize());

        when(mailRepo.save(any())).thenAnswer(putToMailRepo());
        when(mailRepo.findById((UUID) any())).thenAnswer(getMailByUuid());
        doAnswer(addToEmails()).when(emailService).sendEmail(any(), any(), any());
    }

    @Test
    @WithMockCustomUser(role = Role.ADMIN)
    public void givenUserAndAdminsStored_WhenCallingGetUsers_ShouldReturnNonAdminUsers() throws Exception {
        PageRequest pageRequest = emailSortedPageRequest();
        userRepository.insert(userWithEmail("u1@co.do"));
        userRepository.insert(userWithEmail("u2@co.do"));
        userRepository.insert(adminWithEmail("a1@co.do"));

        UserDetails response = (UserDetails) processPostWithRequestData(GET_USERS_URL, pageRequest)
                .andReturn().getAsyncResult();

        assertEquals(response.users, asList(userWithEmail("u1@co.do"),
                userWithEmail("u2@co.do")));
    }

    @Test
    @WithMockCustomUser(role = Role.ADMIN)
    public void givenUserAndAdminsStored_WhenCallingGetAdmins_ShouldReturnAdminUsers() throws Exception {
        PageRequest pageRequest = emailSortedPageRequest();
        userRepository.insert(adminWithEmail("a2@co.do"));
        userRepository.insert(userWithEmail("u1@co.do"));
        userRepository.insert(adminWithEmail("a1@co.do"));

        UserDetails response = (UserDetails) processPostWithRequestData(GET_ADMINS_URL, pageRequest)
                .andReturn().getAsyncResult();

        assertEquals(response.users, asList(adminWithEmail("a1@co.do"),
                adminWithEmail("a2@co.do")));
    }

    @Test
    @WithMockCustomUser(role = Role.ADMIN)
    public void givenUserAndAdminsStored_WhenCallingGetAllUsers_ShouldReturnAllUsers() throws Exception {
        PageRequest pageRequest = emailSortedPageRequest();
        userRepository.insert(adminWithEmail("a2@co.do"));
        userRepository.insert(userWithEmail("u1@co.do"));
        userRepository.insert(adminWithEmail("a1@co.do"));

        Iterable<User> response = (Iterable<User>) processGetWithRequestData(GET_ALL_USERS_URL, pageRequest)
                .andReturn().getAsyncResult();

        assertEquals(response, asList(adminWithEmail("a1@co.do"),
                adminWithEmail("a2@co.do"), userWithEmail("u1@co.do")));
    }

    @Test
    @WithMockCustomUser(role = Role.ADMIN)
    public void givenEmailQuotaNotReached_WhenSendingNotifications_ShouldSendAndSaveToRepo() throws Exception {
        when(emailService.getSendQuota()).thenReturn(quotaOf(4, 0));
        ArrayList<String> recipients = arrayListOf("recipient1", "recipient2", "recipient3");
        NotificationBody notificationBody = getTestEmailsFor(recipients);

        // Non-async response
        String mvcRepsonse = processPostWithRequestData(SEND_NOTIFICATION_URL, notificationBody)
                .andReturn().getResponse().getContentAsString();
        Response resp = fromJson(mvcRepsonse, Response.class);

        assertEquals(new ArrayList<>(userInbox.keySet()), recipients);
        assertEquals(((Mail) sentMail.values().toArray()[0]).getRecipients(), recipients);
        assertEquals(resp.status, RESPONSE_STATUS_OK);
    }

    @Test
    @WithMockCustomUser(role = Role.ADMIN)
    public void givenEmailQuotaReached_WhenSendingNotifications_ShouldNotSendOrSaveToRepo() throws Exception {
        ArrayList<String> recipients = arrayListOf("recipient1", "recipient2", "recipient3");
        when(emailService.getSendQuota()).thenReturn(quotaOf(4, 2));
        NotificationBody notificationBody = getTestEmailsFor(recipients);

        // Non-async response
        String mvcRepsonse = processPostWithRequestData(SEND_NOTIFICATION_URL, notificationBody)
                .andReturn().getResponse().getContentAsString();
        Response resp = fromJson(mvcRepsonse, Response.class);

        assertTrue(userInbox.keySet().isEmpty());
        assertTrue(sentMail.values().isEmpty());
        assertEquals(resp.status, RESPONSE_STATUS_ERROR);
    }

    @Test
    @WithMockCustomUser(role = Role.ADMIN)
    public void givenMailStoredInRepo_WhenDeleting_ShouldMarkMailAsDeleted() throws Exception {
        when(emailService.getSendQuota()).thenReturn(quotaOf(4, 0));
        ArrayList<String> recipients = arrayListOf("recipient1", "recipient2", "recipient3");
        NotificationBody notificationBody = getTestEmailsFor(recipients);

        // send email
        processPostWithRequestData(SEND_NOTIFICATION_URL, notificationBody);

        // get uuid of email that was just sent
        String firstEmailUuid = sentMail.keySet().toArray()[0].toString();

        // check that it was not marked as deleted
        Mail firstEmail = (Mail) sentMail.values().toArray()[0];
        assertNotEquals(firstEmail.getStatus(), MAIL_DELETED_STATUS);

        // default json conversion is overridden here because the json
        // field name is different from the one in the class declaration
        String jsonRep = jsonRepresentationOf(mailUuidOf(firstEmailUuid));

        // delete email that was just sent
        Response response = (Response) processPostWithRequestData(DELETE_MAIL_URL, jsonRep)
                .andReturn().getAsyncResult();

        // check if it has been marked as deleted
        firstEmail = (Mail) sentMail.values().toArray()[0];
        assertEquals(firstEmail.getStatus(), MAIL_DELETED_STATUS);
        assertEquals(response.status, RESPONSE_STATUS_OK);
    }

    private String jsonRepresentationOf(MailUUID firstEmailUuid) {
        return "{\"mailId\": \"" + firstEmailUuid.getMailUUID() + "\"}";
    }

    private MailUUID mailUuidOf(String firstEmailUuid) {
        MailUUID mailUuid = new MailUUID();
        mailUuid.setMailUUID(firstEmailUuid);
        return mailUuid;
    }

    private NotificationBody getTestEmailsFor(ArrayList<String> recipients) {
        String message = "testing is fun! :')";
        String subject = "subject";
        String sender = "sender";
        boolean isHtml = true;
        return new NotificationBody(sender, subject, message, recipients, isHtml);
    }

    private GetSendQuotaResult quotaOf(int max24HrSend, int sentLast24hr) {
        GetSendQuotaResult quota = new GetSendQuotaResult();
        quota.setMax24HourSend((double) max24HrSend);
        quota.setSentLast24Hours((double) sentLast24hr);
        return quota;
    }

    private PageRequest emailSortedPageRequest() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(1);
        pageRequest.setSortBy("email");
        pageRequest.setSortOrder("asc");
        return pageRequest;
    }

    private User userWithEmail(String email) {
        User user = new User();
        user.setEmail(email);
        user.setRoles(singletonList(Role.USER));
        return user;
    }

    private User adminWithEmail(String email) {
        User admin = userWithEmail(email);
        admin.setRoles(singletonList(Role.ADMIN));
        return admin;
    }
    private Answer<Flux<User>> getAdminsSortedByEmail() {
        return invocationOnMock ->
                fromIterable(sortUsersByEmail(adminPredicate()));
    }

    private Answer<Flux<User>> getUsersSortedByEmail() {
        return invocationOnMock ->
                fromIterable(sortUsersByEmail(userPredicate()));
    }

    private Answer<Flux<User>> getAllUsers() {
        return invocationOnMock -> Flux.fromIterable(users.values());
    }


    private Answer<Mono<Mail>> getMailByUuid() {
        return invocationOnMock -> just(sentMail.get(invocationOnMock.getArgument(0)));
    }

    private List<User> sortUsersByEmail(Predicate<User> predicate) {
        return users.values().stream()
                .filter(predicate)
                .sorted(comparing(User::getEmail))
                .collect(Collectors.toList());
    }

    private Answer<Object> getUsersSize() {
        return invocationOnMock -> just(valueOf(users.values().stream().filter(userPredicate()).count()));
    }

    private Answer<Mono<Mail>> putToMailRepo() {
        return invocationOnMock -> {
            Mail mail = invocationOnMock.getArgument(0);
            sentMail.put(mail.getUuid(), mail);
            return just(mail);
        };
    }

    private Answer<Object> getAdminsSize() {
        return invocationOnMock -> just(valueOf(users.values().stream().filter(adminPredicate()).count()));
    }

    private Predicate<User> adminPredicate() {
        return u -> u.getRoles().equals(singletonList(Role.ADMIN));
    }

    private Predicate<User> userPredicate() {
        return u -> u.getRoles().equals(singletonList(Role.USER));
    }

    private <T> ArrayList<T> arrayListOf(T... vals) {
        return new ArrayList<>(asList(vals));
    }
}
