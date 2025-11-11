package api.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackedUser {
    private final String username;
    private final String password;
    private final Long id;
}
