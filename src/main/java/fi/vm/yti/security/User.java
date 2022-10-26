package fi.vm.yti.security;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class User {
  public String email;
  public String firstName;
  public String lastName;
  public boolean superuser;
  public boolean newlyCreated;
  public List<Organization> organization;
  public UUID id;
  public LocalDateTime removalDateTime;
  public LocalDateTime tokenCreatedAt;
  public LocalDateTime tokenInvalidationAt;
  public String containerUri;
  public String tokenRole;
}
