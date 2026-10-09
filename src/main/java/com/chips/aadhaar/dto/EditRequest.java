package com.chips.aadhaar.dto;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
public record EditRequest(
    @NotBlank @Size(max=30) String clientName,
    @NotBlank @Size(max=12) String sa,
    @NotNull LocalDateTime validUpto,
    @Size(max=100) String auaLk,
    @Size(max=100) String kuaLk,
    @NotBlank @Size(max=150) String redirectUri,
    @NotNull @Pattern(regexp="I|E") String clientType,
    @NotNull @Pattern(regexp="Y|N") String kycFlag,
    @NotNull @Pattern(regexp="Y|N") String ekycPacketStorageFlag,
    @NotNull @Pattern(regexp="Y|N") String lkFlag,
    @NotNull @Pattern(regexp="Y|N") String webFlag
) {}
