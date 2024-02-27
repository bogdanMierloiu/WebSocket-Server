package ro.bogdan_mierloiu.websocketserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User implements Principal {

    String email;
    String name;
    String surname;

    @Override
    public String getName() {
        return name;
    }
}
