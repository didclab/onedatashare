package org.onedatashare.server.service.management;

import org.onedatashare.server.model.core.User;
import reactor.core.publisher.Mono;

public interface Manager {

    public Mono<User> createUser(User user);
    public Mono<Void> deleteUser(User user);
    public Mono<Void> setPermissions();

}
