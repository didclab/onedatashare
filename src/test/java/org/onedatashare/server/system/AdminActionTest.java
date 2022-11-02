package org.onedatashare.server.system;

import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.controller.AdminController;
import org.onedatashare.server.model.core.Mail;
import org.onedatashare.server.model.core.Role;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.core.UserDetails;
import org.onedatashare.server.model.request.ChangeRoleRequest;
import org.onedatashare.server.model.request.PageRequest;
import org.onedatashare.server.model.useraction.NotificationBody;
import org.onedatashare.server.model.util.MailUUID;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.repository.MailRepository;
import org.onedatashare.server.system.base.SystemTest;
import org.onedatashare.server.system.mockuser.WithMockCustomUser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Long.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.just;

/**
 * A system test suite that tests operations permitted by admins and owners on users and roles
 * <br><br>
 * Entry point for requests: {@link AdminController}
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class AdminActionTest extends SystemTest {

    private static final String ADMIN_BASE_URL = "/api/stork/admin/";
    private static final String GET_USERS_URL = ADMIN_BASE_URL + "get-users";
    private static final String GET_ADMINS_URL = ADMIN_BASE_URL + "get-admins";
    private static final String GET_ALL_USERS_URL = ADMIN_BASE_URL + "getAllUsers";
    private static final String SEND_NOTIFICATION_URL = ADMIN_BASE_URL + "sendNotifications";
    private static final String DELETE_MAIL_URL = ADMIN_BASE_URL + "deleteMail";
    private static final String GET_ALL_MAIL_URL = ADMIN_BASE_URL + "getMails";
    private static final String GET_TRASH_MAIL_URL = ADMIN_BASE_URL + "getTrashMails";
    private static final String MAKE_ADMIN_URL = ADMIN_BASE_URL + "change-role";

    private static final String MAIL_STATUS_DELETED = "deleted";
    private static final int RESPONSE_STATUS_OK = 200;
    private static final int RESPONSE_STATUS_ERROR = 401;
    private static final String MAIL_STATUS_SENT = "sent";

    private Map<UUID, Mail> sentMail = new HashMap<>();

    @MockBean
    MailRepository mailRepo;

    @BeforeEach
    public void setup() {
        when(userRepository.insert((User) any())).thenAnswer(addToUsers());
        when(userRepository.save(any())).thenAnswer(addToUsers());
        when(userRepository.findAllUsers(any())).thenAnswer(getUsersSortedByEmail());
        when(userRepository.findAllAdministrators(any())).thenAnswer(getAdminsSortedByEmail());
        when(userRepository.findAll()).thenAnswer(getAllUsers());
        when(userRepository.findById((String) any())).thenAnswer(getFromUsers());
        when(userRepository.countUsers()).thenAnswer(getUsersSize());
        when(userRepository.countAdministrators()).thenAnswer(getAdminsSize());

        when(mailRepo.save(any())).thenAnswer(putToMailRepo());
        when(mailRepo.findById((UUID) any())).thenAnswer(getMailByUuid());
        when(mailRepo.findAll()).thenAnswer(getAllMail());
        when(mailRepo.findAllDeleted()).thenAnswer(getAllDeletedMail());

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
        assertNotEquals(firstEmail.getStatus(), MAIL_STATUS_DELETED);
        // default json conversion is overridden here because the json
        // field name is different from the one in the class declaration
        String jsonRep = jsonRepresentationOf(mailUuidOf(firstEmailUuid));
        // delete email that was just sent
        Response response = (Response) processPostWithRequestData(DELETE_MAIL_URL, jsonRep)
                .andReturn().getAsyncResult();

        // check if it has been marked as deleted
        firstEmail = (Mail) sentMail.values().toArray()[0];
        assertEquals(firstEmail.getStatus(), MAIL_STATUS_DELETED);
        assertEquals(response.status, RESPONSE_STATUS_OK);
    }

    @Test
    @WithMockCustomUser(role = Role.ADMIN)
    public void givenEmailsStoredInRepo_WhenRequestingAllMail_ShouldReturnAllMail() throws Exception {
        when(emailService.getSendQuota()).thenReturn(quotaOf(8, 0));
        // 2 email types will be stored: ones that have marked "sent", and ones that have been marked "deleted"
        // email 1 will be kept as "sent"
        String toDeleteMailSubject = "To delete";
        String toKeepMailSubject = "To keep";
        ArrayList<String> recipients = arrayListOf("recipient1");
        NotificationBody notificationBody = getTestEmailsFor(recipients, toKeepMailSubject);
        processPostWithRequestData(SEND_NOTIFICATION_URL, notificationBody);
        // email 2 will be marked "deleted"
        recipients = arrayListOf("recipient3");
        notificationBody = getTestEmailsFor(recipients, toDeleteMailSubject);
        processPostWithRequestData(SEND_NOTIFICATION_URL, notificationBody);
        String deletedMailUuid = getEmailBySubject(toDeleteMailSubject).getUuid().toString();
        String jsonRep = jsonRepresentationOf(mailUuidOf(deletedMailUuid));
        // delete email that was just sent
        processPostWithRequestData(DELETE_MAIL_URL, jsonRep);

        Iterable<Mail> response = (Iterable<Mail>) processGetWithNoRequestData(GET_ALL_MAIL_URL)
                .andReturn().getAsyncResult();
        List<Mail> allMail = toList(response);

        assertEquals(2, allMail.size());
        String firstMailStatus = allMail.get(0).getStatus();
        String secondMailStatus = allMail.get(1).getStatus();
        // deleted is going to be either first one or second one
        assertTrue(MAIL_STATUS_DELETED.equalsIgnoreCase(firstMailStatus)
                || MAIL_STATUS_SENT.equalsIgnoreCase(firstMailStatus));
        // sent is going to be either first one or second one
        assertTrue(MAIL_STATUS_DELETED.equalsIgnoreCase(secondMailStatus)
                || MAIL_STATUS_SENT.equalsIgnoreCase(secondMailStatus));
    }

    @Test
    @WithMockCustomUser(role = Role.ADMIN)
    public void givenEmailsStoredInRepo_WhenRequestingTrashMail_ShouldOnlyReturnTrashMail() throws Exception {
        when(emailService.getSendQuota()).thenReturn(quotaOf(8, 0));
        // 2 email types will be stored: ones that have marked "sent", and ones that have been marked "deleted"
        // email 1 will be kept as "sent"
        String toDeleteMailSubject = "To delete";
        String toKeepMailSubject = "To keep";
        ArrayList<String> recipients = arrayListOf("recipient1");
        NotificationBody notificationBody = getTestEmailsFor(recipients, toKeepMailSubject);
        processPostWithRequestData(SEND_NOTIFICATION_URL, notificationBody);
        // email 2 will be marked "deleted"
        recipients = arrayListOf("recipient3");
        notificationBody = getTestEmailsFor(recipients, toDeleteMailSubject);
        processPostWithRequestData(SEND_NOTIFICATION_URL, notificationBody);
        String deletedMailUuid = getEmailBySubject(toDeleteMailSubject).getUuid().toString();
        String jsonRep = jsonRepresentationOf(mailUuidOf(deletedMailUuid));
        // delete email that was just sent
        processPostWithRequestData(DELETE_MAIL_URL, jsonRep);

        Iterable<Mail> response = (Iterable<Mail>) processGetWithNoRequestData(GET_TRASH_MAIL_URL)
                .andReturn().getAsyncResult();
        List<Mail> allMail = toList(response);

        // should only contain deleted mail
        assertEquals(1, allMail.size());
        String firstMailStatus = allMail.get(0).getStatus();
        assertTrue(MAIL_STATUS_DELETED.equalsIgnoreCase(firstMailStatus));
    }

    @Test
    @WithMockCustomUser(role = Role.OWNER)
    public void givenGrantingUserIsOwnerAndTargetUserIsNotAdmin_WhenMakingAdmin_ShouldGrantAdminRole() throws Exception {
        User user = userWithEmail("user@do.co");
        when(userRepository.findById(user.getEmail())).thenReturn(just(user));

        ChangeRoleRequest request = changeRoleRequestOf(user, true);
        Response response = (Response) processPutWithRequestData(MAKE_ADMIN_URL, request)
                .andReturn().getAsyncResult();

        assertEquals(RESPONSE_STATUS_OK, response.status);
        assertTrue(user.getRoles().stream().anyMatch(Role.ADMIN::equals));
    }

    @Test
    @WithMockCustomUser(role = Role.OWNER)
    public void givenGrantingUserIsOwnerAndTargetUserIsAdmin_WhenMakingNotAdmin_ShouldRevokeAdminRole() throws Exception {
        User user = userWithEmail("user@do.co");
        when(userRepository.findById(user.getEmail())).thenReturn(just(user));

        ChangeRoleRequest request = changeRoleRequestOf(user, false);
        // grant admin role
        processPutWithRequestData(MAKE_ADMIN_URL, request);
        request.setMakeAdmin(false);
        // revoke admin role
        processPutWithRequestData(MAKE_ADMIN_URL, request);

        assertFalse(user.getRoles().stream().anyMatch(Role.ADMIN::equals));
    }

    @Test
    @WithMockCustomUser(role = Role.USER)
    public void givenGrantingUserIsUserAndTargetUserIsNotAdmin_WhenMakingAdmin_ShouldNotGrantAdminRole() throws Exception {
        User user = userWithEmail("user@do.co");
        when(userRepository.findById(user.getEmail())).thenReturn(just(user));

        ChangeRoleRequest request = changeRoleRequestOf(user, true);
        processPutWithRequestData(MAKE_ADMIN_URL, request);

        assertFalse(user.getRoles().stream().anyMatch(Role.ADMIN::equals));
    }

    @Test
    @WithMockCustomUser(role = Role.ADMIN)
    public void givenGrantingUserIsAdminAndTargetUserIsNotAdmin_WhenMakingAdmin_ShouldNotGrantAdminRole() throws Exception {
        User user = userWithEmail("user@do.co");
        when(userRepository.findById(user.getEmail())).thenReturn(just(user));

        ChangeRoleRequest request = changeRoleRequestOf(user, true);
        processPutWithRequestData(MAKE_ADMIN_URL, request);

        assertFalse(user.getRoles().stream().anyMatch(Role.ADMIN::equals));
    }

    @NotNull
    private ChangeRoleRequest changeRoleRequestOf(User user, boolean makeAdmin) {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setEmail(user.getEmail());
        request.setMakeAdmin(makeAdmin);
        return request;
    }

    private <T> List<T> toList(Iterable<T> iter) {
        List<T> list = new ArrayList<>();
        for (T val : iter)
            list.add(val);
        return list;
    }

    private Mail getEmailBySubject(String subject) {
        return sentMail.entrySet().stream()
                .filter(e -> subject.equals(e.getValue().getSubject()))
                .findFirst().get().getValue();
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
        return getTestEmailsFor(recipients, "subject");
    }

    private NotificationBody getTestEmailsFor(ArrayList<String> recipients, String subject) {
        String message = "testing is fun! :')";
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
        user.setRoles(new ArrayList<>(singletonList(Role.USER)));
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
        return invocationOnMock -> fromIterable(users.values());
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

    private Answer<Flux<Mail>> getAllMail() {
        return invocationOnMock -> fromIterable(sentMail.values());
    }

    private Answer<Mono<Mail>> putToMailRepo() {
        return invocationOnMock -> {
            Mail mail = invocationOnMock.getArgument(0);
            sentMail.put(mail.getUuid(), mail);
            return just(mail);
        };
    }

    private Answer<Flux<Mail>> getAllDeletedMail() {
        return invocationOnMock -> fromIterable(sentMail.values().stream()
                .filter(m -> MAIL_STATUS_DELETED.equals(m.getStatus()))
                .collect(Collectors.toList()));
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
