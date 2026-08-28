package gr.aueb.cf.schoolapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserEditDTO(

        @NotNull
        UUID uuid,

        @NotNull
        @Size(min = 2, max = 20)
        String username,

        @NotNull
        Long roleID
) {
}
