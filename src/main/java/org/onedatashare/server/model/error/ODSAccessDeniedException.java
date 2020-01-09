package org.onedatashare.server.model.error;

public class ODSAccessDeniedException extends ODSError{
    public ODSAccessDeniedException(int err) {
        super("Access Denied to this File or Folder");
        type = "AccessDenied";
        error = "Access Denied to this File or Folder.";
    }
}
