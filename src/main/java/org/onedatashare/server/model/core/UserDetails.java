package org.onedatashare.server.model.core;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class UserDetails {
    public List<User> users;
    public Long totalCount;
}
