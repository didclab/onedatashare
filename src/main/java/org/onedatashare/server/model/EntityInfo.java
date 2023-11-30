package org.onedatashare.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityInfo {
    protected String id;
    protected String path;
    protected long size;
}
